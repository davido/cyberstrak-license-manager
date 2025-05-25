package com.cyberstrak.license;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cyberstrak.license.entity.License;
import com.cyberstrak.license.repository.LicenseRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = LicenseManagerApplication.class)
@ActiveProfiles("test")
public class LicenseRepositoryTest {

  @Autowired private LicenseRepository licenseRepository;

  @BeforeEach
  void setUp() {
    licenseRepository.deleteAll();
  }

  @Test
  void testSaveAndFindByKeyAndProductId() {
    License license = new License();
    license.setSerial("123");
    license.setKey("ABC123");
    license.setProductId("PROD1");
    license.setEnabled(true);

    licenseRepository.save(license);

    Optional<License> found = licenseRepository.findByKeyAndProductId("ABC123", "PROD1");
    assertTrue(found.isPresent());
    assertEquals("ABC123", found.get().getKey());
    assertEquals("PROD1", found.get().getProductId());
  }

  @Test
  void testFindByKeyReturnsLicense() {
    License license = new License();
    license.setSerial("456");
    license.setKey("DEF456");
    license.setProductId("PROD2");
    license.setEnabled(true);

    licenseRepository.save(license);

    Optional<License> found = licenseRepository.findByKey("DEF456");
    assertTrue(found.isPresent());
    assertEquals("456", found.get().getSerial());
  }

  @Test
  void testFindByKeyReturnsEmptyWhenNotFound() {
    Optional<License> found = licenseRepository.findByKey("NOT_EXISTING");
    assertFalse(found.isPresent());
  }

  @Test
  void testFindByKeyAndProductIdReturnsEmptyWhenNotFound() {
    Optional<License> found = licenseRepository.findByKeyAndProductId("NO_KEY", "NO_PROD");
    assertFalse(found.isPresent());
  }

  @Test
  void testBasicCrudOperations() {
    License license = new License();
    license.setSerial("789");
    license.setKey("XYZ789");
    license.setProductId("PROD3");
    license.setEnabled(true);

    // Save
    License saved = licenseRepository.save(license);
    assertNotNull(saved.getSerial());

    // Find by id
    Optional<License> byId = licenseRepository.findById("789");
    assertTrue(byId.isPresent());

    // Update
    License l = byId.get();
    l.setEnabled(false);
    licenseRepository.save(l);
    Optional<License> updated = licenseRepository.findById("789");
    assertFalse(updated.get().isEnabled());

    // Delete
    licenseRepository.deleteById("789");
    assertFalse(licenseRepository.findById("789").isPresent());
  }
}
