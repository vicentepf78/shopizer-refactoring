# Onda 4 — Catalog + Customer Specification

**Feature ID:** `onda-4-catalog-customer`
**Phase:** Specify → Design → Tasks (Execute blocked until Onda 3 complete)
**Complexity:** Large (2 deployable services + Strangler + contract migration)
**Source:** [MIGRATION-MASTER-PLAN.md](../../../docs/decomposition/MIGRATION-MASTER-PLAN.md) § Onda 4
**Prerequisite:** Onda 3 — `ProductSnapshot`, `CustomerSnapshot`, `LanguageCode`, `MerchantStoreId`, Checkout Application Service foundations

---

## Problem Statement

Catalog is the **highest afferent-coupling domain** in `sm-core` (10 inbound service references: order, cart, shipping, search, merchant, reference, and others). Extracting catalog CRUD wholesale would drag half the monolith into the first cut. Customer is comparatively isolated at the service layer (score 5/10) but is **transactionally coupled to order** (customer creation during checkout) and to **shopping cart merge** (session cart + authenticated customer cart in one DB transaction).

Wave 3 delivers cross-service DTOs (`ProductSnapshot`, `CustomerSnapshot`) and tenant primitives (`LanguageCode`, `MerchantStoreId`) that unblock read-path extraction. Without a formal Wave 4 spec, teams either:

- Extract catalog writes too early and recreate the upstream blocker graph remotely, or
- Delay customer extraction indefinitely because cart merge cannot be untangled.

This spec defines **phased catalog read extraction** and **customer profile extraction with explicit cart-merge decoupling**, following the Strangler pattern proven in Waves 1–2.

---

## Goals

- [ ] `catalog-service` deployable as Spring Boot application serving **storefront and public read APIs** for products, categories, manufacturers, inventory, and pricing
- [ ] `customer-service` deployable serving **profile, addresses, opt-in, and review read/write** APIs (excluding login/checkout orchestration)
- [ ] Monolith remains **write authority** for admin catalog mutations (private product CRUD) until a later wave
- [ ] Monolith consumes both services via HTTP Strangler on **frozen REST paths**
- [ ] Zero JPA entities in JSON responses from migrated endpoints (Onda 1 criterion)
- [ ] `ProductIndexPayload` migrates to **`ProductSnapshot`** for search indexing (Wave 3 contract)
- [ ] Cart merge decoupled: monolith orchestrates merge using **`CustomerSnapshot`** from customer-service
- [ ] Pact tests for P1 catalog read + customer profile surfaces
- [ ] Reuse `shopizer-api-contracts`, thin cores (`sm-catalog-core`, `sm-customer-core`), RestTemplate, JWT replication

---

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Admin catalog write APIs in `catalog-service` | Master plan: write stays in monolith temporarily; score 7/10 full extraction |
| `shoppingcart-service` / cart persistence extraction | Onda 6; cart merge only **decoupled**, not extracted |
| `order-service`, checkout, `processOrder` saga | Onda 6; Checkout Application Service from Onda 3 stays monolith |
| `ProductTypeApi` admin CRUD in catalog-service | Deferred — read-only product-type listing MAY be included; mutations stay monolith |
| Customer authentication (`AuthenticateCustomerApi`, JWT issuance) | Login authority remains `sm-shop` (AD-006 pattern) |
| Database split per service | AD-003 / AD-022 — shared `SALESMANAGER` schema |
| Full Mapper/Populator consolidation (4 product facades) | Fase 1 quick wins; parallel, not blocking |
| `PaymentModule` / `ShippingQuoteModule` DTO redesign | Onda 5 integration service |
| Greenfield `InitializationDatabaseImpl` move | AD-004 — bootstrap stays monolith |
| Pricing engine / promotion rules redesign | Read pricing only; rules stay in monolith services |

---

## User Stories

### P1: Catalog Service — storefront product & category reads ⭐ MVP

