# Onda 5 — Integration Service Specification

**Feature ID:** `onda-5-integration-service`
**Phase:** Specify → Design → Tasks (Execute blocked)
**Complexity:** Large (1 deployable service + Strangler + plugin registry)
**Source:** [MIGRATION-MASTER-PLAN.md](../../../docs/decomposition/MIGRATION-MASTER-PLAN.md) § Onda 5
**Coupling rank:** 9th of 10 extraction domains (lower than originally assumed; still blocks Wave 6 payments split)

---

## Problem Statement

Payment and shipping quote orchestration today lives inside `sm-core` with **MODEL coupling disguised as plugins**: `PaymentModule` and `ShippingQuoteModule` in `sm-core-modules` accept JPA entities (`Order`, `Customer`, `ShoppingCartItem`, `MerchantStore`). `PaymentServiceImpl` mutates `Order` via `OrderService` after gateway calls, creating the **order ↔ payments cycle** that blocks Order service extraction (Wave 6).

Shipping orchestration depends on catalog pricing/weights (`PricingService`, `Product`), reference data (countries), and system configuration — but does not own order state. Payment orchestration depends on system module configuration and gateway plugins under `modules/integration/`.

Without a formal boundary, Wave 6 cannot split payments from order without duplicating gateway logic or breaking checkout. Wave 3 must deliver DTO-based module contracts and checkout saga foundation first; Wave 4 partial must expose catalog read paths for shipping product snapshots.

This spec defines **what** `integration-service` owns, **what** stays in the monolith BFF/checkout layer, and **how** to remain stateless with respect to `Order` entities.

---

## Goals

- [ ] `integration-service` deployable as independent Spring Boot application (:8086)
- [ ] Monolith consumes integration via HTTP Strangler on existing payment/shipping configuration and quote surfaces
- [ ] Zero JPA entity types in REST JSON responses for migrated endpoints
- [ ] Payment and shipping **orchestration** (module registry, config CRUD, gateway calls) owned by integration-service
- [ ] **Stateless** regarding `Order` — service returns transaction/quote DTOs; order persistence stays in monolith/checkout saga
- [ ] `PaymentModuleV2` / `ShippingQuoteModuleV2` DTO contracts from Onda 3 used at runtime boundaries
- [ ] Break `PaymentServiceImpl` → `OrderService.saveOrUpdate` path for new strangler flow
- [ ] Contract tests (Pact) covering P1 payment config, shipping quotes, and module listing endpoints
- [ ] Reuse `shopizer-api-contracts` for shared integration DTOs and HTTP clients

---

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Order entity ownership / `OrderService` extraction | Wave 6 |
| Shopping cart calculation | Wave 6 |
| Tax calculation at checkout | Remains monolith until order split |
| Full catalog CRUD | Wave 4 — only read snapshots for shipping |
| `ProductType` / catalog write paths | Wave 4+ |
| Database split per service | AD-003 inherited — shared schema during runtime extraction |
| New payment gateways beyond existing plugins | No new providers in this wave |
| Marketplace signup / payment stubs | Incomplete; defer |
| Feign/WebClient/service discovery | AD-005 pattern — RestTemplate only |
| Replacing global AOP transaction for checkout | Saga/outbox foundation from Onda 3; full order saga in Wave 6 |
| Stripe/Braintree SDK upgrades | Maintenance; not decomposition scope |

---

## User Stories

### P1: Integration Service — payment module configuration ⭐ MVP

**User Story**: As a store administrator, I want to configure payment modules (Stripe, PayPal, money order, etc.) via existing admin APIs, so gateway setup does not depend on the monolith runtime.

**Why P1**: Configuration is the lowest-risk surface — no order mutation, validates Strangler + JWT + shared DB pattern for integration domain.

**Acceptance Criteria**:

1. WHEN `GET /api/v1/private/modules/payment` THEN `integration-service` SHALL return configured payment modules as DTOs — SHALL NOT expose `IntegrationModule` JPA types
2. WHEN `POST /api/v1/private/modules/payment/{code}` with module configuration THEN service SHALL validate, encrypt secrets, and persist via `MerchantConfigurationService` equivalent
3. WHEN `DELETE /api/v1/private/modules/payment/{code}` THEN service SHALL remove merchant configuration for that module
4. WHEN `GET /api/v1/payment/config` (public store config) THEN service SHALL return accepted payment methods for the store
5. WHEN admin routes require auth THEN JWT validation SHALL match Wave 1–2 pattern for `/private/**`
6. WHEN tenant is identified THEN service SHALL resolve store by code — without requiring `MerchantStore` entity in request body

