package com.cyberstrak.license;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cyberstrak.license.entity.License;
import com.cyberstrak.license.repository.LicenseRepository;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = LicenseManagerApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class LicenseControllerTest {

  @Value("${issuer.id}")
  private String ISSUER_ID;

  @Value("${issuer.secret}")
  private String ISSUER_SECRET;

  @Autowired private MockMvc mockMvc;

  @Autowired private LicenseRepository licenseRepository;

  @BeforeEach
  void setUp() {
    licenseRepository.deleteAll();
  }

  @Test
  void testHelloEndpoint() throws Exception {
    mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(content().string("Hello World!"));
  }

  @Test
  void testInfoEndpoint() throws Exception {
    // Insert one license
    License l = new License();
    l.setSerial("1");
    l.setLicenseKey("KEY1");
    l.setProductId("PROD1");
    l.setEnabled(true);
    licenseRepository.save(l);

    mockMvc
        .perform(get("/info").with(httpBasic(ISSUER_ID, ISSUER_SECRET)))
        .andExpect(status().isOk())
        .andExpect(content().string(Matchers.containsString("License count: 1")));
  }

  @Test
  void testAddLicenseEndpoint() throws Exception {
    // Save a license to be added
    License license = new License();
    license.setSerial("1");
    license.setLicenseKey("KEY1");
    license.setProductId("PROD1");
    license.setEnabled(true);
    licenseRepository.save(license);

    String json =
        """
        {
          "license": { "key": "KEY1", "aud": "PROD1" },
          "entityId": "ENTITY1",
          "precondition": null
        }
        """;

    mockMvc
        .perform(
            post("/add_license")
                .with(httpBasic(ISSUER_ID, ISSUER_SECRET))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.licenses[0].key").value("KEY1"))
        .andExpect(jsonPath("$.licenses[0].aud").value("PROD1"));
  }

  @Test
  void testRemoveLicenseEndpoint() throws Exception {
    // Save a license to be removed
    License license = new License();
    license.setSerial("1");
    license.setLicenseKey("KEY1");
    license.setProductId("PROD1");
    license.setEnabled(true);
    license.setEntityId("ENTITY1");
    licenseRepository.save(license);

    String json =
        """
        {
          "licenseCluster": {
            "licenses": [
              {
                "id": "1",
                "key": "KEY1",
                "aud": "PROD1",
                "iss": "issuer-id",
                "exp": null,
                "numberOfSeats": null,
                "editions": null,
                "metadata": null
              }
            ]
          },
          "entityId": "ENTITY1"
        }
        """;

    mockMvc
        .perform(
            post("/remove_license")
                .with(httpBasic(ISSUER_ID, ISSUER_SECRET))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(status().isOk());

    // Validate license's entityId was removed
    License updated = licenseRepository.findById("1").orElse(null);
    assertNotNull(updated);
    assertNull(updated.getEntityId());
  }

  @Test
  void testRemoveLicenseEndpointFailsOnMismatch() throws Exception {
    License license = new License();
    license.setSerial("1");
    license.setLicenseKey("KEY1");
    license.setProductId("PROD1");
    license.setEnabled(true);
    license.setEntityId("ENTITY1");
    licenseRepository.save(license);

    String json =
        """
        {
          "licenseCluster": {
            "licenses": [
              {
                "id": "1",
                "key": "KEY1",
                "aud": "PROD1",
                "iss": "issuer-id",
                "exp": null,
                "numberOfSeats": null,
                "editions": null,
                "metadata": null
              }
            ]
          },
          "entityId": "ENTITY2"
        }
        """;

    mockMvc
        .perform(
            post("/remove_license")
                .with(httpBasic(ISSUER_ID, ISSUER_SECRET))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(status().isConflict());
  }

  @Test
  void testGetLicenseEndpoint() throws Exception {
    License license = new License();
    license.setSerial("1");
    license.setLicenseKey("KEY1");
    license.setProductId("PROD1");
    license.setEnabled(true);
    licenseRepository.save(license);

    mockMvc
        .perform(
            get("/get_license")
                .with(httpBasic(ISSUER_ID, ISSUER_SECRET))
                .param("key", "KEY1")
                .param("aud", "PROD1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("1"))
        .andExpect(jsonPath("$.key").value("KEY1"));
  }

  @Test
  void testGetLicenseEndpointNotFound() throws Exception {
    mockMvc
        .perform(
            get("/get_license")
                .with(httpBasic(ISSUER_ID, ISSUER_SECRET))
                .param("key", "NO_KEY")
                .param("aud", "NO_AUD"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testDumpLicensesEndpoint() throws Exception {
    License l1 = new License();
    l1.setSerial("1");
    l1.setLicenseKey("KEY1");
    l1.setProductId("PROD1");
    l1.setEnabled(true);

    License l2 = new License();
    l2.setSerial("2");
    l2.setLicenseKey("KEY2");
    l2.setProductId("PROD2");
    l2.setEnabled(true);

    licenseRepository.saveAll(List.of(l1, l2));

    mockMvc
        .perform(get("/dump_licenses").with(httpBasic(ISSUER_ID, ISSUER_SECRET)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].key").value("KEY1"))
        .andExpect(jsonPath("$[1].key").value("KEY2"));
  }

  @Test
  void testCreateLicenseEndpoint_acceptsNullExpiration() throws Exception {
    String json =
        """
        {
          "license": {
            "key": "KEY_JSON_NULL",
            "aud": "PROD_JSON",
            "email": "json@example.org",
            "comment": "null expiration via JSON"
          },
          "serial": "SERIAL_JSON_NULL",
          "expiration": null,
          "numberOfSeats": 2
        }
        """;

    mockMvc
        .perform(
            post("/create_license")
                .with(httpBasic(ISSUER_ID, ISSUER_SECRET))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.key").value("KEY_JSON_NULL"))
        // falls Jackson null-Felder serialisiert:
        .andExpect(jsonPath("$.exp").doesNotExist());

    License saved =
        licenseRepository.findById("SERIAL_JSON_NULL").orElseThrow();
    assertNull(saved.getExpirationDate());
  }

}
