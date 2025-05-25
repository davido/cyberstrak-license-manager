package com.cyberstrak.license.service;

import com.cyberstrak.license.dto.AddLicenseRequest;
import com.cyberstrak.license.dto.LicenseDto;
import com.cyberstrak.license.entity.License;
import com.cyberstrak.license.exception.BadRequestException;
import com.cyberstrak.license.exception.ConflictException;
import com.cyberstrak.license.exception.PreconditionFailedException;
import com.cyberstrak.license.exception.PreconditionRequiredException;
import com.cyberstrak.license.repository.LicenseRepository;
import jakarta.annotation.PostConstruct;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    logger.debug("H2 DB URL: {}", dataSource.getConnection().getMetaData().getURL());
  }

  public boolean checkAuth(String username, String password) {
    return ISSUER_ID.equals(username) && ISSUER_SECRET.equals(password);
  }

  public long count() {
    return licenseRepo.count();
  }

  public List<LicenseDto> addLicense(AddLicenseRequest payload) {
    String key = payload.license().key();
    String productId = payload.license().aud();
    String entityId = payload.entityId();
    String precondition = payload.precondition();

    License license = licenseRepo.findByKeyAndProductId(key, productId).orElse(null);
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
      License previous = licenseRepo.findByKey(precondition).orElse(null);
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
        a = licenseRepo.findByKey(a.getUpgradeFromKey()).orElse(null);
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
        .findByKeyAndProductId(key, aud)
        .map(this::toDto)
        .orElseThrow(() -> new BadRequestException("The license key is not valid"));
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
        l.getKey(),
        l.getProductId(),
        ISSUER_ID,
        exp,
        l.getNumberOfSeats(),
        editions,
        metadata);
  }
}
