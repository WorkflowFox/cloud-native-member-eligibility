package com.workflowfox.eligibility.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.workflowfox.eligibility.dto.EligibilityResponse;
import com.workflowfox.eligibility.dto.EligibilityStatus;
import com.workflowfox.eligibility.model.Coverage;
import com.workflowfox.eligibility.model.Member;
import com.workflowfox.eligibility.model.Plan;
import com.workflowfox.eligibility.repository.CoverageRepository;
import com.workflowfox.eligibility.repository.MemberRepository;
import com.workflowfox.eligibility.repository.PlanRepository;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the eligibility business rules (spec.md §9), including
 * the boundary dates called out by AC-07 through AC-12.
 */
class EligibilityServiceImplTest {

    private static final String MEMBER_ID = "M-TEST";
    private static final String PLAN_ID = "P-TEST";
    private static final LocalDate EFFECTIVE_DATE = LocalDate.of(2025, 1, 1);
    private static final LocalDate TERMINATION_DATE = LocalDate.of(2025, 12, 31);

    private EligibilityService serviceWithNullTermination;
    private EligibilityService serviceWithTermination;

    @BeforeEach
    void setUp() {
        Member member = new Member(MEMBER_ID, "Test Member");
        Plan plan = new Plan(PLAN_ID, "Test Plan");
        MemberRepository memberRepository = stubMemberRepository(member);
        PlanRepository planRepository = stubPlanRepository(plan);

        Coverage openEndedCoverage = new Coverage(MEMBER_ID, PLAN_ID, EFFECTIVE_DATE, null);
        serviceWithNullTermination = new EligibilityServiceImpl(
                memberRepository, planRepository, stubCoverageRepository(openEndedCoverage));

        Coverage terminatedCoverage = new Coverage(MEMBER_ID, PLAN_ID, EFFECTIVE_DATE, TERMINATION_DATE);
        serviceWithTermination = new EligibilityServiceImpl(
                memberRepository, planRepository, stubCoverageRepository(terminatedCoverage));
    }

    @Test
    void memberNotFoundReturnsMemberNotFoundWithNullFields() {
        MemberRepository emptyMemberRepository = memberId -> Optional.empty();
        EligibilityService service = new EligibilityServiceImpl(
                emptyMemberRepository, planId -> Optional.empty(), memberId -> Optional.empty());

        EligibilityResponse response = service.checkEligibility("M-9999", LocalDate.of(2026, 8, 22));

        assertThat(response.eligibilityStatus()).isEqualTo(EligibilityStatus.MEMBER_NOT_FOUND);
        assertThat(response.memberId()).isEqualTo("M-9999");
        assertThat(response.checkCoverageOnDate()).isEqualTo(LocalDate.of(2026, 8, 22));
        assertThat(response.memberName()).isNull();
        assertThat(response.planName()).isNull();
        assertThat(response.coverageEffectiveDate()).isNull();
        assertThat(response.coverageTerminationDate()).isNull();
    }

    @Test
    void checkDateBeforeEffectiveDateIsNotYetEligible() {
        EligibilityResponse response =
                serviceWithNullTermination.checkEligibility(MEMBER_ID, EFFECTIVE_DATE.minusDays(1));

        assertThat(response.eligibilityStatus()).isEqualTo(EligibilityStatus.NOT_YET_ELIGIBLE);
    }

    @Test
    void checkDateEqualToEffectiveDateIsEligible() {
        EligibilityResponse response = serviceWithNullTermination.checkEligibility(MEMBER_ID, EFFECTIVE_DATE);

        assertThat(response.eligibilityStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
    }

    @Test
    void checkDateBetweenEffectiveAndTerminationInclusiveIsEligible() {
        assertThat(serviceWithTermination.checkEligibility(MEMBER_ID, EFFECTIVE_DATE).eligibilityStatus())
                .isEqualTo(EligibilityStatus.ELIGIBLE);
        assertThat(serviceWithTermination
                        .checkEligibility(MEMBER_ID, EFFECTIVE_DATE.plusMonths(6))
                        .eligibilityStatus())
                .isEqualTo(EligibilityStatus.ELIGIBLE);
        assertThat(serviceWithTermination.checkEligibility(MEMBER_ID, TERMINATION_DATE).eligibilityStatus())
                .isEqualTo(EligibilityStatus.ELIGIBLE);
    }

    @Test
    void checkDateAfterTerminationDateIsIneligible() {
        EligibilityResponse response =
                serviceWithTermination.checkEligibility(MEMBER_ID, TERMINATION_DATE.plusDays(1));

        assertThat(response.eligibilityStatus()).isEqualTo(EligibilityStatus.INELIGIBLE);
    }

    @Test
    void nullTerminationDateIsEligibleForAnyDateOnOrAfterEffectiveDate() {
        EligibilityResponse response =
                serviceWithNullTermination.checkEligibility(MEMBER_ID, EFFECTIVE_DATE.plusYears(50));

        assertThat(response.eligibilityStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
        assertThat(response.coverageTerminationDate()).isNull();
    }

    @Test
    void foundMemberResponseEchoesRequestAndIncludesMemberAndPlanDetails() {
        EligibilityResponse response = serviceWithNullTermination.checkEligibility(MEMBER_ID, EFFECTIVE_DATE);

        assertThat(response.memberId()).isEqualTo(MEMBER_ID);
        assertThat(response.memberName()).isEqualTo("Test Member");
        assertThat(response.planName()).isEqualTo("Test Plan");
        assertThat(response.coverageEffectiveDate()).isEqualTo(EFFECTIVE_DATE);
        assertThat(response.checkCoverageOnDate()).isEqualTo(EFFECTIVE_DATE);
        assertThat(response.eligibilityReason()).isNotBlank();
    }

    private static MemberRepository stubMemberRepository(Member member) {
        Map<String, Member> byId = Map.of(member.memberId(), member);
        return memberId -> Optional.ofNullable(byId.get(memberId));
    }

    private static PlanRepository stubPlanRepository(Plan plan) {
        Map<String, Plan> byId = Map.of(plan.planId(), plan);
        return planId -> Optional.ofNullable(byId.get(planId));
    }

    private static CoverageRepository stubCoverageRepository(Coverage coverage) {
        Map<String, Coverage> byMemberId = Map.of(coverage.memberId(), coverage);
        return memberId -> Optional.ofNullable(byMemberId.get(memberId));
    }
}
