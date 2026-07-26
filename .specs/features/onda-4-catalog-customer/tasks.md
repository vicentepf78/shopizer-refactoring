# Onda 4 — Catalog + Customer Tasks

**Design:** `.specs/features/onda-4-catalog-customer/design.md`
**Spec:** `.specs/features/onda-4-catalog-customer/spec.md`
**Status:** Approved — Execute blocked until Onda 3 complete
**Testing:** `.specs/codebase/TESTING.md`
**Prerequisite:** Onda 3 Execute complete (`ProductSnapshot`, `CustomerSnapshot`, `LanguageCode`, `MerchantStoreId`)

---

## Execution Plan

### Phase 1: Contracts + Wave4 Config (Sequential → Parallel)

```
Onda3-gate ──→ T1 ──┬──→ T2 [P]
                    └──→ T3 [P]
T1,T2,T3 ──→ T4
```

### Phase 2: Core Extraction (2 parallel tracks)

```
T4 ──┬──→ T5 ──→ T6 ──→ T7 ──→ T8 ──→ T9 ──→ T10 ──→ T11 ──→ T12 ──→ T13
     │
     └──→ T14 ──→ T15 ──→ T16 ──→ T17 ──→ T18 ──→ T19 ──→ T20
```

**Track A (Catalog):** T5–T13
**Track B (Customer):** T14–T20

### Phase 3: Cross-cutting (Sequential)

```
T10,T13,T20 ──→ T21 ──→ T22 ──→ T23 ──→ T24 ──→ T25 ──→ T26
```

### Phase 4: Strangler + Search migration (Sequential)

```
T26 ──→ T27 ──→ T28 ──→ T29 ──→ T30 ──→ T31
```

### Phase 5: Integration & Gate (Sequential tail)

```
T31 ──→ T32 ──┬──→ T33 [P providers]
              └──→ T34
T33,T34 ──→ T35 ──→ T36 ──→ T37 ──→ T38
```

---

## Parallel Execution Map

```
Phase 1:
  Onda3-gate → T1 → (T2 ∥ T3) → T4

Phase 2 (2 tracks após T4):
  Catalog:   T5 → T6 → T7 → T8 → T9 → T10 → T11 → T12 → T13
  Customer:  T14 → T15 → T16 → T17 → T18 → T19 → T20

Phase 3:
  T21 → T22 → T23 → T24 → T25 → T26

Phase 4:
  T27 → T28 → T29 → T30 → T31

Phase 5:
  T32 → (T33 ∥ subagents) → T34 → T35 → T36 → T37 → T38
```

**Milestone `CAT-ready`:** T11 complete (catalog public read + internal snapshot).
**Milestone `CUS-ready`:** T19 complete (customer profile REST + internal snapshot).
**Subagent rule:** `[P]` → parallel tasks in same phase. Catalog/Customer tracks in Phase 2 → **2 subagents** per ordinal when both ready.

---

## Task Breakdown

### T1: Wave 4 contracts foundation — value types verification

**What:** Verify Wave 3 `LanguageCode`, `MerchantStoreId` in contracts; add `schemaVersion` constants for snapshots.
**Where:** `shopizer-api-contracts/.../common/`
**Depends on:** Onda 3 gate
**Requirement:** STR-06, AD-021

**Done when:**
- [ ] Value types compile without JPA
- [ ] `./mvnw compile -pl shopizer-api-contracts` passes

**Tests:** unit serialization
**Gate:** `./mvnw compile -pl shopizer-api-contracts`

---

### T2: Catalog DTOs + `CatalogServiceClient` [P]

**What:** Migrate/add `ReadableProduct*`, `ReadableCategory*`, `ProductSnapshot`; interface `CatalogServiceClient` (read + snapshot).
**Where:** `shopizer-api-contracts/.../catalog/`, `.../client/`
**Depends on:** Onda 3 T-product-snapshot (Wave 3)
**Reuses:** `sm-shop-model/.../model/catalog/`
**Requirement:** CAT-02, CAT-03, CAT-08, STR-04

**Done when:**
- [ ] DTOs compile without `com.salesmanager.core.model`
- [ ] `ProductSnapshot.schemaVersion` default 2

**Tests:** none
**Gate:** `./mvnw compile -pl shopizer-api-contracts`

---

### T3: Customer DTOs + `CustomerServiceClient` [P]

