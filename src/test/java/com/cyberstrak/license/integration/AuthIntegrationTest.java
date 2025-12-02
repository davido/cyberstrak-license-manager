package com.cyberstrak.license.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.cyberstrak.license.LicenseManagerApplication;
import com.cyberstrak.license.controller.AuthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    classes = LicenseManagerApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestRestTemplate
public class AuthIntegrationTest {

  @LocalServerPort int port;

  @Autowired TestRestTemplate restTemplate;

  private String baseUrl() {
    return "http://localhost:" + port;
  }

  @Test
  void loginWithValidCredentialsReturnsJwt() {
    // 1. Login Request
    var req = new AuthController.LoginRequest("system", "manager");
    ResponseEntity<AuthController.LoginResponse> resp =
        restTemplate.postForEntity(
            baseUrl() + "/api/auth/login", req, AuthController.LoginResponse.class);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(resp.getBody()).isNotNull();
    assertThat(resp.getBody().token()).isNotBlank();
  }

  @Test
  void loginWithInvalidCredentialsFails() {
    var req = new AuthController.LoginRequest("bob", "wrong");
    ResponseEntity<String> resp =
        restTemplate.postForEntity(baseUrl() + "/api/auth/login", req, String.class);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(resp.getBody()).contains("Unauthorized");
  }

  @Test
  void accessProtectedEndpointRequiresJwt() {
    ResponseEntity<String> resp =
        restTemplate.getForEntity(baseUrl() + "/api/licenses", String.class);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void accessProtectedEndpointWithValidJwtSucceeds() {
    // 1. Login → JWT holen
    var loginReq = new AuthController.LoginRequest("system", "manager");
    var loginResp =
        restTemplate.postForEntity(
            baseUrl() + "/api/auth/login", loginReq, AuthController.LoginResponse.class);

    assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    String jwt = loginResp.getBody().token();

    // 2. Mit JWT an geschützten Endpoint
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(jwt);

    HttpEntity<Void> entity = new HttpEntity<>(headers);

    ResponseEntity<String> resp =
        restTemplate.exchange(baseUrl() + "/dump_licenses", HttpMethod.GET, entity, String.class);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void accessProtectedEndpointWithInvalidJwtFails() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth("invalid-token");

    HttpEntity<Void> entity = new HttpEntity<>(headers);

    ResponseEntity<String> resp =
        restTemplate.exchange(baseUrl() + "/dump_licenses", HttpMethod.GET, entity, String.class);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
}
