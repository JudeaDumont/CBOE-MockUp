package com.example.KafkaStreams;


import com.example.WebSocket.WebSocketHandler;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Suppressed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;


@Configuration
@Component
public class TopologyWebSocket {
    private final String inputTopic;

    private final WebSocketHandler webSocketHandler;

    @Autowired
    public TopologyWebSocket(@Value("${input-topic.name}") final String inputTopic,
                             WebSocketHandler webSocketHandler) {
        this.inputTopic = inputTopic;
        this.webSocketHandler = webSocketHandler;
    }

    @Autowired
    public void webSocketTopology(final StreamsBuilder builder) {
        builder.stream(inputTopic, Consumed.with(Serdes.Integer(), Serdes.String()))
                .groupByKey()
                .count()
                .suppress(Suppressed.untilTimeLimit(Duration.ofMillis(5), Suppressed.BufferConfig.unbounded()))
                .toStream()
                .foreach((key, value) -> {
                    try {
                        System.out.println("Key: " + key + ", Count: " + value);
                        webSocketHandler.sendMessage("Key: " + key + ", Count: " + value);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }); //NOTE THAT THIS RETURNS VOID, HENCE IT IS "TERMINAL"
    }
}
