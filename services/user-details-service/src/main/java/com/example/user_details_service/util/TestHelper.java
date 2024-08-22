package com.example.user_details_service.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TestHelper {

    private static Key key; //This cannot be a randomly generated key
    private static int jwtExpirationInMs;

    private final Environment environment;

    // Constructor to inject the Environment
    public TestHelper(Environment environment) {
        this.environment = environment;
    }

    // Initialize the key and expiration time using values from application.properties
    @PostConstruct
    private void init() {
        String secret = environment.getProperty("jwt.secret");
        key = Keys.hmacShaKeyFor(secret.getBytes());
        jwtExpirationInMs = Integer.parseInt(environment.getProperty("jwt.expiration", "86400000")); // default to 1 day
    }

    // Generates a JWT token for a given username and role
    public static String generateToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", List.of(role));  // Include the role in the claims

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public static String generateAdminDetailsToken() {
        return generateToken("admin", "USERDETAILSADMIN");
    }
}