**Independent Test**: Deploy `integration-service` + `reference-service`; configure Stripe module; list payment methods; verify encrypted config in `MERCHANT_CONFIGURATION`.

**Source components:**

| Role | Path |
|------|------|
| Service | `sm-core/.../services/payments/PaymentServiceImpl.java` |
| Module API | `sm-shop/.../api/v1/payment/PaymentApi.java` |
| Facade | `sm-shop/.../facade/payment/PaymentConfigurationFacadeImpl.java` |
| Contract | `sm-core-modules/.../payment/model/PaymentModule.java` |
| Plugins | `sm-core/.../modules/integration/payment/impl/*.java` |

**Requirement IDs:** PAY-01…PAY-06

---

### P1: Integration Service — shipping configuration and quotes ⭐ MVP

**User Story**: As a storefront customer, I want shipping quotes for my cart via existing APIs, so checkout can show carrier options without the monolith executing shipping plugins in-process.

**Why P1**: Shipping has no order↔payments cycle; quote path is read-heavy and validates catalog snapshot integration from Wave 4.

**Acceptance Criteria**:

1. WHEN `POST /api/v1/auth/cart/{code}/shipping` with delivery address THEN BFF SHALL assemble `ShippingQuoteRequest` DTO and `integration-service` SHALL return `ReadableShippingQuote` with options
2. WHEN shipping modules are configured THEN service SHALL invoke `ShippingQuoteModuleV2` plugins with DTO inputs (delivery, packages, product snapshots)
3. WHEN `GET /api/v1/shipping/countries` THEN service SHALL return ship-to country list using reference-service HTTP for localization
4. WHEN admin configures shipping modules/origin/packaging THEN private configuration APIs SHALL persist settings equivalent to monolith behavior
5. WHEN product weight/dimensions are needed THEN service SHALL fetch `ProductSnapshot` (or shipping subset) from catalog read API — SHALL NOT inject `PricingService` in-process from monolith
6. WHEN no shipping is required for cart items THEN service SHALL return empty quote with `requiresShipping=false`

**Independent Test**: Cart with physical product; POST shipping quote; receive UPS/custom options; verify no `Order` entity in integration-service logs.

**Source components:**

| Role | Path |
|------|------|
| Service | `sm-core/.../services/shipping/ShippingServiceImpl.java` |
| APIs | `sm-shop/.../api/v1/order/OrderShippingApi.java`, `ShippingConfigurationApi.java` |
| Facade | `sm-shop/.../facade/shipping/ShippingFacadeImpl.java` |
| Contract | `sm-core-modules/.../shipping/model/ShippingQuoteModule.java` |
| Plugins | `sm-core/.../modules/integration/shipping/impl/*.java` |

**Requirement IDs:** SHP-01…SHP-07

---

### P1: Integration Service — stateless payment processing ⭐ MVP

**User Story**: As the checkout flow, I want payment authorization/capture/refund executed via integration-service returning a transaction result, so order status updates remain in the checkout application service and the order↔payments cycle is broken.

**Why P1**: Core value of Wave 5 — enables Wave 6 order/payments split.

**Acceptance Criteria**:

1. WHEN `POST /internal/v1/payments/process` with `PaymentProcessRequest` (customer snapshot, payment DTO, cart line snapshots, order snapshot id, amount) THEN service SHALL invoke appropriate `PaymentModuleV2` and return `TransactionResult` — SHALL NOT call `OrderService.saveOrUpdate`
2. WHEN `POST /internal/v1/payments/capture` with capturable transaction reference THEN service SHALL capture and return `TransactionResult`
3. WHEN `POST /internal/v1/payments/refund` with partial flag and amount THEN service SHALL refund via gateway and return `TransactionResult`
4. WHEN `POST /internal/v1/payments/init` for express checkout THEN service SHALL return initialization token/transaction without order persistence
5. WHEN gateway fails THEN service SHALL return structured error — checkout saga in monolith handles order status rollback
6. WHEN transaction is successful THEN service MAY persist `Transaction` record in shared DB — SHALL NOT mutate `Order` rows

**Independent Test**: Mock gateway; process payment via internal API; verify `Transaction` saved; verify no `Order` update in integration-service integration test.

**Requirement IDs:** PAY-07…PAY-12

---

### P1: Strangler BFF — payment and shipping delegation ⭐ MVP

**User Story**: As a platform engineer, I want HTTP adapters for payment/shipping facades behind `wave5.strangler.enabled`, so we can cut over without changing storefront/admin REST paths.

