package com.example.EOSTest.service;

import com.example.EOSTest.model.EnrichedIndexTrade;
import com.example.EOSTest.model.IndexMetadata;
import com.example.EOSTest.model.IndexTrade;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class FinancialIndexStreamProcessor {

    public Properties getStreamsConfiguration() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "financial-index-stream-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName());
        props.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams"); // State store location
        return props;
    }

    public void buildTopology(StreamsBuilder builder) {
        JsonSerde<IndexTrade> tradeSerde = new JsonSerde<>(IndexTrade.class);
        JsonSerde<IndexMetadata> metadataSerde = new JsonSerde<>(IndexMetadata.class);
        JsonSerde<EnrichedIndexTrade> enrichedTradeSerde = new JsonSerde<>(EnrichedIndexTrade.class);

        // KStream for trades
        KStream<String, IndexTrade> tradeStream = builder.stream("index-trades", Consumed.with(Serdes.String(), tradeSerde));

        // KTable for metadata
        KTable<String, IndexMetadata> metadataTable = builder.table("index-metadata", Materialized.with(Serdes.String(), metadataSerde));

        // Join the stream and table
        KStream<String, EnrichedIndexTrade> enrichedStream = tradeStream.join(metadataTable,
                (trade, metadata) -> new EnrichedIndexTrade(
                        trade.getIndexSymbol(),
                        trade.getTradePrice(),
                        trade.getVolume(),
                        metadata.getIndexName(),
                        metadata.getSector()
                ));

        // Write the enriched results to a new topic
        enrichedStream.to("enriched-index-trades", Produced.with(Serdes.String(), enrichedTradeSerde));
    }
}