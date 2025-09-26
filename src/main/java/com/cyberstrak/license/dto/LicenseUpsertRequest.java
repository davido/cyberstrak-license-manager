package com.cyberstrak.license.dto;

public record LicenseUpsertRequest(String key, String audience, Boolean active) {}
