package com.cyberstrak.license.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CreateLicenseRequest(LicenseData license, @NotNull String serial, @Positive long expiration, @Min(1) @Max(10) int numberOfSeats) {
  public record LicenseData(@NotNull String key, @NotNull String aud) {}
}
