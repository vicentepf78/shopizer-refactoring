# PRD: Wave 3 — Contracts DTO + Checkout Application Service

**Feature slug:** `onda-3-contracts-dto`  
**Authoritative TLC:** `.specs/features/onda-3-contracts-dto/` (Option A — frozen scope)  
**Status:** Ready for TechSpec  
**Date:** 2026-07-26

---

## Overview

Waves 1 and 2 proved that Shopizer can extract low- and medium-risk domains behind a Strangler BFF with shared DTO contracts. Wave 3 is different: **no new deployable microservices**. It is a **contracts and internal refactoring wave** inside the monolith that unblocks Waves 4–6 (catalog read, customer, integration hub, shopping cart, order).

Today, ~94% of facade interfaces in `sm-shop-model` accept JPA entities (`MerchantStore`, `Language`) as parameters. Integration plugin contracts (`PaymentModule`, `ShippingQuoteModule`) accept full domain graphs (`Order`, `Customer`, `ShoppingCartItem`). Checkout orchestration lives in `OrderFacadeImpl`, which injects 12+ core services. `processOrder` runs payment, customer persistence, order creation, inventory decrement, and notifications in a single transactional method — a blocker for splitting order and payments.

Wave 3 delivers **cross-service snapshot DTOs**, **tenant identifier value types**, **integration module DTO redesign**, a **Checkout Application Service** in the monolith, and a **saga/outbox foundation** for `processOrder`. Search's interim `ProductIndexPayload` evolves into a proper `ProductSnapshot`; `SearchItem` moves from `shopizer-commons` into `shopizer-api-contracts`.

**Primary users:** platform engineers executing decomposition; indirectly, all API consumers who benefit from stable contracts before catalog/order extraction.

