# PRD: Onda 5 — Integration Service

**Feature slug:** `onda-5-integration-service`
**Source of truth:** TLC at `.specs/features/onda-5-integration-service/` (Option A — authoritative; frozen scope)
**Status:** Ready for TechSpec
**Date:** 2026-07-26

---

## Overview

After Waves 1–2 validated the Strangler pattern and Wave 3 delivers DTO contracts plus checkout saga foundation, Wave 5 extracts **payment and shipping quote orchestration** into `integration-service` so store operators and customers can configure gateways, obtain shipping quotes, and process payments without those capabilities remaining trapped in the monolith's `sm-core` runtime.

Today's problem is architectural: `PaymentModule` and `ShippingQuoteModule` contracts accept JPA entities (`Order`, `Customer`, `ShoppingCartItem`), and `PaymentServiceImpl` writes order status after gateway calls — creating the **order ↔ payments cycle** that makes Order the hardest domain to extract (9/10). Shipping orchestration is less coupled but still pulls catalog pricing and reference data in-process. Wave 5 must be **stateless regarding Order** — integration returns transaction and quote DTOs; the checkout application service (Wave 3) owns order mutations.

This PRD defines **business what and why** for one deployable service behind the existing BFF, with frozen REST paths and no user-facing API breaks. Technical how belongs in the TechSpec and ADRs.

**Primary users:** store administrators (payment/shipping config), storefront customers (quotes and checkout payment), platform engineers (Strangler rollout), and architects unblocking Wave 6 order/payments split.

**Hard prerequisites:** Onda 3 Execute complete (DTO module contracts, checkout app service, saga/outbox foundation); Onda 4 partial (catalog read paths for shipping product snapshots).

---

## Objectives

- Deliver **integration-service** as an independently deployable capability for payment module orchestration and shipping quote orchestration while the monolith remains the customer-facing BFF.
- Preserve existing admin and storefront journeys (payment module setup, shipping configuration, cart shipping quotes, checkout payment) with responses equivalent to today's monolith.
- **Break the order ↔ payments cycle** by ensuring integration-service never persists or mutates `Order` entities.
- Host the existing plugin registry (`Stripe`, `PayPal`, `UPS`, `USPS`, weight rules, etc.) in-process inside integration-service — plugins remain libraries, not separate microservices.
- Prove contract stability (Pact consumer/provider) for P1 migrated surfaces before declaring Wave 5 complete.
- Extend `shopizer-api-contracts` with integration DTOs and HTTP clients following Waves 1–2 patterns.
- Remain **blocked on Onda 3 + Onda 4 partial** — Wave 5 Execute does not start until those gates pass.

### Business outcomes

| Outcome | Indicator |
| ------- | ----------- |
| Wave 6 unblocked | Payment processing path no longer requires in-process `OrderService` from integration |
| Merchant continuity | No breaking change on payment/shipping REST paths used by admin UI and checkout |
| Gateway isolation | New payment/shipping deploys do not require monolith release |
| Honest coupling rank | Wave scheduled 9th per data — not rushed as 4th priority |

---

## User stories

### Store administrator — payment module configuration (P1 / PAY)

As a **store administrator**, I want to configure payment modules via the same admin APIs I use today, so gateway credentials and module enablement are not owned by the monolith runtime.

**Acceptance (business):**

1. List, create, update, and delete payment module configurations per store.
2. Secrets are stored encrypted — same security posture as today.
3. Public store payment method listing reflects configured modules.
4. Tenant identified by store code as today.

**Requirement IDs:** PAY-01…PAY-06

### Storefront customer — shipping quotes (P1 / SHP)

As a **customer**, I want shipping options for my cart via existing APIs, so I can choose delivery without the monolith running carrier plugins in-process.

**Acceptance:**

1. Quote responses match current schema (`ReadableShippingQuote`, options list).
2. Ship-to country list localized via reference capability (Wave 1).
3. Digital-only carts return no shipping required.
4. Product weights come from catalog read API (Wave 4 partial) — not monolith JPA graph.

**Requirement IDs:** SHP-01…SHP-07

### Checkout — stateless payment processing (P1 / PAY)

As the **checkout flow**, I want payment authorization/capture/refund executed via integration-service returning a transaction result, so order status updates stay in the checkout saga and the payments cycle is broken.

**Acceptance:**

1. Process, capture, refund, and init (express checkout) return `TransactionResult` DTOs.
2. Integration-service may persist `Transaction` records but **must not** update `Order`.
3. Gateway failures surface clearly; checkout saga handles compensation.
4. Order referenced by snapshot id only — no `Order` entity crossing the boundary.

**Requirement IDs:** PAY-07…PAY-12

### Platform team — Strangler BFF (P1 / STR)

As a **platform engineer**, I want HTTP delegation for payment/shipping facades behind `wave5.strangler.enabled`, so we validate extraction without rewriting checkout controllers.

**Acceptance:**

1. Strangler on → facades delegate to integration-service; off → legacy in-process.
2. Remote failure → 503 with correlation id — no silent fallback.
3. `OrderPaymentApi` routes through checkout application service + integration client.
4. `OrderShippingApi` assembles DTO requests from cart + catalog snapshots.

**Requirement IDs:** STR-01…STR-06

