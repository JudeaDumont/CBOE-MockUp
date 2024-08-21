package com.example.user_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class UserDetailsClientImpl implements UserDetailsClient {

    private final RestTemplate restTemplate;
    private final String userDetailsServiceUrl;

    public UserDetailsClientImpl(RestTemplate restTemplate, @Value("${user.details.service.url}") String userDetailsServiceUrl) {
        this.restTemplate = restTemplate;
        this.userDetailsServiceUrl = userDetailsServiceUrl;
    }

    @Override
    public UserDetails getUserDetailsByUsername(String username) {
        String url = userDetailsServiceUrl + "/{username}";

        Map<String, Object> response = restTemplate.getForObject(url, Map.class, username);

        if (response != null && response.containsKey("username") && response.containsKey("roles")) {
            String fetchedUsername = (String) response.get("username");
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) response.get("roles");

            return User.withUsername(fetchedUsername)
                    .password("")  // Assuming password isn't needed here
                    .roles(roles.toArray(new String[0]))
                    .build();
        }

        throw new UsernameNotFoundException("User not found");
    }
}