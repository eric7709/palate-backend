package com.app.palate.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // ⭐ SKIP JWT VALIDATION FOR WEBSOCKET ENDPOINTS
        String path = request.getRequestURI();
        if (path.startsWith("/api/palate/ws")) {
            log.debug("Skipping JWT filter for WebSocket endpoint: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

        if (token != null) {
            try {
                if (jwtUtil.isValid(token)) {
                    String email = jwtUtil.getEmail(token);
                    String role = jwtUtil.getRole(token);

                    if (StringUtils.hasText(role)) {
                        var authority = new SimpleGrantedAuthority(role);
                        var auth = new UsernamePasswordAuthenticationToken(email, null, List.of(authority));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("Authenticated user: {} with role: {}", email, role);
                    }
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                log.error("JWT processing failed", e);
            }
        }

        filterChain.doFilter(request, response); // always continue
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");

        if (!StringUtils.hasText(bearer)) {
            return null;
        }
        if (!bearer.startsWith("Bearer ")) {
            return null;
        }
        return bearer.substring(7);
    }
}