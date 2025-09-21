package com.cyberstrak.license.controller;

import com.cyberstrak.license.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public AuthController(AuthenticationManager am, JwtService jwtService) {
    this.authenticationManager = am;
    this.jwtService = jwtService;
  }

  public record LoginRequest(String username, String password) {}

  public record LoginResponse(String token, long expiresInSeconds) {}

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
    var auth =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.username(), req.password()));
    var token = jwtService.generateToken(req.username(), auth.getAuthorities());
    return ResponseEntity.ok(new LoginResponse(token, 3600));
  }
}
