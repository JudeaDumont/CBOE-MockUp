package com.example;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
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
        builder.stream(inputTopic, Consumed.with(Serdes.Integer(), Serdes.String()))
                .selectKey((key, val)-> key+10)
                .to(outputTopic);
    }
}
