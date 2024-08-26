package com.example.index_consumer.service;

import com.example.index_consumer.model.FinancialData;
import com.example.index_consumer.websocket.FinancialDataWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FinancialDataConsumerService {

    private final FinancialDataWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "financial_data", groupId = "financial-data-group")
    public void consume(FinancialData data) {
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            webSocketHandler.sendMessageToAll(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
