---
status: pending
title: integration-service Boot + admin REST
type: backend
complexity: high
---

# integration-service Boot + admin REST

## Overview
Consolidates TLC T17–T19. Creates `integration-service` Spring Boot app on port 8086 with JWT security, actuator, and admin REST controllers mirroring payment/shipping configuration APIs.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- Requires P-ready (task_03) and S-ready (task_04)
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST create `integration-service` Boot module scanning `sm-integration-core` — T17.
2. MUST expose payment module admin endpoints matching frozen paths — T18, PAY-01..06.
3. MUST expose shipping configuration admin endpoints — T19, SHP-04.
4. MUST replicate JWT `/private/**` security pattern from Wave 1–2.
5. MUST restrict JPA entity scan to integration-related packages only — ADR-003.
6. MUST pass `IntegrationServiceApplicationTest` context load.
</requirements>

## Subtasks
- [ ] 5.1 Boot scaffold + security + actuator (T17)
- [ ] 5.2 Payment admin controllers (T18)
- [ ] 5.3 Shipping admin controllers (T19)
- [ ] 5.4 MockMvc tests for config CRUD

## Related ADRs
- [ADR-003](adrs/adr-003.md) — shared DB
- [ADR-005](adrs/adr-005.md) — plugin registry in service

## Deliverables
- `integration-service` runnable on :8086
- Admin REST + MockMvc tests **(REQUIRED)**

## Success Criteria
- Application context loads with orchestrators wired
- Admin config roundtrip for at least one payment + one shipping module
