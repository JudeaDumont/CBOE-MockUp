package com.example.user_service.kafka;

import com.example.user_service.model.User;
import com.example.user_service.repo.UserRepository;
import com.example.user_service.service.UserService;
import com.example.user_service.util.KafkaTestUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"user-registration-topic"},
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9092",
                "port=9092",
                "log.dirs=C:\\KafkaTemp"
        })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@DirtiesContext
public class KafkaConsumerServiceIntegrationTest {

    private KafkaTemplate<String, User> kafkaTemplate;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private UserService userService;

    private final CountDownLatch saveLatch = new CountDownLatch(1);
    private static final CountDownLatch applicationStartLatch = new CountDownLatch(1);

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ApplicationListener<ApplicationReadyEvent> readyEventApplicationListener() {
            return event -> applicationStartLatch.countDown();
        }
    }

    @BeforeAll
    void setup() throws InterruptedException {
        boolean await = applicationStartLatch.await(15, TimeUnit.SECONDS);
        if (!await) {
            throw new RuntimeException("Timed out waiting for application to come up");
        }

        KafkaTestUtils.waitForBroker("localhost:9092", 10, 1000);
        // Setup KafkaTemplate with JsonSerializer
        Map<String, Object> configProps = new HashMap<>();
        configProps.put("bootstrap.servers", "localhost:9092");
        configProps.put("key.serializer", StringSerializer.class);
        configProps.put("value.serializer", JsonSerializer.class);
        DefaultKafkaProducerFactory<String, User> producerFactory = new DefaultKafkaProducerFactory<>(configProps);
        kafkaTemplate = new KafkaTemplate<>(producerFactory);

        // Mock the saveUser method in UserService
        Mockito.when(userService.saveUser(Mockito.any(User.class))).thenAnswer(invocation -> {
            saveLatch.countDown();
            return invocation.getArgument(0);
        });
    }

    @Test
    public void whenMessageConsumed_thenSaveUserIsCalledWithCorrectDetails() throws InterruptedException {
        //Arrange
        User user = User.builder()
                .id(1L)
                .email("email@email.com")
                .username("username")
                .build();

        // Act
        // Despite all the latches,the application readyness check, flushing the kafka template,
        // and setting up a test listener, I still have to resort to a manual wait because
        // the listener is not ready and accepting messages before it gets sent by the template
        // unbelievable.
        Thread.sleep(5000);
        kafkaTemplate.send(new ProducerRecord<>("user-registration-topic", user));
        kafkaTemplate.flush();

        boolean await = saveLatch.await(115, TimeUnit.SECONDS);
        if (!await) {
            throw new RuntimeException("Timed out waiting for save user");
        }

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userService, Mockito.times(1)).saveUser(userCaptor.capture());

        User capturedUser = userCaptor.getValue();

        assertThat(capturedUser).isNotNull();
        assertThat(capturedUser.getId()).isEqualTo(1L);
        assertThat(capturedUser.getEmail()).isEqualTo("email@email.com");
        assertThat(capturedUser.getUsername()).isEqualTo("username");
    }

    @AfterAll
    public void stopKafkaBroker() {
        if (kafkaTemplate != null) {
            kafkaTemplate.flush();
        }
    }

    @KafkaListener(topics = "user-registration-topic", groupId = "user-test-group")
    public void consume(User user) {
        System.out.println("TEST Consumed message: " + user);
    }
}
