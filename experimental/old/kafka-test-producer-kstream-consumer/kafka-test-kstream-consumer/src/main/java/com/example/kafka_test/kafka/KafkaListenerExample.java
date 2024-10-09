package com.example.kafka_test.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaListenerExample {
    @KafkaListener(id = "test-listener", topics = "example-topic")
    public void listen(String in) {
        System.out.println("example-topic received: " + in);
    }
}
