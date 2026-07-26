---
status: pending
title: Extract sm-order-core + cart totals service
type: backend
complexity: high
---

# Extract sm-order-core + cart totals service

## Overview
TLC T15–T17. New `sm-order-core` with order repositories, read services, and `CartTotalsService` (stateless totals from `CartTotalsRequest`). Move totals logic from task_02 boundary into order-core.

<requirements>
1. MUST scaffold sm-order-core with order repositories — T15.
2. MUST extract order read services (get/list/history) without PaymentService — T16, ORD-01–03.
3. MUST implement CartTotalsService with parity tests — T17, CART-03.
4. MUST keep tax lines as input on totals request (pre-computed optional) per ADR-006.
</requirements>

## Subtasks
- [ ] 6.1 Module + repositories (T15)
- [ ] 6.2 OrderReadService (T16)
- [ ] 6.3 CartTotalsService (T17)

## Related ADRs
- [ADR-006: Tax at BFF](adrs/adr-006.md)

## Deliverables
- `sm-order-core` module
- `OrderReadServiceTest`, `CartTotalsServiceTest` **(REQUIRED)**

## Tests
- `./mvnw test -pl sm-order-core`

## Success Criteria
- Totals parity vs legacy
- No PaymentService on read path
