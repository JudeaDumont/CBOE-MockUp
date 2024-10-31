
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
		inputTopic.pipeInput(1, "test1", 1L);
		inputTopic.pipeInput(1, "test2", 1000L);
		inputTopic.pipeInput(1, "test3", 2000L);
		inputTopic.pipeInput(1, "test4", 3000L);
		inputTopic.pipeInput(1, "test5", 4000L);
		inputTopic.pipeInput(1, "test6", 5000L);
		inputTopic.pipeInput(1, "test7", 6000L);
		inputTopic.pipeInput(1, "test8", 7000L);
		inputTopic.pipeInput(1, "test9", 8000L);
		inputTopic.pipeInput(1, "test10", 9000L);
		inputTopic.pipeInput(1, "test11", 10000L);
		inputTopic.pipeInput(1, "test12", 11000L);

		Awaitility.waitAtMost(Duration.ofSeconds(5)).until(() -> outputTopic.getQueueSize() > 0L);
		assertThat(outputTopic.readValuesToList()).isEqualTo(List.of("test1",
				"test1test2",
				"test1test2test3",
				"test1test2test3test4",
				"test4",
				"test1test2test3test4test5",
				"test4test5",
				"test4test5test6",
				"test4test5test6test7",
				"test7",
				"test4test5test6test7test8",
				"test7test8",
				"test7test8test9",
				"test7test8test9test10",
				"test10",
				"test7test8test9test10test11",
				"test10test11",
				"test10test11test12"));
	}
}
