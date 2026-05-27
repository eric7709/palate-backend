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
                        .anyRequest().permitAll());

        return http.build();
    }

    // @Bean
    // public SecurityFilterChain securityFilterChain(HttpSecurity http) throws
    // Exception {

    // http
    // .cors(cors -> cors.configurationSource(corsConfigurationSource()))
    // .csrf(csrf -> csrf.disable())
    // .authorizeHttpRequests(auth -> auth
    // // =========================
    // // 🟢 PUBLIC (CUSTOMERS & AUTH)
    // // =========================
    // .requestMatchers("/api/palate/auth/**").permitAll()
    // .requestMatchers("/api/palate/health").permitAll()
    // .requestMatchers(HttpMethod.POST, "/api/palate/orders").permitAll()
    // .requestMatchers(HttpMethod.POST, "/api/palate/customers").permitAll()

    // // Public READ access
    // .requestMatchers(HttpMethod.GET, "/api/palate/categories/**").permitAll()
    // .requestMatchers(HttpMethod.GET, "/api/palate/menu-items/**").permitAll()
    // .requestMatchers(HttpMethod.GET, "/api/palate/tables/**").permitAll()
    // .requestMatchers(HttpMethod.GET, "/api/palate/orders/**").permitAll()

    // // ⭐ WebSocket endpoint - MUST be before JWT filter
    // .requestMatchers("/api/palate/ws/**").permitAll()

    // // =========================
    // // 🔴 ADMIN ONLY
    // // =========================
    // .requestMatchers("/api/palate/employees/**").hasRole("ADMIN")
    // .requestMatchers(HttpMethod.POST, "/api/palate/categories").hasRole("ADMIN")
    // .requestMatchers(HttpMethod.PUT,
    // "/api/palate/categories/**").hasRole("ADMIN")
    // .requestMatchers(HttpMethod.DELETE,
    // "/api/palate/categories/**").hasRole("ADMIN")
    // .requestMatchers(HttpMethod.POST, "/api/palate/menu-items")
    // .hasAnyRole("ADMIN", "CASHIER")
    // .requestMatchers(HttpMethod.PUT,
    // "/api/palate/menu-items/**").hasAnyRole("ADMIN", "CASHIER")
    // .requestMatchers(HttpMethod.DELETE,
    // "/api/palate/menu-items/**").hasRole("ADMIN")
    // .requestMatchers(HttpMethod.POST, "/api/palate/tables").hasRole("ADMIN")
    // .requestMatchers(HttpMethod.PUT, "/api/palate/tables/**").hasRole("ADMIN")
    // .requestMatchers(HttpMethod.DELETE, "/api/palate/tables/**").hasRole("ADMIN")
    // .requestMatchers(HttpMethod.DELETE,
    // "/api/palate/customers/**").hasRole("ADMIN")

    // // =========================
    // // 🟡 AUTHENTICATED STAFF
    // // =========================
    // .requestMatchers("/api/palate/dashboard/**").authenticated()
    // .requestMatchers("/api/palate/auth/me").authenticated()
    // .requestMatchers("/api/palate/auth/change-password").authenticated()

    // // =========================
    // // 🔒 FALLBACK
    // // =========================
    // .anyRequest().authenticated())
    // .exceptionHandling(ex -> ex
    // .authenticationEntryPoint((req, res, exx) -> {
    // res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    // res.setContentType("application/json");
    // res.getWriter().write("{\"error\":\"Unauthorized\"}");
    // }))
    // // JWT filter only protects secured endpoints
    // .addFilterBefore(jwtAuthenticationFilter,
    // UsernamePasswordAuthenticationFilter.class);
    // return http.build();
    // }

    // =========================
    // CORS CONFIG
    // =========================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }

    // =========================
    // PASSWORD ENCODER
    // =========================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}