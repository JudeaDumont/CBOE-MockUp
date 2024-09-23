package com.example.postgres_test_container_with_kafka.repo;

import com.example.postgres_test_container_with_kafka.model.Auth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<Auth, Long> {
    Optional<Auth> findByUsername(String username);
}
