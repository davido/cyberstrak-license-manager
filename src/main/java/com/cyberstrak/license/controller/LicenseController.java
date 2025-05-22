package com.cyberstrak.license.controller;

import com.cyberstrak.license.dto.AddLicenseRequest;
import com.cyberstrak.license.dto.RemoveLicenseRequest;
import com.cyberstrak.license.service.LicenseService;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for managing license endpoints. */
@RestController
@RequestMapping
public class LicenseController {
  private static final Log LOG = LogFactory.getLog(LicenseController.class);

  private final LicenseService licenseService;

  @Autowired
  public LicenseController(LicenseService licenseService) {
    this.licenseService = licenseService;
  }

  @GetMapping("/")
  public String hello() {
    LOG.debug("Hello endpoint");
    return "Hello World!";
  }

  @GetMapping("/info")
  public String info() {
    return "License count: " + licenseService.count();
  }

  @PostMapping("/add_license")
  public ResponseEntity<?> addLicense(@RequestBody AddLicenseRequest payload) {
    List<Map<String, Object>> licenses = licenseService.addLicense(payload);
    return ResponseEntity.ok(Map.of("licenses", licenses));
  }

  @PostMapping("/remove_license")
  public ResponseEntity<?> removeLicense(@RequestBody RemoveLicenseRequest request) {
    List<Map<String, Object>> cluster = request.licenseCluster().licenses();
    String entityId = request.entityId();

    licenseService.removeLicenses(cluster, entityId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/get_license")
  public ResponseEntity<?> getLicense(
      @RequestParam("key") String key, @RequestParam("aud") String aud) {
    Map<String, Object> license = licenseService.getLicense(key, aud);
    return ResponseEntity.ok(license);
  }

  @GetMapping("/dump_licenses")
  public ResponseEntity<?> dumpLicenses() {
    return ResponseEntity.ok(licenseService.dumpLicenses());
  }
}
