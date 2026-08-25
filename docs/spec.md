# Cloud-Native Member Eligibility

## Product and Technical Specification — MVP v1.1

**Document type:** Combined Product and Technical Specification  
**Status:** Draft for review  
**Product source:** `_docs/outdated/plan.md`, v0.1  
**Technical source:** `_docs/outdated/architecture.md`, v0.2, Approved  
**Implementation trace:** `_docs/outdated/tasks.md`, v0.2, Approved  
**Revision:** Migrated from Next.js/FastAPI/GCP to Angular/Spring Boot/AWS  
**Last updated:** 2026-08-23

This revision preserves the approved product behavior while replacing the former implementation architecture. The delivery sequence now follows Alexey Grigorev's Part 2 progression: specification, Angular frontend with a mock service, OpenAPI contract, Spring Boot backend, frontend/backend integration, PostgreSQL persistence, and then a separately approved deployment stage.

The referenced source documents currently exist under `_docs/outdated/`. Their old framework and cloud choices are superseded by this specification. Product scope, eligibility behavior, UX requirements, and the intent of the original acceptance criteria remain authoritative.

---

## 1. Product Summary

Cloud-Native Member Eligibility is a small, read-only enterprise healthcare application that allows a Service Representative to determine whether a synthetic health-plan member has active coverage on a specified date.

The application answers one business question:

> Does this member have active coverage on this date?

The showcase demonstrates production-oriented, specification-driven, AI-assisted enterprise application engineering. The application itself is deterministic and contains no generative-AI capability.

---

## 2. Problem and Context

A Service Representative may need to answer an eligibility inquiry from a member or provider. When coverage information is distributed across systems or requires manual date comparison, the representative may take too long to respond or provide an inconsistent answer.

The MVP provides one clear, repeatable workflow and returns a consistent eligibility decision with supporting member and coverage information. It is intended for a short public demonstration and as a production-quality reference implementation, not for processing real member data.

All data used by the showcase is synthetic. The application must not contain real protected health information (PHI) or personally identifiable information (PII).

---

## 3. Goals

1. Let a Service Representative complete a date-specific eligibility inquiry without training.
2. Return a consistent decision using explicit, testable date rules.
3. Display the decision, supporting coverage details, and a plain-language reason in an easy-to-scan format.
4. Demonstrate every supported business outcome with synthetic data.
5. Handle invalid input, unknown members, network failures, and backend failures without exposing technical details.
6. Demonstrate Angular and Spring Boot as independently buildable application tiers, with deployment to AWS handled as a later, separately approved stage.
7. Keep the showcase inexpensive to operate, read-only, and deliberately narrow.

---

## 4. Non-Goals

The MVP does not include:

- authentication or authorization;
- registration, user profiles, or role management;
- Salesforce or another CRM;
- member search, member lists, or inquiry history;
- member, plan, or coverage maintenance;
- enrollment, claims, billing, payments, or notifications;
- dashboards, analytics, administration, or reporting;
- generative AI, chatbots, RAG, or agents inside the product;
- production PHI/PII processing;
- microservices, queues, event streaming, or an API gateway;
- Kubernetes or Amazon EKS;
- infrastructure as code in the MVP;
- multiple deployment environments.

---

## 5. Primary User

The primary user is a Service Representative assisting a member or provider.

The user needs to complete the inquiry quickly and should require little or no training. The representative is expected to know the exact Member ID; discovering a member by name or other attributes is not supported.

---

## 6. Core User Flow

1. The user opens the Member Eligibility application.
2. The user enters a Member ID.
3. The user selects a **Check Coverage On** date. The field defaults to today's date.
4. The user selects **Check Eligibility**.
5. The application validates the inputs.
6. The application retrieves the member's coverage and applies the eligibility rules.
7. The application displays one of four business outcomes with a reason.
8. The user selects **Start Another Inquiry** or corrects the input and tries again.

The complete workflow occurs on one page.

---

## 7. Functional Requirements

### 7.1 Eligibility Inquiry

