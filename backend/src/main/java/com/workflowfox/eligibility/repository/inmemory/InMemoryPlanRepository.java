package com.workflowfox.eligibility.repository.inmemory;

import com.workflowfox.eligibility.model.Plan;
import com.workflowfox.eligibility.repository.PlanRepository;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * In-memory {@link PlanRepository} seeded with deterministic synthetic
 * data (spec.md §8.3). Temporary until Stage 6 replaces it with Spring
 * Data JPA / PostgreSQL (spec.md §10.4).
 */
@Repository
public class InMemoryPlanRepository implements PlanRepository {

    private final Map<String, Plan> plansById = Map.of(
            "P-ACME-HEALTH", new Plan("P-ACME-HEALTH", "Acme Health Plan"),
            "P-NORTHWIND-PPO", new Plan("P-NORTHWIND-PPO", "Northwind Choice PPO"),
            "P-BEACON-HMO", new Plan("P-BEACON-HMO", "Beacon Standard HMO"));

    @Override
    public Optional<Plan> findById(String planId) {
        return Optional.ofNullable(plansById.get(planId));
    }
}
