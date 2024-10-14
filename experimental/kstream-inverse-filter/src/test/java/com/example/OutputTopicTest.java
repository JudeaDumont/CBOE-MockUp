
package com.example;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

@SpringBootTest
public class OutputTopicTest {
	private final Logger logger = LoggerFactory.getLogger(OutputTopicTest.class);

	private TopologyTestDriver testDriver;

	@Value("${input-topic.name}")
	private String inputTopicName;

	@Value("${output-topic.name}")
	private String outputTopicName;

	private TestInputTopic<Integer,String> inputTopic;

	private TestOutputTopic<Integer, String> outputTopic;

	@Autowired
	private StreamsBuilderFactoryBean streamsBuilder;

	@BeforeEach
	public void setup() {
		this.testDriver = new TopologyTestDriver(streamsBuilder.getTopology(), streamsBuilder.getStreamsConfiguration());
		logger.info(streamsBuilder.getTopology().describe().toString());
		this.inputTopic = testDriver.createInputTopic(inputTopicName, Serdes.Integer().serializer(), Serdes.String().serializer());
		this.outputTopic = testDriver.createOutputTopic(outputTopicName, Serdes.Integer().deserializer(), Serdes.String().deserializer());
	}

	@AfterEach
	public void after() {
		if (testDriver != null) {
			this.testDriver.close();
		}
	}

	@Test
	public void testTopologyLogic() {
		inputTopic.pipeInput(1, "test1", 1L);
		inputTopic.pipeInput(11, "test11", 10L);
		inputTopic.pipeInput(2, "test2", 2L);

		Awaitility.waitAtMost(Duration.ofSeconds(5)).until(() -> outputTopic.getQueueSize() == 1L);
		assertThat(outputTopic.readValuesToList()).isEqualTo(List.of("test11"));
	}
}
