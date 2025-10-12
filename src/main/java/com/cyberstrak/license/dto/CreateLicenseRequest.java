package com.cyberstrak.license.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateLicenseRequest(
    LicenseData license,
    @NotNull String serial,
    @Positive long expiration,
    @Min(1) @Max(10) int numberOfSeats) {
  public record LicenseData(
      @NotNull String key,
      @NotNull String aud,
      @Email @Size(max = 255) String email,
      @Size(max = 1024) String comment) {}
}
