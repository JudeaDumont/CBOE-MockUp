package com.example.user_details_service.service;

import com.example.user_details_service.model.AuthUser;
import com.example.user_details_service.model.User;
import com.example.user_details_service.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadUserByUsername_UserExists() {
        // Arrange
        AuthUser user = new AuthUser();
        user.setUsername("tester");
        user.setPassword("password");
        user.setRole("tester");
        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));

        // Act
        var userDetails = customUserDetailsService.loadUserByUsername("tester");

        // Assert
        assertNotNull(userDetails);
        assertEquals("tester", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
    }

    @Test
    void loadUserByUsername_UserDoesNotExist() {
        // Arrange
        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("nonexistentuser");
        });
    }

    @KafkaListener(topics = "user-registration-topic", groupId = "user-test-group")
    public void consume(User user) {
        System.out.println("Consumed message: " + user);
    }
}
