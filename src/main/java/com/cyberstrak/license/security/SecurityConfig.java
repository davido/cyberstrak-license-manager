package com.cyberstrak.license.security;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configures Spring Security to use HTTP Basic authentication and allow public access to Swagger
 * and root endpoints.
 */
@Configuration
@ConditionalOnWebApplication
public class SecurityConfig {
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtFilter)
      throws Exception {
    http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/api/auth/login" // <--- LOGIN ERLAUBEN
                        )
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .with(new HttpBasicConfigurer<>(), c -> {})
        .csrf(csrf -> csrf.disable())
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

    // JWT vor UsernamePasswordAuthenticationFilter prüfen
    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public AuthenticationManager authManager(BasicAuthProvider provider) {
    return new ProviderManager(provider);
  }

  // CORS: React-Dev-Server erlauben
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration c = new CorsConfiguration();
    c.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    c.setAllowCredentials(false);
    UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
    src.registerCorsConfiguration("/**", c);
    return src;
  }
}
