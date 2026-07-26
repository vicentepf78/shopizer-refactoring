---
status: pending
title: Contracts catalog/customer snapshots + Wave4 Strangler config
type: backend
complexity: medium
---

# Contracts catalog/customer snapshots + Wave4 Strangler config

## Overview
Consolidates TLC T1–T4. Delivers catalog/customer DTOs, `ProductSnapshot` v2, `CustomerSnapshot` v1, `CatalogServiceClient`, `CustomerServiceClient`, plus Wave4 Strangler profile/properties in sm-shop. **External prerequisite:** Onda 3 Execute complete (snapshots + value types in contracts).

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST verify Wave 3 `LanguageCode`, `MerchantStoreId` in contracts — TLC T1.
2. MUST add/migrate catalog DTOs + `ProductSnapshot` (schemaVersion default 2) + `CatalogServiceClient` — TLC T2.
3. MUST add/migrate customer DTOs + `CustomerSnapshot` (schemaVersion default 1) + `CustomerServiceClient` with `getSnapshot` — TLC T3.
4. MUST add profile `strangler-wave4`, properties `wave4.*.base-url`, `wave4.strangler.enabled`, RestTemplate + correlation interceptor, stub client impls — TLC T4.
5. MUST NOT import `com.salesmanager.core.model` in contracts.
6. MUST coexist with `wave1.*` and `wave2.*` properties.
7. MUST compile `shopizer-api-contracts` and pass `Wave4ClientConfigTest` in sm-shop.
</requirements>

## Subtasks
- [ ] 1.1 Verify Wave 3 value types (T1)
- [ ] 1.2 Catalog DTOs + ProductSnapshot + CatalogServiceClient (T2)
- [ ] 1.3 Customer DTOs + CustomerSnapshot + CustomerServiceClient (T3)
- [ ] 1.4 Wave4ClientConfig + properties + RestTemplate (T4)
- [ ] 1.5 Stub RestTemplate client impls + config tests

## Implementation Details
See TechSpec: **Key interfaces**, **Strangler configuration**, build order step 2–5. Reuse `Wave2ClientConfig` pattern. DTO sources: `sm-shop-model/.../catalog/`, `.../customer/`.

### Relevant Files
- `shopizer-api-contracts/` — target packages
- `sm-shop-model/.../model/catalog/`, `.../customer/` — DTO sources
- `sm-shop/.../api/v1/product/ProductApi.java` — frozen paths
- `sm-shop/.../api/v1/customer/CustomerApi.java`

### Dependent Files
- `shopizer-api-contracts/.../catalog/`, `.../customer/`, `.../client/`
- `sm-shop/.../strangler/config/Wave4ClientConfig.java`
- `sm-shop/src/main/resources/application-strangler-wave4.properties`

### Related ADRs
- [ADR-001](adrs/adr-001.md) — Onda 3 gate
- [ADR-003](adrs/adr-003.md) — ProductSnapshot v2
- [ADR-005](adrs/adr-005.md) — CustomerSnapshot

## Deliverables
- Contracts packages + 2 client interfaces
- Wave4 Strangler profile/properties
- Unit tests 80%+ on serializable DTOs **(REQUIRED)**
- `Wave4ClientConfigTest` **(REQUIRED)**

## Tests
- Unit: ProductSnapshot default schemaVersion 2; CustomerSnapshot default 1; no core.model imports
- Integration: `./mvnw compile -pl shopizer-api-contracts`; `./mvnw test -pl sm-shop -Dtest=Wave4ClientConfigTest`
- Coverage target: >=80%

## Success Criteria
- All tests passing
- Contracts compile isolated
- wave4.* coexists with wave1/2
- Onda 3 gate verified before start
