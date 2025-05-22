package com.cyberstrak.license.dto;

public record AddLicenseRequest(LicenseData license, String entityId, String precondition) {
  public record LicenseData(String key, String aud) {}
}
