package com.cyberstrak.license.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record RemoveLicenseRequest(LicenseCluster licenseCluster, @NotNull String entityId) {
  public record LicenseCluster(List<LicenseDto> licenses) {}
}
