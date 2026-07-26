# PRD: Onda 4 — Catalog + Customer

**Feature slug:** `onda-4-catalog-customer`
**Source of truth:** TLC at `.specs/features/onda-4-catalog-customer/` (Option A — authoritative; scope frozen)
**Status:** Ready for TechSpec
**Date:** 2026-07-26

---

## Overview

After Waves 1–2 validated Strangler extraction for reference, tax, content, search, and merchant, and Wave 3 delivers cross-service contracts (`ProductSnapshot`, `CustomerSnapshot`, `LanguageCode`, `MerchantStoreId`), Wave 4 extracts **catalog read** and **customer profile** capabilities into independently deployable services.

The problem is coupling: catalog has the **highest afferent coupling** in `sm-core` (10 inbound references). Extracting full catalog CRUD would drag order, cart, shipping, and search into a distributed monolith. Customer is service-isolated but **transactionally tied to order creation** and **shopping cart merge** on login.

This PRD defines **what and why** for Wave 4: two services behind the existing BFF, frozen REST paths, read-first catalog boundary, and cart-merge decoupling via `CustomerSnapshot`. Technical how is in the TechSpec and ADRs.

**Primary users:** storefront visitors, store admins (read-only catalog from service; writes still monolith), registered customers, platform engineers running Strangler rollout.

---

## Objectives

- Deliver deployable **catalog read** and **customer profile** services while `sm-shop` remains the customer-facing BFF.
- Preserve existing storefront and admin journeys for product browse, category navigation, customer profile, addresses, and opt-in.
- Enable search indexing to use canonical **`ProductSnapshot`** (Wave 3) instead of interim `ProductIndexPayload`.
- Decouple **cart merge** from in-process `CustomerService` using **`CustomerSnapshot`** HTTP.
- Keep **admin catalog mutations** (private product CRUD) in the monolith for Wave 4.
- Prove contract stability (Pact) for P1 surfaces before declaring Wave 4 complete.
- Extend `shopizer-api-contracts` and thin cores (`sm-catalog-core`, `sm-customer-core`).
- **Blocked until Onda 3 Execute completes** — no Wave 4 code before contract gate.

### Business outcomes

| Outcome | Indicator |
| ------- | ----------- |
| Safer catalog decomposition | Read path exits monolith without moving 22 catalog services' write graph |
| Customer domain isolation | Profile CRUD owned by customer-service; auth stays centralized |
| Search contract upgrade | `ProductSnapshot` v2 indexing end-to-end |
| Cart merge readiness | Login merge works with remote customer snapshot |
| Continuity | No breaking change on frozen REST paths |

---

## User stories

### Storefront visitor — browse products and categories (P1 / CAT)

As a **storefront visitor**, I want to browse products and categories through the same public APIs, so product discovery does not require in-process catalog services.

**Acceptance (business):**

1. Paginated product lists, product detail, SKU lookup, related products, and groups return readable DTOs.
2. Category tree and category detail are localized by language.
3. Public inventory and price reads work without order/cart dependencies.
4. Store and language context use Wave 3 value types via reference/merchant HTTP services.
5. Catalog outage surfaces clear unavailability — no silent empty catalog.

**Requirement IDs:** CAT-01…CAT-07, CAT-12

### Platform — ProductSnapshot for search and integrations (P1 / CAT)

As a **platform engineer**, I want a versioned product snapshot contract, so search indexing and future services share one product read model.

**IDs:** CAT-08, CAT-09, STR-07

### Registered customer — profile and addresses (P1 / CUS)

As a **registered customer**, I want to view and update my profile, shipping/billing addresses, and marketing opt-in via existing APIs.

**IDs:** CUS-01…CUS-07

### Returning customer — cart merge on login (P1 / CUS)

As a **returning customer**, I want my session cart merged with my saved cart after login, without a distributed transaction between cart and customer services.

**IDs:** CUS-08, CUS-09, STR-08

### Platform — Strangler BFF (P1 / STR)

As a **platform engineer**, I want HTTP delegation for catalog read and customer profile facades, with admin product writes remaining in-process.

**IDs:** STR-01, STR-04, STR-06, AD-020

### Admin — product images (P2 / CAT)

As a **store admin**, I want product/variant images stored via content-service (Onda 2 deferral completed).

**IDs:** CAT-10

### Developer — contract confidence (P2 / STR)

As a **developer**, I want Pact tests for catalog read and customer profile endpoints.

**IDs:** STR-02, CAT-11, CUS-10

### Operator — observability (P3 / STR)

As an **operator**, I want health checks and correlation IDs on catalog-service and customer-service.

**IDs:** STR-05

---

## Core features

### F1 — Catalog service (MVP, read-only boundary)

