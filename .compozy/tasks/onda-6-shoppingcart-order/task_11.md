---
status: pending
title: CheckoutApplicationService orchestration + tax at BFF (CHK-ready partial)
type: backend
complexity: high
---

# CheckoutApplicationService orchestration + tax at BFF (CHK-ready partial)

## Overview
TLC T28–T31, T57, T58. Full `CheckoutApplicationService` in sm-shop: tax via tax-service (ADR-006), integration-service payment/shipping, saga steps, OrderApi routing.

<requirements>
1. MUST implement CheckoutApplicationService with all Wave6 clients — T28, CHK-01, HUB-01.
2. MUST compute tax at BFF via tax-service; pass taxItems in OrderSnapshot — T29, CHK-08, ADR-006.
3. MUST implement placeOrder() full saga steps 1–8 — T30, CHK-01–06.
4. MUST route OrderApi checkout through service when saga flag on — T31.
5. MUST wire catalog inventory validation — T57; email outbox consumer — T58.
</requirements>

## Related ADRs
- [ADR-002: Checkout boundary](adrs/adr-002.md)
- [ADR-006: Tax at BFF](adrs/adr-006.md)

## Deliverables
- CheckoutApplicationService complete
- `CheckoutPlaceOrderIntegrationTest`, `CheckoutTaxIntegrationTest` **(REQUIRED)**

## Success Criteria
- E2E happy path in integration test
- order-service does not call TaxService
