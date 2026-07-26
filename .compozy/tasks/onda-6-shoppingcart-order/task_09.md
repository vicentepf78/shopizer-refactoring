---
status: pending
title: ORDER_OUTBOX schema + relay + saga commit endpoints
type: backend
complexity: high
---

# ORDER_OUTBOX schema + relay + saga commit endpoints

## Overview
TLC T22–T25, T61. Transactional outbox table; relay scheduler; `POST /internal/v1/checkout/commit` with idempotency; `PATCH` payment status. ArchUnit: no PaymentService on commit path.

<requirements>
1. MUST create ORDER_OUTBOX migration + repository — T22, CHK-03.
2. MUST implement outbox relay scheduler — T23, CHK-10.
3. MUST implement checkout commit with OrderPlaced outbox in same TX — T24, CHK-02, CHK-09.
4. MUST implement payment status update + OrderPaid/OrderCancelled events — T25.
5. MUST ArchUnit: no PaymentService in order-service commit package — T61.
</requirements>

## Related ADRs
- [ADR-003: Saga](adrs/adr-003.md)
- [ADR-004: Outbox](adrs/adr-004.md)

## Deliverables
- Outbox + saga internal APIs
- `CheckoutCommitIntegrationTest`, `OrderOutboxRelayTest`, ArchUnit test **(REQUIRED)**

## Success Criteria
- Idempotent commit verified
- Outbox relay publishes events
