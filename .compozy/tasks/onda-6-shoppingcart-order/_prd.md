# PRD: Onda 6 — ShoppingCart + Order

**Slug:** `onda-6-shoppingcart-order`
**Source of truth:** TLC at `.specs/features/onda-6-shoppingcart-order/` (Option A — authoritative; frozen scope)
**Status:** Ready for TechSpec
**Date:** 2026-07-26

---

## Overview

After Ondas 1–5 established Strangler patterns, DTO snapshots, catalog/customer/integration services, and saga/outbox on `processOrder`, Onda 6 extracts the **last and highest-risk domains**: shopping cart and order. Storefront visitors, customers completing checkout, and store admins must continue using existing REST paths while cart persistence and order lifecycle move to dedicated runtimes.

Today order is the central orchestrator (coupling **9/10**): `OrderFacadeImpl` injects **12 sm-core services**, `processOrder` runs in a global AOP transaction, and two critical cycles block naive extraction — **order↔payments** and **order↔shoppingcart** (`ShoppingCartCalculationServiceImpl` → `OrderService.calculateShoppingCartTotal`). Without phased specification, teams face big-bang checkout cutover with no rollback.

This PRD defines the **business what and why** for `shoppingcart-service` and `order-service`, with **Checkout Application Service** as the BFF checkout boundary (Onda 3 deliverable). Technical how belongs in TechSpec and ADRs.

**Primary users:** storefront shoppers, checkout customers, store admins viewing orders, platform engineers operating phased cutover and rollback.

---

## Objectives

- Deliver **shoppingcart-service** and **order-service** as independently deployable capabilities behind the existing BFF.
- Preserve cart CRUD, order read/list/history, and place-order journeys with equivalent REST contracts.
- **Break cart↔order cycle** via HTTP totals API — cart domain must not call in-process `OrderService`.
- Run checkout as **saga choreography + transactional outbox** (Onda 3), not monolithic DB transaction.
- **Decompose checkout hub** — 12-service `OrderFacadeImpl` collapses into `CheckoutApplicationService` + thin facades.
- Explicit **phasing, feature flags, and rollback** per domain (`wave6.shoppingcart`, `wave6.order`, `wave6.checkout.saga`).
- Contract tests (Pact) for P1 surfaces before declaring Wave 6 complete.
- **Blocked until Ondas 3, 4, 5 Execute complete.**

### Business outcomes

| Outcome | Indicator |
|---------|-----------|
| Monolith decomposition complete | Cart + order out of sm-core runtime |
| Revenue path protected | Saga checkout with compensation; rollback < 5 min |
| Cycle elimination | No order↔cart in-process dependency in extracted services |
| Hub maintainability | Checkout orchestration in one application service |
| Operational safety | Per-flag rollback; runbooks for SC/OR/CHK milestones |

---

## User stories

### Storefront visitor — shopping cart (P1 / CART)

As a **storefront visitor**, I want to manage my cart via existing `/api/v1/cart` endpoints, so I can prepare checkout when the cart runs outside the monolith.

**Acceptance (business):**

1. Add/update/remove line items scoped by store and session/customer.
2. Product validation uses catalog capability (HTTP) — not monolith JPA product graph.
3. Cart totals come from order capability via HTTP — not in-process order calculation.
4. Strangler flag off preserves legacy in-process behavior.
5. Remote failure returns clear 503 with correlation id — no silent fallback.

**Requirement IDs:** CART-01…CART-07

### Customer — place order (P1 / CHK)

As a **customer**, I want to complete checkout with payment and shipping, so my purchase succeeds when `processOrder` spans remote services.

**Acceptance:**

1. Checkout orchestrated by Checkout Application Service in BFF.
2. Payment and shipping via integration-service (Onda 5).
3. Tax computed at BFF via tax-service; order stores pre-computed tax lines.
4. Saga failure compensates (cancel order, void payment where possible, cart not cleared).
5. Successful order publishes events via transactional outbox.
6. Saga flag off reverts to legacy in-process `processOrder`.

**Requirement IDs:** CHK-01…CHK-10

### Store admin — view orders (P1 / ORD)

As a **store admin**, I want to list and view orders and status history via existing APIs, so order reads work when persistence is remote.

**Requirement IDs:** ORD-01…ORD-06

### Platform engineer — hub decomposition (P1 / HUB)

As a **platform engineer**, I want checkout APIs to delegate to Checkout Application Service instead of 12 sm-core services, so the BFF is maintainable after extraction.

**Requirement IDs:** HUB-01…HUB-04

### Platform engineer — phased rollout (P1 / STR)

As a **platform engineer**, I want independent feature flags and rollback runbooks for cart, order read, and saga checkout.

**Requirement IDs:** STR-05…STR-07, CART-04, ORD-05, CHK-07

### Developer — contract confidence (P2 / STR)

As a **developer**, I want Pact tests for cart, order read, totals, and checkout commit.

**Requirement IDs:** STR-01…STR-04

### Returning customer — cart merge (P2 / CART)

As a **returning customer**, I want my anonymous cart merged on login.

**Requirement IDs:** CART-08

