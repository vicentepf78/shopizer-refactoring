---
status: pending
title: OrderSnapshot and CustomerSnapshot DTOs
type: backend
complexity: medium
---

# OrderSnapshot and CustomerSnapshot DTOs

## Overview
Consolidates TLC T13–T16. Adds checkout-relevant order and customer snapshot DTOs plus builders for use by CheckoutApplicationService and outbox payloads.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST define `OrderSnapshot`, `OrderLineSnapshot`, `OrderTotalSnapshot` in contracts — TLC T13.
2. MUST define `CustomerSnapshot`, `AddressSnapshot` (billing/delivery) in contracts — TLC T14.
3. MUST implement `OrderSnapshotBuilder` and `CustomerSnapshotBuilder` in sm-core — TLC T15.
4. MUST use snapshots in outbox JSON payload design (no entity graphs) — TLC T16.
5. MUST compile without JPA in contracts module.
</requirements>

## Subtasks
- [ ] 3.1 Order snapshot DTOs + tests (T13)
- [ ] 3.2 Customer snapshot DTOs + tests (T14)
- [ ] 3.3 Builders from `Order` / `Customer` entities (T15)
- [ ] 3.4 Document snapshot fields for outbox stages (T16)

## Implementation Details
See TechSpec: **Data models**. Reference `OrderFacadeImpl` and `OrderServiceImpl` for fields needed at checkout.

### Relevant Files
- `sm-core-model/.../order/Order.java`
- `sm-core-model/.../customer/Customer.java`
- `sm-shop-model/.../order/` — existing readable DTOs as field guide

### Dependent Files
- `shopizer-api-contracts/.../order/OrderSnapshot.java` — create
- `shopizer-api-contracts/.../customer/CustomerSnapshot.java` — create
- `sm-core/.../checkout/OrderSnapshotBuilder.java` — create

### Related ADRs
- [ADR-005: Outbox payload uses snapshots](../adrs/adr-005.md)

## Deliverables
- Order and customer snapshot DTOs
- Entity-to-snapshot builders
- Jackson round-trip tests **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] Order snapshot includes status, totals, line SKU/qty
  - [ ] Customer snapshot excludes lazy collections
  - [ ] Builders handle anonymous customer
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- Snapshots usable in CheckoutCommand design
