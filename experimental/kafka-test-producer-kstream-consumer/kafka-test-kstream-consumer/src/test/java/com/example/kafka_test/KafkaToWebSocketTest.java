package com.example.kafka_test;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KafkaToWebSocketTest {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

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

        // Use a CompletableFuture to run the message sending asynchronously
        CompletableFuture.runAsync(() -> {
            IntStream.range(0, 10000).forEach(i -> {
                String key = "key-" + i % 9;
                String value = UUID.randomUUID() + "Test message " + i;

                kafkaTemplate.send("example-topic", key, value);
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Handle interruption
                    System.err.println("Interrupted while waiting between message sends");
                }
            });
        }, executorService).thenRun(() -> System.out.println("All messages sent"));

        // Optionally, shutdown the executor after completion
        executorService.shutdown();
    }

    @Test
    public void testKafkaMessageConsumedAndPublishedToWebSocket() throws Exception {

        // Send message to Kafka
        sendMessagesAsynchronously();

        // Setup WebSocket client
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(transports));

        stompClient.setMessageConverter(new StringMessageConverter());

        String wsUrl = "ws://localhost:" + port + "/ws";
        CompletableFuture<StompSession> stompSessionFuture = stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {
        });

        StompSession stompSession = stompSessionFuture.get(50, TimeUnit.SECONDS);

        stompSession.subscribe("/topic/kafkaMessages", new StompFrameHandler() {
            @Override
            public @NotNull Type getPayloadType(@NotNull StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(@NotNull StompHeaders headers, Object payload) {
                receivedMessage = (String) payload;
                System.out.println("Received WebSocket message: " + receivedMessage);
                latch.countDown();
            }
        });

        // Wait for the message to be received by the WebSocket client
        boolean messageReceived = latch.await(20, TimeUnit.SECONDS);

        assertThat(messageReceived).isTrue();
        assertThat(receivedMessage).isEqualTo("Windowed key: " + "key1" + ", Count: " + "1");
    }


    @KafkaListener(topics = "example-topic", groupId = "test-group")
    public void listen(String message) {
        //System.out.println("Received message: " + message);
    }
}