- The application shall expose one eligibility inquiry form on a single page.
- The form shall accept exactly two business inputs: Member ID and **Check Coverage On** date.
- The application shall perform a read-only inquiry; no user action shall create, update, or delete member, plan, or coverage data.
- Submitting valid input shall call the Spring Boot eligibility endpoint through the centralized Angular eligibility service.
- The same database state and input values shall always return the same business outcome.

Input behavior:

| Field | Control | Required | Behavior |
|---|---|---:|---|
| Member ID | Text input | Yes | Accepts the exact synthetic member identifier. Blank input is invalid. No partial-name or fuzzy search is performed. |
| Check Coverage On | Date input | Yes | Defaults to the current date, may be changed, and is sent to the API as an ISO `YYYY-MM-DD` date. |

The form contains one primary action: **Check Eligibility**. No additional healthcare, member, provider, plan, or contact fields shall be added.

### 7.2 Eligibility Outcomes

The application supports exactly four business outcomes:

| Status | Meaning |
|---|---|
| `ELIGIBLE` | Coverage is active on the requested date. |
| `NOT_YET_ELIGIBLE` | The requested date is before the coverage effective date. |
| `INELIGIBLE` | The requested date is after the coverage termination date. |
| `MEMBER_NOT_FOUND` | No member exists for the supplied Member ID. |

All four statuses are normal business results returned with HTTP `200`. `MEMBER_NOT_FOUND` shall not be represented as HTTP `404`.

### 7.3 Result Display

For `ELIGIBLE`, `NOT_YET_ELIGIBLE`, and `INELIGIBLE`, the application must display:

- Member ID;
- Member Name;
- Plan Name;
- Coverage Effective Date;
- Coverage Termination Date, or an appropriate empty/not-applicable value;
- Check Coverage On Date;
- Eligibility Status;
- Eligibility Reason.

The status and reason must be visually prominent. Supporting information must be easy to scan. The UI may format dates for readability, but it shall not change their calendar values. It shall not display raw JSON, internal codes without readable labels, stack traces, database details, or infrastructure details.

### 7.4 Member Not Found

For `MEMBER_NOT_FOUND`, the application must display a clear business message such as:

> Member not found. We could not find a member matching the entered Member ID.

The result shall show the submitted Member ID, selected check date, status, and plain-language reason. Member name, plan, and coverage dates are `null` in the API response and shall not be rendered as empty or misleading data rows. The user must be able to correct the Member ID and submit again. The UI must not present this outcome as a technical failure.

### 7.5 Start Another Inquiry

After viewing a result, the UI must provide **Start Another Inquiry**. This action clears the previous result and result-level messages, returns the page to its initial inquiry state, and restores the current-date default.

### 7.6 Validation

- Both fields shall have persistent visible labels.
- Angular shall prevent submission when the Member ID is blank or the date is missing/invalid and shall show a concise field-level message.
- The API remains authoritative and shall return HTTP `422` for a missing/blank Member ID or a missing/malformed date.
- A `422` response shall be translated into a friendly, non-technical message near the relevant form field or in a form-level validation summary.
- The raw Spring Boot validation payload shall not be shown directly to the user.
- No Member ID pattern, length rule, case conversion, or fuzzy matching is required beyond the existing non-blank exact-match behavior.

### 7.7 Loading Behavior

While an inquiry is in progress, the application must:

- show a visible state such as **Checking eligibility…**;
- prevent duplicate submission;
- keep existing form values visible;
- clear any previous result or technical-error state when a new request starts;
- end loading on a business response, validation response, network failure, or technical failure;
- keep the page layout stable and honor reduced-motion preferences.

### 7.8 Technical Error Behavior

HTTP `500`, an unreachable API, a timeout, or another network failure must produce one generic unavailable-service state:

> Eligibility service temporarily unavailable. Please try again.

The UI must provide a **Try Again** action that repeats the inquiry using the unchanged form values. It must not expose stack traces, status codes, exception messages, SQL, file paths, service names, internal identifiers, or infrastructure details.

---

## 8. UX Requirements

### 8.1 Page Structure

The application uses a focused single-page layout:

