package com.example.EOSTest.service;

import com.example.EOSTest.model.EnrichedIndexTrade;
import com.example.EOSTest.model.IndexMetadata;
import com.example.EOSTest.model.IndexTrade;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class FinancialIndexStreamProcessorTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, IndexTrade> inputTradeTopic;
    private TestInputTopic<String, IndexMetadata> inputMetadataTopic;
    private TestOutputTopic<String, EnrichedIndexTrade> outputEnrichedTopic;
    private FinancialIndexStreamProcessor processor;

    @BeforeEach
    void setup() {
        processor = new FinancialIndexStreamProcessor();
        Properties props = processor.getStreamsConfiguration();

        StreamsBuilder builder = new StreamsBuilder();
        processor.buildTopology(builder);

        testDriver = new TopologyTestDriver(builder.build(), props);

        // Create input and output topics
        inputTradeTopic = testDriver.createInputTopic("index-trades", Serdes.String().serializer(), new JsonSerializer<>());
        inputMetadataTopic = testDriver.createInputTopic("index-metadata", Serdes.String().serializer(), new JsonSerializer<>());
        outputEnrichedTopic = testDriver.createOutputTopic("enriched-index-trades", Serdes.String().deserializer(), new JsonDeserializer<>(EnrichedIndexTrade.class));
    }

    @Test
    void testKStreamKTableJoin() {
        // Prepare and pipe metadata
        IndexMetadata metadata = new IndexMetadata("SPX", "S&P 500", "Equities");
        inputMetadataTopic.pipeInput("SPX", metadata);

        // Prepare and pipe trade
        IndexTrade trade = new IndexTrade("SPX", 4200.50, 100);
        inputTradeTopic.pipeInput("SPX", trade);

        // Retrieve the enriched trade
        EnrichedIndexTrade enrichedTrade = outputEnrichedTopic.readValue();

        // Assertions
        assertThat(enrichedTrade).isNotNull();
        assertThat(enrichedTrade.getIndexSymbol()).isEqualTo("SPX");
        assertThat(enrichedTrade.getTradePrice()).isEqualTo(4200.50);
        assertThat(enrichedTrade.getVolume()).isEqualTo(100);
        assertThat(enrichedTrade.getIndexName()).isEqualTo("S&P 500");
        assertThat(enrichedTrade.getSector()).isEqualTo("Equities");
    }
}