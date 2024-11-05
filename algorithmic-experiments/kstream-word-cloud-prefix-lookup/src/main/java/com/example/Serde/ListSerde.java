package com.example.Serde;

import org.apache.kafka.common.serialization.Serdes;

import java.util.List;

public class ListSerde extends Serdes.WrapperSerde<List<Integer>> {
    public ListSerde() {
        super(new ListSerializer(), new ListDeserializer());
    }
}