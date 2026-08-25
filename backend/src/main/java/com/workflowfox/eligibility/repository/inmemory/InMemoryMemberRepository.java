package com.workflowfox.eligibility.repository.inmemory;

import com.workflowfox.eligibility.model.Member;
import com.workflowfox.eligibility.repository.MemberRepository;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * In-memory {@link MemberRepository} seeded with deterministic synthetic
 * data (spec.md §8.3). Temporary until Stage 6 replaces it with Spring
 * Data JPA / PostgreSQL (spec.md §10.4); {@code M-9999} is intentionally
 * absent to demonstrate {@code MEMBER_NOT_FOUND}.
 */
@Repository
public class InMemoryMemberRepository implements MemberRepository {

    private final Map<String, Member> membersById = Map.of(
            "M-1001", new Member("M-1001", "Jordan Testcase"),
            "M-1002", new Member("M-1002", "Riley Sampleton"),
            "M-1003", new Member("M-1003", "Avery Placeholder"));

    @Override
    public Optional<Member> findById(String memberId) {
        return Optional.ofNullable(membersById.get(memberId));
    }
}
