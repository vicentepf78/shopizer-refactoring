# Onda 6 — ShoppingCart + Order Specification

**Feature ID:** `onda-6-shoppingcart-order`
**Phase:** Specify + Design (Execute blocked until Ondas 3–5 gate green)
**Complexity:** XLarge (2 services + checkout boundary + saga; coupling 9/10)
**Source:** [MIGRATION-MASTER-PLAN.md](../../../docs/decomposition/MIGRATION-MASTER-PLAN.md) § Onda 6
**Exploration:** Coupling analysis — order hub, cart↔order cycle, `processOrder` transactional (2026-07-04)

---

## Problem Statement

Onda 6 is the **last and highest-risk extraction wave**. Order is the central orchestrator (difficulty **9/10**): six outbound domain dependencies, a transactional `processOrder` spanning payments, shipping, tax, catalog, customer, and cart, and a checkout hub in `sm-shop` that injects **12 sm-core services** plus `CustomerFacade` and `ShoppingCartFacade`. Two critical cycles block naive splits:

- **order ↔ payments** — `OrderServiceImpl.processOrder` → `paymentService.processPayment`; `PaymentServiceImpl` → `orderService.saveOrUpdate`
- **order ↔ shoppingcart** — `ShoppingCartCalculationServiceImpl` → `orderService.calculateShoppingCartTotal`; `OrderServiceImpl` → `shoppingCartService`

Without formal specification, teams either extract cart and order together in one big-bang (unacceptable rollback surface) or defer indefinitely while the monolith remains the checkout authority.

Ondas 3–5 deliver the prerequisites: DTO snapshots, Checkout Application Service, saga/outbox on `processOrder`, catalog/customer/integration services. This spec defines **what** to extract (`shoppingcart-service`, `order-service`), **what** stays in the BFF (`CheckoutApplicationService`), and **how** to phase cutover with explicit feature flags and rollback.

---

## Goals

- [ ] `shoppingcart-service` and `order-service` deployable as independent Spring Boot applications
- [ ] Monolith BFF consumes both via HTTP Strangler; frozen REST paths for cart and order APIs
- [ ] **Cart↔order cycle broken** — cart totals via HTTP contract, not in-process `OrderService`
- [ ] **Checkout Application Service** is the sole checkout orchestration boundary in `sm-shop`
- [ ] `processOrder` runs as **saga choreography** with **transactional outbox** (Onda 3 pattern), not global AOP transaction
- [ ] Hub decomposition: `OrderFacadeImpl` reduced to delegation; bypass APIs (`OrderPaymentApi`, `OrderTotalApi`, `OrderShippingApi`) routed through checkout boundary
- [ ] Feature flags per domain with documented rollback (`wave6.*`)
- [ ] Pact coverage for P1 cart, totals, order read, checkout commit
- [ ] Zero JPA entities in migrated REST JSON responses

---

## Out of Scope

| Feature | Reason |
|---------|--------|
| Physical database split per service | AD-003 inherited — shared `SALESMANAGER` schema during transition |
| Moving `CheckoutApplicationService` to its own deployable | AD-024 — stays in BFF for Wave 6; optional post-wave extraction |
| Remote tax calculation inside `order-service` | OQ-03 / ADR-006 — tax lines supplied by BFF from `tax-service` |
| Full catalog write extraction | Onda 4 scope |
| Rewriting payment/shipping plugins | Onda 5 `integration-service` |
| Eliminating `MerchantStoreArgumentResolver` (~450 refs) | BFF retains resolver; passes `MerchantStoreId` / snapshots |
| Order analytics, reporting, BI exports | Not in checkout critical path |
| Greenfield `InitializationDatabaseImpl` split | Services assume populated DB |
| API V1 deprecation | Fase 4 roadmap |

---

## User Stories

### P1: Shopping Cart — CRUD and session cart ⭐ MVP

