package com.cyberstrak.license.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaFallbackController {

  // Alle GETs auf /licenses und Unterrouten gehen auf index.html
  @GetMapping({"/licenses", "/licenses/**"})
  public String forwardLicenses() {
    return "forward:/index.html";
  }

  // Falls du noch weitere SPA-Routen hast, ergänze hier weitere forward-Methoden
  // @GetMapping({"/settings", "/settings/**"}) ...
}