**User Story**: As a storefront visitor, I want to browse products and categories via existing public APIs, so product discovery does not depend on in-process catalog services in the monolith.

**Why P1**: Catalog read path is the highest-traffic, highest-coupling surface that Wave 3 `ProductSnapshot` unlocks without moving writes.

**Acceptance Criteria**:

1. WHEN `GET /api/v1/products/**` (public list, by id, by sku, related, group) THEN `catalog-service` SHALL return `ReadableProduct` / list DTOs — SHALL NOT expose entity `Product`
2. WHEN `GET /api/v1/category/**` (tree, by id, product counts) THEN `catalog-service` SHALL return `ReadableCategory` DTOs localized by `lang`
3. WHEN `GET /api/v1/products/inventory/**` or price endpoints (public read) THEN `catalog-service` SHALL return inventory/price DTOs without cross-call to order/cart
4. WHEN tenant context is required THEN `catalog-service` SHALL accept `store` code and resolve via `MerchantStoreId` / reference to merchant-service HTTP (Wave 2) — SHALL NOT inject `MerchantStore` entity
5. WHEN `lang` is required THEN `catalog-service` SHALL resolve `LanguageCode` via `reference-service` HTTP
6. WHEN product not found THEN SHALL return HTTP 404 with same error envelope as monolith
7. WHEN `catalog-service` unavailable and strangler enabled THEN BFF SHALL return HTTP 503 with correlation id — no silent in-process fallback

**Independent Test**: Deploy `catalog-service` + Wave 1–2 deps; `GET /api/v1/products?store=DEFAULT&lang=en` returns paginated products; category tree matches monolith baseline.

**Source components:**

| Role | Path |
| ---- | ---- |
| Entities | `sm-core-model/.../catalog/` |
| Services | `sm-core/.../services/catalog/product/`, `category/`, `inventory/`, `pricing/`, `manufacturer/` |
| APIs | `sm-shop/.../api/v1/product/ProductApi.java`, `CategoryApi.java`, `ProductInventoryApi.java`, `ProductPriceApi.java`, `ProductManufacturerApi.java`, `ProductGroupApi.java` |
| Facades | `ProductFacadeImpl`, `ProductCommonFacadeImpl`, `CategoryFacadeImpl` |
| DTOs | `sm-shop-model/.../model/catalog/` |

**Explicitly OUT of this story:** `POST/PUT/DELETE` private product admin APIs — remain monolith through Wave 4.

---

### P1: Catalog Service — internal ProductSnapshot API ⭐ MVP

**User Story**: As platform engineer, I want a versioned `ProductSnapshot` HTTP API inside catalog-service, so search indexing and cross-service reads use one canonical contract from Wave 3.

**Acceptance Criteria**:

1. WHEN `GET /internal/v1/products/{id}/snapshot?store=&lang=` with network policy THEN SHALL return `ProductSnapshot` with `schemaVersion`
2. WHEN `schemaVersion` requested is unsupported THEN SHALL return HTTP 422
3. WHEN monolith `ProductSnapshotBuilder` (replaces `ProductIndexPayloadBuilder`) builds index document THEN MAY call internal snapshot API OR build in-process during transition — Design picks single builder owner
4. WHEN `search-service` receives snapshot for indexing THEN SHALL accept `ProductSnapshot` schema v2 (supersedes `ProductIndexPayload` v1)

**Requirement IDs:** CAT-08, CAT-09, STR-07

---

### P1: Customer Service — profile, addresses, opt-in ⭐ MVP

**User Story**: As a registered customer, I want to manage my profile, shipping/billing addresses, and marketing opt-in via existing APIs, without the monolith owning customer persistence for those operations.

**Acceptance Criteria**:

