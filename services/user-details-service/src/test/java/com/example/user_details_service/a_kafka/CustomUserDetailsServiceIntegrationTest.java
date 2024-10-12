package com.example.user_details_service.a_kafka;

import com.example.user_details_service.model.AuthUser;
import com.example.user_details_service.model.User;
import com.example.user_details_service.repo.UserRepository;
import com.example.user_details_service.service.CustomUserDetailsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

//todo: for some reason when this test is ran after the controller tests it fails, and
// the listener never receives a message from the topic.
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"user-registration-topic"}, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@DirtiesContext
public class CustomUserDetailsServiceIntegrationTest {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private UserRepository userRepository;

    private BlockingQueue<ConsumerRecord<String, User>> records = new LinkedBlockingQueue<>();

    @KafkaListener(topics = "user-registration-topic", groupId = "test-group")
    public void listen(ConsumerRecord<String, User> record) {
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
        ConsumerRecord<String, User> received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.topic()).isEqualTo("user-registration-topic");

        assertThat(received.value().getUsername()).isEqualTo(user.getUsername());

        // Verify the user is saved in the repository
        AuthUser savedUser = userRepository.findByUsername("testuser").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
    }
}