```text
+--------------------------------------------------+
| WorkflowFox                                     |
| Member Eligibility                              |
+--------------------------------------------------+
| Member Eligibility Inquiry                     |
|                                                  |
| Member ID              [____________________]   |
| Check Coverage On      [____________________]   |
|                         [Check Eligibility]      |
|                                                  |
| Eligibility Result (after submission)           |
| Status and reason                                |
| Member information                              |
| Coverage information                            |
| Checked date                                    |
| [Start Another Inquiry]                         |
+--------------------------------------------------+
```

No sidebar, dashboard, or multi-page navigation is required.

On common desktop widths, Member ID and date may share one row. The result may appear below the form or replace its primary content, but it must remain in the same workspace. Navigation items that imply unsupported features must not be added.

### 8.2 Design Direction

The UI must feel:

- professional;
- modern;
- trustworthy;
- calm;
- minimal;
- enterprise-focused;
- easy to scan.

Suggested palette:

- Deep Navy: `#0F172A`;
- Enterprise Blue: `#2563EB`;
- Emerald: `#10B981`;
- white or light-neutral background.

Color must support hierarchy and status rather than decoration. The design must avoid excessive gradients, glassmorphism, decorative illustrations, AI imagery, and unnecessary animation.

### 8.3 Prototype and Demo Data

All prototype and implementation data is synthetic. The approved deterministic examples are:

| Member ID | Default-date demonstration |
|---|---|
| `M-1001` | `ELIGIBLE` |
| `M-1002` | `NOT_YET_ELIGIBLE` |
| `M-1003` | `INELIGIBLE` |
| `M-9999` | `MEMBER_NOT_FOUND`; this ID is intentionally absent |

The Angular mock service must make all four outcomes, validation, loading, and unavailable-service states reachable. Synthetic names and plan names may be displayed, but no data may represent or be derived from a real person.

### 8.4 Accessibility

The UI must support:

- semantic HTML;
- explicit labels for form controls;
- keyboard navigation;
- visible focus states;
- adequate color contrast;
- readable inline validation;
- screen-reader-friendly status and error announcements;
- visible status text so meaning never depends on color alone.

Validation messages must be associated programmatically with their fields. New results and errors must be announced through an appropriate status/live region without unexpectedly moving focus. Normal text must meet a 4.5:1 contrast ratio; large text and non-text UI indicators must meet 3:1. Touch/click targets should be at least 44 by 44 CSS pixels where practical.

### 8.5 Responsive Behavior

The primary target is an enterprise desktop workflow. At widths of 1024 CSS pixels and above, the form and result must remain readable without horizontal scrolling. At narrower widths, fields must stack vertically and content must reflow without clipping or horizontal page scrolling. The application does not need to imitate a consumer mobile application.

---

## 9. Eligibility Business Rules

Eligibility is evaluated for the supplied `checkDate`.

Rules are applied in this order:

1. If no matching Member ID exists, return `MEMBER_NOT_FOUND`.
2. If `checkDate` is earlier than `coverageEffectiveDate`, return `NOT_YET_ELIGIBLE`.
3. If `coverageTerminationDate` exists and `checkDate` is later than that date, return `INELIGIBLE`.
4. Otherwise, return `ELIGIBLE`.

Boundary behavior:

- Coverage is active on the effective date.
- Coverage is active through and including the termination date.
- A null termination date means coverage has no recorded end date.
- Dates use ISO `YYYY-MM-DD` at the API boundary.
- Date-only comparisons must not depend on browser or server time zones.

Eligibility rules belong in the Spring Boot service/domain layer. They must not be duplicated in Angular or embedded in the persistence layer.

---

## 10. Target Technical Architecture

### 10.1 Logical Architecture

```text
Browser
  |
  v
Angular + TypeScript
  |
  | HTTPS / REST / JSON
  v
Java 17 + Spring Boot
  |
  v
Spring Data JPA / Hibernate
  |
  v
PostgreSQL
```

### 10.2 Frontend

The frontend uses:

