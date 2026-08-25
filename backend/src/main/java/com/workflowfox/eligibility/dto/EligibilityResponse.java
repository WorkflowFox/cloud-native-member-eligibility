package com.workflowfox.eligibility.dto;

import java.time.LocalDate;

/**
 * Mirrors the {@code EligibilityResult} schema in openapi.yaml.
 *
 * <p>For {@link EligibilityStatus#MEMBER_NOT_FOUND}, {@code memberName},
 * {@code planName}, {@code coverageEffectiveDate}, and
 * {@code coverageTerminationDate} are null.
 */
public record EligibilityResponse(
        String memberId,
        String memberName,
        String planName,
        LocalDate coverageEffectiveDate,
        LocalDate coverageTerminationDate,
        LocalDate checkCoverageOnDate,
        EligibilityStatus eligibilityStatus,
        String eligibilityReason) {
}
