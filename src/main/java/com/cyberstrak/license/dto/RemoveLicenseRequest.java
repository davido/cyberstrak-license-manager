package com.cyberstrak.license.dto;

import java.util.List;
import java.util.Map;

public record RemoveLicenseRequest(LicenseCluster licenseCluster, String entityId) {
  public record LicenseCluster(List<Map<String, Object>> licenses) {}
}
