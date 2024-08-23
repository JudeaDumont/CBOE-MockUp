package com.example.user_details_service.kafka;

import com.example.user_details_service.model.AuthUser;
import com.example.user_details_service.model.User;
import com.example.user_details_service.repo.UserRepository;
import com.example.user_details_service.service.CustomUserDetailsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"user-registration-topic"}, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class CustomUserDetailsServiceIntegrationTest {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private UserRepository userRepository;

    private BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();

    @KafkaListener(topics = "user-registration-topic", groupId = "test-group")
    public void listen(ConsumerRecord<String, String> record) {
        records.add(record);
    }

    @Test
    public void whenSaveUserDetails_thenMessageSentToKafka() throws InterruptedException, JsonProcessingException {
        // Arrange
        AuthUser user = new AuthUser();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRole("USER");

        // Act
        customUserDetailsService.saveUserDetails(user);

        // Assert
        ConsumerRecord<String, String> received = records.poll(5, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.topic()).isEqualTo("user-registration-topic");

        User receivedUser = new ObjectMapper().readValue(received.value(), User.class);
        assertThat(receivedUser).usingRecursiveComparison().isEqualTo(user);

        // Verify the user is saved in the repository
        AuthUser savedUser = userRepository.findByUsername("testuser").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
    }
}