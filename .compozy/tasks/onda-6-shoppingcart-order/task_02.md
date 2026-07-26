---
status: pending
title: Cart totals API — break cart↔order cycle (TOT-ready)
type: backend
complexity: high
---

# Cart totals API — break cart↔order cycle (TOT-ready)

## Overview
TLC T6, T56. Extract `calculateShoppingCartTotal` to `CartTotalsService`; expose `POST /internal/v1/orders/totals`; wire `ShoppingCartCalculationServiceImpl` to HTTP when `wave6.totals.http.enabled=true`. Milestone **TOT-ready**.

<requirements>
1. MUST break in-process call `ShoppingCartCalculationServiceImpl` → `OrderService.calculateShoppingCartTotal` when flag on — CART-03, OQ-01.
2. MUST accept `CartTotalsRequest` and return `CartTotalsResponse` matching `OrderTotalSummary` semantics.
3. MUST preserve byte-parity with legacy totals in integration test.
4. MUST protect internal endpoint with `X-Internal-Token`.
5. MUST mark TOT-ready in STATE.md when complete.
</requirements>

## Subtasks
- [ ] 2.1 Extract `CartTotalsService` from OrderServiceImpl logic (T6)
- [ ] 2.2 Internal `CartTotalsController` in sm-shop or sm-order-core (T6)
- [ ] 2.3 HTTP client wiring in ShoppingCartCalculationServiceImpl (T6)
- [ ] 2.4 Property `wave6.totals.http.enabled` + tests (T56)

## Implementation Details
Source: `sm-core/.../shoppingcart/ShoppingCartCalculationServiceImpl.java` line 73; `OrderServiceImpl.calculateShoppingCartTotal`.

### Relevant Files
- `sm-core/.../order/OrderServiceImpl.java`
- `sm-core/.../shoppingcart/ShoppingCartCalculationServiceImpl.java`

### Related ADRs
- [ADR-007: Cart before order phasing](adrs/adr-007.md)

## Deliverables
- CartTotalsService + internal API + flag wiring
- `CartTotalsParityTest` **(REQUIRED)**

## Tests
- Integration: `./mvnw test -pl sm-shop -Dtest=CartTotalsParityTest`

## Success Criteria
- Parity test green with flag on/off
- TOT-ready milestone recorded
