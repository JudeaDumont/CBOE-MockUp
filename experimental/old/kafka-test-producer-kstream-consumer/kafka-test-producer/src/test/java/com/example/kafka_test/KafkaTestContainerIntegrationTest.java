package com.example.kafka_test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KafkaTestContainerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;  // MockMvc to simulate HTTP requests

    private static KafkaContainer kafkaContainer;

    private CountDownLatch latch = new CountDownLatch(1);
    private String receivedMessage;

    static {
        kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.1"));
        kafkaContainer.start();
        System.setProperty("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());
    }

    @Test
    public void testKafkaProducerConsumer() throws Exception {
        mockMvc.perform(post("/api/kafka/publish")
                        .content("Test message")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().string("Message sent to Kafka topic: Test message"));  // Assert the response content

        latch.await(10, TimeUnit.SECONDS);

        assertThat(receivedMessage).isEqualTo("Test message");
    }

    @KafkaListener(topics = "example-topic", groupId = "test-group")
    public void listen(String message) {
        receivedMessage = message;
        latch.countDown();
    }
}