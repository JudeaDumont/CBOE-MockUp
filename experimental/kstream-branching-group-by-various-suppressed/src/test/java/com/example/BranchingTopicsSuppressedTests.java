
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
public class BranchingTopicsSuppressedTests {
	private final Logger logger = LoggerFactory.getLogger(BranchingTopicsSuppressedTests.class);

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
		Properties props = streamsBuilder.getStreamsConfiguration();
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Long().getClass().getName());

		this.testDriver = new TopologyTestDriver(streamsBuilder.getTopology(), props);		logger.info(streamsBuilder.getTopology().describe().toString());
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

		inputTopic.pipeInput("C-msg", "test-C", 31L);
		inputTopic.pipeInput("B-msg", "test-B", 32L);
		inputTopic.pipeInput("A-msg", "test-A", 33L);

		Awaitility.waitAtMost(Duration.ofSeconds(5)).until(() -> outputTopicA.getQueueSize() == 1L);
		Awaitility.waitAtMost(Duration.ofSeconds(5)).until(() -> outputTopicB.getQueueSize() == 1L);
		Awaitility.waitAtMost(Duration.ofSeconds(5)).until(() -> outputTopicC.getQueueSize() == 1L);

		List<KeyValue<String, Long>> keyValuesA = outputTopicA.readKeyValuesToList();

		assertThat(keyValuesA.get(0).key).isEqualTo("test-A");
		assertThat(keyValuesA.get(0).value).isEqualTo(2L);

		List<KeyValue<String, Long>> keyValuesB = outputTopicB.readKeyValuesToList();
		assertThat(keyValuesB.get(0).key).isEqualTo("test-BB-msg");
		assertThat(keyValuesB.get(0).value).isEqualTo(2L);

		List<KeyValue<String, Long>> keyValuesC = outputTopicC.readKeyValuesToList();
		assertThat(keyValuesC.get(0).key).isEqualTo("C-msgtest-C");
		assertThat(keyValuesC.get(0).value).isEqualTo(2L);
	}
}