**User Story**: As a storefront visitor, I want to add, update, and remove items in my shopping cart via existing `/api/v1/cart` endpoints, so checkout preparation works when the cart runtime is outside the monolith.

**Why P1**: Cart has difficulty 7/10 and must be extracted **before** order cutover to break the calculation cycle via HTTP totals.

**Acceptance Criteria**:

1. WHEN `GET/POST/PUT/DELETE` cart endpoints with `store` header THEN `shoppingcart-service` SHALL persist `ShoppingCart` + line items scoped by store and customer/session
2. WHEN cart line references a product THEN `shoppingcart-service` SHALL validate availability via `catalog-service` HTTP (`ProductLineSnapshot`) — SHALL NOT load full `Product` JPA graph
3. WHEN cart display needs totals THEN BFF or `shoppingcart-service` SHALL call `POST /internal/v1/orders/totals` on order-service (or checkout boundary) with `CartTotalsRequest` — SHALL NOT call in-process `OrderService.calculateShoppingCartTotal`
4. WHEN `wave6.shoppingcart.strangler.enabled=false` THEN monolith in-process cart behavior SHALL remain unchanged
5. WHEN remote cart unavailable THEN BFF SHALL return HTTP 503 with `X-Correlation-Id` — no silent fallback

**Independent Test**: Deploy `shoppingcart-service` + dependencies; add item; read cart; verify totals via totals API; strangler flag toggles in-process vs remote.

**Source components:**

| Role | Path |
|------|------|
| Entities | `sm-core-model/.../shoppingcart/` |
| Services | `sm-core/.../services/shoppingcart/` |
| Calculation | `sm-core/.../shoppingcart/ShoppingCartCalculationServiceImpl.java` |
| API | `sm-shop/.../api/v1/shoppingCart/ShoppingCartApi.java` |
| Facade | `sm-shop/.../shoppingCart/ShoppingCartFacadeImpl.java` |

**Requirement IDs:** CART-01…CART-07

---

### P1: Order — read, list, status history ⭐ MVP

**User Story**: As a store admin or customer, I want to view orders and status history via existing order APIs, so order reads work when order persistence is remote.

**Why P1**: Read paths are lower risk than `processOrder`; validates Strangler before saga cutover.

**Acceptance Criteria**:

1. WHEN `GET` order by id/code THEN `order-service` SHALL return `ReadableOrder` DTO — no `Order` entity in JSON
2. WHEN admin lists orders with criteria THEN `order-service` SHALL support pagination/filter equivalent to monolith
3. WHEN status history requested THEN `order-service` SHALL return `ReadableOrderStatusHistory` list
4. WHEN customer snapshot needed THEN `order-service` MAY call `customer-service` HTTP — SHALL use `CustomerSnapshot`, not merge in global transaction
5. WHEN `wave6.order.strangler.enabled=false` THEN read paths remain in-process

**Requirement IDs:** ORD-01…ORD-06

---

### P1: Checkout — place order via saga ⭐ MVP (highest risk)

**User Story**: As a customer completing checkout, I want to place an order with payment and shipping, so the purchase completes correctly when `processOrder` runs across remote services.

**Why P1**: Core revenue path; requires saga/outbox from Onda 3 and integration-service from Onda 5.

**Acceptance Criteria**:

1. WHEN `POST` checkout/place-order (existing paths) THEN `CheckoutApplicationService` in BFF SHALL orchestrate: validate cart → compute totals → reserve/validate inventory → initiate payment via `integration-service` → persist order via `order-service` saga endpoint → clear cart
2. WHEN `processOrder` saga step fails THEN system SHALL run compensating actions (payment void/refund per module capability, order status `CANCELLED`, cart not cleared)
3. WHEN order persisted THEN `order-service` SHALL write domain events to **transactional outbox** (`ORDER_OUTBOX`) in same DB transaction as order row
4. WHEN outbox relay runs THEN events (`OrderPlaced`, `OrderPaid`, etc.) SHALL be published for downstream consumers (email, inventory)
5. WHEN `wave6.checkout.saga.enabled=false` THEN legacy in-process `orderService.processOrder` SHALL execute (rollback path)
6. WHEN tax required THEN BFF SHALL call `tax-service` and pass tax lines in `OrderSnapshot` — order-service SHALL NOT call tax in-process (OQ-03)

