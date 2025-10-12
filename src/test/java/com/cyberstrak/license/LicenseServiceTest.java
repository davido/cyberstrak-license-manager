package com.cyberstrak.license;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cyberstrak.license.dto.AddLicenseRequest;
import com.cyberstrak.license.dto.CreateLicenseRequest;
import com.cyberstrak.license.dto.LicenseDto;
import com.cyberstrak.license.dto.LicenseUpsertRequest;
import com.cyberstrak.license.entity.License;
import com.cyberstrak.license.exception.BadRequestException;
import com.cyberstrak.license.exception.ConflictException;
import com.cyberstrak.license.exception.PreconditionFailedException;
import com.cyberstrak.license.exception.PreconditionRequiredException;
import com.cyberstrak.license.repository.LicenseRepository;
import com.cyberstrak.license.service.LicenseService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = LicenseManagerApplication.class)
@ActiveProfiles("test")
public class LicenseServiceTest {
  @Autowired private LicenseRepository licenseRepository;

  @Autowired private LicenseService licenseService;

  @Value("${issuer.id}")
  private String ISSUER_ID;

  @Value("${issuer.secret}")
  private String ISSUER_SECRET;

  @BeforeEach
  void setUp() {
    // Clear DB before each test
    licenseRepository.deleteAll();
  }

  @Test
  void testCheckAuthWithInvalidCredentials() {
    assertFalse(licenseService.checkAuth("wrong", "wrong"));
  }

  @Test
  void testCheckAuthWithValidCredentials() {
    assertTrue(licenseService.checkAuth(ISSUER_ID, ISSUER_SECRET));
  }

  @Test
  void testCountReturnsCorrectCount() {
    License l1 = new License();
    l1.setSerial("1");
    l1.setLicenseKey("KEY1");
    l1.setProductId("PROD1");
    l1.setEnabled(true);
    licenseRepository.save(l1);

    assertEquals(1, licenseService.count());
  }

  @Test
  void testCreateLicense() {
    // 1) Heute plus 1 Jahr
    LocalDate inOneYear = LocalDate.now().plusYears(1);

    // 2) Zeitpunkt angeben (z. B. Mitternacht in System-Zeitzone)
    LocalDateTime localDateTime = inOneYear.atStartOfDay();

    // 3) In Instant konvertieren
    Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();

    // 4) Epoch-Seconds berechnen
    long epochSeconds = instant.getEpochSecond();

    String id = "SERIAL_42";
    String key = "KEY_42";
    String productId = "PROD_42";
    int numberOfSeats = 2;
    CreateLicenseRequest request =
        new CreateLicenseRequest(
            new CreateLicenseRequest.LicenseData(key, productId, "contact@example.com", "test"),
            id,
            epochSeconds,
            numberOfSeats);

    LicenseDto result = licenseService.createLicense(request);

    assertEquals("SERIAL_42", result.id());

    License created = licenseRepository.findById(id).orElse(null);
    assertNotNull(created);
    assertEquals(key, created.getLicenseKey());
    assertEquals(productId, created.getProductId());
    assertEquals(numberOfSeats, created.getNumberOfSeats());
    assertEquals(localDateTime, created.getExpirationDate());
  }

  @Test
  void testAddLicenseValidLicense() {
    License license = new License();
    license.setSerial("1");
    license.setLicenseKey("KEY1");
    license.setProductId("PROD1");
    license.setEnabled(true);

    licenseRepository.save(license);

    AddLicenseRequest request =
        new AddLicenseRequest(new AddLicenseRequest.LicenseData("KEY1", "PROD1"), "ENTITY1", null);

    List<LicenseDto> result = licenseService.addLicense(request);

    assertEquals(1, result.size());
    LicenseDto added = result.get(0);
    assertEquals("1", added.id());

    License updated = licenseRepository.findById("1").orElse(null);
    assertNotNull(updated);
    assertEquals("ENTITY1", updated.getEntityId());
  }

