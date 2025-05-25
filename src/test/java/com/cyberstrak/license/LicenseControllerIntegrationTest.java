package com.cyberstrak.license;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = LicenseManagerApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class LicenseControllerIntegrationTest {

  @Value("${issuer.id}")
  private String ISSUER_ID;

  @Value("${issuer.secret}")
  private String ISSUER_SECRET;

  @Autowired private MockMvc mockMvc;

  @Test
  void testHelloEndpoint() throws Exception {
    mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(content().string("Hello World!"));
  }

  @Test
  void testInfoEndpoint() throws Exception {
    mockMvc
        .perform(get("/info").with(httpBasic(ISSUER_ID, ISSUER_SECRET)))
        .andExpect(status().isOk());
  }

  @Test
  void testDumpLicensesIsProtected() throws Exception {
    mockMvc.perform(get("/dump_licenses")).andExpect(status().isUnauthorized());
  }
}
