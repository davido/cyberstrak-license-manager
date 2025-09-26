package com.cyberstrak.license.security;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.security.autoconfigure.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
    http.csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(
            auth ->
                auth
                    // 1) Preflight
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    // 2) Statische Ressourcen überall freigeben
                    .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/",
                        "/index.html",
                        "/favicon.ico",
                        "/assets/**",
                        "/manifest.webmanifest",
                        "/robots.txt")
                    .permitAll()
                    // 3) SPA-Routen ohne Auth (Server liefert nur index.html)
                    .requestMatchers(HttpMethod.GET, "/licenses", "/licenses/**")
                    .permitAll()
                    // 4) Deine bisherigen Public-APIs
                    .requestMatchers(
                        "/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**", "/api/auth/login")
                    .permitAll()
                    // 5) Rest der Endpunkte (deine echten APIs) braucht Auth (Basic oder JWT)
                    .anyRequest()
                    .authenticated())
        .httpBasic(Customizer.withDefaults())
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public AuthenticationManager authManager(BasicAuthProvider provider) {
    return new ProviderManager(provider);
  }

  // Globale CORS-Konfiguration (für React-Dev-Server)
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration c = new CorsConfiguration();
    c.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    c.setAllowCredentials(true); // falls Cookies oder Auth-Header mitgeschickt werden sollen
    UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
    src.registerCorsConfiguration("/**", c);
    return src;
  }
}
