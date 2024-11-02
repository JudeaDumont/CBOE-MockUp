package com.example.Serde;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.List;
import java.util.Map;

public class ListDeserializer <T> implements Deserializer<List<T>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {}

    @Override
    public List<T> deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.readValue(data, new TypeReference<List<T>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing List<Integer>", e);
        }
    }

    @Override
    public void close() {}
}