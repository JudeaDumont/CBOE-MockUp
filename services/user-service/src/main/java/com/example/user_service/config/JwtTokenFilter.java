package com.example.user_service.config;

import com.example.user_service.client.UserDetailsClient;
import com.example.user_service.service.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsClient userDetailsClient;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = jwtTokenProvider.resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsernameFromJWT(token);
            Claims claims = jwtTokenProvider.getClaimsFromToken(token);
            List<String> tokenRoles = claims.get("roles", List.class);

            if (username != null && tokenRoles != null) {
                // Retrieve UserDetails using the UserDetailsClient
                UserDetails userDetails = userDetailsClient.getUserDetailsByUsername(username);

                // Extract roles from UserDetails
                List<String> userDetailsRoles = userDetails.getAuthorities().stream()
                        .map(grantedAuthority -> grantedAuthority.getAuthority().substring(5))  // Removing "ROLE_" prefix
                        .toList();

                // Compare roles
                if (new HashSet<>(userDetailsRoles).containsAll(tokenRoles) && new HashSet<>(tokenRoles).containsAll(userDetailsRoles)) {
                    var authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // If roles don't match, access is denied
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
