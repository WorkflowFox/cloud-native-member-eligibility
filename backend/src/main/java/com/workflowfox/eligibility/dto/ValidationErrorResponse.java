package com.workflowfox.eligibility.dto;

import java.util.List;

/**
 * Mirrors the {@code ValidationErrorResponse} schema in openapi.yaml,
 * returned for HTTP 422.
 */
public record ValidationErrorResponse(String detail, List<ValidationFieldError> errors) {

    public ValidationErrorResponse(String detail) {
        this(detail, null);
    }
}