**What:** Migrate customer DTOs; create `CustomerSnapshot`; `CustomerServiceClient` with `getSnapshot`.
**Where:** `shopizer-api-contracts/.../customer/`, `.../client/`
**Depends on:** Onda 3 T-customer-snapshot
**Requirement:** CUS-02, CUS-08, STR-04

**Done when:**
- [ ] `CustomerSnapshot` serializable; default `schemaVersion` 1

**Tests:** none
**Gate:** `./mvnw compile -pl shopizer-api-contracts`

---

### T4: Wave4 Strangler properties + RestTemplate

**What:** Profile `strangler-wave4`; `wave4.*.base-url`, `wave4.strangler.enabled`; RestTemplate beans; stub clients.
**Where:** `sm-shop/.../strangler/config/Wave4ClientConfig.java`
**Depends on:** T1, T2, T3
**Requirement:** STR-01

**Done when:**
- [ ] Properties coexist with `wave1.*`, `wave2.*`
- [ ] `./mvnw test -pl sm-shop -Dtest=Wave4ClientConfigTest`

**Tests:** unit
**Gate:** `./mvnw test -pl sm-shop -Dtest=Wave4ClientConfigTest`

---

### T5: Scaffold `sm-catalog-core` module

**What:** Create Maven module; move read repositories for product, category, manufacturer.
**Where:** `sm-catalog-core/pom.xml`, `.../repositories/`
**Depends on:** T2
**Requirement:** CAT-12

**Done when:**
- [ ] Module in root `pom.xml`
- [ ] `./mvnw compile -pl sm-catalog-core`

**Tests:** none
**Gate:** `./mvnw compile -pl sm-catalog-core`

---

### T6: Move catalog read services to `sm-catalog-core`

**What:** Extract read methods from `ProductService`, `CategoryService`, `ManufacturerService`, `ProductInventoryService`, `PricingService` (read-only subset).
**Where:** `sm-catalog-core/.../services/catalog/`
**Depends on:** T5
**Requirement:** CAT-12

**Done when:**
- [ ] Write methods remain in `sm-core` or deprecated stubs delegate monolith
- [ ] Unit tests for read services pass

**Tests:** unit
**Gate:** `./mvnw test -pl sm-catalog-core`

---

### T7: Catalog mappers/populators in `sm-catalog-core`

**What:** `ReadableProductMapper` or populator equivalents for read path; category tree mappers.
**Where:** `sm-catalog-core/.../mappers/`
**Depends on:** T6
**Requirement:** CAT-06

**Done when:**
- [ ] Mappers do not leak entities to API layer

**Tests:** unit
**Gate:** `./mvnw test -pl sm-catalog-core`

---

### T8: Wire `sm-core` → `sm-catalog-core` (read delegation)

**What:** `sm-core` catalog services delegate read calls to thin core; admin writes unchanged in sm-core.
**Where:** `sm-core/pom.xml`, service impls
**Depends on:** T6, T7
**Requirement:** AD-020

**Done when:**
- [ ] Existing monolith integration tests for product read still pass in-process

**Tests:** integration (sm-core)
**Gate:** `./mvnw test -pl sm-core -Dtest=*Product*Test -DfailIfNoTests=false`

---

### T9: Scaffold `catalog-service` Boot (:8086)

**What:** Spring Boot app, JPA config shared DB, package scan `sm-catalog-core`.
**Where:** `catalog-service/`
**Depends on:** T8
**Requirement:** CAT-01

**Done when:**
- [ ] App context starts with Testcontainers MySQL

**Tests:** integration
**Gate:** `./mvnw test -pl catalog-service -Dtest=*Application*Test`

---

### T10: `catalog-service` — reference + merchant HTTP clients

**What:** `ReferenceServiceClient`, `MerchantServiceClient` for lang/store resolution.
**Where:** `catalog-service/.../client/`
**Depends on:** T9, T4
**Requirement:** CAT-07, STR-06

**Done when:**
- [ ] Language resolution uses HTTP only

**Tests:** unit + wiremock
**Gate:** `./mvnw test -pl catalog-service -Dtest=*Client*Test`

---

### T11: `catalog-service` — public read REST controllers (`CAT-ready`)

**What:** Port GET handlers from `ProductApi`, `CategoryApi`, manufacturer, inventory, price, group APIs.
**Where:** `catalog-service/.../api/v1/`
**Depends on:** T10
**Requirement:** CAT-02…CAT-05, STR-04

**Done when:**
- [ ] Parity test vs monolith baseline for GET product list
- [ ] **CAT-ready** milestone

