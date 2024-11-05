
package com.example;

import com.example.Serde.ListDeserializer;
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
public class OutputTopicTest {
	private final Logger logger = LoggerFactory.getLogger(OutputTopicTest.class);

	private TopologyTestDriver testDriver;

	@Value("${input-topic.name}")
	private String inputTopicName;

	@Value("${output-topic.name}")
	private String outputTopicName;

	private TestInputTopic<String,Integer> inputTopic;

	private TestOutputTopic<String, List<Integer>> outputTopic;

	@Autowired
	private StreamsBuilderFactoryBean streamsBuilder;

	@BeforeEach
	public void setup() {
		this.testDriver = new TopologyTestDriver(streamsBuilder.getTopology(), streamsBuilder.getStreamsConfiguration());
		logger.info(streamsBuilder.getTopology().describe().toString());
		this.inputTopic = testDriver.createInputTopic(inputTopicName, Serdes.String().serializer(), Serdes.Integer().serializer());
		this.outputTopic = testDriver.createOutputTopic(outputTopicName, Serdes.String().deserializer(), new ListDeserializer());
	}

	@AfterEach
	public void after() {
		if (testDriver != null) {
			this.testDriver.close();
		}
	}

	@Test
	public void testTopologyLogic() {
		inputTopic.pipeInput("jeff", 4, 1L);

		Awaitility.waitAtMost(Duration.ofSeconds(5)).until(() -> outputTopic.getQueueSize() > 0L);
		assertThat(outputTopic.readValuesToList().toString()).isEqualTo(
				"[[0, 1, 2, 3], [0, 1, 3, 2], [0, 3, 1, 2], [3, 0, 1, 2], [3, 0, 2, 1], [0, 3, 2, 1], " +
						"[0, 2, 3, 1], [0, 2, 1, 3], [2, 0, 1, 3], [2, 0, 3, 1], [2, 3, 0, 1], [3, 2, 0, 1], " +
						"[3, 2, 1, 0], [2, 3, 1, 0], [2, 1, 3, 0], [2, 1, 0, 3], [1, 2, 0, 3], [1, 2, 3, 0], " +
						"[1, 3, 2, 0], [3, 1, 2, 0], [3, 1, 0, 2], [1, 3, 0, 2], [1, 0, 3, 2], [1, 0, 2, 3]]");
	}
}
