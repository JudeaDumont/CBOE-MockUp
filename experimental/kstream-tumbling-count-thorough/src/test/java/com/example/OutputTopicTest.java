
package com.example;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
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
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class OutputTopicTest {
	private final Logger logger = LoggerFactory.getLogger(OutputTopicTest.class);

	private TopologyTestDriver testDriver;

	@Value("${input-topic.name}")
	private String inputTopicName;

	@Value("${output-topic.name}")
	private String outputTopicName;

	private TestInputTopic<Integer,String> inputTopic;

	private TestOutputTopic<Integer, Long> outputTopic;

	@Autowired
	private StreamsBuilderFactoryBean streamsBuilder;

	@BeforeEach
	public void setup() {

		Properties props = streamsBuilder.getStreamsConfiguration();
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Integer().getClass().getName());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Long().getClass().getName());

		this.testDriver = new TopologyTestDriver(streamsBuilder.getTopology(), props);
		logger.info(streamsBuilder.getTopology().describe().toString());
		this.inputTopic = testDriver.createInputTopic(inputTopicName, Serdes.Integer().serializer(), Serdes.String().serializer());
		this.outputTopic = testDriver.createOutputTopic(outputTopicName, Serdes.Integer().deserializer(), Serdes.Long().deserializer());
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
		inputTopic.pipeInput(1, "test2", 11L);
		inputTopic.pipeInput(2, "test3", 21L);
		inputTopic.pipeInput(2, "test4", 31L);
		inputTopic.pipeInput(3, "test5", 41L);

		Awaitility.waitAtMost(Duration.ofSeconds(5)).until(() -> outputTopic.getQueueSize() > 0L);
		assertThat(outputTopic.readValuesToList()).isEqualTo(List.of(1L, 2L, 1L, 2L, 1L));

		inputTopic.pipeInput(1, "test1", 6001L);
		inputTopic.pipeInput(1, "test2", 6011L);
		inputTopic.pipeInput(2, "test3", 6021L);
		inputTopic.pipeInput(2, "test4", 6031L);
		inputTopic.pipeInput(3, "test5", 6041L);
		inputTopic.pipeInput(1, "test1", 12001L);
		inputTopic.pipeInput(1, "test2", 12011L);
		inputTopic.pipeInput(2, "test3", 12021L);
		inputTopic.pipeInput(2, "test4", 12031L);
		inputTopic.pipeInput(3, "test5", 12041L);

		Awaitility.waitAtMost(Duration.ofSeconds(5)).until(() -> outputTopic.getQueueSize() > 0L);
		assertThat(outputTopic.readValuesToList()).isEqualTo(List.of(1L, 2L, 1L, 2L, 1L, 1L, 2L, 1L, 2L, 1L));

		inputTopic.pipeInput(1, "test1", 18001L);
		inputTopic.pipeInput(1, "test2", 18011L);
		inputTopic.pipeInput(2, "test3", 18021L);
		inputTopic.pipeInput(2, "test4", 18031L);
		inputTopic.pipeInput(3, "test5", 18041L);
		inputTopic.pipeInput(1, "test1", 18051L);
		inputTopic.pipeInput(1, "test2", 18061L);
		inputTopic.pipeInput(2, "test3", 18071L);
		inputTopic.pipeInput(2, "test4", 18081L);
		inputTopic.pipeInput(3, "test5", 18091L);

		Awaitility.waitAtMost(Duration.ofSeconds(5)).until(() -> outputTopic.getQueueSize() > 0L);
		assertThat(outputTopic.readValuesToList()).isEqualTo(List.of(1L, 2L, 1L, 2L, 1L, 3L, 4L, 3L, 4L, 2L));
	}
}
