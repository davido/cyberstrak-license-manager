package com.cyberstrak.license;

import static org.junit.jupiter.api.Assertions.*;

import com.cyberstrak.license.entity.License;
import com.cyberstrak.license.repository.LicenseRepository;
import com.cyberstrak.license.service.LicenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = LicenseManagerApplication.class,
    properties = {
      "spring.shell.interactive.enabled=false",
      "spring.datasource.url=jdbc:h2:mem:cyberstrakdb;NON_KEYWORDS=KEY;DB_CLOSE_DELAY=-1",
    })
public class LicenseServiceTest {
  @Autowired private LicenseRepository licenseRepository;

  @Autowired private LicenseService licenseService;

  @Test
  void testCheckAuthWithInvalidCredentials() {
    assertFalse(licenseService.checkAuth("wrong", "wrong"));
  }

  @Test
  void testGetLicenseReturnsExpectedJson() {
    License license = new License();
    license.setSerial("123");
    license.setKey("LICENSE123");
    license.setProductId("PROD1");
    license.setEnabled(true);

    licenseRepository.save(license);

    var result = licenseService.getLicense("LICENSE123", "PROD1");

    assertEquals("123", result.get("id"));
    assertEquals("LICENSE123", result.get("key"));
    assertEquals("PROD1", result.get("aud"));
  }
}