- Angular;
- TypeScript;
- Angular Reactive Forms;
- Angular `HttpClient` for the real API integration;
- a centralized eligibility service abstraction;
- component and service tests using the Angular project's supported test tooling.

The frontend must keep presentation separate from data access.

During the frontend-prototype stage:

```text
Angular components
        |
        v
Eligibility service contract
        |
        v
Mock eligibility service
```

During integration:

```text
Angular components
        |
        v
Eligibility service contract
        |
        v
HTTP eligibility service
        |
        v
Spring Boot API
```

Mock responses must not be hard-coded directly into UI components.

### 10.3 Backend

The backend uses:

- Java 17;
- Spring Boot;
- Spring Web;
- Bean Validation;
- Spring Data JPA;
- Hibernate;
- PostgreSQL driver;
- JUnit and Spring Boot Test;
- generated OpenAPI documentation or an implementation validated against the approved OpenAPI contract.

The internal structure follows a simple layered design:

```text
EligibilityController
        |
        v
EligibilityService
        |
        v
EligibilityRepository
        |
        v
Spring Data JPA / PostgreSQL
```

Responsibilities:

- Controller: HTTP input/output mapping and validation;
- Service/domain layer: eligibility business rules;
- Repository: persistence access only;
- DTOs: API request and response representation;
- Entities: persistence representation.

Controllers must not contain eligibility rules or access the database directly.

### 10.4 Persistence

PostgreSQL is the target relational database for the completed full-stack showcase.

The database contains seeded synthetic records that demonstrate:

- an eligible member;
- a member whose coverage has not yet started;
- a member whose coverage has ended.

An unknown Member ID demonstrates `MEMBER_NOT_FOUND` and therefore requires no database record.

For the earliest backend-development step, an in-memory repository or an embedded development database may be used temporarily. It must be replaced by PostgreSQL before the persistence milestone is considered complete.

### 10.5 API

Angular communicates directly with the Spring Boot REST API. The application does not require a backend-for-frontend, API gateway, or separate integration service for the MVP.

The backend must restrict CORS to the configured frontend origin outside local development.

### 10.6 Containerization and Automation

- The Angular web application and Spring Boot API must have reproducible builds.
- The deployable application components must be containerized with Docker where appropriate for the selected AWS deployment design.
- GitHub Actions must run build and automated validation on pull requests and the main branch.
- Deployment automation is added only after local full-stack behavior and persistence are validated.

### 10.7 AWS Direction

AWS is the target cloud platform for the deployment stage.

The exact AWS service topology is intentionally deferred to a deployment architecture decision after the local full-stack application is complete. The deployment design must favor:

- low operating cost;
- low operational overhead;
- a public showcase URL;
- managed services where they reduce effort;
- reproducible delivery through GitHub Actions;
- no Kubernetes/EKS requirement.

Candidate services may include:

- Amazon S3 and CloudFront for the Angular static application;
- Amazon ECR plus App Runner, ECS on Fargate, or another simple managed container runtime for Spring Boot;
- Amazon RDS for PostgreSQL when a managed AWS database is justified.

These candidates are not implementation commitments in this specification. The final selection requires a separate deployment design that compares cost, complexity, availability, networking, and showcase value.

---

## 11. Data Model

The MVP uses three relational entities and at most one coverage segment per member.

### 11.1 Member

| Field | Type | Rules |
|---|---|---|
| `member_id` | string | Primary key; synthetic identifier |
| `name` | string | Required; synthetic display name |

### 11.2 Plan

| Field | Type | Rules |
|---|---|---|
| `plan_id` | string | Primary key; synthetic identifier |
| `name` | string | Required; synthetic display name |

### 11.3 Coverage

| Field | Type | Rules |
|---|---|---|
| `member_id` | string | Primary key and foreign key to Member; enforces at most one coverage segment per member |
| `plan_id` | string | Required foreign key to Plan |
| `effective_date` | date | Required |
| `termination_date` | nullable date | Null means no recorded end date |

MVP constraints:

- a member has zero or one coverage record in the schema;
- a plan may be associated with multiple coverage records;
- a coverage record belongs to exactly one member and one plan;
- overlapping coverage resolution is out of scope;
- schema names may use standard Java/JPA naming conventions while preserving the domain meaning above.

