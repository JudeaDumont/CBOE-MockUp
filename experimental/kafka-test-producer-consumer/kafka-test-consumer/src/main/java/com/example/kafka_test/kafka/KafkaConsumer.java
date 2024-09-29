package com.example.kafka_test.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    @KafkaListener(topics = "example-topic", groupId = "group_id")
    public void consume(String message) {
        System.out.println("Received message: " + message);
    }
}