**Requirement IDs:** CHK-01…CHK-10

---

### P1: Hub decomposition — thin facades ⭐ MVP

**User Story**: As a platform engineer, I want checkout APIs to stop injecting 12 sm-core services directly, so the BFF boundary is maintainable after extraction.

**Acceptance Criteria**:

1. WHEN `OrderApi`, `OrderPaymentApi`, `OrderTotalApi`, `OrderShippingApi` handle checkout-related operations THEN they SHALL delegate to `CheckoutApplicationService` only
2. WHEN non-checkout order operations (read, history) THEN `OrderFacade` MAY delegate to `order-service` HTTP adapter
3. WHEN hub decomposition complete THEN `OrderFacadeImpl` SHALL NOT inject `PaymentService`, `ShippingService`, `ProductService` directly for checkout paths

**Requirement IDs:** HUB-01…HUB-04

---

### P2: Cart merge on login

**User Story**: As a returning customer, I want my anonymous cart merged when I log in.

**Requirement IDs:** CART-08

---

### P2: Contract tests (Pact)

**User Story**: As a developer, I want Pact tests for cart, totals, order read, and checkout commit.

**Requirement IDs:** STR-01…STR-04

---

### P3: Observability and rollback runbooks

**User Story**: As an operator, I want health checks, correlation IDs, and documented rollback for each Wave 6 flag.

**Requirement IDs:** STR-05…STR-07

---

## Functional Requirements Summary

| ID | Area | Priority | Summary |
|----|------|----------|---------|
| CART-01 | Cart | P1 | CRUD line items, session/customer scope |
| CART-02 | Cart | P1 | Product validation via catalog HTTP |
| CART-03 | Cart | P1 | Totals via order-service HTTP (cycle break) |
| CART-04 | Cart | P1 | Strangler flag `wave6.shoppingcart.strangler.enabled` |
| CART-05 | Cart | P1 | Promo codes / cart attributes preserved |
| CART-06 | Cart | P1 | Mini-cart and cart count endpoints |
| CART-07 | Cart | P1 | No JPA in JSON |
| CART-08 | Cart | P2 | Anonymous cart merge on login |
| ORD-01 | Order | P1 | Get order by id |
| ORD-02 | Order | P1 | List/search orders (admin) |
| ORD-03 | Order | P1 | Status history |
| ORD-04 | Order | P1 | Order totals breakdown (read) |
| ORD-05 | Order | P1 | Strangler flag `wave6.order.strangler.enabled` |
| ORD-06 | Order | P1 | Internal saga API for checkout commit |
| CHK-01 | Checkout | P1 | CheckoutApplicationService orchestration |
| CHK-02 | Checkout | P1 | Saga choreography for processOrder |
| CHK-03 | Checkout | P1 | Transactional outbox on order persist |
| CHK-04 | Checkout | P1 | Payment via integration-service |
| CHK-05 | Checkout | P1 | Shipping quote via integration-service |
| CHK-06 | Checkout | P1 | Cart clear after successful commit |
| CHK-07 | Checkout | P1 | Saga rollback flag `wave6.checkout.saga.enabled` |
| CHK-08 | Checkout | P1 | Tax lines from BFF (not order-service) |
| CHK-09 | Checkout | P1 | Idempotent checkout with client token |
| CHK-10 | Checkout | P1 | Email notification via async outbox consumer |
| HUB-01 | Hub | P1 | Decompose OrderFacadeImpl checkout paths |
| HUB-02 | Hub | P1 | Route bypass APIs through checkout |
| HUB-03 | Hub | P1 | Reduce direct sm-core injections |
| HUB-04 | Hub | P1 | Preserve frozen REST paths |
| STR-01 | Strangler | P2 | Pact consumer (sm-shop) |
| STR-02 | Strangler | P2 | Pact provider cart |
| STR-03 | Strangler | P2 | Pact provider order |
| STR-04 | Strangler | P2 | Pact checkout commit |
| STR-05 | Ops | P3 | Actuator health per service |
| STR-06 | Ops | P3 | Correlation ID propagation |
| STR-07 | Ops | P3 | Rollback runbook per flag |

