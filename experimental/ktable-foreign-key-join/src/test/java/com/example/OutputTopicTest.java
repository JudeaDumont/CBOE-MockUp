
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

	@Value("${input-topic-x.name}")
	private String inputTopicNameX;

	@Value("${output-topic.name}")
	private String outputTopicName;

	private TestInputTopic<String,Integer> inputTopic;

	private TestInputTopic<Integer,String> inputTopicX;

	private TestOutputTopic<String, String> outputTopic;

	@Autowired
	private StreamsBuilderFactoryBean streamsBuilder;

	@BeforeEach
	public void setup() {

		Properties props = streamsBuilder.getStreamsConfiguration();
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

		this.testDriver = new TopologyTestDriver(streamsBuilder.getTopology(), props);
		logger.info(streamsBuilder.getTopology().describe().toString());
		this.inputTopic = testDriver.createInputTopic(inputTopicName, Serdes.String().serializer(), Serdes.Integer().serializer());
		this.inputTopicX = testDriver.createInputTopic(inputTopicNameX, Serdes.Integer().serializer(), Serdes.String().serializer());
		this.outputTopic = testDriver.createOutputTopic(outputTopicName, Serdes.String().deserializer(), Serdes.String().deserializer());
	}

	@AfterEach
	public void after() {
		if (testDriver != null) {
			this.testDriver.close();
		}
	}

	@Test
	public void testTopologyLogic() {
		inputTopic.pipeInput("a", 1, 1L);
		inputTopic.pipeInput("b", 2, 10L);
		inputTopic.pipeInput("c", 3, 20L);
		inputTopic.pipeInput("g", 4, 30L);
		inputTopicX.pipeInput(1, "d", 40L);
		inputTopicX.pipeInput(2, "e", 50L);
		inputTopicX.pipeInput(3, "f", 60L);
		inputTopicX.pipeInput(5, "h", 70L);

		Awaitility.waitAtMost(Duration.ofSeconds(5)).until(() -> outputTopic.getQueueSize() == 3L);
		assertThat(outputTopic.readValuesToList()).isEqualTo(List.of("1d", "2e", "3f"));
	}
}
