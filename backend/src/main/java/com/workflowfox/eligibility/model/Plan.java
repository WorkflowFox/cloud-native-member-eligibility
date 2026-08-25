package com.workflowfox.eligibility.model;

/**
 * Domain representation of a health plan (spec.md §11.2).
 */
public record Plan(String planId, String name) {
}