---

## 12. API Contract

The OpenAPI document is the interface contract between Angular and Spring Boot.

### 12.1 Endpoint

```http
GET /api/v1/eligibility?memberId={memberId}&checkDate={ISO-date}
```

### 12.2 Query Parameters

| Parameter | Required | Format | Description |
|---|---:|---|---|
| `memberId` | Yes | non-empty string | Member identifier to check |
| `checkDate` | Yes | `YYYY-MM-DD` | Date for which coverage is evaluated |

### 12.3 Successful Business Response

All four business outcomes return HTTP `200`.

```json
{
  "memberId": "M-1001",
  "memberName": "Jordan Testcase",
  "planName": "Acme Health Plan",
  "coverageEffectiveDate": "2025-01-01",
  "coverageTerminationDate": null,
  "checkCoverageOnDate": "2026-08-22",
  "eligibilityStatus": "ELIGIBLE",
  "eligibilityReason": "Coverage is active on 2026-08-22."
}
```

For `MEMBER_NOT_FOUND`, the response echoes the supplied Member ID and check date. `memberName`, `planName`, `coverageEffectiveDate`, and `coverageTerminationDate` are null. The OpenAPI schema must document nullability explicitly.

### 12.4 Status Values

```text
ELIGIBLE
NOT_YET_ELIGIBLE
INELIGIBLE
MEMBER_NOT_FOUND
```

### 12.5 HTTP Behavior

| Condition | HTTP status | Behavior |
|---|---:|---|
| Any valid business outcome | `200` | Return the structured eligibility response |
| Missing, blank, or invalid input | `422` | Return a structured validation response |
| Unexpected technical failure | `500` | Return a safe structured error response; log diagnostic details server-side |

`MEMBER_NOT_FOUND` must not return HTTP `404` because it is a normal domain outcome for an eligibility inquiry.

The stable public shape for an unexpected failure may be:

```json
{
  "detail": "An unexpected error occurred. Please try again."
}
```

No endpoint beyond eligibility and operational health is required by this specification.

### 12.6 OpenAPI Source of Truth

The approved `openapi.yaml` is created after the Angular mock frontend establishes the required service contract and before Spring Boot backend implementation begins.

The Angular client models and Spring Boot DTOs must conform to the approved contract. If generated clients or server interfaces are used, generated code must remain separated from handwritten domain logic.

---

## 13. Security and Privacy

- Only synthetic data may be stored, processed, logged, demonstrated, or published.
- The repository must not contain real PHI, PII, credentials, or cloud secrets.
- Secrets and environment-specific configuration must be supplied through environment variables or an appropriate AWS secret/configuration service during deployment.
- GitHub Actions must use GitHub secrets or short-lived cloud authentication rather than committed credentials.
- CORS must be restricted to approved frontend origins outside local development.
- Public error responses must not expose implementation details.
- Authentication is deliberately excluded from the showcase MVP; this limitation must be documented wherever the public deployment is described.

---

## 14. Reliability and Error Handling

- Angular must distinguish client validation, business outcomes, and technical failures.
- Spring Boot must use centralized exception handling for validation and unexpected failures.
- The backend must return structured error responses consistent with the OpenAPI contract.
- The eligibility service must produce deterministic results for the same Member ID and date.
- Date handling must remain date-only and timezone-independent.
- The UI must prevent accidental duplicate submissions while a request is active.
- A simple health endpoint may be added for deployment health checks; it must not expose sensitive details.

---

## 15. Logging and Observability

For the MVP:

- Spring Boot logs request outcome, latency, and a safe correlation identifier;
- logs must not contain names or other unnecessary member details;
- unexpected exceptions are logged server-side with diagnostic context;
- Angular presents safe, user-oriented messages rather than raw errors;
- deployed application logs must be available through the selected AWS runtime's standard logging integration.

Advanced tracing, dashboards, alerting, and third-party observability platforms are deferred.

---

## 16. Testing and Validation

### 16.1 Angular

Automated tests must cover:

