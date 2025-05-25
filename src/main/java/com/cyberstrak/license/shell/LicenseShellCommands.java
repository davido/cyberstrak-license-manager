package com.cyberstrak.license.shell;

import com.cyberstrak.license.entity.License;
import com.cyberstrak.license.repository.LicenseRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class LicenseShellCommands {

  @Autowired private LicenseRepository licenseRepository;

  @ShellMethod(key = "create-license", value = "Populates the license table with default data")
  public String createLicense() {
    if (licenseRepository.count() > 0) {
      return "License table already populated.";
    }

    License license1 = new License();
    license1.setSerial("SERIAL_NO_1");
    license1.setLicenseKey("LICENSE_KEY_1");
    license1.setProductId("3e200daa-6bf8-470b-bd6a-4f55996052c3");
    license1.setEnabled(true);

    License license2 = new License();
    license2.setSerial("SERIAL_NO_2");
    license2.setLicenseKey("LICENSE_KEY_2");
    license2.setProductId("3e200daa-6bf8-470b-bd6a-4f55996052c3");
    license2.setEnabled(false);

    License license3 = new License();
    license3.setSerial("SERIAL_NO_3");
    license3.setLicenseKey("LICENSE_KEY_3");
    license3.setProductId("3e200daa-6bf8-470b-bd6a-4f55996052c3");
    license3.setEnabled(true);
    license3.setEntityId("595959595959595-|-User");
    license3.setDate(LocalDateTime.of(2024, 11, 30, 0, 0));

    licenseRepository.saveAll(List.of(license1, license2, license3));
    return "Default licenses created.";
  }

  @ShellMethod(key = "list-license", value = "List all licenses.")
  public String listLicense() {
    List<License> licenses = licenseRepository.findAll();

    if (licenses.isEmpty()) {
      return "No licenses found.";
    }

    return licenses.stream()
        .map(
            license ->
                String.format(
                    "- %s | key: %s | product: %s | enabled: %s",
                    license.getSerial(),
                    license.getLicenseKey(),
                    license.getProductId(),
                    license.isEnabled()))
        .collect(Collectors.joining("\n", "Found licenses:\n", ""));
  }

  @ShellMethod(key = "show-license-details", value = "Show detailed info for a license by key.")
  public String showLicenseDetails(String key) {
    return licenseRepository
        .findByLicenseKey(key)
        .map(
            license ->
                String.format(
                    """
                    Serial: %s
                    Key: %s
                    Product ID: %s
                    Enabled: %s
                    Seats: %d
                    Expiration: %s
                    Entity ID: %s
                    Date: %s
                    """,
                    license.getSerial(),
                    license.getLicenseKey(),
                    license.getProductId(),
                    license.isEnabled(),
                    license.getNumberOfSeats(),
                    license.getExpirationDate() != null ? license.getExpirationDate() : "N/A",
                    license.getEntityId() != null ? license.getEntityId() : "N/A",
                    license.getDate() != null ? license.getDate() : "N/A"))
        .orElse("No license found with key: " + key);
  }
}
