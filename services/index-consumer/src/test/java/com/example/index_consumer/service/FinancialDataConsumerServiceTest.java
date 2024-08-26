package com.example.index_consumer.service;

import com.example.index_consumer.model.FinancialData;
import com.example.index_consumer.websocket.FinancialDataWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;

import java.util.Map;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"financial_data"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"}
)
public class FinancialDataConsumerServiceTest {

    @Autowired
    private FinancialDataConsumerService financialDataConsumerService;

    @MockBean
    private FinancialDataWebSocketHandler webSocketHandler;

    @Autowired
    private ObjectMapper objectMapper;

    private FinancialData testData;

    @BeforeEach
    public void setup() {
        testData = FinancialData.builder()
                .indexName("Index_1")
                .value(1050.5)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @DynamicPropertySource
    static void configureKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
    }

    @Test
    public void testConsume() throws Exception {
        String jsonData = objectMapper.writeValueAsString(testData);

        // Configure consumer properties using the dynamic bootstrap server
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("consumer", "false", "localhost:9092");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        ConsumerFactory<String, byte[]> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        ConcurrentKafkaListenerContainerFactory<String, byte[]> containerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        containerFactory.setConsumerFactory(consumerFactory);
        containerFactory.setConcurrency(1);

        // Simulate the Kafka listener method being called
        financialDataConsumerService.consume(testData);

        // Verify that the WebSocket handler sends the correct message
        Mockito.verify(webSocketHandler, Mockito.times(1)).sendMessageToAll(jsonData);
    }
}
