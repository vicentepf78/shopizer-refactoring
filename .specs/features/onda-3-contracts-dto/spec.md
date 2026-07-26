# Wave 3 — Contracts DTO + Checkout Application Service Specification

**Feature ID:** `onda-3-contracts-dto`  
**Phase:** Specify / Design (Execute blocked until tasks approved)  
**Complexity:** Large (cross-cutting monolith refactor, no new services)  
**Source:** [MIGRATION-MASTER-PLAN.md](../../../docs/decomposition/MIGRATION-MASTER-PLAN.md) § Onda 3  
**Prerequisite:** Wave 2 Execute complete (2026-07-26 gate revalidated)

---

## Problem Statement

Waves 1 and 2 extracted reference, tax, content, search, and merchant into deployable services with DTO contracts and Strangler BFF adapters. However, the monolith core remains tightly coupled:

- **Facade interfaces** pass JPA `MerchantStore` and `Language` (B-001) — 20+ interfaces, `AbstractDataPopulator` hard-wired.
- **ReferencesApi** still returns JPA `Language` / `Currency` entities (B-002).
- **Integration plugins** (`PaymentModule`, `ShippingQuoteModule`) accept full entity graphs — blocks integration-service (Wave 5).
- **Checkout hub** — `OrderFacadeImpl` injects 12+ services; `processOrder` is one transactional method with order↔payments cycle.
- **Search** uses interim `ProductIndexPayload` (AD-009); `SearchItem` still lives in `shopizer-commons` (OQ-06).

Wave 3 is the **contracts and internal refactoring wave** with **no new microservices**. It unblocks Waves 4–6 by establishing snapshot DTOs, tenant identifiers, integration DTO redesign, checkout application service, and outbox foundation.

---

## Goals

- [ ] `ProductSnapshot`, `OrderSnapshot`, `CustomerSnapshot` in `shopizer-api-contracts`
- [ ] `MerchantStoreId` / `LanguageCode` on P1 facade interfaces (6 facades)
- [ ] `PaymentModuleV2` / `ShippingQuoteModuleV2` with DTO contexts; legacy plugins via bridge
- [ ] `CheckoutApplicationService` extracted from `OrderFacadeImpl`
- [ ] `CHECKOUT_OUTBOX` + staged `processOrder` behind feature flag
- [ ] `SearchItem` migrated to api-contracts; Pact updated
- [ ] B-002 closed — ReferencesApi returns readable DTOs
- [ ] Facade migration plan for Waves 4–6 published
- [ ] `./mvnw clean install` green; zero new JPA in api-contracts

---

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| New Spring Boot services | AD-W3-001 / master plan |
| Docker Compose Wave 3 | No deployables |
| Catalog-service / customer-service extraction | Wave 4 |
| integration-service extraction | Wave 5 |
| order-service / shoppingcart-service | Wave 6 |
| Full distributed saga / Kafka | Wave 6+ |
| Migrate all 76 facades | Phased — P1 only in Wave 3 |
| Rewrite Stripe/PayPal/USPS plugins to V2 | Optional; bridge suffices |
| Database split per domain | AD-003 |
| Tax calculation extraction | AD-002 |
| MerchantStoreArgumentResolver rewrite (~450 refs) | Remains in BFF |
| Quick wins Mapper/Populator merge | Parallel |

---

## Requirements

### CTR — Contracts foundation

| ID | Requirement | Priority |
| ---- | ----------- | -------- |
| CTR-01 | `shopizer-api-contracts` SHALL NOT import `com.salesmanager.core.model` | P1 |
| CTR-02 | New snapshot DTOs SHALL be Jackson-serializable with stable field names for Pact | P1 |
| CTR-03 | Mappers/builders SHALL live in `sm-core` or `sm-shop`, not in contracts JAR | P1 |
| CTR-04 | Contracts module SHALL publish snapshot packages: `catalog`, `order`, `customer`, `tenant` | P1 |

