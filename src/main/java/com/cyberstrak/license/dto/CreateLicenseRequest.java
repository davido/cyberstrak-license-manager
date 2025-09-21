package com.cyberstrak.license.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateLicenseRequest(
    LicenseData license,
    @NotNull String serial,
    @Positive long expiration,
    @Min(1) @Max(10) int numberOfSeats) {
  public record LicenseData(@NotNull String key, @NotNull String aud) {}
}