**Why it matters:** Without Wave 3, Waves 4–6 repeat MODEL coupling at HTTP boundaries, integration-service extraction remains impossible, and order/payments stay in a bidirectional cycle (master plan issue #1).

---

## Objectives

- Introduce **ProductSnapshot**, **OrderSnapshot**, and **CustomerSnapshot** in `shopizer-api-contracts` with builders in `sm-core` / `sm-shop` (not in the contracts JAR — L-002).
- Replace entity parameters with **MerchantStoreId** and **LanguageCode** on P1 facade interfaces; provide bridge adapters so implementations can hydrate entities in-process during transition (resolves B-001).
- Redesign **PaymentModule** / **ShippingQuoteModule** to accept integration DTOs; keep legacy method signatures via adapter bridge until all plugins migrate (Wave 5).
- Extract **CheckoutApplicationService** from `OrderFacadeImpl` — single orchestration entry for place-order flow without changing public REST paths.
- Add **outbox table + staged processOrder** foundation — not full distributed saga; enough to break the order↔payments in-process assumption for Wave 6.
- Migrate **SearchItem** (and related search response types) to `shopizer-api-contracts`; align `ProductIndexPayloadBuilder` with `ProductSnapshot` (evolves AD-009).
- Close **B-002**: `ReferencesApi` returns `ReadableLanguage` / `ReadableCurrency` DTOs, not JPA entities.
- Publish a **facade interface migration plan** (phased inventory of 76 facades) for Waves 4–6.
- **Hard prerequisite:** Wave 2 Execute complete (gate green, `docker-compose-wave2.yml`, Pact suite).

### Business outcomes

| Outcome | Indicator |
| ------- | --------- |
| Waves 4–6 unblocked | Master plan prerequisites 1–5 addressed in monolith |
| Contract hygiene | Zero new JPA imports in `shopizer-api-contracts` |
| Checkout maintainability | `OrderFacadeImpl` delegates orchestration to application service |
| Integration readiness | `PaymentModule` V2 callable with DTO-only context |
| Search contract stability | Pact uses api-contracts `SearchItem`, not commons |

---

## User stories

### Platform engineer — snapshot contracts (P1 / SNP)

As a **platform engineer**, I want versioned snapshot DTOs for product, order, and customer data, so cross-service reads and index payloads do not require `sm-core-model` on the consumer classpath.

**Acceptance:**

1. `ProductSnapshot` is the canonical catalog read projection; `ProductIndexPayload` delegates or maps from it with `schemaVersion` bump.
2. `OrderSnapshot` and `CustomerSnapshot` capture checkout-relevant fields without lazy JPA associations.
3. Snapshots serialize via Jackson with stable field names for Pact.
4. Builders live outside `shopizer-api-contracts`.

**Requirement IDs:** SNP-01…SNP-07, CTR-01…CTR-03

### Platform engineer — tenant identifiers (P1 / TNT)

As a **platform engineer**, I want facade interfaces to accept `MerchantStoreId` and `LanguageCode` instead of JPA entities, so HTTP Strangler adapters in future waves do not leak persistence types.

**Acceptance:**

1. Value types in `shopizer-api-contracts` with validation (non-blank code).
2. P1 facades migrated: `OrderFacade`, `ShoppingCartFacade`, `SearchFacade`, `ShippingFacade`, `CategoryFacade`, `ProductCommonFacade`.
3. Bridge helpers hydrate `MerchantStore` / `Language` in monolith implementations only.
4. `AbstractDataPopulator` gains overload accepting tenant primitives (backward compatible).

**Requirement IDs:** TNT-01…TNT-06, FAC-01…FAC-05

### Platform engineer — integration DTOs (P1 / INT)

As a **platform engineer**, I want payment and shipping plugin contracts to accept DTOs, so `integration-service` extraction in Wave 5 does not drag `Order` entities across process boundaries.

**Acceptance:**

1. New DTOs: `PaymentRequestContext`, `ShippingQuoteRequestContext`, etc. in `sm-core-modules`.
2. `PaymentModuleV2` / `ShippingQuoteModuleV2` parallel interfaces; registry resolves V2 when plugin implements it.
3. Legacy plugins continue working via entity→DTO adapter in `PaymentServiceImpl` / `ShippingServiceImpl`.
4. No breaking change to existing Stripe/PayPal/USPS plugin bytecode in Wave 3.

**Requirement IDs:** INT-01…INT-06

### Storefront visitor — unchanged checkout (P1 / CHK)

As a **storefront visitor**, I want checkout to behave exactly as today, so contract refactoring does not regress order placement.

**Acceptance:**

1. Public order REST paths unchanged (`STR-04 pattern from Wave 2).
2. `CheckoutApplicationService.placeOrder(...)` produces identical order outcomes for happy path and known error paths.
3. No new user-facing latency budget breach (p95 ≤ 2× baseline).

**Requirement IDs:** CHK-01…CHK-06

### Platform engineer — processOrder foundation (P1 / SAG)

As a **platform engineer**, I want `processOrder` to record outbox events per stage, so Wave 6 can split payment confirmation from order persistence without rewriting business rules.

**Acceptance:**

1. `CHECKOUT_OUTBOX` table (or equivalent) with idempotent event keys.
2. Stages: `PAYMENT_REQUESTED`, `PAYMENT_CONFIRMED`, `ORDER_PERSISTED`, `INVENTORY_DECREMENTED` (minimum).
3. Same-transaction outbox write + business step for Wave 3 (no message broker yet).
4. Feature flag `checkout.outbox.enabled` default false; tests cover both paths.

**Requirement IDs:** SAG-01…SAG-05

### API consumer — reference DTOs (P1 / REF)

As an **API consumer**, I want language and currency list endpoints to return readable DTOs, so public reference responses match Wave 1 contract hygiene (closes B-002).

**Requirement IDs:** REF-01…REF-02

### Search consumer — stable search schema (P2 / SRCH)

As a **search-service maintainer**, I want `SearchItem` in api-contracts, so Pact no longer depends on `shopizer-commons` (OQ-06 resolution from Wave 2).

**Requirement IDs:** SRCH-01…SRCH-04

### Platform engineer — migration inventory (P2 / FAC)

As a **platform engineer**, I want a documented phased plan for remaining facade interfaces, so Waves 4–6 execute without rediscovering 76 facades.

**Requirement IDs:** FAC-06

---

## Core features

### F1 — Snapshot DTOs (MVP)

`ProductSnapshot`, `OrderSnapshot`, `CustomerSnapshot` in contracts; builders and mappers in monolith modules.

### F2 — Tenant value types (MVP)

`MerchantStoreId`, `LanguageCode`; P1 facade signature migration with bridges.

### F3 — Integration module redesign (MVP)

DTO contexts + V2 module interfaces + legacy adapters.

### F4 — Checkout Application Service (MVP)

Extract orchestration from `OrderFacadeImpl`; thin facade delegates.

### F5 — processOrder outbox foundation (MVP)

Local outbox + staged steps behind feature flag.

### F6 — SearchItem migration (Phase 2)

Move types to contracts; update search-service and Pact.

### F7 — ReferencesApi DTO fix (MVP)

Wire `ReadableLanguage` / `ReadableCurrency` on public reference endpoints.

### F8 — Facade migration plan document (Phase 2)

Inventory + phasing for Waves 4–6.

---

## UX / API constraints

- **No new REST paths** for checkout or reference in Wave 3.
- **No admin UI changes** — behavioral parity only.
- **No new microservices or Docker Compose services** — monolith-only diff.
- Shared DB (AD-003) unchanged; outbox table lives in `SALESMANAGER` schema.

---

## Non-goals

| Excluded | Reason |
| -------- | ------ |
| Deploy catalog-service, customer-service, order-service | Waves 4–6 |
| Full distributed saga / message broker | Wave 6+; Wave 3 = foundation only |
| Migrate all 76 facades in one wave | Phased; P1 subset only |
| Rewrite all payment/shipping plugins to V2 | Adapter bridge; plugin rewrites optional |
| Split database per domain | Future wave |
| Tax calculation extraction | AD-002 — stays in monolith |
| Quick wins (Mapper/Populator merge) | Parallel, not blocking |
| Feign / service mesh | AD-005 — RestTemplate pattern continues |

---

## Phased rollout

### MVP (Phase 1) — P1 stories

- Snapshot DTOs + tenant types + P1 facade migration
- Integration V2 interfaces + adapters
- CheckoutApplicationService + outbox foundation (flag off by default)
- ReferencesApi DTO fix

**Exit criteria:** `./mvnw clean install` green; Pact updated for SearchItem migration; B-001 partially resolved (P1 facades); B-002 closed.

### Phase 2

- SearchItem in api-contracts + `ProductIndexPayload` → `ProductSnapshot` alignment
- Facade migration plan published
- Outbox flag enabled in integration test profile

### Phase 3

- ArchUnit rule: no new `MerchantStore`/`Language` in new facade methods
- STATE.md updated; Wave 4 Specify unblocked

---

## Success metrics

| Metric | Target |
| ------ | ------ |
| Reactor gate | `./mvnw clean install` green |
| Contracts purity | Zero `com.salesmanager.core.model` in api-contracts |
| Checkout parity | Order placement integration tests pass (flag on/off) |
| P1 facade migration | 6 facade interfaces use tenant primitives |
| Integration V2 | At least one plugin path tested via adapter |
| B-002 | ReferencesApi returns DTOs only |
| Documentation | TLC spec + 48 tasks + 5 ADRs |

---

## Traceability

| Source | Link |
| ------ | ---- |
| Master plan § Onda 3 | `docs/decomposition/MIGRATION-MASTER-PLAN.md` |
| Blockers B-001, B-002 | `.specs/project/STATE.md` |
| AD-009 ProductIndexPayload | `.specs/project/STATE.md` |
| Wave 2 OQ-06 SearchItem | `.specs/features/onda-2-content-search-merchant/design.md` |
| TLC spec | `.specs/features/onda-3-contracts-dto/spec.md` |

---

## Open questions (resolved in Design)

| ID | Question | Resolution |
| ---- | -------- | ---------- |
| OQ-01 | Replace or wrap ProductIndexPayload? | **Wrap** — ProductSnapshot canonical; payload maps with schemaVersion 2 |
| OQ-02 | Big-bang vs phased facade migration? | **Phased** — P1 checkout-adjacent facades in Wave 3 |
| OQ-03 | Outbox broker now? | **No** — same-DB outbox; broker deferred Wave 6 |
| OQ-04 | Break PaymentModule binary compat? | **No** — V2 parallel interface + adapter |
| OQ-05 | CheckoutApplicationService package? | **`sm-core/.../checkout`** — domain orchestration, not shop layer |