### TNT — Tenant identifiers

| ID | Requirement | Priority |
| ---- | ----------- | -------- |
| TNT-01 | `MerchantStoreId` SHALL wrap non-blank store code with value equality | P1 |
| TNT-02 | `LanguageCode` SHALL wrap non-blank ISO language code | P1 |
| TNT-03 | `TenantEntityBridge` SHALL hydrate `MerchantStore`/`Language` in monolith only | P1 |
| TNT-04 | `AbstractDataPopulator` SHALL accept tenant primitives via overload | P1 |
| TNT-05 | ArchUnit SHALL fail if new facade methods add `MerchantStore`/`Language` params | P2 |
| TNT-06 | Controllers MAY still resolve entities via argument resolver; conversion at facade boundary | P1 |

### SNP — Snapshots

| ID | Requirement | Priority |
| ---- | ----------- | -------- |
| SNP-01 | `ProductSnapshot` SHALL be canonical catalog read projection | P1 |
| SNP-02 | `ProductIndexPayload` SHALL map from `ProductSnapshot` with schemaVersion 2 | P1 |
| SNP-03 | `ProductSnapshotBuilder` SHALL build from JPA `Product` without exposing entities in contracts | P1 |
| SNP-04 | `OrderSnapshot` SHALL include status, totals, line items as nested DTOs | P1 |
| SNP-05 | `CustomerSnapshot` SHALL include id, email, billing/delivery address DTOs | P1 |
| SNP-06 | `search-service` SHALL accept index payload schema v1 and v2 | P1 |
| SNP-07 | Snapshot builders SHALL have unit tests with fixture entities | P1 |

### INT — Integration modules

| ID | Requirement | Priority |
| ---- | ----------- | -------- |
| INT-01 | Integration DTOs in `sm-core-modules` SHALL NOT reference JPA entity types | P1 |
| INT-02 | `PaymentModuleV2` SHALL use `PaymentRequestContext` and related DTOs | P1 |
| INT-03 | `ShippingQuoteModuleV2` SHALL use `ShippingQuoteRequestContext` | P1 |
| INT-04 | Legacy V1 plugins SHALL continue working without source changes | P1 |
| INT-05 | `PaymentServiceImpl` SHALL route to V2 when plugin supports it or via bridge | P1 |
| INT-06 | At least one plugin path SHALL be integration-tested via V2 bridge | P1 |

### CHK — Checkout application service

| ID | Requirement | Priority |
| ---- | ----------- | -------- |
| CHK-01 | `CheckoutApplicationService.placeOrder` SHALL be single orchestration entry | P1 |
| CHK-02 | Public order REST paths and schemas SHALL remain unchanged | P1 |
| CHK-03 | `OrderFacadeImpl` SHALL delegate orchestration to application service | P1 |
| CHK-04 | Happy-path order placement SHALL match pre-refactor behavior | P1 |
| CHK-05 | Known validation/payment error paths SHALL match pre-refactor behavior | P1 |
| CHK-06 | Checkout p95 latency SHALL NOT exceed 2× baseline in integration env | P2 |

### SAG — Saga / outbox foundation

| ID | Requirement | Priority |
| ---- | ----------- | -------- |
| SAG-01 | `CHECKOUT_OUTBOX` table SHALL exist in shared schema | P1 |
| SAG-02 | Outbox events SHALL include PAYMENT_REQUESTED, PAYMENT_CONFIRMED, ORDER_PERSISTED, INVENTORY_DECREMENTED | P1 |
| SAG-03 | Outbox write SHALL occur in same transaction as business step when flag enabled | P1 |
| SAG-04 | `checkout.outbox.enabled` SHALL default false | P1 |
| SAG-05 | In-process dispatcher SHALL mark events processed (no external broker) | P1 |

### FAC — Facade migration

