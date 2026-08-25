package com.workflowfox.eligibility.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End-to-end tests of {@code GET /api/v1/eligibility} against the real,
 * seeded in-memory repositories, covering all four business outcomes
 * (AC-07 through AC-12) and request validation (AC-04).
 */
@SpringBootTest
@AutoConfigureMockMvc
class EligibilityControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void eligibleMemberReturns200WithEligibleStatus() throws Exception {
        mockMvc.perform(get("/api/v1/eligibility").param("memberId", "M-1001").param("checkDate", "2026-08-22"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value("M-1001"))
                .andExpect(jsonPath("$.memberName").value("Jordan Testcase"))
                .andExpect(jsonPath("$.planName").value("Acme Health Plan"))
                .andExpect(jsonPath("$.coverageEffectiveDate").value("2025-01-01"))
                .andExpect(jsonPath("$.coverageTerminationDate").doesNotExist())
                .andExpect(jsonPath("$.checkCoverageOnDate").value("2026-08-22"))
                .andExpect(jsonPath("$.eligibilityStatus").value("ELIGIBLE"));
    }

    @Test
    void notYetEligibleMemberReturns200WithNotYetEligibleStatus() throws Exception {
        mockMvc.perform(get("/api/v1/eligibility").param("memberId", "M-1002").param("checkDate", "2026-08-22"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligibilityStatus").value("NOT_YET_ELIGIBLE"));
    }

    @Test
    void ineligibleMemberReturns200WithIneligibleStatus() throws Exception {
        mockMvc.perform(get("/api/v1/eligibility").param("memberId", "M-1003").param("checkDate", "2026-08-22"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligibilityStatus").value("INELIGIBLE"));
    }

    @Test
    void unknownMemberReturns200WithMemberNotFoundAndNullFields() throws Exception {
        mockMvc.perform(get("/api/v1/eligibility").param("memberId", "M-9999").param("checkDate", "2026-08-22"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value("M-9999"))
                .andExpect(jsonPath("$.checkCoverageOnDate").value("2026-08-22"))
                .andExpect(jsonPath("$.eligibilityStatus").value("MEMBER_NOT_FOUND"))
                .andExpect(jsonPath("$.memberName").doesNotExist())
                .andExpect(jsonPath("$.planName").doesNotExist())
                .andExpect(jsonPath("$.coverageEffectiveDate").doesNotExist())
                .andExpect(jsonPath("$.coverageTerminationDate").doesNotExist());
    }

    @Test
    void checkDateEqualToEffectiveDateIsEligible() throws Exception {
        mockMvc.perform(get("/api/v1/eligibility").param("memberId", "M-1001").param("checkDate", "2025-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligibilityStatus").value("ELIGIBLE"));
    }

    @Test
    void checkDateEqualToTerminationDateIsEligible() throws Exception {
        mockMvc.perform(get("/api/v1/eligibility").param("memberId", "M-1003").param("checkDate", "2025-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligibilityStatus").value("ELIGIBLE"));
    }

    @Test
    void blankMemberIdReturns422() throws Exception {
        mockMvc.perform(get("/api/v1/eligibility").param("memberId", "").param("checkDate", "2026-08-22"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void missingMemberIdReturns422() throws Exception {
        mockMvc.perform(get("/api/v1/eligibility").param("checkDate", "2026-08-22"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void missingCheckDateReturns422() throws Exception {
        mockMvc.perform(get("/api/v1/eligibility").param("memberId", "M-1001"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void malformedCheckDateReturns422() throws Exception {
        mockMvc.perform(get("/api/v1/eligibility").param("memberId", "M-1001").param("checkDate", "not-a-date"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.errors", hasSize(1)));
    }
}