  @Test
  void testAddLicenseThrowsIfLicenseNotFound() {
    AddLicenseRequest request =
        new AddLicenseRequest(new AddLicenseRequest.LicenseData("KEY1", "PROD1"), "ENTITY1", null);

    assertThrows(ConflictException.class, () -> licenseService.addLicense(request));
  }

  @Test
  void testGetLicenseReturnsDto() {
    License license = new License();
    license.setSerial("1");
    license.setLicenseKey("KEY1");
    license.setProductId("PROD1");
    license.setEnabled(true);

    licenseRepository.save(license);

    LicenseDto dto = licenseService.getLicense("KEY1", "PROD1");

    assertEquals("1", dto.id());
    assertEquals("KEY1", dto.key());
    assertEquals("PROD1", dto.aud());
  }

  @Test
  void testGetLicenseThrowsIfNotFound() {
    assertThrows(BadRequestException.class, () -> licenseService.getLicense("NO_KEY", "NO_AUD"));
  }

  @Test
  void testDumpLicensesReturnsAll() {
    License l1 = new License();
    l1.setSerial("1");
    l1.setLicenseKey("KEY1");
    l1.setProductId("PROD1");
    l1.setEnabled(true);

    License l2 = new License();
    l2.setSerial("2");
    l2.setLicenseKey("KEY2");
    l2.setProductId("PROD2");
    l2.setEnabled(true);

    licenseRepository.saveAll(List.of(l1, l2));

    List<LicenseDto> dump = licenseService.dumpLicenses();
    assertEquals(2, dump.size());
  }

  @Test
  void testRemoveLicensesSuccess() {
    License license = new License();
    license.setSerial("1");
    license.setLicenseKey("KEY1");
    license.setProductId("PROD1");
    license.setEntityId("ENTITY1");
    license.setEnabled(true);
    licenseRepository.save(license);

    LicenseDto dto =
        new LicenseDto(
            "1", "KEY1", "PROD1", "Entity1", true, "issuer-id", null, null, null, null, null, null);

    licenseService.removeLicenses(List.of(dto), "ENTITY1");

    License updated = licenseRepository.findById("1").orElse(null);
    assertNotNull(updated);
    assertNull(updated.getEntityId());
  }

  @Test
  void testRemoveLicensesThrowsOnMismatch() {
    License license = new License();
    license.setSerial("1");
    license.setLicenseKey("KEY1");
    license.setProductId("PROD1");
    license.setEntityId("ENTITY1");
    license.setEnabled(true);
    licenseRepository.save(license);

    LicenseDto dto =
        new LicenseDto(
            "1", "KEY1", "PROD1", "Entity1", true, "issuer-id", null, null, null, null, null, null);

    // Different entity ID to force mismatch
    assertThrows(
        ConflictException.class, () -> licenseService.removeLicenses(List.of(dto), "ENTITY2"));
  }

  @Test
  void testGetLicenseReturnsExpectedDto() {
    License license = new License();
    license.setSerial("123");
    license.setLicenseKey("LICENSE123");
    license.setProductId("PROD1");
    license.setEnabled(true);

    licenseRepository.save(license);

    var result = licenseService.getLicense("LICENSE123", "PROD1");

    assertEquals("123", result.id());
    assertEquals("LICENSE123", result.key());
    assertEquals("PROD1", result.aud());
  }

  @Test
  void testEraseLicenseSuccess() {
    License license = new License();
    license.setSerial("ID1");
    license.setLicenseKey("KEY1");
    license.setProductId("PROD1");
    licenseRepository.save(license);

    LicenseDto erased = licenseService.eraseLicense("KEY1");

    assertEquals("KEY1", erased.key());
    assertFalse(licenseRepository.findById("ID1").isPresent());
  }

  @Test
  void testEraseLicenseThrowsIfNotFound() {
    assertThrows(ConflictException.class, () -> licenseService.eraseLicense("MISSING_KEY"));
  }

