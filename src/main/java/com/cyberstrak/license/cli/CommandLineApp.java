package com.cyberstrak.license.cli;

import com.cyberstrak.license.entity.License;
import com.cyberstrak.license.repository.LicenseRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** CLI module that allows creating and listing licenses from command line. */
@Component
public class CommandLineApp implements CommandLineRunner {

  private final LicenseRepository licenseRepo;

  public CommandLineApp(LicenseRepository licenseRepo) {
    this.licenseRepo = licenseRepo;
  }

  @Override
  public void run(String... args) {
    if (args.length == 0) return;

    switch (args[0]) {
      case "create_license" -> createSampleLicenses();
      case "list_licenses" -> listAllLicenses();
      case "show_license" -> {
        if (args.length > 1) showLicense(args[1]);
        else System.err.println("Usage: show_license <serial>");
      }
      default -> System.err.println("Unknown command: " + args[0]);
    }
  }

  private void createSampleLicenses() {
    License one = new License();
    one.setSerial("SERIAL_NO_1");
    one.setKey("KEY_1");
    one.setProductId("PROD_A");
    one.setEnabled(true);
    one.setDate(LocalDateTime.now());

    License two = new License();
    two.setSerial("SERIAL_NO_2");
    two.setKey("KEY_2");
    two.setProductId("PROD_A");
    two.setEnabled(false);

    licenseRepo.saveAll(List.of(one, two));
    System.out.println("Sample licenses created.");
  }

  private void listAllLicenses() {
    licenseRepo.findAll().forEach(System.out::println);
  }

  private void showLicense(String serial) {
    licenseRepo
        .findById(serial)
        .ifPresentOrElse(
            System.out::println, () -> System.err.println("License not found: " + serial));
  }
}
