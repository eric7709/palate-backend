package com.app.palate.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // ── Public auth ──────────────────────────────────────
                        .requestMatchers("/api/palate/auth/login").permitAll()
                        .requestMatchers("/api/palate/auth/refresh").permitAll()
                        .requestMatchers("/api/palate/health").permitAll()

                        // ── WebSocket (anyone) ───────────────────────────────
                        .requestMatchers("/api/palate/ws/**").permitAll()
                        .requestMatchers("/api/palate/ping").permitAll()

                        // ── Menu items (public read + real-time) ─────────────
                        .requestMatchers(HttpMethod.GET, "/api/palate/menu-items/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/palate/menu-items/unavailable").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/palate/menu-items").hasAnyRole("ADMIN", "CASHIER")
                        .requestMatchers(HttpMethod.PUT, "/api/palate/menu-items/**").hasAnyRole("ADMIN", "CASHIER")
                        .requestMatchers(HttpMethod.DELETE, "/api/palate/menu-items/**").hasRole("ADMIN")

                        // ── Categories (public read) ─────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/palate/categories/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/palate/categories").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/palate/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/palate/categories/**").hasRole("ADMIN")

                        // ── Orders ───────────────────────────────────────────
                        // Customers place orders and view their own orders without login
                        .requestMatchers(HttpMethod.POST, "/api/palate/orders").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/palate/orders/customer/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/palate/orders/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/palate/orders").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/palate/orders/**").hasAnyRole("ADMIN", "WAITER", "CASHIER")

                        // ── Customers ────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/palate/customers").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/palate/customers").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/palate/customers/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/palate/customers/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/palate/customers/**").hasRole("ADMIN")

                        // ── Tables ───────────────────────────────────────────
                        // Waiters need to see tables and their allocations
                        .requestMatchers(HttpMethod.GET, "/api/palate/tables/**").hasAnyRole("ADMIN", "MANAGER", "WAITER", "CASHIER")
                        .requestMatchers(HttpMethod.GET, "/api/palate/tables").hasAnyRole("ADMIN", "MANAGER", "WAITER", "CASHIER")
                        .requestMatchers(HttpMethod.POST, "/api/palate/tables").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/palate/tables/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/palate/tables/**").hasRole("ADMIN")

                        // ── Table allocations ────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/palate/table-allocations/**").hasAnyRole("ADMIN", "MANAGER", "WAITER")
                        .requestMatchers(HttpMethod.POST, "/api/palate/table-allocations/**").hasAnyRole("ADMIN", "MANAGER")

                        // ── Employees ────────────────────────────────────────
                        .requestMatchers("/api/palate/employees/**").hasRole("ADMIN")

                        // ── Analytics ────────────────────────────────────────
                        .requestMatchers("/api/palate/analytics/**").hasAnyRole("ADMIN", "MANAGER")

                        // ── Dashboard ────────────────────────────────────────
                        .requestMatchers("/api/palate/dashboard/**").hasAnyRole("ADMIN", "MANAGER", "WAITER", "CASHIER")

                        // ── Authenticated staff endpoints ────────────────────
                        .requestMatchers("/api/palate/auth/me").authenticated()
                        .requestMatchers("/api/palate/auth/change-password").authenticated()

                        // ── Fallback ─────────────────────────────────────────
                        .anyRequest().authenticated())

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, exx) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"Unauthorized\"}");
                        }))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "https://palate-seven-drab.vercel.app",
                "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}