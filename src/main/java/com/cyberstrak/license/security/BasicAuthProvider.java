package com.cyberstrak.license.security;

import com.cyberstrak.license.service.LicenseService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.stereotype.Component;

/** Custom authentication provider that uses LicenseService to validate issuer credentials. */
@Component
public class BasicAuthProvider implements AuthenticationProvider {
  private static final Logger logger = LoggerFactory.getLogger(BasicAuthProvider.class);

  @Autowired private LicenseService licenseService;

  @Override
  public Authentication authenticate(Authentication authentication) {
    String username = authentication.getName();
    String password = authentication.getCredentials().toString();

    if (licenseService.checkAuth(username, password)) {
      return new UsernamePasswordAuthenticationToken(username, password, List.of());
    }

    String msg = "Invalid credentials for user: " + username;
    logger.error(msg);
    throw new BadCredentialsException(msg);
  }

  @Override
  public boolean supports(Class<?> authType) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authType);
  }
}