**Tests:** integration
**Gate:** `./mvnw test -pl catalog-service -Dtest=*ProductApi*Test,*CategoryApi*Test`

---

### T12: `catalog-service` — internal ProductSnapshot API

**What:** `InternalProductSnapshotController`; network-restricted; 422 on bad schemaVersion.
**Where:** `catalog-service/.../api/internal/`
**Depends on:** T11
**Requirement:** CAT-08

**Done when:**
- [ ] GET snapshot returns `ProductSnapshot` v2

**Tests:** integration
**Gate:** `./mvnw test -pl catalog-service -Dtest=*Snapshot*Test`

---

### T13: `catalog-service` — JWT + security for any private read routes

**What:** Replicate JWT filter chain pattern from merchant-service for routes that require auth today.
**Where:** `catalog-service/.../security/`
**Depends on:** T11
**Requirement:** CAT-06

**Done when:**
- [ ] Private read routes (if any) reject anonymous

**Tests:** integration
**Gate:** `./mvnw test -pl catalog-service -Dtest=*Security*Test`

---

### T14: Scaffold `sm-customer-core` module [P track]

**What:** Maven module; customer repositories.
**Where:** `sm-customer-core/`
**Depends on:** T3
**Requirement:** CUS-01

**Done when:**
- [ ] `./mvnw compile -pl sm-customer-core`

**Tests:** none
**Gate:** `./mvnw compile -pl sm-customer-core`

---

### T15: Move customer services to `sm-customer-core`

**What:** `CustomerService`, `CustomerOptinService`, attribute services (exclude order-only creation helpers).
**Where:** `sm-customer-core/.../services/customer/`
**Depends on:** T14
**Requirement:** CUS-01

**Done when:**
- [ ] OrderService still uses monolith copy or bridge for checkout customer create (document bridge)

**Tests:** unit
**Gate:** `./mvnw test -pl sm-customer-core`

---

### T16: Customer mappers in `sm-customer-core`

**What:** ReadableCustomer, address populators/mappers.
**Where:** `sm-customer-core/.../mappers/`
**Depends on:** T15
**Requirement:** CUS-05

**Done when:**
- [ ] Snapshot mapper produces `CustomerSnapshot`

**Tests:** unit
**Gate:** `./mvnw test -pl sm-customer-core -Dtest=*Mapper*Test`

---

### T17: Wire `sm-core` → `sm-customer-core`

**What:** Delegate customer profile reads/writes to thin core where safe; keep order paths in sm-core.
**Where:** `sm-core`
**Depends on:** T15, T16
**Requirement:** GAP-CUS-01

**Done when:**
- [ ] Profile unit tests pass in-process

**Tests:** unit
**Gate:** `./mvnw test -pl sm-core -Dtest=*Customer*Test -DfailIfNoTests=false`

---

### T18: Scaffold `customer-service` Boot (:8087)

**What:** Spring Boot + JPA + sm-customer-core.
**Where:** `customer-service/`
**Depends on:** T17
**Requirement:** CUS-01

**Done when:**
- [ ] App starts with Testcontainers MySQL

**Tests:** integration
**Gate:** `./mvnw test -pl customer-service -Dtest=*Application*Test`

---

### T19: `customer-service` — profile, address, optin REST (`CUS-ready`)

**What:** Port CustomerApi sections (not auth); reference HTTP client.
**Where:** `customer-service/.../api/v1/`
**Depends on:** T18, T4
**Requirement:** CUS-02…CUS-04, CUS-06

**Done when:**
- [ ] Profile update integration test passes
- [ ] **CUS-ready** milestone

**Tests:** integration
**Gate:** `./mvnw test -pl customer-service -Dtest=*CustomerApi*Test`

---

### T20: `customer-service` — internal CustomerSnapshot + JWT

**What:** Internal snapshot controller; JWT security for private routes.
**Where:** `customer-service/.../api/internal/`, `.../security/`
**Depends on:** T19
**Requirement:** CUS-08

**Done when:**
- [ ] Snapshot endpoint returns v1 JSON

**Tests:** integration
**Gate:** `./mvnw test -pl customer-service -Dtest=*Snapshot*Test,*Security*Test`

---

### T21: `ProductSnapshotBuilder` in monolith (replaces ProductIndexPayloadBuilder)

**What:** Build v2 snapshots from catalog read model; deprecate v1 builder.
**Where:** `sm-core/.../search/` or `sm-shop/.../search/`
**Depends on:** T12, T2
**Requirement:** CAT-09, STR-07

