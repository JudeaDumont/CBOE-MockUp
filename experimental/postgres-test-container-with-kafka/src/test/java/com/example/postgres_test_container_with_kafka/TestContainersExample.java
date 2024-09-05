package com.example.postgres_test_container_with_kafka;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestContainersExample {

    private static PostgreSQLContainer<?> postgreSQLContainer;

    @BeforeAll
    public static void setUp() {
        // Start the PostgreSQL Testcontainer
        postgreSQLContainer = new PostgreSQLContainer<>("postgres:14.2")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpassword");
        postgreSQLContainer.start();
        
        System.out.println("PostgreSQL URL: " + postgreSQLContainer.getJdbcUrl());
        System.out.println("PostgreSQL Username: " + postgreSQLContainer.getUsername());
        System.out.println("PostgreSQL Password: " + postgreSQLContainer.getPassword());
    }

    @AfterAll
    public static void tearDown() {
        // Stop the container after all tests are done
        postgreSQLContainer.stop();
    }

    @Test
    public void basicTest() {
        // You can now use the JDBC URL, username, and password to connect to the database.
        System.out.println("Running a basic test with Testcontainers.");
    }
}
