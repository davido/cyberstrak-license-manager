package com.cyberstrak.license.dto;

import java.util.List;

public record RemoveLicenseRequest(LicenseCluster licenseCluster, String entityId) {
  public record LicenseCluster(List<LicenseDto> licenses) {}
}
