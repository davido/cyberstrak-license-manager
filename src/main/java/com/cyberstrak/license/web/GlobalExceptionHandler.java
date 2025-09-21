package com.cyberstrak.license.web;

import com.cyberstrak.license.exception.BadRequestException;
import com.cyberstrak.license.exception.ConflictException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthentication(
      AuthenticationException ex, HttpServletRequest req) {
    return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(
      AccessDeniedException ex, HttpServletRequest req) {
    return build(HttpStatus.FORBIDDEN, ex.getMessage(), req);
  }

  // === Domänen-Exceptions explizit mappen ===
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorResponse> handleBadRequest(
      BadRequestException ex, HttpServletRequest req) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponse> handleConflict(
      ConflictException ex, HttpServletRequest req) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), req);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(
      NotFoundException ex, HttpServletRequest req) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
  }

  // === Spring-/Validierungsfehler auf 400 etc. ===
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest req) {
    String msg =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
            .collect(Collectors.joining("; "));
    return build(HttpStatus.BAD_REQUEST, msg, req);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraint(
      ConstraintViolationException ex, HttpServletRequest req) {
    String msg =
        ex.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + " " + v.getMessage())
            .collect(Collectors.joining("; "));
    return build(HttpStatus.BAD_REQUEST, msg, req);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleBadJson(
      HttpMessageNotReadableException ex, HttpServletRequest req) {
    return build(HttpStatus.BAD_REQUEST, "Malformed JSON request", req);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
    return build(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), req);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleRse(
      ResponseStatusException ex, HttpServletRequest req) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
    String message =
        (ex.getReason() != null && !ex.getReason().isBlank())
            ? ex.getReason()
            : status.getReasonPhrase();
    return build(status, message, req);
  }

  // Optional: 404 via NoHandlerFoundException (nur wenn in application.yml aktiviert)
  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoHandler(
      NoHandlerFoundException ex, HttpServletRequest req) {
    return build(
        HttpStatus.NOT_FOUND,
        "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL(),
        req);
  }

  // === Fallback ===
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest req) {
    return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), req);
  }

  private ResponseEntity<ErrorResponse> build(
      HttpStatus status, String message, HttpServletRequest req) {
    ErrorResponse body =
        ErrorResponse.of(
            status.value(),
            status.getReasonPhrase(),
            (message == null || message.isBlank()) ? status.getReasonPhrase() : message,
            req.getRequestURI());
    return ResponseEntity.status(status).body(body);
  }
}
