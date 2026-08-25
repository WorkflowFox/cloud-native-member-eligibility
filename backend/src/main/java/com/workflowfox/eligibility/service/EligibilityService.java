package com.workflowfox.eligibility.service;

import com.workflowfox.eligibility.dto.EligibilityResponse;
import java.time.LocalDate;

/**
 * Business logic for evaluating member coverage eligibility (spec.md §9).
 */
public interface EligibilityService {

    EligibilityResponse checkEligibility(String memberId, LocalDate checkDate);
}
