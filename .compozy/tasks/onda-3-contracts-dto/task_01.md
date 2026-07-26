---
status: pending
title: Tenant types and contracts foundation
type: backend
complexity: medium
---

# Tenant types and contracts foundation

## Overview
Consolidates TLC T1–T6. Introduces `MerchantStoreId`, `LanguageCode`, shared contract conventions, `TenantEntityBridge` stub, and ArchUnit baseline for contracts purity. **External prerequisite:** Wave 2 gate green.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST add `MerchantStoreId` and `LanguageCode` to `shopizer-api-contracts` with validation and Jackson support — TLC T1–T2.
2. MUST add `com.salesmanager.contracts.order` and `com.salesmanager.contracts.customer` package placeholders with `ShopEntity` conventions — TLC T3.
3. MUST create `TenantEntityBridge` interface in `sm-shop` to resolve store/lang entities from codes — TLC T4.
4. MUST add ArchUnit test `ContractsMustNotDependOnCoreModel` in `shopizer-api-contracts` or `sm-shop` — TLC T5.
5. MUST extend `AbstractDataPopulator` with tenant-primitive overload (backward compatible) — TLC T6.
6. MUST NOT introduce `com.salesmanager.core.model` imports in api-contracts.
7. MUST compile `./mvnw compile -pl shopizer-api-contracts,sm-shop-model,sm-shop -am`.
</requirements>

## Subtasks
- [ ] 1.1 Value types `MerchantStoreId`, `LanguageCode` + unit tests (T1–T2)
- [ ] 1.2 Package structure and shared serialization tests (T3)
- [ ] 1.3 `TenantEntityBridge` + default impl using existing services (T4)
- [ ] 1.4 ArchUnit contracts purity rule (T5)
- [ ] 1.5 `AbstractDataPopulator` overload (T6)

## Implementation Details
See TechSpec: **Key interfaces**, **Principles**. Reuse `ReadableLanguage` code patterns from reference contracts.

### Relevant Files
- `shopizer-api-contracts/src/main/java/com/salesmanager/contracts/` — target packages
- `sm-core/src/main/java/com/salesmanager/core/business/utils/AbstractDataPopulator.java`
- `sm-shop-model/src/main/java/com/salesmanager/shop/store/controller/` — facade consumers

### Dependent Files
- `shopizer-api-contracts/.../tenant/MerchantStoreId.java` — create
- `shopizer-api-contracts/.../tenant/LanguageCode.java` — create
- `sm-shop/.../tenant/TenantEntityBridge.java` — create
- `sm-shop/.../tenant/TenantEntityBridgeImpl.java` — create

### Related ADRs
- [ADR-003: Phased facade migration](../adrs/adr-003.md)
- [ADR-001: Monolith-only wave](../adrs/adr-001.md)

## Deliverables
- Tenant value types in api-contracts
- Bridge interface + implementation
- ArchUnit test for contracts module
- Populator overload
- Unit tests with 80%+ coverage on value types **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] `MerchantStoreId` rejects blank code
  - [ ] `LanguageCode` serializes/deserializes in JSON
  - [ ] Bridge returns store for valid code
- Integration tests:
  - [ ] `./mvnw test -pl shopizer-api-contracts -Dtest=*Tenant*`
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- Zero core.model imports in api-contracts
- Wave 2 gate verified before start
