package com.cyberstrak.license.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.cyberstrak.license.LicenseManagerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = LicenseManagerApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SpaFallbackControllerTest {

  @Autowired MockMvc mockMvc;

  @Test
  void spaRoute_servesIndexHtml() throws Exception {
    mockMvc
        .perform(get("/licenses")) // direkte SPA-Route
        .andExpect(status().isOk())
        .andExpect(forwardedUrl("/index.html"));
  }
}