1. WHEN `GET /api/v1/customer/profile` (authenticated) THEN `customer-service` SHALL return `ReadableCustomer` — no `Customer` entity in JSON
2. WHEN `PUT /api/v1/customer/profile` or address endpoints THEN SHALL mutate customer rows with store scoping equivalent to monolith
3. WHEN `POST /api/v1/customer/optin` THEN SHALL persist opt-in via `CustomerOptinService` logic in `sm-customer-core`
4. WHEN country/zone/language resolution needed THEN SHALL call `reference-service` HTTP
5. WHEN `POST /api/v1/customer` (registration body) is called on **private admin** paths THEN MAY remain monolith OR delegate per OQ-06 — **public self-registration stays monolith**
6. WHEN customer not found THEN HTTP 404; unauthorized THEN 401/403 per existing security rules

**Independent Test**: JWT customer token; update profile; add address; verify DB row; opt-in recorded.

**Source components:**

| Role | Path |
| ---- | ---- |
| Entities | `sm-core-model/.../customer/` |
| Services | `sm-core/.../services/customer/`, `optin/`, `attribute/` |
| APIs | `sm-shop/.../api/v1/customer/CustomerApi.java`, `CustomerNewsletterApi.java`, review APIs (read path) |
| Facade | `CustomerFacadeImpl` |
| DTOs | `sm-shop-model/.../model/customer/` |

---

### P1: Cart merge decoupling ⭐ MVP

**User Story**: As a returning customer, I want my session cart merged with my saved cart on login, without `customer-service` participating in the shopping-cart database transaction.

**Why P1**: Master plan explicitly calls out "decouple cart merge" as Wave 4 customer deliverable.

**Acceptance Criteria**:

1. WHEN login succeeds in monolith THEN `CustomerFacade` SHALL obtain `CustomerSnapshot` from `customer-service` (or cache) before calling `ShoppingCartService.mergeShoppingCarts`
2. WHEN merge executes THEN `ShoppingCartService` SHALL NOT require in-process `Customer` entity from `CustomerService` — uses snapshot id + store code
3. WHEN `customer-service` is down during merge THEN monolith SHALL fail merge with clear error OR documented degrade path (Design: fail closed preferred)
4. WHEN order checkout creates customer in same transaction (today) THEN behavior unchanged in Wave 4 — order coupling deferred to Onda 6

**Requirement IDs:** CUS-08, CUS-09, STR-08

---

### P1: Strangler BFF — catalog + customer HTTP adapters ⭐ MVP

**User Story**: As platform engineer, I want feature-flagged HTTP adapters for catalog and customer facades, to validate extraction without rewriting controllers.

**Acceptance Criteria**:

1. WHEN `wave4.strangler.enabled=true` THEN public product/category/manufacturer/inventory/price GET adapters SHALL delegate to `catalog-service`
2. WHEN strangler enabled THEN customer profile/address/optin adapters SHALL delegate to `customer-service`
3. WHEN strangler disabled THEN in-process facades SHALL behave as today
4. WHEN private admin product **write** APIs invoked THEN SHALL remain in-process monolith (no catalog-service delegation)
5. WHEN remote failure THEN HTTP 503 + `X-Correlation-Id` — no silent fallback
6. JWT forwarded on private customer routes per Wave 1–2 pattern

**Requirement IDs:** STR-01…STR-06

---

### P2: Product images via content-service

**User Story**: As store admin, I want product/variant/option images uploaded through existing flows, with blobs owned by content-service (Onda 2 deferral OQ-02).

**Acceptance Criteria**:

1. WHEN `ProductOptionFacadeImpl` / variant group uploads image THEN monolith SHALL use `ContentServiceClient` (already Wave 2) with `FileContentType` PRODUCT/VARIANT/PROPERTY
2. WHEN `catalog-service` serves product read DTOs THEN image URLs SHALL remain consistent with `LocationImageConfig` / static proxy semantics
3. WHEN `/static/products/**` requested THEN monolith `StaticContentProxy` extended OR catalog-service redirect — Design detail

**Requirement IDs:** CAT-10, CNT-W4-01

---

