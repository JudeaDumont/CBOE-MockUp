package com.example.postgres_test_container_with_kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class PostgresTestContainerWithKafkaApplication {

	public static void main(String[] args) {
		SpringApplication.run(PostgresTestContainerWithKafkaApplication.class, args);
	}
}
