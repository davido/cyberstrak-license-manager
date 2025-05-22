package com.cyberstrak.license.service;

import com.cyberstrak.license.dto.AddLicenseRequest;
import com.cyberstrak.license.entity.License;
import com.cyberstrak.license.exception.*;
import com.cyberstrak.license.repository.LicenseRepository;
import jakarta.annotation.PostConstruct;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Service for business logic related to license validation, upgrades, and entity assignment. */
@Service
public class LicenseService {
  private static final Log LOG = LogFactory.getLog(LicenseService.class);

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
    LOG.debug("H2 DB URL: " + dataSource.getConnection().getMetaData().getURL());
  }

  public boolean checkAuth(String username, String password) {
    return ISSUER_ID.equals(username) && ISSUER_SECRET.equals(password);
  }

  public long count() {
    return licenseRepo.count();
  }

  public List<Map<String, Object>> addLicense(AddLicenseRequest payload) {
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

      List<Map<String, Object>> licenses =
          new ArrayList<>(List.of(toJsonDict(previous), toJsonDict(license)));
      License a = previous;
      while (a.getUpgradeFromKey() != null) {
        a = licenseRepo.findByKey(a.getUpgradeFromKey()).orElse(null);
        if (a != null) licenses.add(toJsonDict(a));
      }
      return licenses;
    } else {
      license.setEntityId(entityId);
      license.setDate(LocalDateTime.now());
      licenseRepo.save(license);
      return List.of(toJsonDict(license));
    }
  }

  public void removeLicenses(List<Map<String, Object>> cluster, String entityId) {
    List<License> found = new ArrayList<>();
    for (Map<String, Object> entry : cluster) {
      String serial = (String) entry.get("id");
      String aud = (String) entry.get("aud");

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

  public Map<String, Object> getLicense(String key, String aud) {
    return licenseRepo
        .findByKeyAndProductId(key, aud)
        .map(this::toJsonDict)
        .orElseThrow(() -> new BadRequestException("The license key is not valid"));
  }

  public List<Map<String, Object>> dumpLicenses() {
    return licenseRepo.findAll().stream().map(this::toJsonDict).toList();
  }

  public Map<String, Object> toJsonDict(License l) {
    Map<String, Object> json = new LinkedHashMap<>();
    json.put("id", l.getSerial());
    json.put("key", l.getKey());
    json.put("aud", l.getProductId());
    json.put("iss", ISSUER_ID);
    json.put(
        "exp",
        l.getExpirationDate() != null
            ? l.getExpirationDate().atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
            : null);
    json.put("numberOfSeats", l.getNumberOfSeats());
    json.put("editions", Map.of("en", "Full Edition"));
    json.put("metadata", null);
    return json;
  }
}
