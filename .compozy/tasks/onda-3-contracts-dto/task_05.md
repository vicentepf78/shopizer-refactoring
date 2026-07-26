---
status: pending
title: Integration payment and shipping DTOs
type: backend
complexity: high
---

# Integration payment and shipping DTOs

## Overview
Consolidates TLC T21–T24. Adds integration context DTOs to `sm-core-modules` without JPA types, preparing PaymentModuleV2 and ShippingQuoteModuleV2.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST add `IntegrationStoreContext` with store code, currency, default language — TLC T21.
2. MUST add `PaymentRequestContext`, `PaymentCaptureContext`, `PaymentLineItemDto`, `TransactionResult` — TLC T22.
3. MUST add `ShippingQuoteRequestContext`, `ShippingAddressDto`, `PackageDetailsDto` — TLC T23.
4. MUST use `MerchantStoreId` from api-contracts (sm-core-modules depends on contracts) — TLC T24.
5. MUST NOT reference `Order`, `Customer`, `ShoppingCartItem` entities in new DTOs.
</requirements>

## Subtasks
- [ ] 5.1 Common integration context DTOs (T21)
- [ ] 5.2 Payment context DTOs (T22)
- [ ] 5.3 Shipping context DTOs (T23)
- [ ] 5.4 Maven dependency contracts → sm-core-modules (T24)

## Implementation Details
See TechSpec: **Data models**, ADR-004. Map fields from current `PaymentModule` method signatures.

### Relevant Files
- `sm-core-modules/.../payment/model/PaymentModule.java`
- `sm-core-modules/.../shipping/model/ShippingQuoteModule.java`
- `sm-core/.../payments/PaymentServiceImpl.java`

### Dependent Files
- `sm-core-modules/.../integration/common/dto/` — create
- `sm-core-modules/.../integration/payment/dto/` — create
- `sm-core-modules/.../integration/shipping/dto/` — create

### Related ADRs
- [ADR-004: V2 parallel interfaces](../adrs/adr-004.md)

## Deliverables
- Integration DTO packages in sm-core-modules
- Serialization unit tests
- `./mvnw compile -pl sm-core-modules` green **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] PaymentRequestContext round-trip JSON
  - [ ] ShippingQuoteRequestContext holds delivery/origin DTOs
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- DTOs cover all V2 method parameter needs
- No JPA imports in new DTOs