### Developer — contract confidence (P2 / STR)

As a **developer**, I want Pact tests for P1 payment config and shipping quote surfaces, so breaking DTO changes fail CI before deploy.

**Requirement IDs:** STR-07, STR-08

### Operator — observability (P2 / STR)

As an **operator**, I want health checks and correlation IDs on integration-service, including DB, module registry, reference-service, and catalog-service dependencies.

**Requirement IDs:** STR-08, STR-09

---

## Core capabilities

### F1 — integration-service (MVP)

Own payment/shipping orchestration, plugin registry, merchant integration configuration persistence (shared DB), and internal payment/quote APIs. Port 8086. Stateless w.r.t. Order.

### F2 — sm-integration-core (MVP)

Thin domain module: `PaymentOrchestrator`, `ShippingOrchestrator`, moved plugin implementations, packaging rules, encryption for credentials.

### F3 — Strangler BFF (MVP)

HTTP adapters for payment/shipping configuration facades; checkout wiring for `OrderPaymentApi` / `OrderShippingApi`; `wave5.*` properties coexisting with wave1–4.

### F4 — Contract tests and Compose (Phase 2)

Pact provider/consumer; `docker-compose-wave5.yml`; JaCoCo gates.

---

## User experience

| Persona | Goal |
| ------- | ---- |
| Admin | Configure Stripe/PayPal/shipping modules without noticing runtime change |
| Customer | See shipping options and complete payment as today |
| Platform engineer | Toggle strangler; observe health; trust pact gates |

**UX constraints:** No new screens; behavioral parity is the bar. p95 public endpoints ≤ 2× monolith baseline.

---

## High-level technical constraints

- Integrate with **reference-service** (Wave 1) for country/language resolution in shipping.
- Integrate with **catalog-service read API** (Wave 4 partial) for product weight/dimension snapshots.
- Preserve **frozen REST paths** (STR-06).
- No JPA entities in migrated JSON responses.
- **Shared operational database** during extraction (AD-003 inherited).
- JWT on `/private/**` equivalent to prior waves.
- Execute blocked until **Onda 3 + Onda 4 partial** complete.

---

## Non-goals

| Excluded | Why |
| -------- | --- |
| Execute before Onda 3 + Onda 4 partial | Hard gates — contracts and catalog reads |
| Order / shopping cart service extraction | Wave 6 |
| New payment gateway providers | Out of scope |
| Database-per-service split | AD-003 |
| Feign/WebClient/service mesh | AD-005 RestTemplate pattern |
| Fixing `ConfigurationsApi` payment/shipping null stubs | Incomplete legacy |
| Full catalog CRUD extraction | Wave 4 |
| Replacing all global checkout transactions | Saga foundation Wave 3; full order saga Wave 6 |

---

## Phased rollout

### MVP (Phase 1) — P1 stories

- integration-service: config CRUD, quotes, stateless payment internal APIs.
- sm-integration-core with moved plugins.
- Strangler adapters + checkout wiring.
- Monolith profile preserved for rollback.

**Exit criteria:** P1 endpoints healthy; no Order writes from integration-service; pact green for P1.

### Phase 2

- Full Pact suite; Docker Compose wave5; JaCoCo verify gates.

### Phase 3

- STATE/ROADMAP updated; GAP-INT documented; pattern reusable for Wave 6.

---

## Success metrics

| Metric | Target |
| ------ | ------ |
| P1 endpoint availability | integration-service + BFF paths respond |
| Contract parity | Pact green payment config + shipping quote |
| Entity leakage | Zero JPA types in migrated JSON |
| Cycle break | No `OrderService` call from integration payment path |
| Quote accuracy | Options returned for configured UPS/custom modules |
| Latency | p95 ≤ 2× monolith |
| Gate discipline | No Wave 5 Execute before Onda 3 + Onda 4 partial |

---

## Risks and mitigations

| Risk | Mitigation |
| ---- | ---------- |
| Onda 3 delay blocks calendar | Keep Compozy docs ready; freeze Execute |
| Catalog snapshot incomplete for packaging | GAP-INT-01 documented fallback |
| Legacy plugins resist V2 contracts | AD-017 adapter bridge |
| Merchants notice payment regressions | Strangler rollback profile; pact |
| Scope creep into order extraction | Explicit non-goals; stateless ADR |

---

## Architecture decision records

- [ADR-001: Single Compozy workflow for integration-service](adrs/adr-001.md)
- [ADR-002: Stateless payment orchestration — no Order ownership](adrs/adr-002.md)
- [ADR-003: Shared MySQL for integration configuration](adrs/adr-003.md)
- [ADR-004: PaymentModuleV2 / ShippingQuoteModuleV2 from Onda 3](adrs/adr-004.md)
- [ADR-005: In-process plugin registry in integration-service](adrs/adr-005.md)
- [ADR-006: Checkout APIs remain on BFF with checkout application service](adrs/adr-006.md)
- [ADR-007: Catalog read HTTP for shipping product snapshots](adrs/adr-007.md)

---

## Open questions

All TLC OQ-01…OQ-06 **resolved** in `.specs/features/onda-5-integration-service/context.md`. No blocking product ambiguities remain.

Residual: exact catalog snapshot field set for packaging — confirm at Onda 4 partial gate review.
