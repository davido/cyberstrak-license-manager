package com.cyberstrak.license.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  // mind. 256 Bit für HS256; besser aus application.yml lesen
  @Value("${security.jwt.secret:change-me-change-me-change-me-change-me-32bytes}")
  private String secret;

  @Value("${security.jwt.ttl-seconds:3600}")
  private long ttlSeconds;

  private SecretKey key() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateToken(String username, Collection<? extends GrantedAuthority> authorities) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(username)
        .claim("roles", authorities.stream().map(GrantedAuthority::getAuthority).toList())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(ttlSeconds)))
        .signWith(key())
        .compact();
  }

  public Jws<Claims> parse(String token) {
    return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token);
  }

  public String extractUsername(String token) {
    return parse(token).getPayload().getSubject();
  }

  public List<String> extractRoles(String token) {
    Object roles = parse(token).getPayload().get("roles");
    if (roles instanceof Collection<?> c) {
      return c.stream().map(Object::toString).toList();
    }
    return List.of();
  }
}
