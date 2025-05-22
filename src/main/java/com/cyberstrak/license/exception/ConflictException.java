package com.cyberstrak.license.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when a conflict arises, such as duplicate or invalid license state. */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {
  private static final long serialVersionUID = -6370819873631496126L;

  public ConflictException(String message) {
    super(message);
  }
}
