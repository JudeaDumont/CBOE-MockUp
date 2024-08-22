package com.example.user_details_service.controller;

import com.example.user_details_service.model.User;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.core.userdetails.User.withUsername;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerUserDetailsForbiddenTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Generate a JWT token using TestHelper for a different role, e.g., USER role
        userToken = TestHelper.generateToken("user", "USER");

        UserDetails mockUserDetails = withUsername("admin")
                .password("password123")
                .roles("USERDETAILSADMIN")
                .build();

        when(userDetailsService.loadUserByUsername(any(String.class))).thenReturn(mockUserDetails);
    }

    @Test
    void createUserDetails_ShouldReturnForbidden_WhenRoleIsNotAdminDetails() throws Exception {
        // Arrange
        User userDetails = new User();
        userDetails.setUsername("tester");
        userDetails.setPassword("password123");
        userDetails.setRole("USERADMIN");

        // Act & Assert
        mockMvc.perform(post("/api/user-details")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDetails)))
                .andExpect(status().isForbidden());

        verify(userDetailsService, never()).saveUserDetails(any(User.class));
    }

    @Test
    void getUserDetailsByUsername_ShouldReturnForbidden_WhenRoleIsNotAdminDetails() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/user-details/tester")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        verify(userDetailsService, never()).findByUsername(any(String.class));
    }

    @Test
    void deleteUserDetails_ShouldReturnForbidden_WhenRoleIsNotAdminDetails() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/user-details/1")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        verify(userDetailsService, never()).deleteUserDetails(any(Long.class));
    }
}
