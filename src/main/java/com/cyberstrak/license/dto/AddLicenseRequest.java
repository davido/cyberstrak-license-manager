package com.cyberstrak.license.dto;

import jakarta.validation.constraints.NotNull;

public record AddLicenseRequest(
    LicenseData license, @NotNull String entityId, String precondition) {
  public record LicenseData(@NotNull String key, @NotNull String aud) {}
}
