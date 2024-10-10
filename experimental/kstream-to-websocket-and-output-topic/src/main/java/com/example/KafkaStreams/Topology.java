package com.example.KafkaStreams;


import java.io.IOException;
import java.time.Duration;

import com.example.WebSocket.WebSocketHandler;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Suppressed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


@Configuration
@Component
public class Topology {
	private final String inputTopic;

	private final String outputTopic;

	private final WebSocketHandler webSocketHandler;

	@Autowired
	public Topology(@Value("${input-topic.name}") final String inputTopic,
					@Value("${output-topic.name}") final String outputTopic,
					WebSocketHandler webSocketHandler) {
		this.inputTopic = inputTopic;
		this.outputTopic = outputTopic;
		this.webSocketHandler = webSocketHandler;
	}

	@Autowired
	public void defaultTopology(final StreamsBuilder builder) {
		builder.stream(inputTopic, Consumed.with(Serdes.Integer(), Serdes.String()))
				.groupByKey()
				.count()
				.suppress(Suppressed.untilTimeLimit(Duration.ofMillis(5), Suppressed.BufferConfig.unbounded()))
				.toStream()
				.peek((key, value) -> {
					try {
						System.out.println("Key: " + key + ", Count: " + value);
						webSocketHandler.sendMessage("Key: " + key + ", Count: " + value);
					} catch (IOException e) {
						e.printStackTrace();
					}
				})
				.to(outputTopic);
	}
}
