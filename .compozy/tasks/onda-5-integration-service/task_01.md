---
status: pending
title: Integration contracts, client, Wave5 Strangler config
type: backend
complexity: medium
---

# Integration contracts, client, Wave5 Strangler config

## Overview
Consolidates TLC T1–T5. Delivers integration DTOs and `IntegrationServiceClient` in `shopizer-api-contracts`, registers `sm-integration-core` module, and adds Wave5 Strangler profile/properties in the monolith. **External gates:** Onda 3 Execute + Onda 4 partial catalog read MUST be complete before starting.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST verify Onda 3 artifacts compile: `PaymentModuleV2`, `ShippingQuoteModuleV2`, snapshots, checkout service — TLC T1.
2. MUST add integration DTOs in `com.salesmanager.contracts.integration` — TLC T2.
3. MUST create `IntegrationServiceClient` in `com.salesmanager.contracts.client` — TLC T3.
4. MUST add profile `strangler-wave5`, properties `wave5.*`, `Wave5ClientConfig`, RestTemplate with correlation — TLC T4.
5. MUST register `sm-integration-core` Maven module in reactor — TLC T5.
6. MUST NOT import `com.salesmanager.core.model` in contracts.
7. MUST compile `shopizer-api-contracts` and pass `Wave5ClientConfigTest`.
</requirements>

## Subtasks
- [ ] 1.1 Gate check script/test for Onda 3 + Onda 4 prerequisites (T1)
- [ ] 1.2 Integration request/response DTOs (T2)
- [ ] 1.3 `IntegrationServiceClient` interface (T3)
- [ ] 1.4 Wave5 properties + config beans (T4)
- [ ] 1.5 `sm-integration-core` pom + reactor entry (T5)

## Implementation Details
See TechSpec: **Key interfaces**, **Data models**, **Strangler properties**. Reuse `Wave1ClientConfig` / `Wave2ClientConfig` patterns.

### Relevant Files
- `shopizer-api-contracts/` — new integration package
- `sm-shop-model/.../order/transaction/` — source payment DTO shapes
- `sm-shop-model/.../order/shipping/` — source shipping DTO shapes
- `sm-core-modules/.../PaymentModule.java` — legacy contract reference

### Dependent Files
- `sm-integration-core/pom.xml` — to create
- `sm-shop/.../strangler/config/Wave5ClientConfig.java` — to create
- `sm-shop/src/main/resources/application-strangler-wave5.properties` — to create

### Related ADRs
- [ADR-001](adrs/adr-001.md) — single workflow
- [ADR-004](adrs/adr-004.md) — V2 contracts from Onda 3

## Deliverables
- Integration DTOs + client interface in contracts
- `sm-integration-core` module skeleton
- Wave5 Strangler config in sm-shop
- Unit tests for DTO serialization **(REQUIRED)**
- `Wave5ClientConfigTest` **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] DTOs serialize/deserialize without JPA types
  - [ ] `Wave5ClientConfig` loads `wave5.integration-service.base-url`
- Integration tests:
  - [ ] `./mvnw compile -pl shopizer-api-contracts,sm-integration-core -am`
- Test coverage target: >=80% on new DTOs
- All tests must pass

## Success Criteria
- All tests passing
- External gates verified and documented
- `wave5.*` coexists with `wave1.*`–`wave4.*`
