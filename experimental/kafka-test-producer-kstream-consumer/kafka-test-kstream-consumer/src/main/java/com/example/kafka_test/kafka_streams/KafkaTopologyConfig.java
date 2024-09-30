package com.example.kafka_test.kafka_streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Configuration
@EnableKafkaStreams
public class KafkaTopologyConfig {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    public void queryStateStore(String key) throws Exception {
        // Obtain KafkaStreams from the StreamsBuilderFactoryBean
        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();

        // Query the windowed state store
        ReadOnlyWindowStore<String, Long> windowStore = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType("windowed-counts-store", QueryableStoreTypes.windowStore())
        );

        // Query for the given key within a specific time range
        WindowStoreIterator<Long> iterator = windowStore.fetch(key, Instant.now().minusSeconds(20), Instant.now());

        // Print out the results
        while (iterator.hasNext()) {
            KeyValue<Long, Long> next = iterator.next();
            System.out.println("Window start: " + next.key + ", Count: " + next.value);
        }
    }


    @Autowired
    public void configureTopology(StreamsBuilder builder) {

        // Define a stream from the Kafka topic, with explicit deserialization
        KStream<String, String> messageStream = builder.stream("example-topic", Consumed.with(Serdes.String(), Serdes.String()));

        // Perform windowed aggregation (e.g., count messages within 1-minute windows)
        KTable<Windowed<String>, Long> windowedCounts = messageStream
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(1)))  // New API
                .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>as("windowed-counts-store")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Long()));

        messageStream.groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .count(Materialized.with(Serdes.String(), Serdes.Long())).toStream()
                .peek((key, value) -> System.out.println("Grouped key: " + key + ", Count: " + value));

        // Log messages that are being consumed
        messageStream.peek((key, value) -> {
            //System.out.println(key);
            if(Objects.equals(value, "Test message 750")){
                try {
                    //System.out.println(key);
                    //queryStateStore("key1");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            //System.out.println("Received message in stream - Key: " + key + ", Value: " + value);
        });


        windowedCounts.toStream().peek((windowedKey, count) -> {
            System.out.println("Windowed event - Key: " + windowedKey.key() +
                    ", Count: " + count +
                    ", Window start: " + windowedKey.window().start() +
                    ", Window end: " + windowedKey.window().end());

            // Forward the windowed result to WebSocket
            messagingTemplate.convertAndSend("/topic/kafkaMessages", "Windowed key: " + windowedKey.key() + ", Count: " + count);
        });
    };
}
