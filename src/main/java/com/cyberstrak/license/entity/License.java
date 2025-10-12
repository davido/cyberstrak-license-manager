package com.cyberstrak.license.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Represents a software license entity with optional upgrade chain support. */
@Entity
@Table(name = "licenses")
public class License {

  @Id private String serial;

  @Column(nullable = false, unique = true)
  private String licenseKey;

  @Column(nullable = false)
  private String productId;

  private String entityId;

  @Column(nullable = false)
  private boolean enabled;

  @Column(nullable = false)
  private int numberOfSeats = 1;

  private LocalDateTime expirationDate;

  @Column(nullable = false)
  private boolean isUpgrade = false;

  private String upgradeFromKey;

  private LocalDateTime date;

  // 🔸 Neue Felder
  @Column(length = 255)
  private String email;

  @Column(length = 1024)
  private String comment;

  // Getters & Setters
  public String getSerial() {
    return serial;
  }

  public void setSerial(String serial) {
    this.serial = serial;
  }

  public String getLicenseKey() {
    return licenseKey;
  }

  public void setLicenseKey(String licenseKey) {
    this.licenseKey = licenseKey;
  }

  public String getProductId() {
    return productId;
  }

  public void setProductId(String productId) {
    this.productId = productId;
  }

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public int getNumberOfSeats() {
    return numberOfSeats;
  }

  public void setNumberOfSeats(int numberOfSeats) {
    this.numberOfSeats = numberOfSeats;
  }

  public LocalDateTime getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(LocalDateTime expirationDate) {
    this.expirationDate = expirationDate;
  }

  public boolean isUpgrade() {
    return isUpgrade;
  }

  public void setUpgrade(boolean upgrade) {
    isUpgrade = upgrade;
  }

  public String getUpgradeFromKey() {
    return upgradeFromKey;
  }

  public void setUpgradeFromKey(String upgradeFromKey) {
    this.upgradeFromKey = upgradeFromKey;
  }

  public LocalDateTime getDate() {
    return date;
  }

  public void setDate(LocalDateTime date) {
    this.date = date;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
}
