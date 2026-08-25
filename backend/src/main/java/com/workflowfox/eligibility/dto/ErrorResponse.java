package com.workflowfox.eligibility.dto;

/**
 * Mirrors the {@code ErrorResponse} schema in openapi.yaml, returned for
 * HTTP 500. Never exposes stack traces, SQL, file paths, or other
 * infrastructure details (spec.md §7.8, §12.5).
 */
public record ErrorResponse(String detail) {
}
