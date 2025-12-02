package com.cyberstrak.license.dto;

public record LicenseUpsertRequest(
		String key,
		String aud,
		Boolean active,
		Long expiration) {}
