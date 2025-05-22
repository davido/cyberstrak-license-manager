package com.cyberstrak.license.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when a precondition provided is invalid or not met. */
@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
public class PreconditionFailedException extends RuntimeException {
  private static final long serialVersionUID = 151323190891587865L;

  public PreconditionFailedException(String message) {
    super(message);
  }
}