**Acceptance Criteria**:

1. WHEN `wave5.strangler.enabled=true` THEN `PaymentConfigurationFacade` and shipping facades SHALL delegate to `integration-service`
2. WHEN remote failure THEN SHALL return HTTP 503 with `correlationId` — no silent in-process fallback
3. WHEN `OrderPaymentApi` processes payment THEN SHALL use checkout application service → integration HTTP client (not in-process `PaymentService`)
4. WHEN `OrderShippingApi` requests quote THEN SHALL build DTO request from cart facade + catalog snapshots
5. WHEN correlation header present THEN SHALL propagate `X-Correlation-Id` to integration-service

**Requirement IDs:** STR-01…STR-06

---

### P2: Contract tests and observability

**User Story**: As a developer/operator, I want Pact coverage and health indicators for integration-service dependencies.

**Acceptance Criteria**:

1. Pact provider tests for payment config and shipping quote P1 endpoints
2. Consumer pact in `sm-shop` (`Wave5ConsumerPactTest`)
3. Actuator health reports DB, module registry, reference-service, catalog-service reachability
4. JaCoCo gate on `integration-service` and `sm-integration-core` per repository convention

**Requirement IDs:** STR-07…STR-09

---

### P3: Docker local topology

**User Story**: As a developer, I want `docker-compose-wave5.yml` extending Wave 1–4 topology with integration-service.

**Requirement IDs:** STR-10

---

## Requirement Traceability

| ID | Priority | Summary | Wave 3 dependency |
|----|----------|---------|-------------------|
| PAY-01 | P1 | List payment modules | `IntegrationModuleDto` |
| PAY-02 | P1 | Save payment module config | `PersistableIntegrationConfig` |
| PAY-03 | P1 | Delete payment module config | — |
| PAY-04 | P1 | Public accepted payment methods | `PaymentMethodDto` |
| PAY-05 | P1 | JWT on private routes | — |
| PAY-06 | P1 | Store code tenant resolution | `MerchantStoreId` |
| PAY-07 | P1 | Process payment stateless | `PaymentProcessRequest`, `OrderSnapshot` |
| PAY-08 | P1 | Capture payment | `TransactionResult` |
| PAY-09 | P1 | Refund payment | — |
| PAY-10 | P1 | Init transaction (express) | — |
| PAY-11 | P1 | Gateway error mapping | — |
| PAY-12 | P1 | Transaction persistence only | — |
| SHP-01 | P1 | Cart shipping quote | `ShippingQuoteRequest` |
| SHP-02 | P1 | Plugin invocation with DTOs | `ShippingQuoteModuleV2` |
| SHP-03 | P1 | Ship-to countries | reference HTTP |
| SHP-04 | P1 | Admin shipping config | — |
| SHP-05 | P1 | Catalog product snapshots | `ProductSnapshot` (Onda 4) |
| SHP-06 | P1 | Empty quote when not required | — |
| SHP-07 | P1 | Shipping summary DTO | `ShippingSummaryDto` |
| STR-01 | P1 | Strangler facades | `wave5.*` properties |
| STR-02 | P1 | 503 on remote failure | — |
| STR-03 | P1 | OrderPaymentApi → checkout + HTTP | Checkout app service |
| STR-04 | P1 | OrderShippingApi DTO assembly | — |
| STR-05 | P1 | Correlation propagation | — |
| STR-06 | P1 | Frozen REST paths | — |
| STR-07 | P2 | Pact provider | — |
| STR-08 | P2 | Health indicators | — |
| STR-09 | P2 | JaCoCo gates | — |
| STR-10 | P3 | Docker Compose wave5 | — |

---

## Success Criteria

- [ ] `integration-service` health UP with module registry loaded
- [ ] Payment config CRUD parity with monolith for ≥ 2 modules (e.g. moneyorder + stripe)
- [ ] Shipping quote returns options for configured store with catalog snapshot data
- [ ] Payment process via internal API does not update `Order` table
- [ ] Pact green for P1 surfaces
- [ ] `wave5.strangler.enabled` profile documented in Compose
- [ ] Pattern documented in `STATE.md` for Wave 6 reuse

---

## Open Questions

All OQ-01…OQ-06 resolved in `context.md`. No blocking product ambiguities remain.

**Residual (non-blocking):**

- Exact catalog snapshot fields for packaging — confirm with Onda 4 partial deliverable
- Whether `TransactionService` moves entirely to integration-service or shared thin module — see ADR-003
