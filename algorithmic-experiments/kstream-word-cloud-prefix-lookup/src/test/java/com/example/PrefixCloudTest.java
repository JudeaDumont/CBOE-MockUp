
package com.example;

import com.example.Model.PrefixCloud;
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
public class PrefixCloudTest {

	@Test
	public void testPrefixCloudTest() {
		PrefixCloud.add("bamboozle");
		assertThat(PrefixCloud.query("bamboozle")).isEqualTo( List.of("bamboozle"));
		assertThat(PrefixCloud.query("b")).isEqualTo( List.of("bamboozle"));
	}


	@Test
	public void testPrefixCloudTest2() {
		PrefixCloud.add("bamboozle");
		assertThat(PrefixCloud.query("b")).isEqualTo( List.of("bamboozle"));
	}


	@Test
	public void testPrefixCloudTest3() {
		PrefixCloud.add("bamboozle");
		PrefixCloud.add("boomer");
		assertThat(PrefixCloud.query("b")).isEqualTo( List.of("bamboozle", "boomer"));
	}
}
