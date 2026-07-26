---
status: pending
title: Facade P1 migration to tenant identifiers
type: refactor
complexity: high
---

# Facade P1 migration to tenant identifiers

## Overview
Consolidates TLC T30–T34. Migrates six P1 facade interfaces and implementations to use `MerchantStoreId` and `LanguageCode` per ADR-003.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST update interfaces: `OrderFacade`, `ShoppingCartFacade`, `SearchFacade`, `ShippingFacade`, `CategoryFacade`, `ProductCommonFacade` — TLC T30.
2. MUST update all implementations and HTTP adapters (Wave 2 search/content unaffected) — TLC T31–T32.
3. MUST fix compile errors in controllers calling facades (convert entity → tenant at boundary) — TLC T33.
4. MUST add ArchUnit `facades_no_new_entity_params` for sm-shop-model — TLC T34.
5. MUST use `TenantEntityBridge` inside implementations only.
</requirements>

## Subtasks
- [ ] 7.1 Interface signature updates (T30)
- [ ] 7.2 Implementation + bridge hydration (T31–T32)
- [ ] 7.3 Controller call-site fixes (T33)
- [ ] 7.4 ArchUnit facade rule (T34)

## Implementation Details
See TechSpec: **Facade migration (Phase 1)**. `MerchantStoreArgumentResolver` still provides entities to controllers — convert at facade call.

### Relevant Files
- `sm-shop-model/.../order/facade/v1/OrderFacade.java`
- `sm-shop-model/.../shoppingCart/facade/v1/ShoppingCartFacade.java`
- `sm-shop-model/.../search/facade/SearchFacade.java`
- `sm-shop/.../facade/*Impl.java`

### Dependent Files
- All P1 facade interfaces and implementations
- Controllers under `sm-shop/.../api/v1/order/`, `search/`, `shipping/`

### Related ADRs
- [ADR-003: Phased facade migration](../adrs/adr-003.md)

## Deliverables
- Six migrated facade interfaces + impls
- Compiling sm-shop with updated call sites
- ArchUnit test **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] Facade impl hydrates store via bridge
- Integration tests:
  - [ ] Existing facade tests compile and pass
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- B-001 partially resolved for P1 facades
- No REST path changes
