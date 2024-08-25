package com.example.user_service.util;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;

import java.util.Properties;

public class KafkaTestUtils {

    public static void waitForBroker(String bootstrapServers, int maxRetries, long waitTimeInMillis) throws InterruptedException {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        int attempt = 0;
        boolean connected = false;

        while (attempt < maxRetries && !connected) {
            try (AdminClient client = AdminClient.create(props)) {
                client.describeCluster().nodes().get();
                connected = true;
            } catch (Exception e) {
                attempt++;
                System.out.println("Waiting for Kafka broker... Attempt " + attempt + "/" + maxRetries);
                Thread.sleep(waitTimeInMillis);
            }
        }

        if (!connected) {
            throw new IllegalStateException("Kafka broker is not available after " + maxRetries + " attempts.");
        }
    }
}