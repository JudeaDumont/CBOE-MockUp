package com.example.user_service.service;

import com.example.user_service.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final UserService userService;

    @KafkaListener(topics = "user-registration-topic", groupId = "user-service-group")
    public void consume(User user) {
        userService.saveUser(user);
        System.out.println("Consumed message: " + user);
    }
}