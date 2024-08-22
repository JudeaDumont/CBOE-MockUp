package com.example.user_details_service.controller;

import com.example.user_details_service.model.JwtResponse;
import com.example.user_details_service.model.User;
import com.example.user_details_service.repo.UserRepository;
import com.example.user_details_service.service.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerLoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {        // Clear the database before each test
        userRepository.deleteAll();

        // Create and save a test user
        User user = new User();
        user.setUsername("tester");
        user.setPassword(passwordEncoder.encode("password"));  // Encrypt the password
        user.setRole("USER");  // Set the role for the user

        userRepository.save(user);
    }

    @Test
    void loginShouldReturnJwtToken() throws Exception {
        // Arrange
        String username = "tester";
        String password = "password";

        // Act: Perform the login request
        MvcResult mvcResult = mockMvc.perform(post("/api/user-details/login")
                .contentType("application/json")
                .content("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        // Extract the JwtResponse from the JSON response
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        JwtResponse jwtResponse = new ObjectMapper().readValue(jsonResponse, JwtResponse.class);

        // Assert: Check that the JwtResponse contains a JWT token
        String jwtToken = jwtResponse.token();
        assertThat(jwtToken).isNotEmpty();
        System.out.println(jwtToken);

        // Validate the JWT token
        String extractedUsername = jwtTokenProvider.getUsernameFromJWT(jwtToken);
        assertThat(extractedUsername).isEqualTo(username);
        System.out.println(extractedUsername);
    }
}