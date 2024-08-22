package com.example.user_details_service.controller;

import com.example.user_details_service.model.LoginRequest;
import com.example.user_details_service.model.User;
import com.example.user_details_service.service.AuthService;
import com.example.user_details_service.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user-details")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {

    private final CustomUserDetailsService userDetailsService;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        return authService.authenticateUser(loginRequest);
    }

    @PostMapping
    @PreAuthorize("hasRole('USERDETAILSADMIN')")
    public ResponseEntity<User> createUserDetails(@RequestBody User userDetails) {
        User savedUserDetails = userDetailsService.saveUserDetails(userDetails);
        return ResponseEntity.ok(savedUserDetails);
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasRole('USERDETAILSADMIN')")
    public ResponseEntity<User> getUserDetailsByUsername(@PathVariable String username) {
        Optional<User> userDetails = userDetailsService.findByUsername(username);
        return userDetails.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USERDETAILSADMIN')")
    public ResponseEntity<Void> deleteUserDetails(@PathVariable Long id) {
        userDetailsService.deleteUserDetails(id);
        return ResponseEntity.noContent().build();
    }
}
