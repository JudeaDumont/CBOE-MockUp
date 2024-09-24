package com.example.postgres_test_container_with_kafka;

import com.example.postgres_test_container_with_kafka.service.KafkaConsumerService;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KafkaConsumerServiceTest {

    private static KafkaContainer kafkaContainer;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeAll
    public void setUp() throws ExecutionException, InterruptedException {
        // Start Kafka container
        kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.1"))
                .withExposedPorts(9093, 9092); // Explicit port to expose Kafka
        kafkaContainer.start();

        // Set system properties for Kafka
        System.setProperty("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());

        // Create Kafka Admin Client to create topics
        Map<String, Object> config = new HashMap<>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        AdminClient adminClient = AdminClient.create(config);

        // Create the topic 'my-topic'
        NewTopic topic = new NewTopic("my-topic", 1, (short) 1);
        adminClient.createTopics(Collections.singletonList(topic)).all().get();
        Thread.sleep(5000); // Add a delay after topic creation to ensure Kafka is fully ready.

        System.out.println("Topic 'my-topic' created.");
        adminClient.close();
    }

    @Test
    public void testKafkaConsumerReceivesMessages() {
        // Clear messages list before the test
        KafkaConsumerService.messages.clear();
        System.out.println("Cleared KafkaConsumerService messages list.");

        // Send a message to the Kafka topic using KafkaTemplate
        kafkaTemplate.send("my-topic", "test-key", "test-message");
        System.out.println("Message 'test-message' sent to topic 'my-topic'.");

        // Wait for the message to be consumed using Awaitility
        await().atMost(100, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(KafkaConsumerService.messages).isNotEmpty();
            assertThat(KafkaConsumerService.messages).contains("test-message");
        });

        System.out.println("Kafka message received by consumer.");
    }

    @AfterAll
    public void tearDown() {
        // Stop Kafka container
        if (kafkaContainer != null && kafkaContainer.isRunning()) {
            kafkaContainer.stop();
        }
    }
}
