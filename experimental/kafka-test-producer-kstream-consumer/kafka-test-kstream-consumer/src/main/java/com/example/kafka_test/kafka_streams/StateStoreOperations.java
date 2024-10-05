package com.example.kafka_test.kafka_streams;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.streams.KafkaStreamsInteractiveQueryService;
import org.springframework.stereotype.Component;

@Component
public class StateStoreOperations {

    @Autowired
    private KafkaStreamsInteractiveQueryService interactiveQueryService;

    public void queryStateStore(String storeName) {
        ReadOnlyKeyValueStore<Integer, String> store = interactiveQueryService.retrieveQueryableStore(storeName, QueryableStoreTypes.keyValueStore());

        KeyValueIterator<Integer, String> iterator = store.all();
        while (iterator.hasNext()) {
            KeyValue<Integer, String> entry = iterator.next();
            System.out.println("Key: " + entry.key + ", Value: " + entry.value);
        }
    }
}
