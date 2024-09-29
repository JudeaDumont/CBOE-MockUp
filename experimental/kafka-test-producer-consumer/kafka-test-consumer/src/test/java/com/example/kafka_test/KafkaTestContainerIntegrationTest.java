package com.example.kafka_test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KafkaTestContainerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static KafkaContainer kafkaContainer;

    private CountDownLatch latch = new CountDownLatch(1);
    private String receivedMessage;

    static {
        // Initialize and start the Kafka container in the static block
        kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.1"));
        kafkaContainer.start();
        // Set the system property dynamically based on the started Kafka container
        System.setProperty("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());
    }

    @Test
    public void testKafkaProducerConsumer() throws InterruptedException {
        // Send message
        kafkaTemplate.send("test-topic", "Test message");

        // Wait for the message to be received by the consumer
        latch.await(10, TimeUnit.SECONDS);

        // Assert that the message was received
        assertThat(receivedMessage).isEqualTo("Test message");
    }

    @KafkaListener(topics = "test-topic", groupId = "test-group")
    public void listen(String message) {
        receivedMessage = message;
        latch.countDown();
    }
}