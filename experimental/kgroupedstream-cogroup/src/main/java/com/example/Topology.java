package com.example;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


@Configuration
@Component
public class Topology {
    private final String inputTopic;

    private final String outputTopic;

    @Autowired
    public Topology(@Value("${input-topic.name}") final String inputTopic,
                    @Value("${output-topic.name}") final String outputTopic) {
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;
    }

    @Autowired
    public void defaultTopology(final StreamsBuilder builder) {
        KStream<Integer, String> stream = builder.stream(inputTopic, Consumed.with(Serdes.Integer(), Serdes.String()));
        KGroupedStream<Integer, String> integerStringKGroupedStream = stream
                .groupByKey();
        KGroupedStream<Integer, String> stringStringKGroupedStream2 = stream
                .groupBy((key, val) -> key + 100);

        integerStringKGroupedStream
                .cogroup((key, val, aggval) -> aggval += val)
                .cogroup(stringStringKGroupedStream2, (key, val, aggval) -> aggval += val)
                .aggregate(()->"")
                .toStream()
                .to(outputTopic);
    }
}
