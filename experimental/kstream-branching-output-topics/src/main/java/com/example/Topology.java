/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

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
				.toStream()
				.to(outputTopicA);

		branches.get("Branch-B")
				.groupByKey()
				.count()
				.toStream()
				.to(outputTopicB);

		branches.get("Branch-C")
				.groupByKey()
				.count()
				.toStream()
				.to(outputTopicC);
	}
}
