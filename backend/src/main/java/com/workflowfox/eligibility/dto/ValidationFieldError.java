package com.workflowfox.eligibility.dto;

/**
 * Mirrors an item of the {@code ValidationErrorResponse.errors} array in
 * openapi.yaml.
 */
public record ValidationFieldError(String field, String message) {
}
