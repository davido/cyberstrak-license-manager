package com.cyberstrak.license;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cyberstrak.license.entity.License;
import com.cyberstrak.license.repository.LicenseRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = LicenseManagerApplication.class)
@ActiveProfiles("test")
public class LicenseRepositoryTest {

  @Autowired private LicenseRepository licenseRepository;

  @BeforeAll
  public static void enableH2Trace() {
    System.setProperty("h2.traceLevelSystemOut", "3");
  }

  @Test
  void testSaveAndFindByKeyAndProductId() {
    License license = new License();
    license.setSerial("123");
    license.setKey("ABC123");
    license.setProductId("PROD");
    license.setEnabled(true);

    licenseRepository.save(license);

    Optional<License> found = licenseRepository.findByKeyAndProductId("ABC123", "PROD");
    assertTrue(found.isPresent());
    assertEquals("ABC123", found.get().getKey());
  }
}
