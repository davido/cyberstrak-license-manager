package com.cyberstrak.license.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.cyberstrak.license.LicenseManagerApplication;
import com.cyberstrak.license.security.JwtService;
// import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(classes = LicenseManagerApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AuthControllerTest {

  @Autowired MockMvc mvc;

  @Autowired ObjectMapper objectMapper;

  @MockitoBean AuthenticationManager authenticationManager;

  @MockitoBean JwtService jwtService;

  @Test
  void loginWithValidCredentialsReturnsToken() throws Exception {
    Authentication auth = new UsernamePasswordAuthenticationToken("alice", "pw", List.of());
    when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);
    when(jwtService.generateToken("alice", auth.getAuthorities())).thenReturn("dummy-token");

    AuthController.LoginRequest req = new AuthController.LoginRequest("alice", "pw");

    mvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("dummy-token"))
        .andExpect(jsonPath("$.expiresInSeconds").value(3600));
  }

  @Test
  void loginWithInvalidCredentialsReturns401() throws Exception {
    when(authenticationManager.authenticate(any(Authentication.class)))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    AuthController.LoginRequest req = new AuthController.LoginRequest("bob", "wrong");

    mvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error").value("Unauthorized"))
        .andExpect(jsonPath("$.message").value("Bad credentials"));
  }
}
