package com.example.index_producer.service;

import com.example.index_producer.model.FinancialData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class FinancialDataService {

    private final KafkaTemplate<String, FinancialData> kafkaTemplate;

    private static final String TOPIC = "financial_data";
    private Random random = new Random();

    @Scheduled(fixedRate = 1000) // Publish a message every second
    public void publishData() {
        FinancialData data = new FinancialData();
        data.setIndexName("Index_" + random.nextInt(10));
        data.setValue(1000 + random.nextDouble() * 100);
        data.setTimestamp(System.currentTimeMillis());

        kafkaTemplate.send(TOPIC, data.getIndexName(), data);
    }
}
