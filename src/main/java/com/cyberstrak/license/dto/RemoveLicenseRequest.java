package com.cyberstrak.license.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RemoveLicenseRequest(LicenseCluster licenseCluster, @NotNull String entityId) {
  public record LicenseCluster(List<LicenseDto> licenses) {}
}
