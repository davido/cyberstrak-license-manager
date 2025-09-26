package com.cyberstrak.license.dto;

import java.util.Map;

public record LicenseDto(
    String id,
    String key,
    String audience,
    boolean active,
    String iss,
    Long exp,
    Integer numberOfSeats,
    Map<String, String> editions,
    Object metadata) {}
