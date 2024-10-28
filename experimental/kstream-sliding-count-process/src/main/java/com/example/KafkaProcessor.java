package com.example;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

public class KafkaProcessor implements Processor<Integer, Long, Integer, Long> {

    private ProcessorContext<Integer, Long> context;


    public KafkaProcessor() {
    }

    @Override
    public void init(ProcessorContext<Integer, Long> context) {
        this.context = context;
    }

    @Override
    public void process(Record<Integer, Long> record) {
        Record<Integer, Long> newRecord = new Record<>
                (record.key(), record.value() + 1, record.timestamp());
        context.forward(newRecord);
    }

    @Override
    public void close() {
    }
}
