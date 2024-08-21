package com.example.user_service.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class UserDetailsClientImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserDetailsClientImpl userDetailsClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize the mocks
    }

    @Value("${user.details.service.url}")
    private String userDetailsServiceUrl;

    @Test
    void getUserDetailsByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // Arrange
        Map<String, Object> response = new HashMap<>();
        response.put("username", "user");
        response.put("roles", List.of("USER"));

        // Forcefully mocking RestTemplate -> idk why i have to do this
        RestTemplate mockRestTemplate = Mockito.mock(RestTemplate.class);

        String specificUrl = userDetailsServiceUrl + "/{username}";

        when(mockRestTemplate.getForObject(eq(specificUrl), eq(Map.class), eq("user")))
                .thenReturn(response);

        // Inject the mock RestTemplate into the userDetailsClient
        userDetailsClient = new UserDetailsClientImpl(mockRestTemplate, userDetailsServiceUrl);

        // Act
        UserDetails userDetails = userDetailsClient.getUserDetailsByUsername("user");

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }
}