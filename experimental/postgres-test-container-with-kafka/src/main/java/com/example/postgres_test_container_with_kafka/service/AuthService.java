package com.example.postgres_test_container_with_kafka.service;

import com.example.postgres_test_container_with_kafka.model.Auth;
import com.example.postgres_test_container_with_kafka.repo.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository userRepository;

    public Optional<Auth> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void deleteUserDetails(Long id) {
        userRepository.deleteById(id);
    }
}
