package com.workflowfox.eligibility.model;

import java.time.LocalDate;

/**
 * Domain representation of a member's coverage segment (spec.md §11.3).
 * At most one coverage segment exists per member. A null
 * {@code terminationDate} means coverage has no recorded end date.
 */
public record Coverage(String memberId, String planId, LocalDate effectiveDate, LocalDate terminationDate) {
}
