package com.example.kafka_test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KafkaTestContainerServiceIntegrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static KafkaContainer kafkaContainer;

    private CountDownLatch latch = new CountDownLatch(1);

    static {
        kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.1"));
        kafkaContainer.start();
        System.setProperty("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());
    }

    @Test
    public void testKafkaProducerConsumer() throws Exception {
        // Send a POST request to /api/kafka/publish with a message payload
        mockMvc.perform(post("/api/kafka/publish")
                        .content("Test message")
                        .contentType("application/json"))
                .andExpect(status().isOk())  // Assert that the status is 200 OK
                .andExpect(content().string("Message sent to Kafka topic: Test message"));  // Assert the response content

        // Wait for the latch to ensure the message is processed
        latch.await(10, TimeUnit.SECONDS);
    }
}