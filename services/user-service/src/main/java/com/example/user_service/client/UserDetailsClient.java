package com.example.user_service.client;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserDetailsClient {
    UserDetails getUserDetailsByUsername(String username);
}