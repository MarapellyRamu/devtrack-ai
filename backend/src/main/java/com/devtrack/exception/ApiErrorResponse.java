package com.devtrack.exception;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(int status, String error, String message, String path, Instant timestamp,
        Map<String, String> fieldErrors) {
}
