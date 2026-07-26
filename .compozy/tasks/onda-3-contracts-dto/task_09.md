---
status: pending
title: CheckoutApplicationService extraction
type: backend
complexity: high
---

# CheckoutApplicationService extraction

## Overview
Consolidates TLC T39–T43. Extracts place-order orchestration from `OrderFacadeImpl` into `CheckoutApplicationService` without changing public REST behavior (CHK requirements).

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST define `CheckoutApplicationService` and `CheckoutCommand` in `sm-core/.../checkout` — TLC T39.
2. MUST move orchestration logic from `OrderFacadeImpl` place-order methods to service — TLC T40–T41.
3. MUST keep `OrderFacadeImpl` as thin delegate (validation + DTO mapping only) — TLC T42.
4. MUST use `CustomerSnapshot` / tenant types in command where applicable — TLC T43.
5. MUST preserve identical outcomes for happy path and known validation errors (CHK-01..CHK-06).
6. MUST NOT change `OrderApi` paths or request/response schemas.
</requirements>

## Subtasks
- [ ] 9.1 CheckoutApplicationService interface + command (T39)
- [ ] 9.2 Extract process flow from OrderFacadeImpl (T40–T41)
- [ ] 9.3 Thin facade delegation (T42)
- [ ] 9.4 Parity integration tests (T43)

## Implementation Details
See TechSpec: **Checkout flow**. `OrderFacadeImpl` currently injects 12+ services — CAS should own orchestration, facade keeps HTTP concerns.

### Relevant Files
- `sm-shop/.../order/facade/OrderFacadeImpl.java` (~1600 lines)
- `sm-core/.../order/OrderServiceImpl.java`
- `sm-shop/.../api/v1/order/OrderApi.java`

### Dependent Files
- `sm-core/.../checkout/CheckoutApplicationService.java` — create
- `sm-core/.../checkout/CheckoutApplicationServiceImpl.java` — create
- `sm-core/.../checkout/CheckoutCommand.java` — create

### Related ADRs
- [ADR-001: Monolith-only](../adrs/adr-001.md)
- [ADR-005: Outbox hooks prepared in task_10](../adrs/adr-005.md)

## Deliverables
- CheckoutApplicationService with extracted flow
- Thinned OrderFacadeImpl
- Checkout parity integration tests **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] CheckoutCommand builder validation
- Integration tests:
  - [ ] Place order happy path matches pre-extraction behavior
  - [ ] Payment failure paths unchanged
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- CHK-ready milestone
- OrderFacadeImpl line count materially reduced
