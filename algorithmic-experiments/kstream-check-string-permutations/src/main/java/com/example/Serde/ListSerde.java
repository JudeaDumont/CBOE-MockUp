package com.example.Serde;

import org.apache.kafka.common.serialization.Serdes;

import java.util.List;

public class ListSerde <T> extends Serdes.WrapperSerde<List<T>> {
    public ListSerde() {
        super(new ListSerializer<T>(), new ListDeserializer<T>());
    }
}