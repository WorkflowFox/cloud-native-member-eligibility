package com.workflowfox.eligibility.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.workflowfox.eligibility.service.EligibilityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifies that an unexpected failure from the service layer produces the
 * safe HTTP 500 shape defined by openapi.yaml, never exposing exception
 * details (spec.md §7.8, §12.5). Uses a mocked service so the failure is
 * deterministic and does not depend on corrupting seeded data.
 */
@WebMvcTest(EligibilityController.class)
class EligibilityControllerErrorHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EligibilityService eligibilityService;

    @Test
    void unexpectedServiceFailureReturns500WithSafeBody() throws Exception {
        given(eligibilityService.checkEligibility(anyString(), any()))
                .willThrow(new IllegalStateException("boom: sensitive internal detail"));

        mockMvc.perform(get("/api/v1/eligibility").param("memberId", "M-1001").param("checkDate", "2026-08-22"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred. Please try again."));
    }
}