---

## Core capabilities

### F1 — ShoppingCart service (MVP)

Own cart persistence and line-item CRUD; validate products via catalog HTTP; obtain totals via order-service HTTP. Strangler adapter in BFF. Milestone **SC-ready**.

### F2 — Order service read path (MVP)

Own order persistence for GET/list/history; DTO responses only. Strangler adapter. Milestone **OR-read-ready**.

### F3 — Saga checkout (MVP — highest risk)

Order-service commit endpoint + outbox; BFF orchestrates payment/shipping/tax/cart-clear. Milestone **CHK-ready**.

### F4 — Checkout Application Service (MVP)

Single BFF boundary for place-order, payment, totals, shipping quotes; replaces hub injections.

### F5 — Hub decomposition (MVP)

Route `OrderPaymentApi`, `OrderTotalApi`, `OrderShippingApi` through checkout service; thin `OrderFacadeImpl`.

### F6 — Contracts, Pact, Docker, runbooks (Phase 2–3)

Wave 6 topology; rollback drill; STATE update.

---

## UX

| Persona | Goal |
|---------|------|
| Shopper | Cart and checkout unchanged from UI perspective |
| Admin | Order management unchanged |
| Platform | Toggle flags; observe health; execute rollback runbooks |

**Constraint:** No new storefront/admin screens — behavioral parity only. p95 checkout ≤ 2.5× monolith baseline.

---

## High-level technical constraints

- Integrate with Ondas 1–5 capabilities (reference, tax, merchant, catalog, customer, integration).
- Preserve frozen REST paths (HUB-04).
- No JPA entities in migrated JSON responses.
- Shared operational DB during extraction (AD-003 inherited).
- Execute blocked until Ondas 3–5 complete.
- JWT on `/private/**` equivalent to today.

---

## Non-goals

| Excluded | Why |
|----------|-----|
| Execute before Ondas 3–5 | Hard prerequisite — snapshots, saga, catalog, customer, integration |
| Physical DB-per-service split | Post-Wave 6 |
| Remote tax inside order-service | ADR-006 — BFF owns tax for Wave 6 |
| Extract CheckoutApplicationService to own deployable | AD-024 — optional post-wave |
| Catalog write / ProductType | Onda 4 scope |
| API V1 deprecation | Fase 4 |
| Rewrite ~450 MerchantStore resolver refs | BFF retains resolver |
| Greenfield DB bootstrap split | Assumes populated DB |

---

## Phased rollout

### Phase 0 — Gate

Ondas 3–5 green; saga PoC on `processOrder`; CheckoutApplicationService skeleton.

### Phase 1 — MVP (P1)

- Totals HTTP (cycle break) — **TOT-ready**
- shoppingcart-service + strangler — **SC-ready**
- order-service reads + strangler — **OR-read-ready**
- Saga checkout + hub decomposition — **CHK-ready**

### Phase 2

Pact suite; Docker Compose wave6; JaCoCo gates.

### Phase 3

Health/correlation; rollback runbooks; STATE.md; chaos tests.

### Rollback

| Flag | Action |
|------|--------|
| `wave6.checkout.saga.enabled=false` | Legacy processOrder (immediate) |
| `wave6.order.strangler.enabled=false` | In-process order |
| `wave6.shoppingcart.strangler.enabled=false` | In-process cart |

---

## Success metrics

| Metric | Target |
|--------|--------|
| P1 integration | Cart + order read + checkout in wave6 topology |
| Pact | Green STR-01…04 |
| Cycles | Zero OrderService in shoppingcart-service |
| Hub | OrderFacade checkout ≤ 4 direct sm-core deps |
| Saga | Compensation green in chaos test |
| Rollback | < 5 min all flags false |

---

## Risks

| Risk | Mitigation |
|------|------------|
| Big-bang checkout | Phased flags; CHK-ready gate; canary |
| Cart inconsistency on shadow | Shadow compare + reconciliation |
| Outbox lag | Monitor depth; alert |
| Tax mismatch | Single BFF tax call (ADR-006) |
| Ondas 3–5 delay | Docs ready; code blocked |

---

## Architecture decision records

- [ADR-001: One Compozy workflow for ShoppingCart + Order](adrs/adr-001.md)
- [ADR-002: Checkout Application Service as BFF boundary](adrs/adr-002.md)
- [ADR-003: Saga choreography for processOrder](adrs/adr-003.md)
- [ADR-004: Transactional outbox for order events](adrs/adr-004.md)
- [ADR-005: Hub OrderFacade decomposition](adrs/adr-005.md)
- [ADR-006: Tax calculation at BFF (deferred in order-service)](adrs/adr-006.md)
- [ADR-007: ShoppingCart before Order cutover phasing](adrs/adr-007.md)
- [ADR-008: Feature flags and rollback plan](adrs/adr-008.md)

---

## Open questions

All OQ-01…OQ-08 resolved in `.specs/features/onda-6-shoppingcart-order/context.md`. No blocking ambiguities.

Residual: idempotency key header format (Design T39); outbox relay in-process vs worker (ADR-004 default in-process).