- required-field validation;
- default date behavior;
- submission/loading behavior;
- all four eligibility outcomes;
- technical-error and retry behavior;
- Start Another Inquiry;
- service abstraction behavior;
- key accessibility behaviors where practical.

### 16.2 Spring Boot

Automated tests must cover:

- each eligibility business rule, including boundary dates;
- controller request validation;
- all four `200` business outcomes;
- `422` validation behavior;
- safe `500` behavior;
- repository/persistence behavior with a test database;
- API response serialization and date formats.

### 16.3 Contract and Integration

Validation must confirm:

- Angular request/response models match `openapi.yaml`;
- Spring Boot implements `openapi.yaml`;
- Angular can call Spring Boot locally;
- CORS is correctly configured;
- seeded PostgreSQL data produces each demonstrable domain outcome;
- the application passes an end-to-end smoke test before deployment work begins.

### 16.4 Engineering Gates

GitHub Actions must run applicable:

- Angular build, tests, linting, and type checking;
- Java compilation and tests;
- OpenAPI validation;
- container build validation after Docker artifacts are introduced.

An implementation is not complete merely because an AI agent reports success. Test output, diffs, and acceptance evidence are required.

---

## 17. MVP Acceptance Criteria

Each criterion is independently testable and must be recorded as PASS or FAIL.

| ID | Acceptance criterion |
|---|---|
| AC-01 | On initial page load, exactly one eligibility inquiry form is visible with labeled Member ID and Check Coverage On controls and a **Check Eligibility** action. |
| AC-02 | On initial page load, Check Coverage On contains the current calendar date and can be changed to another valid date. |
| AC-03 | Submitting a blank Member ID is prevented or produces a friendly validation message; no business result is displayed. |
| AC-04 | Calling the API without `memberId`, with an empty `memberId`, without `checkDate`, or with a malformed `checkDate` returns HTTP `422`. |
| AC-05 | A valid integrated inquiry causes Angular to call `GET /api/v1/eligibility` through its HTTP eligibility service with `memberId` and ISO `checkDate` query parameters. |
| AC-06 | While a request is pending, a visible loading state is shown and a second submit does not create a duplicate in-flight request. |
| AC-07 | For a check date before the effective date, the API returns HTTP `200` with `eligibilityStatus: NOT_YET_ELIGIBLE`. |
| AC-08 | For a check date equal to the effective date, the API returns HTTP `200` with `eligibilityStatus: ELIGIBLE`. |
| AC-09 | For a check date between effective and termination dates, inclusive, the API returns HTTP `200` with `eligibilityStatus: ELIGIBLE`. |
| AC-10 | For a check date after the termination date, the API returns HTTP `200` with `eligibilityStatus: INELIGIBLE`. |
| AC-11 | For coverage with a null termination date, any check date on or after the effective date returns HTTP `200` with `eligibilityStatus: ELIGIBLE`. |
| AC-12 | Querying the intentionally absent `M-9999` returns HTTP `200` with `eligibilityStatus: MEMBER_NOT_FOUND`, the submitted Member ID and check date, and null member/plan/coverage fields. |
| AC-13 | The Angular mock service and PostgreSQL seed data can each demonstrate `ELIGIBLE`, `NOT_YET_ELIGIBLE`, `INELIGIBLE`, and `MEMBER_NOT_FOUND` without changing application code or using real data. |
| AC-14 | A found-member result displays status, reason, Member ID, member name, plan name, effective date, termination state/date, and checked date. |
| AC-15 | The not-found UI displays a business result and reason, omits empty member/plan/coverage detail rows, and does not label the condition as a technical error. |
| AC-16 | Selecting **Start Another Inquiry** clears the displayed result and returns the page to a clean inquiry form with the current-date default. |
| AC-17 | An HTTP `500` or simulated network failure displays a generic unavailable-service message and **Try Again** while preserving the submitted values. |
| AC-18 | No `422` or `500` UI state displays raw JSON, stack traces, exception text, SQL, file paths, or infrastructure details. |
| AC-19 | The entire inquiry, result review, retry, and start-another flow can be completed with a keyboard, with visible focus on the active control. |
| AC-20 | Form labels are programmatically associated with controls, validation is associated with invalid fields, and new results/errors are announced by an accessible status/live region. |
| AC-21 | Normal text meets 4.5:1 contrast; large text and non-text UI indicators meet 3:1 contrast; every status remains understandable without color. |
| AC-22 | At 1024 CSS pixels and 1440 CSS pixels wide, the page has no horizontal scrolling and the form and result remain readable and usable. |
| AC-23 | At 375 CSS pixels wide, fields stack vertically and the page has no clipped controls or horizontal page scrolling. |
| AC-24 | The delivered UI uses Deep Navy `#0F172A`, Enterprise Blue `#2563EB`, Emerald `#10B981`, and white/light-neutral surfaces without gradients, glassmorphism, dashboards, sidebars, illustrations, or AI imagery. |
| AC-25 | An approved `openapi.yaml` documents the eligibility endpoint, validation behavior, four business statuses, nullability, and error responses; the Spring Boot implementation conforms to it and exposes interactive API documentation for the showcase. |
| AC-26 | The API reads member, plan, and coverage data through Spring Data JPA/Hibernate from seeded PostgreSQL and exposes no runtime data-mutation endpoint. |
| AC-27 | The Spring Boot API starts successfully, serves its health endpoint, and returns a seeded eligibility inquiry through the PostgreSQL-backed repository. |
| AC-28 | In the integrated/deployed environment, a browser request from the configured frontend origin is allowed by CORS and a request from an unconfigured origin is denied by CORS. |
| AC-29 | CI passes only when Angular and Spring Boot linting/static checks, automated tests, OpenAPI validation, and applicable Docker image builds succeed on a pull request. |
| AC-30 | After the AWS deployment stage is approved and implemented, GitHub Actions deploys the application to the selected AWS services, the public URL passes a smoke test, and the deployed resources match the approved low-cost deployment architecture. |

