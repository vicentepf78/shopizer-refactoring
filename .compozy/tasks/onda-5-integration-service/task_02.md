---
status: pending
title: Payment plugins + PaymentOrchestrator extract
type: backend
complexity: high
---

# Payment plugins + PaymentOrchestrator extract

## Overview
Consolidates TLC T6–T8. Moves payment plugin implementations to `sm-integration-core`, adds V2 adapter bridge, and extracts configuration/module resolution from `PaymentServiceImpl` into `PaymentOrchestratorImpl` (no order mutation methods).

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST relocate `sm-core/.../modules/integration/payment/impl/*` to `sm-integration-core` — T6.
2. MUST implement `PaymentModuleV2Adapter` bridging legacy plugins — T7.
3. MUST extract config CRUD, `getPaymentMethods`, credit card validation to orchestrator — T8.
4. MUST NOT include `OrderService` in sm-integration-core payment package.
5. MUST preserve encryption behavior for stored credentials.
6. MUST pass relocated plugin unit tests.
</requirements>

## Subtasks
- [ ] 2.1 Move payment plugin classes + update `ModulesConfiguration` (T6)
- [ ] 2.2 `PaymentModuleV2Adapter` + mapper utilities (T7)
- [ ] 2.3 `PaymentOrchestratorImpl` config paths (T8)
- [ ] 2.4 Unit tests for config save/load

## Implementation Details
TechSpec **Build order** steps 3–4. Source: `PaymentServiceImpl.java`, `ModulesConfiguration.java`.

### Relevant Files
- `sm-core/.../modules/integration/payment/impl/StripePayment.java` (and siblings)
- `sm-core/.../services/payments/PaymentServiceImpl.java`
- `sm-core/.../configuration/ModulesConfiguration.java`

### Related ADRs
- [ADR-004](adrs/adr-004.md) — V2 contracts
- [ADR-005](adrs/adr-005.md) — in-process registry

## Deliverables
- Payment plugins in sm-integration-core
- `PaymentOrchestrator` interface + impl (config portion)
- Unit tests >=80% on orchestrator config paths **(REQUIRED)**

## Tests
- [ ] MoneyOrder or mock module configuration roundtrip
- [ ] Adapter invokes legacy module with DTO-mapped context
- [ ] `./mvnw test -pl sm-integration-core -Dtest=*Payment*`

## Success Criteria
- No `OrderService` import in payment orchestrator package
- All payment plugin tests green in new module