Own storefront **read** APIs for products, categories, manufacturers, inventory, and prices; internal `ProductSnapshot` API. **Does not** own admin product mutations.

### F2 — Customer service (MVP)

Own profile, address, opt-in (and review read); internal `CustomerSnapshot` API. **Does not** own login/JWT issuance.

### F3 — Cart merge decoupling (MVP)

Monolith orchestrates merge using `CustomerSnapshot` from customer-service; `ShoppingCartService` refactored to avoid in-process customer entity dependency.

### F4 — Search snapshot migration (MVP)

`ProductSnapshotBuilder` replaces `ProductIndexPayloadBuilder`; search-service accepts v2.

### F5 — Strangler BFF (MVP)

HTTP adapters with `wave4.strangler.enabled`; admin writes stay local.

### F6 — Product images via content (Phase 2)

Product file managers call content-service HTTP.

### F7 — Contracts and observability (Phases 2–3)

Pact; health; correlation propagation.

---

## User experience

| Persona | Goal |
| ------- | ---- |
| Visitor | Browse/search catalog unchanged |
| Customer | Manage profile/addresses |
| Admin | Edit products in monolith (unchanged write UX) |
| Platform engineer | Toggle strangler; observe health; trust pact gates |

**UX constraint:** No new screens — behavioral parity only. Public read p95 ≤ 2× monolith baseline.

---

## High-level technical constraints

- Integrate Wave 1 **reference** and Wave 2 **merchant** / **content** / **search** services.
- Preserve **frozen REST paths** (STR-04).
- No JPA entities in migrated JSON responses.
- **Shared operational DB** (AD-003/AD-022).
- **Onda 3 complete** before Execute.
- JWT on private customer routes equivalent to today.
- Catalog **read-only** at service boundary (AD-020).

---

## Non-goals

| Excluded | Why |
| -------- | --- |
| Execute before Onda 3 | ProductSnapshot/CustomerSnapshot prerequisite |
| Admin catalog writes in catalog-service | Master plan phased extraction; coupling 10/10 |
| shoppingcart-service | Onda 6 |
| order/checkout extraction | Onda 6 |
| Customer login/register in customer-service | Auth authority stays sm-shop (OQ-06) |
| DB-per-service split | AD-022 |
| Full product facade merge (4 facades) | Fase 1 parallel work |
| Payment/shipping integration redesign | Onda 5 |

---

## Phased rollout

### MVP (Phase 1) — P1 stories

- catalog-service public read + internal snapshot
- customer-service profile/address/optin + internal snapshot
- Cart merge decoupling
- ProductSnapshot indexing
- Strangler adapters (read/profile only)

**Exit:** P1 endpoints healthy; no JPA in JSON; pact green for P1; merge test passes.

### Phase 2

- Product images via content-service
- Full Pact suite; docker-compose-wave4.yml

### Phase 3

- Health indicators; STATE/traceability; GAP docs

---

## Success metrics

| Metric | Target |
| ------ | ------ |
| P1 endpoint availability | Both services + BFF paths respond |
| Contract parity | Pact green catalog + customer |
| Entity leakage | Zero JPA types in migrated JSON |
| Search v2 | Index accepts ProductSnapshot |
| Cart merge | Integration test with remote snapshot |
| Latency | Public read p95 ≤ 2× baseline |
| Prerequisite discipline | No Execute before Onda 3 gate |

---

## Risks and mitigations

| Risk | Mitigation |
| ---- | ---------- |
| Onda 3 delay | Docs ready; Execute blocked |
| Catalog read/write split confusion | AD-020; explicit adapter matrix |
| Cart merge regression | Dedicated integration tests; fail-closed on snapshot miss |
| ProductSnapshot drift vs index | schemaVersion + pact |
| Scope creep (admin writes) | Frozen non-goals |

---

## Architecture decision records

- [ADR-001: One Compozy workflow for Catalog + Customer](adrs/adr-001.md)
- [ADR-002: Catalog read-only extraction first](adrs/adr-002.md)
- [ADR-003: ProductSnapshot as canonical product contract](adrs/adr-003.md)
- [ADR-004: Thin sm-catalog-core / sm-customer-core modules](adrs/adr-004.md)
- [ADR-005: Cart merge via CustomerSnapshot orchestrated in monolith](adrs/adr-005.md)
- [ADR-006: Admin catalog writes remain in monolith](adrs/adr-006.md)
- [ADR-007: Product images via content-service](adrs/adr-007.md)

---

## Open questions

All OQ-01…OQ-06 resolved in `.specs/features/onda-4-catalog-customer/context.md`. No blocking product ambiguities remain.

Residual (non-blocking):

- Exact cache TTL for catalog read adapter — tuning during Execute.
- Whether review POST moves to customer-service in Wave 4 or Wave 5 — default read-only in Wave 4.
