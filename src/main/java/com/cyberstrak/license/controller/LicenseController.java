package com.cyberstrak.license.controller;

import com.cyberstrak.license.dto.AddLicenseRequest;
import com.cyberstrak.license.dto.CreateLicenseRequest;
import com.cyberstrak.license.dto.LicenseDto;
import com.cyberstrak.license.dto.RemoveLicenseRequest;
import com.cyberstrak.license.service.LicenseService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for managing license endpoints. */
@RestController
@RequestMapping
public class LicenseController {
  private static final Logger logger = LoggerFactory.getLogger(LicenseController.class);

  private final LicenseService licenseService;

  @Autowired
  public LicenseController(LicenseService licenseService) {
    this.licenseService = licenseService;
  }

  @GetMapping("/")
  public String hello() {
    logger.debug("Hello endpoint called");
    String response = "Hello World!";
    logger.debug("Returning: {}", response);
    return response;
  }

  @GetMapping("/info")
  public String info() {
    long count = licenseService.count();
    String response = "License count: " + count;
    logger.debug("Returning: {}", response);
    return response;
  }

  @PostMapping("/create_license")
  public ResponseEntity<?> createLicense(@Valid @RequestBody CreateLicenseRequest payload) {
    LicenseDto license = licenseService.createLicense(payload);
    logger.debug("Returning license: {}", license);
    return ResponseEntity.ok(license);
  }

  @PostMapping("/add_license")
  public ResponseEntity<?> addLicense(@Valid @RequestBody AddLicenseRequest payload) {
    List<LicenseDto> licenses = licenseService.addLicense(payload);
    logger.debug("Returning licenses: {}", licenses);
    return ResponseEntity.ok(Map.of("licenses", licenses));
  }

  @PostMapping("/remove_license")
  public ResponseEntity<?> removeLicense(@Valid @RequestBody RemoveLicenseRequest request) {
    List<LicenseDto> cluster = request.licenseCluster().licenses();
    String entityId = request.entityId();
    logger.debug("Removing licenses: {}, entityId: {}", cluster, entityId);
    licenseService.removeLicenses(cluster, entityId);
    logger.debug("Licenses removed successfully");
    return ResponseEntity.ok().build();
  }

  @GetMapping("/get_license")
  public ResponseEntity<LicenseDto> getLicense(
      @RequestParam("key") String key, @RequestParam("aud") String aud) {
    LicenseDto license = licenseService.getLicense(key, aud);
    logger.debug("Returning license: {}", license);
    return ResponseEntity.ok(license);
  }

  @PostMapping("/erase_license")
  public ResponseEntity<LicenseDto> eraseLicense(@RequestParam("key") String key) {
    LicenseDto license = licenseService.eraseLicense(key);
    logger.debug("Erased license: {}", license);
    return ResponseEntity.ok(license);
  }

  @GetMapping("/dump_licenses")
  public ResponseEntity<?> dumpLicenses() {
    var licenses = licenseService.dumpLicenses();
    logger.debug("Returning all licenses: {}", licenses);
    return ResponseEntity.ok(licenses);
  }
}