### P2: Pact contract tests — Wave 4

**User Story**: As developer, I want Pact coverage for catalog read and customer profile endpoints, so contract drift fails CI before deploy.

**Acceptance Criteria**:

1. WHEN `ProductSnapshot` or `ReadableProduct` schema breaks THEN consumer pact SHALL fail
2. WHEN gate Full runs THEN SHALL include `Wave4ConsumerPactTest` + provider tests on both services
3. WHEN `schemaVersion` increments THEN pact fixtures SHALL pin supported versions

**Requirement IDs:** STR-02, CAT-11, CUS-10

---

### P3: Observability — Wave 4

**User Story**: As operator, I want health checks and correlation IDs on catalog-service and customer-service.

**Acceptance Criteria**:

1. WHEN `GET /actuator/health` THEN each service reports DB + HTTP deps (reference, merchant for catalog)
2. WHEN request processed THEN `X-Correlation-Id` propagated per Waves 1–2
3. WHEN catalog read p95 exceeds 2× monolith baseline THEN document tuning (connection pool, snapshot cache)

**Requirement IDs:** STR-05

---

## Edge Cases

### Catalog

- WHEN product has variants without inventory THEN read DTO SHALL match monolith empty-inventory semantics
- WHEN category tree depth exceeds pagination THEN SHALL preserve lazy-load behavior of `CategoryFacadeImpl`
- WHEN `ProductFacadeV2` and `ProductFacadeImpl` diverge THEN strangler adapter MUST target **V1 public paths first**; V2 delegation documented in Design
- WHEN only price changes (no product save event) THEN search index MAY be stale — accept per GAP-SRCH; snapshot reindex is monolith producer responsibility
- WHEN digital product file metadata requested on public API THEN SHALL NOT expose download tokens without auth — preserve monolith rules

### Customer

- WHEN duplicate email per store THEN registration conflict semantics unchanged (monolith auth path)
- WHEN address `stateProvince` populator bug (quick win) THEN fix MAY land in Wave 4 if trivial — not required for gate
- WHEN customer review POST remains monolith THEN `customer-service` MAY expose read-only reviews initially — Design phases write path
- WHEN merge races (two tabs login) THEN merge idempotency follows existing `ShoppingCartService` behavior

### Cross-cutting

- WHEN `reference-service` or `merchant-service` down THEN catalog/customer SHALL 503 on routes needing resolution
- WHEN shared DB migration runs THEN coordinate ownership — catalog/customer services use same tables as monolith writers temporarily

---

## Requirement Traceability

