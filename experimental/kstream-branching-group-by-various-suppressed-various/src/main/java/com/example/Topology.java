
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
	private final String outputTopicD;

	@Autowired
	public Topology(@Value("${input-topic.name}") final String inputTopic,
					@Value("${output-topic-a.name}") final String outputTopicA,
					@Value("${output-topic-b.name}") final String outputTopicB,
					@Value("${output-topic-c.name}") final String outputTopicC,
					@Value("${output-topic-d.name") final String outputTopicD) {
		this.inputTopic = inputTopic;
		this.outputTopicA = outputTopicA;
		this.outputTopicB = outputTopicB;
		this.outputTopicC = outputTopicC;
		this.outputTopicD = outputTopicD;
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
						.branch((key, value) -> key.startsWith("C"),
								Branched.as("C"))
						.defaultBranch(Branched.as("D"));

		branches.get("Branch-A")
				.groupBy((key, val)->val)
				.count()
				.toStream()
				.to(outputTopicA);

		branches.get("Branch-B")
				.groupBy((key, val)->val+key)
				.count()
				.suppress(Suppressed.untilTimeLimit(Duration.ofMillis(1), Suppressed.BufferConfig.unbounded()))
				.toStream()
				.to(outputTopicB);

		branches.get("Branch-C")
				.groupBy((key, val)->key+val)
				.count()
				.suppress(Suppressed.untilTimeLimit(Duration.ofMillis(200), Suppressed.BufferConfig.unbounded()))
				.toStream()
				.to(outputTopicC);


		branches.get("Branch-D")
				.groupBy((key, val)->key+val)
				.count()
				.suppress(Suppressed.untilTimeLimit(Duration.ofMillis(200), Suppressed.BufferConfig.unbounded()))
				.toStream()
				.to(outputTopicD);
	}
}