---

## 18. Delivery Sequence

The project follows Alexey Grigorev's Part 2 sequence while using the WorkflowFox target stack.

### Stage 1 — Specification

- Approve this combined product and technical specification.
- Preserve the narrow MVP scope.

**Gate:** Specification approved before implementation.

### Stage 2 — Angular Frontend with Mock Service

- Build the Angular/TypeScript UI.
- Use Reactive Forms.
- Define the eligibility service contract.
- Implement a mock service supporting all four outcomes and the technical-error state.
- Validate UX, accessibility, and frontend behavior.

**Gate:** The complete frontend workflow operates against mocks and its tests pass.

### Stage 3 — OpenAPI Contract

- Derive `openapi.yaml` from the approved business/API behavior and the frontend service contract.
- Review request parameters, response fields, enum values, nullability, and errors.
- Validate the OpenAPI document.

**Gate:** Contract approved before Spring Boot implementation.

### Stage 4 — Spring Boot Backend

- Implement Java 17/Spring Boot controller, DTOs, service, and repository abstraction.
- Implement deterministic eligibility rules.
- Begin with an in-memory or embedded development repository if useful.
- Implement tests against the approved contract.

**Gate:** Backend behavior and tests satisfy the OpenAPI contract.

### Stage 5 — Frontend/Backend Integration

- Add the Angular HTTP service implementation.
- Replace the mock service through configuration without redesigning UI components.
- Configure local CORS.
- Resolve contract, date, serialization, and error-handling mismatches.

**Gate:** Angular and Spring Boot pass the local integrated workflow.

### Stage 6 — PostgreSQL Persistence

- Add Spring Data JPA/Hibernate entities and repositories.
- Add PostgreSQL configuration and synthetic seed data.
- Add database migration/versioning support selected during implementation design.
- Validate persistence and full-stack behavior.

**Gate:** The integrated application works locally with PostgreSQL and all tests pass.

### Stage 7 — Deployment Design

- Select the minimum-cost AWS topology.
- Document cost, security, networking, configuration, health checks, and rollback.
- Decide the Angular hosting, Spring Boot runtime, PostgreSQL hosting, and container registry.

