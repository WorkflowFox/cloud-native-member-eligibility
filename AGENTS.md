# AGENTS.md

Instructions for coding agents (and humans) working in this repository.

## What this is

Cloud-Native Member Eligibility: a small, read-only enterprise healthcare
showcase. A Service Representative enters a Member ID and a date; the app
returns one of four deterministic eligibility outcomes with a plain-language
reason. All data is synthetic — never add real PHI/PII.

**Read `docs/spec.md` before making product or architecture decisions.** It
is the authoritative combined product and technical specification. This file
only covers repo mechanics and working conventions; the spec covers
requirements, business rules, UX, API contract, and acceptance criteria.

## Repository layout

```
/backend      Spring Boot (Java 17) API — added in Stage 4, currently empty
/docs         Supporting documentation (spec.md, future design/deployment docs)
/frontend     Angular application (Stage 2: complete, mock-backed)
AGENTS.md     This file
openapi.yaml  API agreement between /frontend and /backend (spec §12)
```

## Delivery sequence

The project is built in the staged sequence defined in `docs/spec.md` §18
(Alexey Grigorev's Part 2 progression). Do not skip ahead of the current
stage or introduce a later stage's technology early.

1. Specification — done.
2. Angular frontend with mock service — done (`/frontend`).
3. OpenAPI contract — done (`/openapi.yaml`).
4. Spring Boot backend (in-memory/embedded repo acceptable initially) — next.
5. Frontend/backend integration (swap the mock for HTTP via DI, no component redesign).
6. PostgreSQL persistence.
7. Deployment design (AWS topology — proposal only, needs approval before provisioning).
8. Docker, CI/CD, AWS deployment.

Each stage has a gate in the spec. Don't consider a stage complete without
satisfying its gate and the acceptance criteria in spec §17 that apply to it.

## Working conventions

- **Business rules live in one place.** Eligibility logic (spec §9) belongs
  in the backend service/domain layer once it exists. It is currently
  duplicated into `MockEligibilityService` only because Stage 4 hasn't
  started; that duplication is temporary scaffolding, not a pattern to
  repeat elsewhere.
- **The frontend depends on an abstraction, not an implementation.**
  Components inject `EligibilityService` (abstract), never
  `MockEligibilityService` directly, except the prototype demo bar, which
  optionally injects the concrete mock to drive its controls and disappears
  automatically once a real HTTP service is provided instead.
- **`openapi.yaml` is the source of truth for the API.** Update it first,
  then bring frontend types and backend DTOs into conformance — not the
  other way around.
- **Synthetic data only.** Seed data must stay deterministic and cover all
  four outcomes (`M-1001` eligible, `M-1002` not-yet-eligible, `M-1003`
  ineligible, `M-9999` intentionally absent) per spec §8.3.
- **No scope creep.** Section 19 of the spec is an explicit out-of-scope
  list (no auth, no search, no dashboards, no AI-in-product, etc.). Treat it
  as a hard boundary, not a backlog.
- **An agent's self-report is not evidence.** Per spec §16.4, back up
  "it works" with actual test output, a build log, or a screenshot from a
  running instance — not a claim.

## Building and testing

### Frontend (`/frontend`)

```bash
cd frontend
npm install
npm start        # ng serve — dev server
npm run build     # ng build
npm test          # ng test — vitest-based unit tests
```

### Backend (`/backend`)

Not yet implemented (Stage 4). When it lands, document its build/run/test
commands here.

## Commit and PR expectations

- Keep frontend and backend changes in separate, reviewable commits where
  practical.
- Any change to `openapi.yaml` should be called out explicitly in the PR
  description, since both tiers depend on it.