  @Test
  void testEraseLicenseThrowsIfInUse() {
    License license = new License();
    license.setSerial("ID2");
    license.setLicenseKey("KEY2");
    license.setProductId("PROD1");
    license.setEntityId("ENTITY1"); // Lizenz ist in Verwendung
    licenseRepository.save(license);

    assertThrows(ConflictException.class, () -> licenseService.eraseLicense("KEY2"));
  }

  @Test
  void testUpdateLicenseSuccess() {
    License license = new License();
    license.setSerial("ID3");
    license.setLicenseKey("OLD_KEY");
    license.setProductId("PROD1");
    license.setEnabled(false);
    licenseRepository.save(license);

    var payload = new LicenseUpsertRequest("NEW_KEY", "NEW_PROD", true);

    LicenseDto updated = licenseService.updateLicense("OLD_KEY", payload);

    assertEquals("NEW_KEY", updated.key());
    assertEquals("NEW_PROD", updated.aud());
    assertTrue(updated.active());
  }

  @Test
  void testUpdateLicenseThrowsIfNotFound() {
    var payload = new LicenseUpsertRequest("KEY_X", "PROD_X", true);

    assertThrows(
        ConflictException.class, () -> licenseService.updateLicense("MISSING_KEY", payload));
  }

  @Test
  void testAddLicenseWithUpgradeSuccess() {
    // Vorherige Lizenz
    License previous = new License();
    previous.setSerial("PREV_ID");
    previous.setLicenseKey("PREV_KEY");
    previous.setProductId("PROD1");
    previous.setEnabled(true);
    previous.setEntityId("ENTITY1");
    licenseRepository.save(previous);

    // Upgrade-Lizenz
    License upgrade = new License();
    upgrade.setSerial("UPGRADE_ID");
    upgrade.setLicenseKey("UPGRADE_KEY");
    upgrade.setProductId("PROD1");
    upgrade.setEnabled(true);
    upgrade.setUpgrade(true);
    upgrade.setUpgradeFromKey("PREV_KEY");
    licenseRepository.save(upgrade);

    AddLicenseRequest request =
        new AddLicenseRequest(
            new AddLicenseRequest.LicenseData("UPGRADE_KEY", "PROD1"), "ENTITY1", "PREV_KEY");

    List<LicenseDto> result = licenseService.addLicense(request);

    assertEquals(2, result.size()); // Upgrade und Previous zurückgegeben
  }

  @Test
  void testAddLicenseUpgradeThrowsIfPreconditionMissing() {
    License upgrade = new License();
    upgrade.setSerial("UP_ID");
    upgrade.setLicenseKey("UP_KEY");
    upgrade.setProductId("PROD1");
    upgrade.setEnabled(true);
    upgrade.setUpgrade(true);
    licenseRepository.save(upgrade);

    AddLicenseRequest request =
        new AddLicenseRequest(
            new AddLicenseRequest.LicenseData("UP_KEY", "PROD1"),
            "ENTITY1",
            null); // Precondition fehlt

    assertThrows(PreconditionRequiredException.class, () -> licenseService.addLicense(request));
  }

  @Test
  void testAddLicenseUpgradeThrowsIfInvalidPrecondition() {
    License upgrade = new License();
    upgrade.setSerial("UP_ID2");
    upgrade.setLicenseKey("UP_KEY2");
    upgrade.setProductId("PROD1");
    upgrade.setEnabled(true);
    upgrade.setUpgrade(true);
    licenseRepository.save(upgrade);

    AddLicenseRequest request =
        new AddLicenseRequest(
            new AddLicenseRequest.LicenseData("UP_KEY2", "PROD1"),
            "ENTITY1",
            "WRONG_KEY"); // Falscher Precondition

    assertThrows(PreconditionFailedException.class, () -> licenseService.addLicense(request));
  }
}
