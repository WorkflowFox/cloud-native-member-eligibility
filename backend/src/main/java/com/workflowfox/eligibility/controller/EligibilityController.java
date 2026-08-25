package com.workflowfox.eligibility.controller;

import com.workflowfox.eligibility.dto.EligibilityResponse;
import com.workflowfox.eligibility.service.EligibilityService;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implements {@code GET /api/v1/eligibility} exactly as defined by
 * openapi.yaml. Contains no business logic; delegates to
 * {@link EligibilityService} (spec.md §10.3).
 */
@RestController
@Validated
public class EligibilityController {

    private final EligibilityService eligibilityService;

    public EligibilityController(EligibilityService eligibilityService) {
        this.eligibilityService = eligibilityService;
    }

    @GetMapping("/api/v1/eligibility")
    public EligibilityResponse checkEligibility(
            @RequestParam @NotBlank(message = "memberId must not be blank.") String memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkDate) {
        return eligibilityService.checkEligibility(memberId, checkDate);
    }
}
