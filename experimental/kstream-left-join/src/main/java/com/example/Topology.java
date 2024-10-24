package com.example;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.Duration;


@Configuration
@Component
public class Topology {
    private final String inputTopic;

    private final String inputTopicX;

    private final String outputTopic;

    @Autowired
    public Topology(@Value("${input-topic.name}") final String inputTopic,
                    @Value("${input-topic-x.name}") final String inputTopicX,
                    @Value("${output-topic.name}") final String outputTopic) {
        this.inputTopic = inputTopic;
        this.inputTopicX = inputTopicX;
        this.outputTopic = outputTopic;
    }

    @Autowired
    public void defaultTopology(final StreamsBuilder builder) {
        KStream<Integer, String> stream = builder.stream(inputTopic, Consumed.with(Serdes.Integer(), Serdes.String()));
        KStream<Integer, String> streamX = builder.stream(inputTopicX, Consumed.with(Serdes.Integer(), Serdes.String()));
        stream.leftJoin(streamX, (left, right) -> right != null ? left + right : left + "0", JoinWindows.ofTimeDifferenceAndGrace(Duration.ofSeconds(5), Duration.ofSeconds(1)))
                .to(outputTopic);
    }
}
