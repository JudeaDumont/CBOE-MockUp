package com.example.kafka_test.state_store;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class StateStoreQueryService {

    // Use StreamsBuilderFactoryBean to access KafkaStreams
    @Autowired
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    public void queryStateStore(String storeName, String key) {
        try {
            // Get KafkaStreams instance from StreamsBuilderFactoryBean
            KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();

            if (kafkaStreams == null) {
                System.err.println("Kafka Streams is not initialized yet.");
                return;
            }

            // Retrieve the state store
            ReadOnlyWindowStore<String, Long> windowStore = kafkaStreams.store(
                    StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.windowStore())
            );

            // Query the store for a specific key and time range
            Instant timeFrom = Instant.now().minusSeconds(20);  // 20 seconds ago
            Instant timeTo = Instant.now();  // Current time

            WindowStoreIterator<Long> iterator = windowStore.fetch(key, timeFrom, timeTo);

            // Print results to the console
            while (iterator.hasNext()) {
                KeyValue<Long, Long> entry = iterator.next();
                System.out.println("Window start time: " + entry.key + ", Count: " + entry.value);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error querying the state store: " + e.getMessage());
        }
    }
}
