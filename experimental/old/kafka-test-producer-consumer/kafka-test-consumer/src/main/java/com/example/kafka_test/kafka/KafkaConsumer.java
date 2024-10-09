package com.example.kafka_test.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "example-topic", groupId = "group_id")
    public void consume(String message) {
        System.out.println("Received message from Kafka: " + message);

        messagingTemplate.convertAndSend("/topic/kafkaMessages", message);
    }
}