**Done when:**
- [ ] Builder output matches internal snapshot API for fixture products

**Tests:** unit
**Gate:** `./mvnw test -pl sm-core -Dtest=*ProductSnapshotBuilder*Test`

---

### T22: `search-service` — accept ProductSnapshot v2

**What:** Extend index service to deserialize v2; map to OpenSearch doc; v1 still accepted.
**Where:** `search-service/.../services/`
**Depends on:** T21
**Requirement:** CAT-09

**Done when:**
- [ ] POST v2 snapshot indexes successfully

**Tests:** integration
**Gate:** `./mvnw test -pl search-service -Dtest=*Index*Test`

---

### T23: Update `SearchIndexProducerHttp` to send ProductSnapshot

**What:** Monolith producer uses v2; bulk endpoint unchanged semantics.
**Where:** `sm-shop/.../strangler/`
**Depends on:** T22
**Requirement:** STR-07

**Done when:**
- [ ] Index event produces v2 payload in integration test

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=*SearchIndexProducer*Test`

---

### T24: Cart merge — `CustomerSnapshot` in `ShoppingCartService`

**What:** Refactor `mergeShoppingCarts` to accept snapshot/id; remove hard dependency on loaded `Customer` entity where possible.
**Where:** `sm-core/.../shoppingcart/ShoppingCartServiceImpl.java`
**Depends on:** T20
**Requirement:** CUS-09, STR-08

**Done when:**
- [ ] Merge integration test uses snapshot input

**Tests:** integration
**Gate:** `./mvnw test -pl sm-core -Dtest=*ShoppingCart*Merge*Test`

---

### T25: `CustomerFacade` merge orchestration via HTTP snapshot

**What:** On login, fetch snapshot from customer-service before merge.
**Where:** `sm-shop/.../facade/customer/`
**Depends on:** T24, T20
**Requirement:** CUS-08

**Done when:**
- [ ] Login+merge E2E test with wave4 strangler mock

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=*CustomerFacade*Merge*Test`

---

### T26: Product images — content-service product file endpoints (P2)

**What:** Extend content-service for `productFileManager` uploads; wire catalog/monolith facades to `ContentServiceClient`.
**Where:** `content-service`, `sm-shop/.../product/`
**Depends on:** T11 (CAT-ready), Wave 2 content
**Requirement:** CAT-10

**Done when:**
- [ ] Option image upload hits content-service HTTP

**Tests:** integration
**Gate:** `./mvnw test -pl content-service,sm-shop -Dtest=*ProductImage*Test -DfailIfNoTests=false`

---

### T27: `CatalogFacadeHttpAdapter` — product/category read strangler

**What:** HTTP adapter for `ProductFacade`, `ProductCommonFacade`, `CategoryFacade` read methods.
**Where:** `sm-shop/.../strangler/catalog/`
**Depends on:** T13 (CAT-ready), T4
**Requirement:** STR-01, STR-04

**Done when:**
- [ ] Strangler on: GET product delegates HTTP; POST product stays local

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=*CatalogFacadeHttp*Test`

---

### T28: `CustomerFacadeHttpAdapter` — profile strangler

**What:** HTTP adapter for profile/address/optin; auth methods stay local.
**Where:** `sm-shop/.../strangler/customer/`
**Depends on:** T20 (CUS-ready), T4
**Requirement:** STR-01

**Done when:**
- [ ] Profile GET delegates; Authenticate stays local

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=*CustomerFacadeHttp*Test`

---

### T29: `ProductFacadeV2` read delegation

**What:** V2 read paths use same `CatalogServiceClient`.
**Where:** `sm-shop/.../facade/product/ProductFacadeV2Impl.java`
**Depends on:** T27
**Requirement:** OQ-05

