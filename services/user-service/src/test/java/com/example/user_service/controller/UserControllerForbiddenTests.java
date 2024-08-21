package com.example.user_service.controller;

import com.example.user_service.client.UserDetailsClient;
import com.example.user_service.model.User;
import com.example.user_service.service.JwtTokenProvider;
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
import org.springframework.security.core.userdetails.UserDetails;
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
class UserControllerForbiddenTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsClient userDetailsClient;

    private String userToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock the UserDetailsClient to return a UserDetails object with USER role
        UserDetails userUserDetails = org.springframework.security.core.userdetails.User
                .withUsername("user")
                .password("password")
                .roles("USER")
                .build();

        when(userDetailsClient.getUserDetailsByUsername("user")).thenReturn(userUserDetails);

        userToken = TestHelper.generateToken("user", "USER");
    }

    @Test
    void testCreateUser() throws Exception {
        // Arrange
        User user = new User();
        user.setUsername("tester");
        user.setEmail("tester@example.com");
        user.setFullName("Test User");

        when(userService.saveUser(any(User.class))).thenReturn(user);

        // Act & Assert - Should fail because the role is not USERADMIN
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"tester\", \"email\": \"tester@example.com\", \"fullName\": \"Test User\"}"))
                .andExpect(status().isForbidden());

        verify(userService, never()).saveUser(any(User.class));
    }

    @Test
    void testGetUserByUsername_UserExists() throws Exception {
        // Arrange
        User user = new User();
        user.setUsername("tester");
        user.setEmail("tester@example.com");
        user.setFullName("Test User");

        when(userService.findByUsername("tester")).thenReturn(Optional.of(user));

        // Act & Assert - Should pass because the role is USER
        mockMvc.perform(get("/api/users/tester")
                        .header("Authorization", "Bearer " + userToken))
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

        // Act & Assert - Should pass because the role is USER
        mockMvc.perform(get("/api/users/nonexistent")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findByUsername("nonexistent");
    }

    @Test
    void testDeleteUser() throws Exception {
        // Act & Assert - Should fail because the role is not USERADMIN
        mockMvc.perform(delete("/api/users/1")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        verify(userService, never()).deleteUser(1L);
    }
}