
package com.example;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketTest {
    private final Logger logger = LoggerFactory.getLogger(WebSocketTest.class);

    private TopologyTestDriver testDriver;

    @Value("${input-topic.name}")
    private String inputTopicName;

    @Value("${output-topic.name}")
    private String outputTopicName;

    @LocalServerPort
    private int port;

    private TestInputTopic<Integer,String> inputTopic;

    private TestOutputTopic<Integer, Long> outputTopic;

    @Autowired
    private StreamsBuilderFactoryBean streamsBuilder;

    private WebSocketConnectionManager webSocketConnectionManager;

    private final LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    private final CountDownLatch webSocketReady = new CountDownLatch(1);

    @BeforeEach
    public void setup() {
        this.testDriver = new TopologyTestDriver(streamsBuilder.getTopology(), streamsBuilder.getStreamsConfiguration());
        logger.info(streamsBuilder.getTopology().describe().toString());
        this.inputTopic = testDriver.createInputTopic(inputTopicName, Serdes.Integer().serializer(), Serdes.String().serializer());
        this.outputTopic = testDriver.createOutputTopic(outputTopicName, Serdes.Integer().deserializer(), Serdes.Long().deserializer());

        System.out.println("WebEnvironment: " + port);
        StandardWebSocketClient client = new StandardWebSocketClient();
        webSocketConnectionManager = new WebSocketConnectionManager(client, new AbstractWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) {
                webSocketReady.countDown();
            }

            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                System.out.println("Websocket received: " + message.getPayload());
                messageQueue.add(message.getPayload());
            }
        }, "ws://localhost:"+port+"/ws/data");


        webSocketConnectionManager.setHeaders(new WebSocketHttpHeaders());
        webSocketConnectionManager.setAutoStartup(true);
        webSocketConnectionManager.start();

    }

    @AfterEach
    public void after() {
        if (testDriver != null) {
            this.testDriver.close();
        }

        if (webSocketConnectionManager != null) {
            webSocketConnectionManager.stop();
        }
    }

    @Test
    public void testTopologyLogic() throws InterruptedException {

        boolean await = webSocketReady.await(5, TimeUnit.SECONDS);
        if(!await){
            throw new RuntimeException("WebSocket 'Ready' timed out");
        }

        inputTopic.pipeInput(1, "test", 1L);
        inputTopic.pipeInput(1, "test", 10L);
        inputTopic.pipeInput(2, "test", 2L);

        Awaitility.waitAtMost(Duration.ofSeconds(5)).until(() -> outputTopic.getQueueSize() == 2L);
        assertThat(outputTopic.readValuesToList()).isEqualTo(List.of(2L, 1L));

        String receivedMessage = messageQueue.poll(5, TimeUnit.SECONDS);
        assertThat(receivedMessage).contains("Key: 1, Count: 2");
    }
}
