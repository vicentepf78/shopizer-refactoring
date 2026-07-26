# OQ Resolutions — Onda 6 Design (2026-07-26)

Decisions confirmed for the final extraction wave. Details in `design.md`.

| ID | Decision | Choice |
|----|----------|--------|
| OQ-01 | Cart↔order cycle break | **Checkout totals API** — `CartTotalsClient` calls order-service (or checkout boundary) for `OrderTotalSummary`; cart-service SHALL NOT inject `OrderService` in-process |
| OQ-02 | `processOrder` consistency | **Saga choreography + transactional outbox** (Onda 3 deliverable) — no distributed 2PC; compensating steps for payment/shipping failures |
| OQ-03 | Tax at checkout | **Stay in monolith BFF** for Wave 6 Execute — `CheckoutApplicationService` calls `tax-service` HTTP; order-service receives pre-computed tax lines in `OrderSnapshot` (ADR-006) |
| OQ-04 | Hub `OrderFacadeImpl` (12 services) | **Decompose into Checkout Application Service + thin facades** — checkout orchestration leaves facade; read/update paths split by concern |
| OQ-05 | Extraction order | **ShoppingCart first (shadow), Order second (saga-ready)** — cart cutover before `processOrder` remote |
| OQ-06 | Shared DB | **Retain shared MySQL** (AD-003 inherited) — physical DB split deferred post-Wave 6 |
| OQ-07 | Rollback | **Per-domain feature flags** `wave6.shoppingcart.strangler.enabled`, `wave6.order.strangler.enabled`, `wave6.checkout.saga.enabled` — independent rollback |
| OQ-08 | Bypass APIs (`OrderPaymentApi`, `OrderTotalApi`, `OrderShippingApi`) | **Route through Checkout Application Service** in BFF — no direct sm-core service injection after hub decomposition |

**Additional design decisions:**

| ID | Decision |
|----|----------|
| AD-020 | One Compozy workflow `onda-6-shoppingcart-order` for both services |
| AD-021 | `sm-shoppingcart-core` and `sm-order-core` thin modules |
| AD-022 | `shoppingcart-service` :8086, `order-service` :8087 |
| AD-023 | Outbox table `ORDER_OUTBOX` owned by order-service; relay in-process initially |
| AD-024 | `CheckoutApplicationService` lives in `sm-shop` until post-Wave 6 optional extraction |
| AD-025 | Pact covers cart CRUD, cart totals, order read, checkout commit (saga start) |

**Prerequisites (assumed complete):**

- Onda 3: `OrderSnapshot`, `CustomerSnapshot`, `ProductLineSnapshot`, `CheckoutApplicationService` skeleton, saga/outbox PoC on `processOrder`
- Onda 4: `catalog-service` read + `customer-service` with `CustomerSnapshot`
- Onda 5: `integration-service` (payments/shipping stateless); DTO `PaymentModule` / `ShippingQuoteModule`

**Status:** Ready for Tasks
