package com.cyberstrak.license.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when a required precondition (like an upgrade key) is missing. */
@ResponseStatus(HttpStatus.PRECONDITION_REQUIRED)
public class PreconditionRequiredException extends RuntimeException {
  private static final long serialVersionUID = 7510325653136481447L;

  public PreconditionRequiredException(String message) {
    super(message);
  }
}
