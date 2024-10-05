package com.example.kafka_test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KafkaToWebSocketTest {

    @LocalServerPort
    private int port;

    @Autowired
    private KafkaTemplate<Integer, String> kafkaTemplate;

    private static final KafkaContainer kafkaContainer;

    private final CountDownLatch latch = new CountDownLatch(1);
    private String receivedMessage;

    static {
        kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.1"));
        kafkaContainer.start();
        System.setProperty("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());
    }

    public void sendMessagesAsynchronously() {
        ExecutorService executorService = Executors.newFixedThreadPool(10); // Define a thread pool

        CompletableFuture.runAsync(() -> {
            IntStream.range(0, 20).forEach(i -> {
                Integer key = 1;
                String value = "Test message " + i;

                kafkaTemplate.send("example-topic", key, value);
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Interrupted while waiting between message sends");
                }
            });
        }, executorService).thenRun(() -> System.out.println("All messages sent"));
        executorService.shutdown();
    }

    @Test
    public void testKafkaMessageConsumedAndPublishedToWebSocket() throws Exception {

        sendMessagesAsynchronously();

        String wsUrl = "ws://localhost:" + port + "/ws";

        // Wait for the message to be received by the WebSocket client
        boolean messageReceived = latch.await(20, TimeUnit.SECONDS);

        assertThat(messageReceived).isTrue();
        assertThat(receivedMessage).isEqualTo("Windowed key: " + "key1" + ", Count: " + "1");
    }


    @KafkaListener(topics = "flush-topic", groupId = "test-group")
    public void listen(String message) {
        System.out.println("Received message: " + message);
        latch.countDown();
        receivedMessage = message;
    }
}