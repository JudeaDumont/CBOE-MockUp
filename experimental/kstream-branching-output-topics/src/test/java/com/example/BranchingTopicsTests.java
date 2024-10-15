
package com.example;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class BranchingTopicsTests {
	private final Logger logger = LoggerFactory.getLogger(BranchingTopicsTests.class);

	private TopologyTestDriver testDriver;

	@Value("${input-topic.name}")
	private String inputTopicName;

	@Value("${output-topic-a.name}")
	private String outputTopicNameA;

	@Value("${output-topic-b.name}")
	private String outputTopicNameB;

	@Value("${output-topic-c.name}")
	private String outputTopicNameC;

	private TestInputTopic<String,String> inputTopic;

	private TestOutputTopic<String, Long> outputTopicA;
	private TestOutputTopic<String, Long> outputTopicB;
	private TestOutputTopic<String, Long> outputTopicC;

	@Autowired
	private StreamsBuilderFactoryBean streamsBuilder;

	@BeforeEach
	public void setup() {
		this.testDriver = new TopologyTestDriver(streamsBuilder.getTopology(), streamsBuilder.getStreamsConfiguration());
		logger.info(streamsBuilder.getTopology().describe().toString());
		this.inputTopic = testDriver.createInputTopic(inputTopicName, Serdes.String().serializer(), Serdes.String().serializer());
		this.outputTopicA = testDriver.createOutputTopic(outputTopicNameA, Serdes.String().deserializer(), Serdes.Long().deserializer());
		this.outputTopicB = testDriver.createOutputTopic(outputTopicNameB, Serdes.String().deserializer(), Serdes.Long().deserializer());
		this.outputTopicC = testDriver.createOutputTopic(outputTopicNameC, Serdes.String().deserializer(), Serdes.Long().deserializer());
	}

	@AfterEach
	public void after() {
		if (testDriver != null) {
			this.testDriver.close();
		}
	}

	@Test
	public void testTopologyLogic() {
		inputTopic.pipeInput("C-msg", "test-C", 1L);
		inputTopic.pipeInput("B-msg", "test-B", 10L);
		inputTopic.pipeInput("A-msg", "test-A", 20L);

		Awaitility.waitAtMost(Duration.ofSeconds(5)).until(() -> outputTopicA.getQueueSize() == 1L);
		Awaitility.waitAtMost(Duration.ofSeconds(5)).until(() -> outputTopicB.getQueueSize() == 1L);
		Awaitility.waitAtMost(Duration.ofSeconds(5)).until(() -> outputTopicC.getQueueSize() == 1L);
		assertThat(outputTopicA.readValuesToList()).isEqualTo(List.of(1L));
		assertThat(outputTopicB.readValuesToList()).isEqualTo(List.of(1L));
		assertThat(outputTopicC.readValuesToList()).isEqualTo(List.of(1L));
	}

}
