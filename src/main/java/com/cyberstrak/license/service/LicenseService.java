package com.cyberstrak.license.service;

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
import jakarta.annotation.PostConstruct;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Service for business logic related to license validation, upgrades, and entity assignment. */
@Service
public class LicenseService {
  private static final Logger logger = LoggerFactory.getLogger(LicenseService.class);

  private final LicenseRepository licenseRepo;
  private final DataSource dataSource;

  @Value("${issuer.id}")
  private String ISSUER_ID;

  @Value("${issuer.secret}")
  private String ISSUER_SECRET;

  @Value("${issuer.name}")
  private String ISSUER_NAME;

  @Value("${issuer.support.url}")
  private String ISSUER_SUPPORT_URL;

  public LicenseService(LicenseRepository licenseRepo, DataSource dataSource) {
    this.licenseRepo = licenseRepo;
    this.dataSource = dataSource;
  }

  @PostConstruct
  public void logDbUrl() throws SQLException {
    logger.debug("DB URL: {}", dataSource.getConnection().getMetaData().getURL());
  }

  public boolean checkAuth(String username, String password) {
    if (!ISSUER_ID.equals(username) || !ISSUER_SECRET.equals(password)) {
      logger.error("Wrong user or password combination: " + username + ":" + password);
    }
    return ISSUER_ID.equals(username) && ISSUER_SECRET.equals(password);
  }

  public long count() {
    return licenseRepo.count();
  }

  public LicenseDto createLicense(CreateLicenseRequest payload) {
    String key = payload.license().key();
    String productId = payload.license().aud();
    String serial = payload.serial();
    Long expiration = payload.expiration();

    License license = licenseRepo.findByLicenseKey(serial).orElse(null);
    if (license != null)
      throw new ConflictException("The license id '" + serial + "' already exists.");

    license = new License();

    license.setLicenseKey(key);
    license.setProductId(productId);
    license.setSerial(serial);
    license.setNumberOfSeats(payload.numberOfSeats());
    license.setExpirationDate(
        LocalDateTime.ofInstant(Instant.ofEpochSecond(expiration), ZoneId.systemDefault()));
    // Defaults
    license.setEnabled(true);
    license.setDate(LocalDateTime.now());

    licenseRepo.save(license);
    return toDto(license);
  }

  public List<LicenseDto> addLicense(AddLicenseRequest payload) {
    String key = payload.license().key();
    String productId = payload.license().aud();
    String entityId = payload.entityId();
    String precondition = payload.precondition();

    License license = licenseRepo.findByLicenseKeyAndProductId(key, productId).orElse(null);
    if (license == null) throw new ConflictException("The license key '" + key + "' is not valid.");

    if (!license.isEnabled()) {
      throw new ConflictException(
          "The license key '"
              + key
              + "' is disabled. Contact "
              + ISSUER_NAME
              + " ("
              + ISSUER_SUPPORT_URL
              + ")");
    }

    if (license.getEntityId() != null && !license.getEntityId().equals(entityId)) {
      throw new ConflictException(
          "The license key '" + key + "' is already validated by someone else.");
    }

    if (license.isUpgrade()) {
      if (precondition == null)
        throw new PreconditionRequiredException("Previous license key required.");
      License previous = licenseRepo.findByLicenseKey(precondition).orElse(null);
      if (previous == null) throw new PreconditionFailedException("Invalid upgrade key.");
      if (license.getUpgradeFromKey() != null
          && !license.getUpgradeFromKey().equals(precondition)) {
        throw new PreconditionFailedException("License already upgraded using a different key.");
      }
      if (previous.isUpgrade() && previous.getEntityId() == null) {
        throw new PreconditionFailedException("Previous upgrade license is unassigned.");
      }

      List<LicenseDto> licenses = new ArrayList<>(List.of(toDto(previous), toDto(license)));
      License a = previous;
      while (a.getUpgradeFromKey() != null) {
        a = licenseRepo.findByLicenseKey(a.getUpgradeFromKey()).orElse(null);
        if (a != null) {
          licenses.add(toDto(a));
        }
      }
      return licenses;
    } else {
      license.setEntityId(entityId);
      license.setDate(LocalDateTime.now());
      licenseRepo.save(license);
      return List.of(toDto(license));
    }
  }

  public void removeLicenses(List<LicenseDto> cluster, String entityId) {
    List<License> found = new ArrayList<>();

    for (LicenseDto dto : cluster) {
      String serial = dto.id();
      String aud = dto.aud();

      licenseRepo
          .findById(serial)
          .ifPresent(
              license -> {
                if (license.getProductId().equals(aud) && entityId.equals(license.getEntityId())) {
                  found.add(license);
                }
              });
    }

    if (found.size() == cluster.size()) {
      found.forEach(
          l -> {
            l.setEntityId(null);
            l.setDate(LocalDateTime.now());
          });
      licenseRepo.saveAll(found);
    } else {
      throw new ConflictException("Mismatch in license cluster.");
    }
  }

  public LicenseDto getLicense(String key, String aud) {
    return licenseRepo
        .findByLicenseKeyAndProductId(key, aud)
        .map(this::toDto)
        .orElseThrow(() -> new BadRequestException("The license key is not valid"));
  }

  public LicenseDto getLicense(String id) {
    return licenseRepo
        .findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new BadRequestException("The license key is not valid"));
  }

  public LicenseDto eraseLicense(String key) {
    License license = licenseRepo.findByLicenseKey(key).orElse(null);
    if (license == null) throw new ConflictException("The license key '" + key + "' is not valid.");
    if (license.getEntityId() != null)
      throw new ConflictException("The license key '" + key + "' is in use. Remove license first!");
    licenseRepo.delete(license);
    return toDto(license);
  }

  public List<LicenseDto> dumpLicenses() {
    return licenseRepo.findAll().stream().map(this::toDto).toList();
  }

  private LicenseDto toDto(License l) {
    Map<String, String> editions = Map.of("en", "Full Edition");
    Long exp =
        l.getExpirationDate() != null
            ? l.getExpirationDate().atZone(ZoneId.systemDefault()).toEpochSecond()
            : null;
    Object metadata = null;
    return new LicenseDto(
        l.getSerial(),
        l.getLicenseKey(),
        l.getProductId(),
        l.isEnabled(),
        ISSUER_ID,
        exp,
        l.getNumberOfSeats(),
        editions,
        metadata);
  }

  public LicenseDto updateLicense(String key, LicenseUpsertRequest payload) {
    var existing = licenseRepo.findByLicenseKey(key).orElse(null);
    if (existing == null) {
      throw new ConflictException("The license key '" + key + "' is in use. Remove license first!");
    }

    // Felder aktualisieren (an deine License-Entity anpassen!)
    existing.setLicenseKey(payload.key());
    existing.setProductId(payload.aud());
    existing.setEnabled(payload.active());
    existing.setExpirationDate(LocalDateTime.now().plusYears(1));

    licenseRepo.save(existing);

    return getLicense(payload.key(), payload.aud());
  }

  public LicenseDto createLicense(LicenseUpsertRequest payload) {
    var existing = new License();
    existing.setSerial(UUID.randomUUID().toString());
    existing.setNumberOfSeats(1);

    // Felder aktualisieren (an deine License-Entity anpassen!)
    existing.setLicenseKey(payload.key());
    existing.setProductId(payload.aud());
    existing.setEnabled(payload.active());
    existing.setExpirationDate(LocalDateTime.now().plusYears(1));

    licenseRepo.save(existing);

    return getLicense(payload.key());
  }
}
