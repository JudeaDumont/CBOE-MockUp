package com.example.kafka_test.controller;

import com.example.kafka_test.kafka.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kafka")
public class KafkaProducerController {
    @Autowired
    private KafkaProducer kafkaProducer;

    @PostMapping("/publish")
    public ResponseEntity<String> publish(@RequestBody String message) {
        kafkaProducer.sendMessage(message);
        return ResponseEntity.ok("Message sent to Kafka topic: " + message);
    }
}