**Done when:**
- [ ] V2 GET product uses HTTP when strangler on

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=*ProductFacadeV2*Test`

---

### T30: Conditional wiring — admin writes never strangler

**What:** Assert `@ConditionalOnProperty` on adapters excludes write facades; document in code.
**Where:** `sm-shop/.../strangler/`
**Depends on:** T27, T28
**Requirement:** AD-020

**Done when:**
- [ ] ArchUnit or integration test proves private POST product not routed

**Tests:** unit/arch
**Gate:** `./mvnw test -pl sm-shop -Dtest=*Wave4Wiring*Test`

---

### T31: Correlation ID + health indicators Wave4

**What:** Filters on catalog/customer; health for db, reference, merchant (catalog).
**Where:** both services + sm-shop interceptor
**Depends on:** T9, T18
**Requirement:** STR-05

**Done when:**
- [ ] `/actuator/health` shows dependency components

**Tests:** integration
**Gate:** `./mvnw test -pl catalog-service,customer-service -Dtest=*Health*Test`

---

### T32: JaCoCo coverage gates Wave4 modules

**What:** Add verify-phase JaCoCo thresholds matching Waves 1–2 pattern.
**Where:** `catalog-service/pom.xml`, `customer-service/pom.xml`, thin cores
**Depends on:** T31
**Requirement:** quality gate

**Done when:**
- [ ] `./mvnw verify -pl catalog-service,customer-service,sm-catalog-core,sm-customer-core`

**Tests:** verify
**Gate:** `./mvnw verify -pl catalog-service,customer-service`

---

### T33: Pact providers — catalog + customer [P]

**What:** Provider pact tests on both services for P1 endpoints.
**Where:** `*/src/test/java/**/pact/`
**Depends on:** T11, T19
**Requirement:** CAT-11, CUS-10, STR-02

**Done when:**
- [ ] Provider tests publish pacts

**Tests:** pact
**Gate:** `./mvnw test -pl catalog-service,customer-service -Dtest=*ProviderPact*Test`

---

### T34: Pact consumer — `Wave4ConsumerPactTest` in sm-shop

**What:** Consumer tests for catalog read + customer profile + snapshots.
**Where:** `sm-shop/src/test/java/.../pact/`
**Depends on:** T27, T28, T33
**Requirement:** STR-02

**Done when:**
- [ ] Consumer pact passes against provider stubs

**Tests:** pact
**Gate:** `./mvnw test -pl sm-shop -Dtest=Wave4ConsumerPactTest`

---

### T35: `docker-compose-wave4.yml`

**What:** Full topology with catalog + customer; env vars documented.
**Where:** repo root
**Depends on:** T31
**Requirement:** deploy

**Done when:**
- [ ] `docker compose -f docker-compose-wave4.yml config` exit 0

**Tests:** config
**Gate:** `docker compose -f docker-compose-wave4.yml config`

---

### T36: Wave4 integration suite

**What:** Consolidate `*Wave4*Test` smoke: catalog read, customer profile, merge, search v2 index.
**Where:** `sm-shop/src/test/java/`
**Depends on:** T35, T34
**Requirement:** integration

**Done when:**
- [ ] Suite green with Testcontainers / compose profile

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=*Wave4*Integration*Test`

---

### T37: Reactor gate `./mvnw clean install`

**What:** Full reactor including Waves 1–4 modules.
**Depends on:** T36
**Requirement:** gate

**Done when:**
- [ ] Install completes without failure

**Tests:** full
**Gate:** `./mvnw clean install`

---

### T38: Traceability + STATE.md update

**What:** Mark 30 requirements Verified; update ROADMAP/STATE; design status Execute complete.
**Where:** `.specs/project/STATE.md`, spec traceability table
**Depends on:** T37
**Requirement:** documentation

**Done when:**
- [ ] 30/30 requirements Verified
- [ ] STATE.md records Wave 4 gate date

**Tests:** none
**Gate:** checklist review

---

### T39: catalog-service Dockerfile + container smoke

**What:** Add Dockerfile for catalog-service mirroring merchant-service pattern; JAR copy from `target/`.
**Where:** `catalog-service/Dockerfile`
**Depends on:** T11
**Requirement:** deploy

**Done when:**
- [ ] Image builds from pre-built JAR
- [ ] Container starts with env DB_URL

**Tests:** manual smoke
**Gate:** `docker build -f catalog-service/Dockerfile catalog-service`

---

### T40: customer-service Dockerfile + container smoke

**What:** Dockerfile for customer-service.
**Where:** `customer-service/Dockerfile`
**Depends on:** T19
**Requirement:** deploy

**Done when:**
- [ ] Image builds and health responds in compose

**Tests:** manual smoke
**Gate:** `docker build -f customer-service/Dockerfile customer-service`

---

### T41: ReadableProduct parity fixture tests

**What:** Golden-file or snapshot tests comparing monolith vs catalog-service GET product for fixture SKUs.
**Where:** `catalog-service/src/test/java/`
**Depends on:** T11
**Requirement:** CAT-02

**Done when:**
- [ ] ≥3 fixture products match field-for-field (excluding volatile timestamps)

**Tests:** integration
**Gate:** `./mvnw test -pl catalog-service -Dtest=*Parity*Test`