| Requirement ID | Story | Summary | Phase | Status |
| -------------- | ----- | ------- | ----- | ------ |
| CAT-01 | P1 Catalog | `catalog-service` deployable Spring Boot | Execute | Planned |
| CAT-02 | P1 Catalog | Public product read APIs frozen paths | Execute | Planned |
| CAT-03 | P1 Catalog | Category read APIs | Execute | Planned |
| CAT-04 | P1 Catalog | Manufacturer + product group read | Execute | Planned |
| CAT-05 | P1 Catalog | Inventory/price public read | Execute | Planned |
| CAT-06 | P1 Catalog | Zero JPA in JSON responses | Execute | Planned |
| CAT-07 | P1 Catalog | reference + merchant HTTP for tenant/lang | Execute | Planned |
| CAT-08 | P1 Snapshot | Internal `ProductSnapshot` API | Execute | Planned |
| CAT-09 | P1 Snapshot | Search index uses ProductSnapshot v2 | Execute | Planned |
| CAT-10 | P2 Images | Product images via content-service | Execute | Planned |
| CAT-11 | P2 Pact | Catalog provider pact | Execute | Planned |
| CAT-12 | P1 Core | `sm-catalog-core` read services extracted | Execute | Planned |
| CUS-01 | P1 Customer | `customer-service` deployable | Execute | Planned |
| CUS-02 | P1 Customer | Profile read/update | Execute | Planned |
| CUS-03 | P1 Customer | Address CRUD | Execute | Planned |
| CUS-04 | P1 Customer | Opt-in endpoints | Execute | Planned |
| CUS-05 | P1 Customer | Zero JPA in JSON | Execute | Planned |
| CUS-06 | P1 Customer | reference HTTP for geo/lang | Execute | Planned |
| CUS-07 | P1 Customer | Reviews read (write optional P2) | Execute | Planned |
| CUS-08 | P1 Merge | CustomerSnapshot for cart merge | Execute | Planned |
| CUS-09 | P1 Merge | ShoppingCart merge without in-process CustomerService | Execute | Planned |
| CUS-10 | P2 Pact | Customer provider pact | Execute | Planned |
| STR-01 | P1 Strangler | `wave4.strangler.enabled` feature flag | Execute | Planned |
| STR-02 | P2 Pact | Wave4 consumer pact in sm-shop | Execute | Planned |
| STR-03 | AD-022 | Shared DB schema | Execute | Planned |
| STR-04 | P1 | Frozen REST paths | Execute | Planned |
| STR-05 | P3 | Actuator health + correlation | Execute | Planned |
| STR-06 | P1 | catalog→reference/merchant; customer→reference | Execute | Planned |
| STR-07 | P1 | ProductSnapshot builder migration | Execute | Planned |
| STR-08 | P1 | Cart merge orchestration in monolith | Execute | Planned |

**Coverage:** 30 total, 30 mapped, 0 unmapped

---

## Open Questions — Resolved ✅

See [context.md](./context.md) and [design.md](./design.md).

| ID | Decision |
|----|----------|
| OQ-01 | Catalog read-first; writes in monolith |
| OQ-02 | ProductSnapshot canonical |
| OQ-03 | CustomerSnapshot + monolith merge orchestration |
| OQ-04 | Product images via content-service |
| OQ-05 | Strangler on V1 paths; V2 delegates same adapter |
| OQ-06 | Auth endpoints stay monolith |

---

## Success Criteria

- [ ] `catalog-service` and `customer-service` pass health check and all P1 endpoints in integration
- [ ] Strangler produces equivalent responses to in-process baseline (pact green)
- [ ] No migrated endpoint returns JPA entity types in JSON
- [ ] Search indexing accepts `ProductSnapshot` v2 from monolith producer
- [ ] Cart merge integration test passes with customer-service remote
- [ ] Admin product **write** APIs still function in monolith only
- [ ] `./mvnw clean install` reactor green with Wave 4 modules
- [ ] Pattern documented in STATE.md for Onda 5
- [ ] Public read p95 ≤ 2× monolith baseline

---

## Appendix A — Coupling scores (master plan)

| Domain | Difficulty | Afferent | Wave 4 approach |
|--------|------------|----------|-----------------|
| catalog | 7/10 | 10 refs | Read API extraction only |
| customer | 5/10 | order creates customer | Profile extraction; order txn deferred |

---

## Appendix B — Key source files

### Catalog

| Role | Path |
|------|------|
| Product service | `sm-core/.../catalog/product/ProductServiceImpl.java` |
| Category service | `sm-core/.../catalog/category/CategoryServiceImpl.java` |
| Product API | `sm-shop/.../api/v1/product/ProductApi.java` |
| Category API | `sm-shop/.../api/v1/category/CategoryApi.java` |
| Facades | `sm-shop/.../facade/product/ProductFacadeImpl.java` |

### Customer

| Role | Path |
|------|------|
| Customer service | `sm-core/.../customer/CustomerServiceImpl.java` |
| Cart merge | `sm-core/.../shoppingcart/ShoppingCartServiceImpl.java` (`mergeShoppingCarts`) |
| Customer API | `sm-shop/.../api/v1/customer/CustomerApi.java` |
| Facade | `sm-shop/.../facade/customer/CustomerFacadeImpl.java` |
