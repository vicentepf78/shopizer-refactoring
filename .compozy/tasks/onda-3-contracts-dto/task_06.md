---
status: pending
title: PaymentModuleV2, ShippingQuoteModuleV2 and bridges
type: backend
complexity: high
---

# PaymentModuleV2, ShippingQuoteModuleV2 and bridges

## Overview
Consolidates TLC T25–T29. Introduces V2 plugin interfaces, entity→DTO mappers in Payment/Shipping services, and legacy bridge for existing plugins.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST define `PaymentModuleV2` and `ShippingQuoteModuleV2` interfaces — TLC T25.
2. MUST implement `LegacyPaymentModuleBridge` wrapping V1 plugins as V2 — TLC T26.
3. MUST update `PaymentServiceImpl` to prefer V2 when available — TLC T27.
4. MUST update `ShippingServiceImpl` similarly — TLC T28.
5. MUST add integration test with simplest plugin (e.g. MoneyOrder) via V2 path — TLC T29.
6. MUST NOT break existing V1 plugin registration.
</requirements>

## Subtasks
- [ ] 6.1 V2 interface definitions (T25)
- [ ] 6.2 Legacy bridges V1→V2 (T26)
- [ ] 6.3 PaymentServiceImpl routing (T27)
- [ ] 6.4 ShippingServiceImpl routing (T28)
- [ ] 6.5 Plugin path integration test (T29)

## Implementation Details
See TechSpec: **Key interfaces**, ADR-004. Registry pattern: `Map<String, PaymentModule>` unchanged; add optional V2 map or adapter wrapper.

### Relevant Files
- `sm-core/.../payments/PaymentServiceImpl.java`
- `sm-core/.../shipping/ShippingServiceImpl.java`
- `sm-core/.../modules/integration/payment/impl/MoneyOrderPayment.java`

### Dependent Files
- `sm-core-modules/.../PaymentModuleV2.java` — create
- `sm-core/.../payments/LegacyPaymentModuleBridge.java` — create
- `sm-core/.../shipping/LegacyShippingQuoteModuleBridge.java` — create

### Related ADRs
- [ADR-004: Parallel V2 interfaces](../adrs/adr-004.md)

## Deliverables
- V2 interfaces + legacy bridges
- Service layer routing
- Integration test proving V2 authorize path **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] Bridge maps entity cart items to PaymentLineItemDto
- Integration tests:
  - [ ] MoneyOrder authorize via V2 bridge
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- INT-ready milestone
- V1 plugins still pass existing tests
