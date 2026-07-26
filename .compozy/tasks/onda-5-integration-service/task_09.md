---
status: pending
title: Checkout + OrderShipping HTTP wiring
type: backend
complexity: high
---

# Checkout + OrderShipping HTTP wiring

## Overview
Consolidates TLC T27–T28. Wires `OrderPaymentApi` through `CheckoutApplicationService` → `IntegrationServiceClient`; wires `OrderShippingApi` to build `ShippingQuoteRequest` from cart + catalog snapshots.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- Depends on task_07 stateless boundary and task_08 adapters
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST route `OrderPaymentApi` payment processing through checkout service — T27, STR-03.
2. MUST build `PaymentProcessRequest` from order/cart/customer snapshots — PAY-07.
3. MUST route `OrderShippingApi` through integration client with DTO assembly — T28, STR-04.
4. MUST preserve response schemas (`ReadableTransaction`, `ReadableShippingSummary`).
5. MUST E2E test checkout payment with mock integration-service — T27.
</requirements>

## Subtasks
- [ ] 9.1 CheckoutApplicationService integration client injection (T27)
- [ ] 9.2 OrderPaymentApi rewire (T27)
- [ ] 9.3 OrderShippingApi DTO builder (T28)
- [ ] 9.4 E2E tests with TestRestTemplate or WireMock

## Related ADRs
- [ADR-006](adrs/adr-006.md)
- [ADR-002](adrs/adr-002.md)

## Deliverables
- Checkout + OrderShipping wiring
- `CheckoutPaymentE2ETest` **(REQUIRED)**
- `OrderShippingQuoteE2ETest` **(REQUIRED)**

## Success Criteria
- Payment flow updates order only in checkout saga, not integration client path
- Shipping quote returns options in strangler profile
