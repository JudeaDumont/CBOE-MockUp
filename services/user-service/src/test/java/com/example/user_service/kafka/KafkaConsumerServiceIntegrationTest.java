package com.example.user_service.kafka;

import com.example.user_service.model.User;
import com.example.user_service.repo.UserRepository;
import com.example.user_service.service.UserService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"user-registration-topic"}, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableKafka
@ActiveProfiles("test")
public class KafkaConsumerServiceIntegrationTest {

    private KafkaTemplate<String, User> kafkaTemplate;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private UserService userService;

    @BeforeAll
    void setup() {
        // Setup KafkaTemplate with JsonSerializer
        Map<String, Object> configProps = new HashMap<>();
        configProps.put("bootstrap.servers", "localhost:9092");
        configProps.put("key.serializer", org.apache.kafka.common.serialization.StringSerializer.class);
        configProps.put("value.serializer", JsonSerializer.class);
        DefaultKafkaProducerFactory<String, User> producerFactory = new DefaultKafkaProducerFactory<>(configProps);
        kafkaTemplate = new KafkaTemplate<>(producerFactory);

        // Mock the saveUser method in UserService
        Mockito.when(userService.saveUser(Mockito.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    public void whenMessageConsumed_thenSaveUserIsCalledWithCorrectDetails() throws InterruptedException {
        // Arrange
        User user = User.builder()
                .id(1L)
                .email("email@email.com")
                .username("username")
                .build();

        // Act
        kafkaTemplate.send(new ProducerRecord<>("user-registration-topic", user));

        // Allow some time for the message to be consumed
        TimeUnit.SECONDS.sleep(5);

        // Capture the User object passed to saveUser
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userService, Mockito.times(1)).saveUser(userCaptor.capture());

        User capturedUser = userCaptor.getValue();

        // Assert on the captured User object's fields
        assertThat(capturedUser).isNotNull();
        assertThat(capturedUser.getId()).isEqualTo(1L);
        assertThat(capturedUser.getEmail()).isEqualTo("email@email.com");
        assertThat(capturedUser.getUsername()).isEqualTo("username");
    }

    @AfterAll
    public void tearDown() {
        // Clean up resources if needed
        userRepository.deleteAll();
    }
}