---

## Phased Rollout

### Phase 0 — Gate (no Wave 6 code)

- Ondas 3, 4, 5 Execute complete
- Saga/outbox PoC green on `processOrder` in monolith
- `CheckoutApplicationService` skeleton merged

### Phase 1 — Contracts + cycle break (MVP foundation)

- `CartTotalsRequest`/`CartTotalsResponse`, `OrderSnapshot`, cart/order clients
- Wave 6 Strangler properties
- Totals API on order boundary (in monolith first, then order-service)

### Phase 2 — ShoppingCart extraction (shadow → cutover)

- `sm-shoppingcart-core`, `shoppingcart-service`
- Strangler shadow: dual-write or read-remote/write-local per ADR-007
- **Milestone `SC-ready`**: cart CRUD remote, totals HTTP

### Phase 3 — Order read extraction

- `sm-order-core`, `order-service` read APIs
- **Milestone `OR-read-ready`**: order GET/list remote

### Phase 4 — Saga checkout cutover (highest risk)

- Saga endpoint on `order-service`
- `CheckoutApplicationService` full orchestration
- **Milestone `CHK-ready`**: place-order via saga with rollback flag

### Phase 5 — Hub decomposition + hardening

- Thin facades, Pact, Docker Compose wave6, STATE update

### Rollback plan

| Flag | Rollback action |
|------|-----------------|
| `wave6.checkout.saga.enabled=false` | Revert to in-process `processOrder` (immediate) |
| `wave6.order.strangler.enabled=false` | Order reads/writes in monolith |
| `wave6.shoppingcart.strangler.enabled=false` | Cart in monolith |
| All false | Full monolith checkout path — Wave 6 services idle |

---

## Success Metrics

| Metric | Target |
|--------|--------|
| P1 endpoints available | Cart + order read + checkout in integration topology |
| Contract parity | Pact green for STR-01…STR-04 |
| Cycle elimination | Zero `OrderService` import in `shoppingcart-service` |
| Hub reduction | `OrderFacadeImpl` ≤ 4 sm-core injections for checkout (delegates to CheckoutApplicationService) |
| Saga reliability | 0 lost orders in chaos test (payment fail → compensated) |
| Latency | p95 checkout ≤ 2.5× monolith baseline (acceptable for final wave) |
| Rollback time | < 5 min to disable all `wave6.*` flags |

---

## Risks

| Risk | Mitigation |
|------|------------|
| Big-bang checkout failure | Phased flags; saga rollback; CHK-ready gate before production cutover |
| Dual-write cart inconsistency | Shadow mode + reconciliation job; ADR-007 phasing |
| Outbox relay lag | Monitor outbox depth; alert > 100 pending |
| Tax mismatch remote vs local | BFF owns tax call; single source in checkout request (ADR-006) |
| integration-service unavailable | Checkout fails fast 503; no partial order without payment state |
| Shared DB migration conflicts | Flyway/Liquibase coordination — order-service owns ORDER_* migrations only |

---

## Open Questions

All OQ-01…OQ-08 resolved in `context.md`. No blocking product ambiguities.

Residual (non-blocking):

- Exact idempotency key format for checkout (`Idempotency-Key` header vs body) — decide in Design T39
- Outbox relay: in-process vs standalone worker — default in-process (ADR-023)
