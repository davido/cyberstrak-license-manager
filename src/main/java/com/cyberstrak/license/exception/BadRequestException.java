package com.cyberstrak.license.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when a license request is malformed or contains invalid parameters. */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
  private static final long serialVersionUID = -2377100086464310857L;

  public BadRequestException(String message) {
    super(message);
  }
}
