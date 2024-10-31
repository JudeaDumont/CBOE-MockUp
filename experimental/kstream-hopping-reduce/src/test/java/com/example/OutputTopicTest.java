
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

	private TestOutputTopic<Integer, String> outputTopic;

	@Autowired
	private StreamsBuilderFactoryBean streamsBuilder;

	@BeforeEach
	public void setup() {

		Properties props = streamsBuilder.getStreamsConfiguration();
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Integer().getClass().getName());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

		this.testDriver = new TopologyTestDriver(streamsBuilder.getTopology(), props);
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
		inputTopic.pipeInput(1, "t1", 1L);
		inputTopic.pipeInput(1, "t2", 1000L);
		inputTopic.pipeInput(1, "t3", 2000L);
		inputTopic.pipeInput(1, "t4", 3000L);
		inputTopic.pipeInput(1, "t5", 4000L);
		inputTopic.pipeInput(1, "t6", 5000L);
		inputTopic.pipeInput(1, "t7", 6000L);
		inputTopic.pipeInput(1, "t8", 7000L);
		inputTopic.pipeInput(1, "t9", 8000L);
		inputTopic.pipeInput(1, "t10", 9000L);
		inputTopic.pipeInput(1, "t11", 10000L);
		inputTopic.pipeInput(1, "t12", 11000L);
		inputTopic.pipeInput(1, "t13", 12000L);

		Awaitility.waitAtMost(Duration.ofSeconds(5)).until(() -> outputTopic.getQueueSize() > 0L);
		assertThat(outputTopic.readValuesToList()).isEqualTo(List.of("t1",
				"t1t2",
				"t1t2t3",
				"t1t2t3t4",
				"t4",
				"t1t2t3t4t5",
				"t4t5",
				"t4t5t6",
				"t4t5t6t7",
				"t7",
				"t4t5t6t7t8",
				"t7t8",
				"t7t8t9",
				"t7t8t9t10",
				"t10",
				"t7t8t9t10t11",
				"t10t11",
				"t10t11t12",
				"t10t11t12t13",
				"t13"));
	}
}
