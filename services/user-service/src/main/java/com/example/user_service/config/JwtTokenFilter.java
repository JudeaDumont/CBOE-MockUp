package com.example.user_service.config;

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
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

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

            UserDetails userDetails = jwtTokenProvider.getUserDetails(username);

            if (userDetails != null && tokenRoles != null && !tokenRoles.isEmpty()) {
                // Check if all authorities in UserDetails matches the roles in the token
                boolean hasRole = userDetails.getAuthorities().stream()
                        .allMatch(grantedAuthority -> tokenRoles.contains(grantedAuthority.getAuthority().replace("ROLE_", "")));

                if (!hasRole) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid role");
                    return;
                }

                // Set authentication in the security context
                var authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}