---
status: pending
title: Hub decomposition — thin OrderFacade + bypass APIs
type: refactor
complexity: high
---

# Hub decomposition — thin OrderFacade + bypass APIs

## Overview
TLC T33–T36. Route OrderPaymentApi, OrderTotalApi, OrderShippingApi through CheckoutApplicationService; thin OrderFacadeImpl; consolidate duplicate OrderFacadeImpl packages.

<requirements>
1. MUST remove direct PaymentService from OrderPaymentApi — T33, HUB-02.
2. MUST route OrderTotalApi and OrderShippingApi through checkout — T34.
3. MUST reduce OrderFacadeImpl checkout sm-core injections to ≤4 — T35, HUB-01, HUB-03.
4. MUST consolidate v1 OrderFacadeImpl duplicate — T36, GAP-ORD-01.
</requirements>

## Related ADRs
- [ADR-005: Hub decomposition](adrs/adr-005.md)

## Deliverables
- Refactored hub facades and bypass APIs
- `OrderFacadeThinTest`, routing tests **(REQUIRED)**

## Success Criteria
- ArchUnit or static check: no PaymentService in OrderPaymentApi
- Characterization tests pass
