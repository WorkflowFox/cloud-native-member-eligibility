package com.workflowfox.eligibility.repository.inmemory;

import com.workflowfox.eligibility.model.Coverage;
import com.workflowfox.eligibility.repository.CoverageRepository;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * In-memory {@link CoverageRepository} seeded with deterministic synthetic
 * data (spec.md §8.3):
 *
 * <ul>
 *   <li>{@code M-1001} — coverage effective 2025-01-01, no termination date
 *       (demonstrates {@code ELIGIBLE}).</li>
 *   <li>{@code M-1002} — coverage effective 2027-02-01, no termination date
 *       (demonstrates {@code NOT_YET_ELIGIBLE} for dates before then).</li>
 *   <li>{@code M-1003} — coverage effective 2023-03-01, terminated
 *       2025-12-31 (demonstrates {@code INELIGIBLE} for dates after
 *       termination).</li>
 * </ul>
 *
 * Temporary until Stage 6 replaces it with Spring Data JPA / PostgreSQL
 * (spec.md §10.4).
 */
@Repository
public class InMemoryCoverageRepository implements CoverageRepository {

    private final Map<String, Coverage> coverageByMemberId = Map.of(
            "M-1001", new Coverage("M-1001", "P-ACME-HEALTH", LocalDate.of(2025, 1, 1), null),
            "M-1002", new Coverage("M-1002", "P-NORTHWIND-PPO", LocalDate.of(2027, 2, 1), null),
            "M-1003", new Coverage("M-1003", "P-BEACON-HMO", LocalDate.of(2023, 3, 1), LocalDate.of(2025, 12, 31)));

    @Override
    public Optional<Coverage> findByMemberId(String memberId) {
        return Optional.ofNullable(coverageByMemberId.get(memberId));
    }
}
