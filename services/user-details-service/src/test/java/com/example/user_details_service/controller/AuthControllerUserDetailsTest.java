package com.example.user_details_service.controller;

import com.example.user_details_service.model.AuthUser;
import com.example.user_details_service.service.CustomUserDetailsService;
import com.example.user_details_service.util.TestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.security.core.userdetails.User.withUsername;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerUserDetailsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminDetailsToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Generate the JWT token using TestHelper for USERADMINDETAILS role
        adminDetailsToken = TestHelper.generateAdminDetailsToken();

        UserDetails mockUserDetails = withUsername("admin")
                .password("password123")
                .roles("USERDETAILSADMIN")
                .build();

        when(userDetailsService.loadUserByUsername(any(String.class))).thenReturn(mockUserDetails);
    }

    @Test
    void createUserDetails_ShouldReturnCreatedUserDetails() throws Exception {
        // Arrange
        AuthUser userDetails = new AuthUser();
        userDetails.setUsername("tester");
        userDetails.setPassword("password123");
        userDetails.setRole("USERADMIN");

        when(userDetailsService.saveUserDetails(any(AuthUser.class))).thenReturn(userDetails);

        // Act & Assert
        mockMvc.perform(post("/api/user-details")
                        .header("Authorization", "Bearer " + adminDetailsToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("tester"))
                .andExpect(jsonPath("$.password").value("password123"))
                .andExpect(jsonPath("$.role").value("USERADMIN"));

        verify(userDetailsService, times(1)).saveUserDetails(any(AuthUser.class));
    }

    @Test
    void getUserDetailsByUsername_ShouldReturnUserDetails_WhenUserExists() throws Exception {
        // Arrange
        AuthUser userDetails = new AuthUser();
        userDetails.setUsername("tester");
        userDetails.setPassword("password123");
        userDetails.setRole("USER");

        when(userDetailsService.findByUsername("tester")).thenReturn(Optional.of(userDetails));

        // Act & Assert
        mockMvc.perform(get("/api/user-details/tester")
                        .header("Authorization", "Bearer " + adminDetailsToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("tester"))
                .andExpect(jsonPath("$.password").value("password123"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userDetailsService, times(1)).findByUsername("tester");
    }

    @Test
    void getUserDetailsByUsername_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        // Arrange
        when(userDetailsService.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/user-details/nonexistent")
                        .header("Authorization", "Bearer " + adminDetailsToken))
                .andExpect(status().isNotFound());

        verify(userDetailsService, times(1)).findByUsername("nonexistent");
    }

    @Test
    void deleteUserDetails_ShouldReturnNoContent_WhenUserDetailsDeleted() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/user-details/1")
                        .header("Authorization", "Bearer " + adminDetailsToken))
                .andExpect(status().isNoContent());

        verify(userDetailsService, times(1)).deleteUserDetails(1L);
    }
}
