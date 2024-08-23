package com.example.user_service.controller;

import com.example.user_service.model.User;
import com.example.user_service.security.JwtTokenProvider;
import com.example.user_service.service.UserService;
import com.example.user_service.util.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminToken = TestHelper.generateAdminToken();
    }

    @Test
    void testCreateUser() throws Exception {
        // Arrange
        User user = new User();
        user.setUsername("tester");
        user.setEmail("tester@example.com");

        when(userService.saveUser(any(User.class))).thenReturn(user);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"tester\", \"email\": \"tester@example.com\", \"fullName\": \"Test User\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("tester"))
                .andExpect(jsonPath("$.email").value("tester@example.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"));

        verify(userService, times(1)).saveUser(any(User.class));
    }

    @Test
    void testGetUserByUsername_UserExists() throws Exception {
        // Arrange
        User user = new User();
        user.setUsername("tester");
        user.setEmail("tester@example.com");

        when(userService.findByUsername("tester")).thenReturn(Optional.of(user));

        // Act & Assert
        mockMvc.perform(get("/api/users/tester")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("tester"))
                .andExpect(jsonPath("$.email").value("tester@example.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"));

        verify(userService, times(1)).findByUsername("tester");
    }

    @Test
    void testGetUserByUsername_UserDoesNotExist() throws Exception {
        // Arrange
        when(userService.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/users/nonexistent")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findByUsername("nonexistent");
    }

    @Test
    void testDeleteUser() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/users/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }
}
