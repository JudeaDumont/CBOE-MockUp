package com.example;

import com.example.Model.GeneratePermutationsChecks;
import com.example.Serde.ListSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import static com.example.Model.GeneratePermutationsChecks.generatePermutations;


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

        builder.stream(inputTopic, Consumed.with(Serdes.String(), new ListSerde<String>()))
                .mapValues((key, val)->generatePermutations(key, val.toArray(new String[0])))
                .to(outputTopic, Produced.with(Serdes.String(), new ListSerde<Integer>()));
    }
}
