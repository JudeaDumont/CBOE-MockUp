
package com.example;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
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

	private TestInputTopic<String,String> inputTopic;

	private TestOutputTopic<String, Integer> outputTopic;

	@Autowired
	private StreamsBuilderFactoryBean streamsBuilder;

	@BeforeEach
	public void setup() {

		Properties props = streamsBuilder.getStreamsConfiguration();
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Integer().getClass().getName());

		this.testDriver = new TopologyTestDriver(streamsBuilder.getTopology(), props);
		logger.info(streamsBuilder.getTopology().describe().toString());
		this.inputTopic = testDriver.createInputTopic(inputTopicName, Serdes.String().serializer(), Serdes.String().serializer());
		this.outputTopic = testDriver.createOutputTopic(outputTopicName, Serdes.String().deserializer(), Serdes.Integer().deserializer());
	}

	@AfterEach
	public void after() {
		if (testDriver != null) {
			this.testDriver.close();
		}
	}

	@Test
	public void testTopologyLogic() {
		inputTopic.pipeInput("user", "R", 1L);
		inputTopic.pipeInput("never", "A", 11L);
		inputTopic.pipeInput("never", "R", 21L);

		Awaitility.waitAtMost(Duration.ofSeconds(5)).until(() -> outputTopic.getQueueSize() == 4);

		List<KeyValue<String, Integer>> keyValues = outputTopic.readKeyValuesToList();
		assertThat(keyValues.get(0).key).isEqualTo("R");
		assertThat(keyValues.get(0).value).isEqualTo(4);

		assertThat(keyValues.get(1).key).isEqualTo("A");
		assertThat(keyValues.get(1).value).isEqualTo(5);

		assertThat(keyValues.get(2).key).isEqualTo("A");
		assertThat(keyValues.get(2).value).isEqualTo(0);

		assertThat(keyValues.get(3).key).isEqualTo("R");
		assertThat(keyValues.get(3).value).isEqualTo(9);
	}
}
