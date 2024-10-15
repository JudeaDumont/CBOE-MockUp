
package com.example;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;


@Configuration
@Component
public class Topology {
	private final String inputTopic;

	private final String outputTopicA;
	private final String outputTopicB;
	private final String outputTopicC;

	@Autowired
	public Topology(@Value("${input-topic.name}") final String inputTopic,
					@Value("${output-topic-a.name}") final String outputTopicA,
					@Value("${output-topic-b.name}") final String outputTopicB,
					@Value("${output-topic-c.name}") final String outputTopicC) {
		this.inputTopic = inputTopic;
		this.outputTopicA = outputTopicA;
		this.outputTopicB = outputTopicB;
		this.outputTopicC = outputTopicC;
	}

	@Autowired
	public void defaultTopology(final StreamsBuilder builder) {
		KStream<String, String> stream = builder.stream(inputTopic, Consumed.with(Serdes.String(), Serdes.String()));
		Map<String, KStream<String, String>> branches =
				stream.split(Named.as("Branch-"))
						.branch((key, value) -> key.startsWith("A"),
								Branched.as("A"))
						.branch((key, value) -> key.startsWith("B"),
								Branched.as("B"))
						.defaultBranch(Branched.as("C"));

		branches.get("Branch-A")
				.groupByKey()
				.count()
				.suppress(Suppressed.untilTimeLimit(Duration.ofMillis(5), Suppressed.BufferConfig.unbounded()))
				.toStream()
				.to(outputTopicA);

		branches.get("Branch-B")
				.groupByKey()
				.count()
				.suppress(Suppressed.untilTimeLimit(Duration.ofMillis(5), Suppressed.BufferConfig.unbounded()))
				.toStream()
				.to(outputTopicB);

		branches.get("Branch-C")
				.groupByKey()
				.count()
				.suppress(Suppressed.untilTimeLimit(Duration.ofMillis(5), Suppressed.BufferConfig.unbounded()))
				.toStream()
				.to(outputTopicC);
	}
}
