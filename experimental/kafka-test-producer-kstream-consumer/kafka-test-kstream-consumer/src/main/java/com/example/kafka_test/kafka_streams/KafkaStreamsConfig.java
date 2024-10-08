package com.example.kafka_test.kafka_streams;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;

import java.time.Duration;
import java.util.stream.IntStream;

import static org.apache.kafka.streams.kstream.SlidingWindows.ofTimeDifferenceAndGrace;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaStreamsConfig {

    @Bean
    public StreamsBuilderFactoryBeanConfigurer configurer() {
        return fb -> {
            fb.setStateListener((newState, oldState) -> {
                System.out.println("State transition from " + oldState + " to " + newState);
                if (newState == KafkaStreams.State.ERROR) {
                    IntStream.range(0, 100).forEach(i -> {
                        System.err.println("Kafka Streams entered ERROR state. Please check logs.");
                    });
                }
            });

            fb.setKafkaStreamsCustomizer(kafkaStreams -> kafkaStreams.setUncaughtExceptionHandler((t, e) -> {
                System.err.println("Uncaught exception in Kafka Streams thread: " + t.getName());
                e.printStackTrace();
            }));
        };
    }

    @Bean
    public KStream<Integer, String> kStream(StreamsBuilder kStreamBuilder) {
        KStream<Integer, String> stream = kStreamBuilder.stream("example-topic");
        stream
                .groupByKey()
                .windowedBy(ofTimeDifferenceAndGrace(Duration.ofSeconds(1), Duration.ofSeconds(1)))
                .reduce((oldValue, newValue) -> {
                            System.out.println("Old Value: " + oldValue +
                                    ", New Value: " + newValue);
                            return oldValue + newValue;
                        },
                        Named.as("my-test-store"))
                .toStream()
                .map((s, s2) -> {
                    System.out.println("New Key: " + s.key());
                    System.out.println("New Value: " + s2);
                    return new KeyValue<>(s.key(), s2);
                })
                .to("flush-topic");

        return stream;
    }
}