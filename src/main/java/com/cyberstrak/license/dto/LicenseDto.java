package com.cyberstrak.license.dto;

import java.util.Map;

public record LicenseDto(
	    String id,
	    String key,
	    String aud,
	    String iss,
	    Long exp,
	    Integer numberOfSeats,
	    Map<String, String> editions,
	    Object metadata
	) {}