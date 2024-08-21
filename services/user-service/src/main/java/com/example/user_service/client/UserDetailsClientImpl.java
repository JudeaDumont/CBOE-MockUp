package com.example.user_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserDetailsClientImpl implements UserDetailsClient {

    private final RestTemplate restTemplate;
    private final String userDetailsServiceUrl;

    public UserDetailsClientImpl(RestTemplate restTemplate, 
                                 @Value("${user.details.service.url}") String userDetailsServiceUrl) {
        this.restTemplate = restTemplate;
        this.userDetailsServiceUrl = userDetailsServiceUrl;
    }

    @Override
    public UserDetails getUserDetailsByUsername(String username) {
        String url = userDetailsServiceUrl + "/api/userdetails/" + username;
        return restTemplate.getForObject(url, UserDetails.class);
    }
}