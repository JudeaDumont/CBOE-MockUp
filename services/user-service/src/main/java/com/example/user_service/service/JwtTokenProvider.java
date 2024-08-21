package com.example.user_service.service;

import com.example.user_service.client.UserDetailsClient;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final UserDetailsClient userDetailsClient;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret, UserDetailsClient userDetailsClient) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.userDetailsClient = userDetailsClient;
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // Handle token validation errors
            return false;
        }
    }

    // Implement a method to get UserDetails by username
    public UserDetails getUserDetails(String username) {
        // Implement this to load UserDetails from the database or cache
        return userDetailsClient.getUserDetailsByUsername(username);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
