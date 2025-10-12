package com.cyberstrak.license.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.cyberstrak.license.LicenseManagerApplication;
import com.cyberstrak.license.controller.AuthController;
import com.cyberstrak.license.dto.AddLicenseRequest;
import com.cyberstrak.license.dto.CreateLicenseRequest;
import com.cyberstrak.license.dto.LicenseUpsertRequest;
import com.cyberstrak.license.entity.License;
import com.cyberstrak.license.repository.LicenseRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.test.LocalServerPort;
import org.springframework.boot.web.server.test.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    classes = LicenseManagerApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LicenseControllerIntegrationTest {

  @LocalServerPort int port;
  @Autowired TestRestTemplate restTemplate;
  @Autowired LicenseRepository licenseRepository;

  private String baseUrl() {
    return "http://localhost:" + port;
  }

  @BeforeEach
  void setup() {
    licenseRepository.deleteAll();
  }

  // 🔹 Utility: login and return JWT token
  private String loginAndGetJwt() {
    var loginReq = new AuthController.LoginRequest("system", "manager");
    ResponseEntity<AuthController.LoginResponse> loginResp =
        restTemplate.postForEntity(
            baseUrl() + "/api/auth/login", loginReq, AuthController.LoginResponse.class);

    assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(loginResp.getBody()).isNotNull();
    assertThat(loginResp.getBody().token()).isNotBlank();

    return loginResp.getBody().token();
  }

  // 🔹 Utility: create standard headers with JWT
  private HttpHeaders authHeaders(String jwtToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(jwtToken);
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  // 🔹 Utility: typed POST/PUT exchange with parameterized response
  private <T> ResponseEntity<Map<String, Object>> exchange(
      String url, HttpMethod method, T body, String jwtToken) {

    HttpEntity<T> entity = new HttpEntity<>(body, authHeaders(jwtToken));

    return restTemplate.exchange(
        baseUrl() + url, method, entity, new ParameterizedTypeReference<Map<String, Object>>() {});
  }

  // ──────────────────────────────────────────────────────────────
  // Tests
  // ──────────────────────────────────────────────────────────────

  @Test
  void createLicense_persistsAllFieldsCorrectly() {
    String jwt = loginAndGetJwt();

    String serial = "INTEG_FULL_001";
    String key = "KEY_FULL_001";
    String productId = "PROD_FULL_001";
    int numberOfSeats = 3;
    String email = "fulltest@example.org";
    String comment = "Integration test for all fields";

    LocalDateTime expirationDate = LocalDate.now().plusYears(1).atStartOfDay();
    long expirationEpoch = expirationDate.atZone(ZoneId.systemDefault()).toEpochSecond();

    CreateLicenseRequest.LicenseData licenseData =
        new CreateLicenseRequest.LicenseData(key, productId, email, comment);
    CreateLicenseRequest req =
        new CreateLicenseRequest(licenseData, serial, expirationEpoch, numberOfSeats);

    ResponseEntity<Map<String, Object>> response =
        exchange("/create_license", HttpMethod.POST, req, jwt);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    License persisted = licenseRepository.findById(serial).orElseThrow();

    assertThat(persisted.getSerial()).isEqualTo(serial);
    assertThat(persisted.getLicenseKey()).isEqualTo(key);
    assertThat(persisted.getProductId()).isEqualTo(productId);
    assertThat(persisted.getEmail()).isEqualTo(email);
    assertThat(persisted.getComment()).isEqualTo(comment);
    assertThat(persisted.getNumberOfSeats()).isEqualTo(numberOfSeats);
    assertThat(persisted.isEnabled()).isTrue();
    assertThat(persisted.getExpirationDate()).isEqualTo(expirationDate);
    assertThat(persisted.getDate()).isNotNull();
  }

  @Test
  void addLicense_setsEntityIdAndPersistsCorrectly() {
    String jwt = loginAndGetJwt();

    License base = new License();
    base.setSerial("SERIAL_ADD_1");
    base.setLicenseKey("ADD_KEY_001");
    base.setProductId("PROD_ADD");
    base.setEnabled(true);
    licenseRepository.save(base);

    AddLicenseRequest.LicenseData data =
        new AddLicenseRequest.LicenseData("ADD_KEY_001", "PROD_ADD");
    AddLicenseRequest req = new AddLicenseRequest(data, "ENTITY_ADD_42", null);

    ResponseEntity<Map<String, Object>> resp = exchange("/add_license", HttpMethod.POST, req, jwt);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

    License persisted = licenseRepository.findById("SERIAL_ADD_1").orElseThrow();
    assertThat(persisted.getEntityId()).isEqualTo("ENTITY_ADD_42");
    assertThat(persisted.getLicenseKey()).isEqualTo("ADD_KEY_001");
    assertThat(persisted.getProductId()).isEqualTo("PROD_ADD");
    assertThat(persisted.isEnabled()).isTrue();
  }

  @Test
  void updateLicense_updatesFieldsCorrectly() {
    String jwt = loginAndGetJwt();

    License license = new License();
    license.setSerial("SERIAL_UPD_1");
    license.setLicenseKey("OLD_KEY_001");
    license.setProductId("PROD_OLD");
    license.setEnabled(false);
    licenseRepository.save(license);

    LicenseUpsertRequest updateReq = new LicenseUpsertRequest("NEW_KEY_001", "PROD_NEW", true);

    ResponseEntity<Map<String, Object>> resp =
        exchange("/api/licenses/OLD_KEY_001", HttpMethod.PUT, updateReq, jwt);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

    License updated = licenseRepository.findById("SERIAL_UPD_1").orElseThrow();
    assertThat(updated.getLicenseKey()).isEqualTo("NEW_KEY_001");
    assertThat(updated.getProductId()).isEqualTo("PROD_NEW");
    assertThat(updated.isEnabled()).isTrue();
  }
}