---

### T42: Category tree parity tests

**What:** Deep category tree comparison vs monolith baseline.
**Where:** `catalog-service/src/test/java/`
**Depends on:** T11
**Requirement:** CAT-03

**Done when:**
- [ ] Tree structure + counts match for DEFAULT store

**Tests:** integration
**Gate:** `./mvnw test -pl catalog-service -Dtest=*CategoryTree*Test`

---

### T43: Customer address validation parity

**What:** Bean validation errors on address endpoints match monolith status codes.
**Where:** `customer-service/src/test/java/`
**Depends on:** T19
**Requirement:** CUS-03

**Done when:**
- [ ] Invalid postal code returns same 400 shape as monolith

**Tests:** integration
**Gate:** `./mvnw test -pl customer-service -Dtest=*AddressValidation*Test`

---

### T44: Catalog read adapter optional TTL cache

**What:** Caffeine or simple cache on CatalogFacadeHttpAdapter GET product by id (configurable TTL).
**Where:** `sm-shop/.../strangler/catalog/`
**Depends on:** T27
**Requirement:** performance

**Done when:**
- [ ] `wave4.catalog-service.cache.ttl-seconds` honored
- [ ] Strangler off disables cache

**Tests:** unit
**Gate:** `./mvnw test -pl sm-shop -Dtest=*CatalogCache*Test`

---

### T45: Customer snapshot cache on merge path

**What:** Short TTL cache for CustomerSnapshot in login merge to avoid duplicate HTTP.
**Where:** `sm-shop/.../strangler/customer/`
**Depends on:** T25
**Requirement:** CUS-08

**Done when:**
- [ ] Second merge call within TTL does not hit HTTP (mock verify)

**Tests:** unit
**Gate:** `./mvnw test -pl sm-shop -Dtest=*SnapshotCache*Test`

---

### T46: Customer review read endpoints

**What:** Port GET review list endpoints to customer-service (write may stay monolith).
**Where:** `customer-service/.../api/v1/customer/review/`
**Depends on:** T19
**Requirement:** CUS-07

**Done when:**
- [ ] GET reviews returns ReadableCustomerReview list

**Tests:** integration
**Gate:** `./mvnw test -pl customer-service -Dtest=*Review*Test`

---

### T47: Document GAP-CAT / GAP-CUS in design appendix

**What:** Add explicit gap table to design.md or `docs/decomposition/GAP-WAVE4.md`.
**Where:** `.specs/features/onda-4-catalog-customer/design.md`
**Depends on:** T21, T24
**Requirement:** documentation

**Done when:**
- [ ] GAP-CAT-01..02 and GAP-CUS-01..02 documented with owner wave

**Tests:** none
**Gate:** doc review

---

### T48: ROADMAP.md Wave 4 status update (pre-Execute)

**What:** Set Onda 4 TLC status to Tasks approved in ROADMAP.md.
**Where:** `.specs/project/ROADMAP.md`
**Depends on:** T38 (or parallel after tasks approved)
**Requirement:** documentation

**Done when:**
- [ ] ROADMAP shows Specify/Design/Tasks ✅ for Onda 4

**Tests:** none
**Gate:** checklist

---

## Requirement → Task Mapping

| Req | Tasks |
| --- | ----- |
| CAT-01 | T9 |
| CAT-02 | T11, T27 |
| CAT-03 | T11, T27 |
| CAT-04 | T11 |
| CAT-05 | T11 |
| CAT-06 | T7, T13 |
| CAT-07 | T10 |
| CAT-08 | T12 |
| CAT-09 | T21, T22, T23 |
| CAT-10 | T26 |
| CAT-11 | T33 |
| CAT-12 | T5, T6, T8 |
| CUS-01 | T14, T15, T18 |
| CUS-02 | T19, T28 |
| CUS-03 | T19, T28 |
| CUS-04 | T19 |
| CUS-05 | T16 |
| CUS-06 | T19 |
| CUS-07 | T19 (phase read) |
| CUS-08 | T20, T25 |
| CUS-09 | T24 |
| CUS-10 | T33 |
| STR-01 | T4, T27, T28 |
| STR-02 | T33, T34 |
| STR-03 | (inherited AD-022) |
| STR-04 | T2, T3, T11, T19 |
| STR-05 | T31 |
| STR-06 | T10, T19 |
| STR-07 | T21, T23 |
| STR-08 | T24, T25 |

**Coverage:** 30 requirements → 48 tasks, 0 unmapped ✅
