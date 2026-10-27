package com.devtrack.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return error(HttpStatus.BAD_REQUEST, "Request validation failed.", request, fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedBody(HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Request body is missing or malformed.", request, Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidArgument(IllegalArgumentException exception,
            HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEmail(HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "An account with this email address already exists.", request, Map.of());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "Invalid email or password.", request, Map.of());
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleProjectNotFound(HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "Project not found.", request, Map.of());
    }

    @ExceptionHandler(IssueNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleIssueNotFound(HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "Issue not found.", request, Map.of());
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotificationNotFound(HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "Notification not found.", request, Map.of());
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiErrorResponse> handleAiServiceFailure(HttpServletRequest request) {
        return error(HttpStatus.BAD_GATEWAY, "AI suggestions are temporarily unavailable.", request, Map.of());
    }

    public static ApiErrorResponse body(HttpStatus status, String message, String path,
            Map<String, String> fieldErrors) {
        return new ApiErrorResponse(status.value(), status.getReasonPhrase(), message, path, Instant.now(), fieldErrors);
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String message, HttpServletRequest request,
            Map<String, String> fieldErrors) {
        return ResponseEntity.status(status).body(body(status, message, request.getRequestURI(), fieldErrors));
    }
}