| ID | Requirement | Priority |
| ---- | ----------- | -------- |
| FAC-01 | `OrderFacade` SHALL use tenant identifier types | P1 |
| FAC-02 | `ShoppingCartFacade`, `SearchFacade`, `ShippingFacade` SHALL use tenant types | P1 |
| FAC-03 | `CategoryFacade`, `ProductCommonFacade` read paths SHALL use tenant types | P1 |
| FAC-04 | Wave 2 HTTP adapters SHALL compile with updated facade signatures | P1 |
| FAC-05 | Remaining facades SHALL be inventoried with Wave 4–6 phase assignment | P2 |
| FAC-06 | `FACADE-MIGRATION-PLAN.md` SHALL document phased migration | P2 |

### SRCH — Search contracts

| ID | Requirement | Priority |
| ---- | ----------- | -------- |
| SRCH-01 | `SearchItem` SHALL live in `shopizer-api-contracts` | P2 |
| SRCH-02 | `search-service` and `sm-shop` SHALL import SearchItem from contracts | P2 |
| SRCH-03 | Pact tests SHALL use contracts SearchItem | P2 |
| SRCH-04 | JSON field names SHALL remain compatible with Wave 2 Pact | P2 |

### REF — References API

| ID | Requirement | Priority |
| ---- | ----------- | -------- |
| REF-01 | Language list endpoints SHALL return `ReadableLanguage` DTOs | P1 |
| REF-02 | Currency list endpoints SHALL return `ReadableCurrency` DTOs | P1 |

### GAT — Gates

| ID | Requirement | Priority |
| ---- | ----------- | -------- |
| GAT-01 | `./mvnw clean install` SHALL pass on completion | P1 |
| GAT-02 | Wave 1+2 Pact suites SHALL remain green | P1 |
| GAT-03 | `.specs/project/STATE.md` SHALL be updated on Wave 3 completion | P2 |

**Requirement count: 49 IDs** (CTR-04, TNT-06, SNP-07, INT-06, CHK-06, FAC-06, GAT-03 included)

---

## User Stories (summary)

### P1 — Platform: snapshot contracts (SNP, CTR)

As a platform engineer, I need versioned snapshot DTOs so cross-service reads do not require `sm-core-model` on consumer classpath.

### P1 — Platform: tenant types (TNT, FAC)

As a platform engineer, I need facade interfaces to accept store/lang codes so future HTTP adapters do not leak JPA types (B-001 partial resolution).

### P1 — Platform: integration DTOs (INT)

As a platform engineer, I need payment/shipping plugins to accept DTOs so Wave 5 integration-service extraction is viable.

### P1 — Visitor: checkout parity (CHK)

As a storefront visitor, checkout must behave exactly as today while orchestration moves to an application service.

### P1 — Platform: outbox foundation (SAG)

As a platform engineer, I need staged processOrder with outbox rows so Wave 6 can split order and payments.

### P1 — API consumer: reference DTOs (REF)

As an API consumer, language/currency endpoints must return DTOs (B-002).

### P2 — Search maintainer (SRCH)

As search-service maintainer, SearchItem must be in api-contracts (OQ-06).

---

## Phasing

### Phase 1 (MVP)

CTR, TNT, SNP, INT, CHK, SAG (flag off), REF, P1 FAC

### Phase 2

SRCH migration, FAC-06 plan, outbox enabled in test profile

### Phase 3

ArchUnit enforcement, STATE update, Wave 4 Specify unblocked

---

## Traceability

| PRD section | Requirement IDs |
| ----------- | --------------- |
| Snapshot contracts | SNP-01..07, CTR-01..04 |
| Tenant identifiers | TNT-01..06, FAC-01..04 |
| Integration DTOs | INT-01..06 |
| Checkout service | CHK-01..06 |
| Outbox | SAG-01..05 |
| SearchItem | SRCH-01..04 |
| References | REF-01..02 |
| Gates | GAT-01..03 |

Compozy workflow: `.compozy/tasks/onda-3-contracts-dto/`
