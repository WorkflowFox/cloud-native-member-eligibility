package com.workflowfox.eligibility.repository;

import com.workflowfox.eligibility.model.Member;
import java.util.Optional;

/**
 * Persistence abstraction for {@link Member} lookups.
 *
 * <p>Kept independent of any storage technology so the in-memory
 * implementation used today can be swapped for a Spring Data JPA /
 * PostgreSQL implementation later without changing services or
 * controllers.
 */
public interface MemberRepository {

    Optional<Member> findById(String memberId);
}
