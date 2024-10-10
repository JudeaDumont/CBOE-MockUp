package com.example.user_details_service.service;

import com.example.user_details_service.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, User> kafkaTemplate;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, User> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, String key, User message) {
        kafkaTemplate.send(topic, key, message);
    }

    public void sendMessage(String topic, User message) {
        kafkaTemplate.send(topic, message);
    }
}