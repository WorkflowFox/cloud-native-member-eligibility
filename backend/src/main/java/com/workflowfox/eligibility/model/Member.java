package com.workflowfox.eligibility.model;

/**
 * Domain representation of a health-plan member (spec.md §11.1).
 */
public record Member(String memberId, String name) {
}
