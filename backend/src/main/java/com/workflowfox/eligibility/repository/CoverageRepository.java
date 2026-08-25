package com.workflowfox.eligibility.repository;

import com.workflowfox.eligibility.model.Coverage;
import java.util.Optional;

/**
 * Persistence abstraction for {@link Coverage} lookups.
 *
 * <p>Kept independent of any storage technology so the in-memory
 * implementation used today can be swapped for a Spring Data JPA /
 * PostgreSQL implementation later without changing services or
 * controllers. A member has at most one coverage segment (spec.md §11.3).
 */
public interface CoverageRepository {

    Optional<Coverage> findByMemberId(String memberId);
}