**Gate:** Deployment architecture approved before provisioning AWS resources.

### Stage 8 — Docker, CI/CD, and AWS Deployment

- Create and validate Docker artifacts.
- Extend GitHub Actions for delivery.
- Provision only the approved AWS resources.
- Deploy, smoke-test, and document the public showcase.

**Gate:** Public application, successful pipeline evidence, runbook, and validation report.

This sequence intentionally keeps cloud provisioning separate from Part 2 application construction. Kubernetes/EKS is not required.

---

## 19. Explicitly Out of Scope

The MVP shall not include:

- authentication, authorization, enterprise SSO, or multiple user roles;
- Salesforce or another enterprise-system integration;
- AI functionality, chatbots, RAG, recommendations, or agents inside the product;
- dashboards, analytics, audit reporting, notifications, or inquiry history;
- member search by name or attributes; only exact Member ID inquiry is supported;
- member, plan, or coverage updates;
- multiple coverage segments or coordination of benefits;
- enrollment, quoting, shopping, plan comparison, or eligibility enrollment changes;
- claims, benefits, accumulators, authorizations, billing, payments, or unrelated healthcare functionality;
- real PHI, PII, customer data, or production data;
- API gateway or backend-for-frontend proxy;
- Kubernetes or Amazon EKS;
- queues, event streams, or asynchronous job processing;
- infrastructure as code for the MVP;
- unnecessary microservices beyond the Angular web tier and Spring Boot API;
- multiple hosted environments or a staging environment;
- a separate observability platform beyond the selected AWS services' built-in capabilities.

---

## 20. Deferred Decisions and Future Evolution

The following remain explicitly deferred:

1. **Exact AWS runtime topology** — compare S3/CloudFront plus App Runner, ECS Fargate, Elastic Beanstalk, or another simple managed option before selection.
2. **Managed PostgreSQL hosting** — evaluate Amazon RDS cost and operational value against lower-cost showcase alternatives while keeping PostgreSQL compatibility.
3. **Database migrations** — select Flyway or Liquibase during Spring Boot implementation design.
4. **Angular version and UI component library** — use the current supported Angular release; introduce a component library only if the design requires it.
5. **OpenAPI code generation** — decide whether to generate Angular clients/Spring interfaces or validate handwritten code against the contract.
6. **AWS infrastructure as code** — defer Terraform/CDK/CloudFormation until manual architecture and cost are understood; not required for MVP.
7. **Authentication and authorization** — required before any real enterprise or sensitive-data use, but excluded from this synthetic public showcase.
8. **Advanced observability** — distributed tracing, dashboards, alerting, and SLOs are future production-hardening steps.
9. **Kubernetes/EKS** — deferred indefinitely unless a future workload provides a concrete need.
10. **Multiple environments** — dev/stage/prod separation is a future production evolution.
11. **Production healthcare controls** — HIPAA controls, audit requirements, encryption policies, data retention, and formal threat modeling are outside this synthetic MVP and mandatory before real PHI use.
12. **Member without coverage** — the schema permits a member with zero coverage records, but the product defines no fifth outcome. Seed data must avoid this state until a future product decision defines whether it means not found, ineligible, or a separate outcome.
13. **Synthetic dataset size** — add fictional records only if they materially improve the demonstration; the MVP needs only deterministic examples of the four outcomes.
14. **Repository topology** — a monorepo remains the working assumption; splitting Angular and Spring Boot into separate repositories is not required.
15. **Broader healthcare capabilities** — any coverage-history, enrollment, claims, benefits, billing, or integration capability requires a separate discovery and specification phase.

No deferred decision authorizes implementation beyond the MVP defined here.

---

## 21. Final Architecture Principle

```text
The business problem defines the capability.

Angular owns the user experience.

OpenAPI defines the interface contract.

Spring Boot owns eligibility behavior.

PostgreSQL owns persisted synthetic data.

Docker and GitHub Actions support repeatable delivery.

AWS hosts the approved deployment when the application is ready.

AI assists the engineering process; human review and validation retain ownership.
```
