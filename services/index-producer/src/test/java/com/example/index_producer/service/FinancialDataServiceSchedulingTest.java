package com.example.index_producer.service;

import com.example.index_producer.util.KafkaTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 1, topics = {"financial_data"}, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class FinancialDataServiceSchedulingTest {

    @BeforeEach
    void setUp() throws Exception {
        KafkaTestUtils.waitForBroker("localhost:9092", 10, 1000);  // Wait for broker with max 10 retries, 1 second apart
    }

    @SpyBean
    private FinancialDataService financialDataService;

    @Test
    public void testScheduledPublishing() throws InterruptedException {
        // Wait for 1 seconds to allow the scheduler to run
        Thread.sleep(1000);

        // Verify that the publishData method was called at least 1000 times
        verify(financialDataService, atLeast(1000)).publishData();
        verify(financialDataService, atMost(1250)).publishData();
    }
}
