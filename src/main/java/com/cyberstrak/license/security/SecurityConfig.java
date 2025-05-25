package com.cyberstrak.license.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures Spring Security to use HTTP Basic authentication and allow public access to Swagger
 * and root endpoints.
 */
@Configuration
@ConditionalOnWebApplication
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/",
                        "/actuator/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-ui/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .with(new HttpBasicConfigurer<>(), c -> {})
        .csrf(csrf -> csrf.disable()) // Use lambda style for built-in configurers
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())); // Same here

    return http.build();
  }

  @Bean
  public AuthenticationManager authManager(BasicAuthProvider provider) {
    return new ProviderManager(provider);
  }
}
