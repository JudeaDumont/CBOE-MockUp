package com.example.user_service.service;

import com.example.user_service.model.User;
import com.example.user_service.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();  // Ensure the database is clean before each test
    }

    @Test
    void testSaveUser() {
        // Arrange
        User user = new User();
        user.setUsername("tester");
        user.setEmail("tester@example.com");

        // Act
        User savedUser = userService.saveUser(user);

        // Assert
        assertNotNull(savedUser);
        assertEquals("tester", savedUser.getUsername());
        assertEquals("tester@example.com", savedUser.getEmail());
    }

    @Test
    void testFindByUsername_UserExists() {
        // Arrange
        User user = new User();
        user.setUsername("tester");
        user.setEmail("tester@example.com");
        userRepository.save(user);

        // Act
        Optional<User> foundUser = userService.findByUsername("tester");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("tester", foundUser.get().getUsername());
    }

    @Test
    void testFindByUsername_UserDoesNotExist() {
        // Act
        Optional<User> foundUser = userService.findByUsername("nonexistent");

        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testDeleteUser() {
        // Arrange
        User user = new User();
        user.setUsername("tester");
        user.setEmail("tester@example.com");
        User savedUser = userRepository.save(user);

        // Act
        userService.deleteUser(savedUser.getId());

        // Assert
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertFalse(foundUser.isPresent());
    }
}