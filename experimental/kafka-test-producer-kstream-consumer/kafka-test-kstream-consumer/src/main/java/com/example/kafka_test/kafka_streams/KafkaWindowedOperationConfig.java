package com.example.kafka_test.kafka_streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.time.Duration;
import java.util.Properties;

@EnableKafkaStreams
public class KafkaWindowedOperationConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaStreams kafkaStreams() {
        StreamsBuilder builder = new StreamsBuilder();

        // Define a stream from the Kafka topic
        KStream<String, String> messageStream = builder.stream("example-topic");

        // Perform windowed aggregation (e.g., count messages within 1-minute windows)
        KTable<Windowed<String>, Long> windowedCounts = messageStream
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofMinutes(1)))
                .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>as("windowed-counts-store"));

        // Forward the windowed counts to a Kafka topic or to a WebSocket (later)
        windowedCounts.toStream().foreach((windowedKey, count) -> {
            // Forward the windowed result to WebSocket (or another output)
            System.out.println("Windowed key: " + windowedKey + ", Count: " + count);
        });

        // Build the Kafka Streams topology
        Topology topology = builder.build();
        return new KafkaStreams(topology, getStreamsConfig());
    }

    private Properties getStreamsConfig() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-windowed-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        return props;
    }
}
