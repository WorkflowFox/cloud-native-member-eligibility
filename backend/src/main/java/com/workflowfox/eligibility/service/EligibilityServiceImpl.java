package com.workflowfox.eligibility.service;

import com.workflowfox.eligibility.dto.EligibilityResponse;
import com.workflowfox.eligibility.dto.EligibilityStatus;
import com.workflowfox.eligibility.model.Coverage;
import com.workflowfox.eligibility.model.Member;
import com.workflowfox.eligibility.model.Plan;
import com.workflowfox.eligibility.repository.CoverageRepository;
import com.workflowfox.eligibility.repository.MemberRepository;
import com.workflowfox.eligibility.repository.PlanRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Implements the eligibility business rules defined in spec.md §9, applied
 * in this order:
 *
 * <ol>
 *   <li>No matching member id → {@code MEMBER_NOT_FOUND}.</li>
 *   <li>{@code checkDate} before {@code coverageEffectiveDate} →
 *       {@code NOT_YET_ELIGIBLE}.</li>
 *   <li>{@code coverageTerminationDate} present and {@code checkDate}
 *       after it → {@code INELIGIBLE}.</li>
 *   <li>Otherwise → {@code ELIGIBLE}.</li>
 * </ol>
 *
 * Coverage is active on the effective date and through/including the
 * termination date; a null termination date means no recorded end date.
 */
@Service
public class EligibilityServiceImpl implements EligibilityService {

    private final MemberRepository memberRepository;
    private final PlanRepository planRepository;
    private final CoverageRepository coverageRepository;

    public EligibilityServiceImpl(
            MemberRepository memberRepository, PlanRepository planRepository, CoverageRepository coverageRepository) {
        this.memberRepository = memberRepository;
        this.planRepository = planRepository;
        this.coverageRepository = coverageRepository;
    }

    @Override
    public EligibilityResponse checkEligibility(String memberId, LocalDate checkDate) {
        Optional<Member> member = memberRepository.findById(memberId);
        if (member.isEmpty()) {
            return new EligibilityResponse(
                    memberId,
                    null,
                    null,
                    null,
                    null,
                    checkDate,
                    EligibilityStatus.MEMBER_NOT_FOUND,
                    "No member matches ID %s. Confirm the member ID and check again.".formatted(memberId));
        }

        Coverage coverage = coverageRepository
                .findByMemberId(memberId)
                .orElseThrow(() -> new IllegalStateException("No coverage record found for member " + memberId));
        Plan plan = planRepository
                .findById(coverage.planId())
                .orElseThrow(() -> new IllegalStateException("No plan record found for plan " + coverage.planId()));

        EligibilityStatus status;
        String reason;
        if (checkDate.isBefore(coverage.effectiveDate())) {
            status = EligibilityStatus.NOT_YET_ELIGIBLE;
            reason = "Coverage does not begin until %s, which is after %s.".formatted(coverage.effectiveDate(), checkDate);
        } else if (coverage.terminationDate() != null && checkDate.isAfter(coverage.terminationDate())) {
            status = EligibilityStatus.INELIGIBLE;
            reason = "Coverage ended on %s, which is before %s.".formatted(coverage.terminationDate(), checkDate);
        } else {
            status = EligibilityStatus.ELIGIBLE;
            reason = "Coverage is active on %s.".formatted(checkDate);
        }

        return new EligibilityResponse(
                memberId,
                member.get().name(),
                plan.name(),
                coverage.effectiveDate(),
                coverage.terminationDate(),
                checkDate,
                status,
                reason);
    }
}
