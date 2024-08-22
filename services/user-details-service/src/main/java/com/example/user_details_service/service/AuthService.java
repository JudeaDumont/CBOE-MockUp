package com.example.user_details_service.service;

import com.example.user_details_service.model.JwtResponse;
import com.example.user_details_service.model.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {
        try {
            // Retrieve user details from the CustomUserDetailsService
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());

            // Check if the password matches
            if (passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())) {
                // Create an authentication token
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // Set authentication in the security context
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Generate JWT token
                String jwt = jwtTokenProvider.generateToken(authentication);

                // Return the JWT token
                return ResponseEntity.ok(new JwtResponse(jwt));
            } else {
                // If the password does not match
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
            }
        } catch (AuthenticationException e) {
            // Handle authentication failure
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
        }

    }
}
