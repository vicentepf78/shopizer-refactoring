---
status: pending
title: processOrder outbox, gate, and STATE update
type: infra
complexity: medium
---

# processOrder outbox, gate, and STATE update

## Overview
Consolidates TLC T44–T48. Adds CHECKOUT_OUTBOX table, staged processOrder with feature flag, reactor gate, Pact verification, and STATE.md update.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST add `CHECKOUT_OUTBOX` schema migration — TLC T44.
2. MUST implement staged processOrder in CheckoutApplicationService with outbox writes — TLC T45–T46.
3. MUST add `checkout.outbox.enabled` flag (default false) and in-process dispatcher — TLC T47.
4. MUST run `./mvnw clean install` full reactor gate — TLC T48, GAT-01.
5. MUST update `.specs/project/STATE.md` — Wave 3 complete, B-001/B-002 status, ADR references.
6. MUST verify Wave 1+2 Pact suites still green after SearchItem migration.
</requirements>

## Subtasks
- [ ] 10.1 Outbox table + repository (T44)
- [ ] 10.2 Staged checkout + outbox events (T45–T46)
- [ ] 10.3 Feature flag + dispatcher (T47)
- [ ] 10.4 Full reactor gate + STATE update (T48)

## Implementation Details
See TechSpec: **Database**, ADR-005. Stages: PAYMENT_REQUESTED, PAYMENT_CONFIRMED, ORDER_PERSISTED, INVENTORY_DECREMENTED.

### Relevant Files
- `sm-core/.../checkout/CheckoutApplicationServiceImpl.java`
- `sm-core/.../order/OrderServiceImpl.java` — processOrder delegation
- `.specs/project/STATE.md`

### Dependent Files
- `sm-core/.../checkout/outbox/CheckoutOutboxEvent.java` — create
- `sm-core/.../checkout/outbox/CheckoutOutboxRepository.java` — create
- DB migration script — create

### Related ADRs
- [ADR-005: Local transactional outbox](../adrs/adr-005.md)

## Deliverables
- Outbox table + repository + dispatcher
- Staged processOrder behind flag
- Green `./mvnw clean install`
- Updated STATE.md
- Integration tests flag on/off **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] Outbox append idempotent per aggregate+event type
- Integration tests:
  - [ ] Outbox rows created when flag enabled
  - [ ] Legacy path when flag disabled
  - [ ] Full reactor build
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- Wave 3 gate green
- STATE.md reflects completion
- Waves 4–6 prerequisites met in monolith
