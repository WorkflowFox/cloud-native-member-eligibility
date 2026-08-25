package com.workflowfox.eligibility.dto;

/**
 * Mirrors the {@code EligibilityStatus} enum in openapi.yaml.
 */
public enum EligibilityStatus {
    ELIGIBLE,
    NOT_YET_ELIGIBLE,
    INELIGIBLE,
    MEMBER_NOT_FOUND
}
