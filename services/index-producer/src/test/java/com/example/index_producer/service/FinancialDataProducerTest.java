package com.example.index_producer.service;

import com.example.index_producer.model.FinancialData;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"financial_data"}, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@DirtiesContext
public class FinancialDataProducerTest {

    @Autowired
    private KafkaTemplate<String, FinancialData> kafkaTemplate;

    @Autowired
    private FinancialDataService financialDataService;

    @Test
    public void testPublishOneMessagePerSecondForFourSeconds() throws InterruptedException {
        // Set up Kafka consumer properties
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        JsonDeserializer<FinancialData> jsonDeserializer = new JsonDeserializer<>(FinancialData.class);
        jsonDeserializer.addTrustedPackages("com.example.index_producer.model");

        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, jsonDeserializer);

        CountDownLatch latch = new CountDownLatch(4);  // Expecting 4 messages

        ConsumerFactory<String, FinancialData> consumerFactory = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                jsonDeserializer
        );
        ConcurrentMessageListenerContainer<String, FinancialData> container = new ConcurrentMessageListenerContainer<>(consumerFactory, new ContainerProperties("financial_data"));

        // Set up a listener for the container
        container.setupMessageListener((MessageListener<String, FinancialData>) record -> {
            assertThat(record.value()).isNotNull();
            latch.countDown();
        });

        container.start();
        ContainerTestUtils.waitForAssignment(container, 1); // wait for assignment to ensure messages can be consumed

        // Trigger the producer to send data every second
        for (int i = 0; i < 4; i++) {
            financialDataService.publishData();
            Thread.sleep(1000); // Wait for 1 second between messages
        }

        boolean allMessagesReceived = latch.await(5, TimeUnit.SECONDS); // Wait for up to 5 seconds

        assertThat(allMessagesReceived).isTrue(); // Ensure all 4 messages were received

        container.stop();
    }

    @Test
    public void testPublishData() throws InterruptedException {
        // Set up Kafka consumer properties
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "your-group-id");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());

        ConcurrentMessageListenerContainer<String, FinancialData> container =
                getStringFinancialDataConcurrentMessageListenerContainer(consumerProps);
        ContainerTestUtils.waitForAssignment(container, 1); // wait for assignment to ensure messages can be consumed

        // Trigger the producer to send data
        financialDataService.publishData();

        // Give some time for the message to be consumed
        Thread.sleep(1000);

        container.stop();
    }

    private static ConcurrentMessageListenerContainer<String, FinancialData> getStringFinancialDataConcurrentMessageListenerContainer(Map<String, Object> consumerProps) {
        ConsumerFactory<String, FinancialData> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        ConcurrentMessageListenerContainer<String, FinancialData> container = new ConcurrentMessageListenerContainer<>(consumerFactory, new ContainerProperties("financial_data"));

        // Set up a listener for the container
        MessageListener<String, FinancialData> messageListener = new MessageListener<String, FinancialData>() {
            @Override
            public void onMessage(ConsumerRecord<String, FinancialData> record) {
                assertThat(record.value()).isNotNull();
                assertThat(record.value().getIndexName()).startsWith("Index_");
                assertThat(record.value().getValue()).isBetween(1000.0, 1100.0);
            }
        };

        container.setupMessageListener(messageListener);
        container.start();
        return container;
    }
}