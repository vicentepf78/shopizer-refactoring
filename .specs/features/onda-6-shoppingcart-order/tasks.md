# Onda 6 — ShoppingCart + Order Tasks

**Design:** `.specs/features/onda-6-shoppingcart-order/design.md`
**Spec:** `.specs/features/onda-6-shoppingcart-order/spec.md`
**Status:** Approved — Execute blocked until Ondas 3–5 gate
**Testing:** `.specs/codebase/TESTING.md`
**Prerequisite:** Ondas 3, 4, 5 Execute complete (snapshots, saga/outbox PoC, catalog, customer, integration-service)

---

## Execution Plan

### Phase 0: Gate

```
Onda3-T48 ∧ Onda4-T42 ∧ Onda5-T36 ──→ T1
```

### Phase 1: Contracts + Totals API (cycle break foundation)

```
T1 ──→ T2 ──┬──→ T3 [P]
            └──→ T4 [P]
T2,T3,T4 ──→ T5 ──→ T6
```

### Phase 2: ShoppingCart track

```
T6 ──→ T7 ──→ T8 ──→ T9 ──→ T10 ──→ T11 ──→ T12 ──→ T13 ──→ T14
```

### Phase 3: Order core + read path

```
T6 ──→ T15 ──→ T16 ──→ T17 ──→ T18 ──→ T19 ──→ T20 ──→ T21
```

### Phase 4: Saga + outbox

```
T17 ──→ T22 ──→ T23 ──→ T24 ──→ T25 ──→ T26 ──→ T27
```

### Phase 5: Checkout Application Service

```
T25,T14 ──→ T28 ──→ T29 ──→ T30 ──→ T31 ──→ T32
```

### Phase 6: Hub decomposition + Strangler

```
T21,T32 ──→ T33 ──→ T34 ──→ T35 ──→ T36 ──→ T37 ──→ T38
```

### Phase 7: Integration gate

```
T38 ──→ T39 ──┬──→ T40 [P]
              ├──→ T41 [P]
              └──→ T42 [P]
T39,T40,T41,T42 ──→ T43 ──→ T44 ──→ T45
```

**Milestones:** `TOT-ready` = T6 | `SC-ready` = T14 | `OR-read-ready` = T21 | `CHK-ready` = T32

---

## Task Breakdown

### T1: Gate verification — Ondas 3–5 artifacts

**What:** Verify `OrderSnapshot`, saga/outbox PoC, `CheckoutApplicationService` skeleton, catalog/customer/integration services exist and tests pass.
**Where:** CI script `scripts/wave6-gate.sh` (new)
**Depends on:** Onda 3 T48, Onda 4 T42, Onda 5 T36
**Requirement:** CHK-01 (prerequisite)

**Done when:**
- [ ] Gate script exits 0
- [ ] Documented in `.specs/project/STATE.md`

**Tests:** none
**Gate:** `./scripts/wave6-gate.sh`

**Commit:** `chore(wave6): add ondas 3-5 gate script`

---

### T2: Cart/order DTOs in `shopizer-api-contracts`

**What:** Add `CartLineSnapshot`, `ReadableShoppingCart`, `PersistableCartLine`, `CartTotalsRequest`, `CartTotalsResponse`, `OrderSnapshot`, `ReadableOrder` packages.
**Where:** `shopizer-api-contracts/.../cart/`, `.../order/`, `.../checkout/`
**Depends on:** T1
**Reuses:** Onda 3 snapshot types where present
**Requirement:** CART-07, ORD-01, HUB-04

**Done when:**
- [ ] Zero `com.salesmanager.core.model` imports in new DTOs
- [ ] `./mvnw compile -pl shopizer-api-contracts`

**Tests:** unit (serialization)
**Gate:** `./mvnw test -pl shopizer-api-contracts -Dtest=Wave6ContractsSerializationTest`

**Commit:** `feat(contracts): add wave6 cart order checkout DTOs`

---

### T3: Client interfaces — cart, order, totals [P]

**What:** `ShoppingCartServiceClient`, `OrderServiceClient`, `CartTotalsClient`, `CheckoutCommitClient`.
**Where:** `shopizer-api-contracts/.../client/`
**Depends on:** T1
**Requirement:** CART-03, ORD-06

**Done when:**
- [ ] Interfaces compile with DTOs from T2
- [ ] `./mvnw compile -pl shopizer-api-contracts`

**Tests:** none
**Gate:** `./mvnw compile -pl shopizer-api-contracts`

**Commit:** `feat(contracts): add wave6 HTTP client interfaces`

---

### T4: Checkout saga DTOs [P]

**What:** `CheckoutCommitRequest`, `CheckoutCommitResponse`, `SagaStepStatus`, `PaymentStatusUpdate`.
**Where:** `shopizer-api-contracts/.../checkout/`
**Depends on:** T1
**Requirement:** CHK-02, CHK-09

**Done when:**
- [ ] Idempotency key field documented in Javadoc
- [ ] `./mvnw compile -pl shopizer-api-contracts`

**Tests:** unit
**Gate:** `./mvnw test -pl shopizer-api-contracts -Dtest=CheckoutCommitDtoTest`

**Commit:** `feat(contracts): add checkout saga DTOs`

---

### T5: Wave6 Strangler properties + RestTemplate

**What:** Profile `strangler-wave6`; `wave6.*.base-url`, three feature flags; `Wave6ClientConfig` + correlation interceptor.
**Where:** `sm-shop/.../strangler/config/Wave6ClientConfig.java`, `application-strangler-wave6.properties`
**Depends on:** T2, T3, T4
**Requirement:** CART-04, ORD-05, CHK-07

**Done when:**
- [ ] Properties coexist with wave1/wave2
- [ ] `./mvnw test -pl sm-shop -Dtest=Wave6ClientConfigTest`

**Tests:** unit
**Gate:** `./mvnw test -pl sm-shop -Dtest=Wave6ClientConfigTest`

**Commit:** `feat(shop): add wave6 strangler properties and clients`

---

### T6: Cart totals API — monolith boundary (`TOT-ready`)

**What:** Extract `calculateShoppingCartTotal` logic to `CartTotalsService`; expose `POST /internal/v1/orders/totals` in sm-shop (temporary) or sm-order-core module; wire `ShoppingCartCalculationServiceImpl` to HTTP client when flag set.
**Where:** `sm-core/.../order/totals/`, `sm-shop/.../internal/CartTotalsController.java`
**Depends on:** T5
**Requirement:** CART-03, OQ-01

**Done when:**
- [ ] Cart calculation uses HTTP when `wave6.totals.http.enabled=true`
- [ ] Integration test parity with in-process totals
- [ ] `./mvnw test -pl sm-core,sm-shop -Dtest=CartTotalsParityTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=CartTotalsParityTest`

**Commit:** `feat(order): extract cart totals API and break cart-order cycle`

---

### T7: Scaffold `sm-shoppingcart-core`

**What:** New Maven module; move cart repositories.
**Where:** `sm-shoppingcart-core/`, root `pom.xml`
**Depends on:** T2
**Reuses:** Onda 2 `sm-content-core` pattern
**Requirement:** CART-01

**Done when:**
- [ ] `@DataJpaTest` smoke passes
- [ ] `./mvnw test -pl sm-shoppingcart-core`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shoppingcart-core`

**Commit:** `feat(cart-core): scaffold and extract cart repositories`

---

### T8: Move `ShoppingCartService` to sm-shoppingcart-core

**What:** Move `ShoppingCartServiceImpl`; remove direct `OrderService` dependency — inject `CartTotalsClient`.
**Where:** `sm-shoppingcart-core/.../services/shoppingcart/`
**Depends on:** T6, T7
**Requirement:** CART-03

**Done when:**
- [ ] Zero `OrderService` imports in module
- [ ] `./mvnw test -pl sm-shoppingcart-core`

**Tests:** unit + integration
**Gate:** `./mvnw test -pl sm-shoppingcart-core`

**Commit:** `feat(cart-core): extract ShoppingCartService with HTTP totals`

---

### T9: Catalog validation client in cart-core

**What:** `CatalogLineValidator` calling catalog-service HTTP for `ProductLineSnapshot`.
**Where:** `sm-shoppingcart-core/.../integration/`
**Depends on:** T8
**Requirement:** CART-02

**Done when:**
- [ ] Invalid product returns structured error
- [ ] `./mvnw test -pl sm-shoppingcart-core -Dtest=CatalogLineValidatorTest`

**Tests:** unit (mock catalog)
**Gate:** `./mvnw test -pl sm-shoppingcart-core -Dtest=CatalogLineValidatorTest`

**Commit:** `feat(cart-core): catalog HTTP validation for cart lines`

---

### T10: Scaffold `shoppingcart-service` Boot app

**What:** Spring Boot app :8086, JPA, JWT `/private/**`, health actuator.
**Where:** `shoppingcart-service/`
**Depends on:** T8
**Requirement:** CART-01, STR-05

**Done when:**
- [ ] `./mvnw package -pl shoppingcart-service`
- [ ] Context loads with shared DB config

**Tests:** integration
**Gate:** `./mvnw test -pl shoppingcart-service -Dtest=ShoppingCartServiceApplicationTest`

**Commit:** `feat(shoppingcart-service): scaffold application`

---

### T11: Public cart REST controllers

**What:** Mirror `ShoppingCartApi` paths; mappers cart entity ↔ DTO.
**Where:** `shoppingcart-service/.../api/v1/`
**Depends on:** T10
**Requirement:** CART-01, CART-05, CART-06

**Done when:**
- [ ] CRUD paths registered
- [ ] `./mvnw test -pl shoppingcart-service -Dtest=ShoppingCartApiIntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl shoppingcart-service -Dtest=ShoppingCartApiIntegrationTest`

**Commit:** `feat(shoppingcart-service): public cart REST endpoints`

---

### T12: Internal cart checkout cleanup API

**What:** `DELETE /internal/v1/carts/{id}/after-checkout` with `X-Internal-Token`.
**Where:** `shoppingcart-service/.../api/internal/`
**Depends on:** T11
**Requirement:** CHK-06

**Done when:**
- [ ] Token invalid → 401
- [ ] `./mvnw test -pl shoppingcart-service -Dtest=InternalCartControllerTest`

**Tests:** integration
**Gate:** `./mvnw test -pl shoppingcart-service -Dtest=InternalCartControllerTest`

**Commit:** `feat(shoppingcart-service): internal post-checkout cart clear`

---

### T13: Strangler `ShoppingCartFacadeHttpAdapter`

**What:** HTTP adapter in sm-shop; feature flag `wave6.shoppingcart.strangler.enabled`.
**Where:** `sm-shop/.../strangler/cart/`
**Depends on:** T11, T5
**Requirement:** CART-04, STR-06

**Done when:**
- [ ] Flag off → in-process; flag on → HTTP
- [ ] Remote failure → 503 + correlation id
- [ ] `./mvnw test -pl sm-shop -Dtest=ShoppingCartFacadeHttpAdapterTest`

**Tests:** unit + integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=ShoppingCartFacadeHttpAdapterTest`

**Commit:** `feat(shop): shopping cart strangler HTTP adapter`

---

### T14: Cart shadow mode + `SC-ready` gate

**What:** Optional read-remote shadow comparing responses; document cutover runbook.
**Where:** `sm-shop/.../strangler/cart/`, `docs/runbooks/wave6-cart-cutover.md`
**Depends on:** T13
**Requirement:** CART-04, STR-07

**Done when:**
- [ ] Shadow mode logs mismatches without user impact
- [ ] Runbook reviewed
- [ ] Milestone `SC-ready` marked in STATE.md

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=ShoppingCartShadowModeTest`

**Commit:** `feat(shop): cart strangler shadow mode and cutover runbook`

---

### T15: Scaffold `sm-order-core`

**What:** New module; order repositories, `OrderTotalService`.
**Where:** `sm-order-core/`, root `pom.xml`
**Depends on:** T2
**Requirement:** ORD-01

**Done when:**
- [ ] `@DataJpaTest` smoke
- [ ] `./mvnw test -pl sm-order-core`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-order-core`

**Commit:** `feat(order-core): scaffold and extract order repositories`

---

### T16: Move order read services to sm-order-core

**What:** Extract get/list/history from `OrderServiceImpl`; no `PaymentService` in read path.
**Where:** `sm-order-core/.../services/order/`
**Depends on:** T15
**Requirement:** ORD-01, ORD-02, ORD-03

**Done when:**
- [ ] Read methods isolated
- [ ] `./mvnw test -pl sm-order-core -Dtest=OrderReadServiceTest`

**Tests:** unit
**Gate:** `./mvnw test -pl sm-order-core -Dtest=OrderReadServiceTest`

**Commit:** `feat(order-core): extract order read services`

---

### T17: `CartTotalsService` in sm-order-core

**What:** Stateless totals from `CartTotalsRequest`; move logic from T6 monolith boundary.
**Where:** `sm-order-core/.../services/order/totals/`
**Depends on:** T6, T15
**Requirement:** CART-03, ORD-04

**Done when:**
- [ ] Parity tests vs legacy
- [ ] `./mvnw test -pl sm-order-core -Dtest=CartTotalsServiceTest`

**Tests:** unit
**Gate:** `./mvnw test -pl sm-order-core -Dtest=CartTotalsServiceTest`

**Commit:** `feat(order-core): cart totals calculation service`

---

### T18: Scaffold `order-service` Boot app

**What:** Spring Boot :8087, JPA, JWT, actuator, internal token filter.
**Where:** `order-service/`
**Depends on:** T16
**Requirement:** ORD-05, STR-05

**Done when:**
- [ ] `./mvnw package -pl order-service`
- [ ] `./mvnw test -pl order-service -Dtest=OrderServiceApplicationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl order-service -Dtest=OrderServiceApplicationTest`

**Commit:** `feat(order-service): scaffold application`

---

### T19: Public order read REST

**What:** GET order, list orders, status history — mirror monolith paths.
**Where:** `order-service/.../api/v1/`
**Depends on:** T18
**Requirement:** ORD-01, ORD-02, ORD-03

**Done when:**
- [ ] No JPA in JSON responses
- [ ] `./mvnw test -pl order-service -Dtest=OrderReadApiIntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl order-service -Dtest=OrderReadApiIntegrationTest`

**Commit:** `feat(order-service): public order read endpoints`

---

### T20: Internal totals API on order-service

**What:** `POST /internal/v1/orders/totals`; retire monolith-only endpoint from T6.
**Where:** `order-service/.../api/internal/`
**Depends on:** T17, T18
**Requirement:** CART-03

**Done when:**
- [ ] shoppingcart-service calls order-service totals
- [ ] `./mvnw test -pl order-service -Dtest=InternalTotalsControllerTest`

**Tests:** integration
**Gate:** `./mvnw test -pl order-service -Dtest=InternalTotalsControllerTest`

**Commit:** `feat(order-service): internal cart totals API`

---

### T21: Strangler order read adapter + `OR-read-ready`

**What:** `OrderFacadeHttpAdapter` for reads; flag `wave6.order.strangler.enabled`.
**Where:** `sm-shop/.../strangler/order/`
**Depends on:** T19, T5
**Requirement:** ORD-05

**Done when:**
- [ ] Read paths remote when flag on
- [ ] STATE.md milestone `OR-read-ready`
- [ ] `./mvnw test -pl sm-shop -Dtest=OrderFacadeHttpAdapterTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=OrderFacadeHttpAdapterTest`

**Commit:** `feat(shop): order read strangler adapter`

---

### T22: `ORDER_OUTBOX` schema + entity

**What:** Flyway/Liquibase migration; JPA entity `OrderOutboxEntry`.
**Where:** `order-service/src/main/resources/db/`, `sm-order-core/.../outbox/`
**Depends on:** T15
**Requirement:** CHK-03

**Done when:**
- [ ] Migration applies on shared DB
- [ ] `./mvnw test -pl sm-order-core -Dtest=OrderOutboxRepositoryTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-order-core -Dtest=OrderOutboxRepositoryTest`

**Commit:** `feat(order-core): order outbox schema and repository`

---

### T23: Outbox relay scheduler

**What:** `@Scheduled` poller; publish to Spring `ApplicationEventPublisher` (email/inventory hooks).
**Where:** `order-service/.../outbox/OrderOutboxRelay.java`
**Depends on:** T22
**Requirement:** CHK-03, CHK-10

**Done when:**
- [ ] Unpublished rows relayed; `published_at` set
- [ ] `./mvnw test -pl order-service -Dtest=OrderOutboxRelayTest`

**Tests:** integration
**Gate:** `./mvnw test -pl order-service -Dtest=OrderOutboxRelayTest`

**Commit:** `feat(order-service): transactional outbox relay`

---

### T24: Saga — `CheckoutCommitHandler` persist order

**What:** `POST /internal/v1/checkout/commit`; idempotency; persist order + outbox `OrderPlaced` in one transaction.
**Where:** `sm-order-core/.../checkout/`, `order-service/.../api/internal/`
**Depends on:** T22, T18
**Requirement:** CHK-02, CHK-03, CHK-09

**Done when:**
- [ ] Duplicate idempotency key returns same orderId
- [ ] `./mvnw test -pl order-service -Dtest=CheckoutCommitIntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl order-service -Dtest=CheckoutCommitIntegrationTest`

**Commit:** `feat(order-service): checkout commit saga step with outbox`

---

### T25: Saga — payment status update endpoint

**What:** `PATCH /internal/v1/orders/{id}/payment-status`; outbox `OrderPaid` / `OrderCancelled`.
**Where:** `order-service/.../api/internal/`
**Depends on:** T24
**Requirement:** CHK-02, CHK-04

**Done when:**
- [ ] Status transitions validated
- [ ] `./mvnw test -pl order-service -Dtest=PaymentStatusUpdateTest`

**Tests:** integration
**Gate:** `./mvnw test -pl order-service -Dtest=PaymentStatusUpdateTest`

**Commit:** `feat(order-service): payment status saga step`

---

### T26: Remove `PaymentService` from order commit path in sm-core

**What:** Refactor legacy `OrderServiceImpl.processOrder` to delegate to saga when flag on; narrow global AOP pointcut for checkout.
**Where:** `sm-core/.../order/OrderServiceImpl.java`, `shopizer-core-config.xml`
**Depends on:** T24
**Requirement:** CHK-07, GAP-CHK-02

**Done when:**
- [ ] Legacy path preserved when saga flag off
- [ ] `./mvnw test -pl sm-core -Dtest=OrderServiceSagaDelegationTest`

**Tests:** unit
**Gate:** `./mvnw test -pl sm-core -Dtest=OrderServiceSagaDelegationTest`

**Commit:** `refactor(core): delegate processOrder to saga when enabled`

---

### T27: Saga compensation tests

**What:** Test payment failure → order CANCELLED; cart not cleared.
**Where:** `sm-shop/src/test/.../checkout/SagaCompensationTest.java`
**Depends on:** T25, T26
**Requirement:** CHK-02

**Done when:**
- [ ] Compensation paths green
- [ ] `./mvnw test -pl sm-shop -Dtest=SagaCompensationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=SagaCompensationTest`

**Commit:** `test(shop): saga compensation scenarios`

---

### T28: `CheckoutApplicationService` — scaffold orchestrator

**What:** New service in sm-shop; inject wave5 integration client, wave6 cart/order clients, tax client.
**Where:** `sm-shop/.../checkout/CheckoutApplicationService.java`
**Depends on:** T5, T14, T21
**Requirement:** CHK-01, HUB-01

**Done when:**
- [ ] Service compiles with all clients
- [ ] `./mvnw test -pl sm-shop -Dtest=CheckoutApplicationServiceTest`

**Tests:** unit (mocked deps)
**Gate:** `./mvnw test -pl sm-shop -Dtest=CheckoutApplicationServiceTest`

**Commit:** `feat(shop): checkout application service scaffold`

---

### T29: Checkout — tax at BFF (ADR-006)

**What:** `computeTaxLines()` calls tax-service; embed in `OrderSnapshot` for commit.
**Where:** `CheckoutApplicationService`
**Depends on:** T28
**Requirement:** CHK-08, OQ-03

**Done when:**
- [ ] Tax lines in commit request
- [ ] order-service does not call TaxService
- [ ] `./mvnw test -pl sm-shop -Dtest=CheckoutTaxIntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=CheckoutTaxIntegrationTest`

**Commit:** `feat(shop): checkout tax via tax-service at BFF`

---

### T30: Checkout — full place-order saga orchestration

**What:** `placeOrder()` runs saga steps 1–8; flag `wave6.checkout.saga.enabled`.
**Where:** `CheckoutApplicationService`
**Depends on:** T27, T29, T12, T24, T25
**Requirement:** CHK-01, CHK-02, CHK-04, CHK-05, CHK-06

**Done when:**
- [ ] E2E happy path in integration test
- [ ] `./mvnw test -pl sm-shop -Dtest=CheckoutPlaceOrderIntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=CheckoutPlaceOrderIntegrationTest`

**Commit:** `feat(shop): checkout place-order saga orchestration`

---

### T31: Wire `OrderApi` checkout to CheckoutApplicationService

**What:** Replace direct `orderFacade.processOrder` with `checkoutApplicationService.placeOrder` when saga enabled.
**Where:** `sm-shop/.../api/v1/order/OrderApi.java`
**Depends on:** T30
**Requirement:** HUB-04, CHK-07

**Done when:**
- [ ] Both flags paths tested
- [ ] `./mvnw test -pl sm-shop -Dtest=OrderApiCheckoutRoutingTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=OrderApiCheckoutRoutingTest`

**Commit:** `feat(shop): route OrderApi checkout through application service`

---

### T32: `CHK-ready` gate + staging canary checklist

**What:** Document canary rollout; enable saga in docker-compose-wave6 staging profile.
**Where:** `docs/runbooks/wave6-checkout-cutover.md`, `docker-compose-wave6.yml`
**Depends on:** T31
**Requirement:** CHK-07, STR-07

**Done when:**
- [ ] Milestone `CHK-ready` in STATE.md
- [ ] Runbook complete

**Tests:** manual checklist
**Gate:** Staging E2E place-order

**Commit:** `docs(wave6): checkout cutover runbook and CHK-ready gate`

---

### T33: Hub — route `OrderPaymentApi` through checkout

**What:** Remove direct `PaymentService` injection from API; delegate to CheckoutApplicationService.
**Where:** `OrderPaymentApi.java`
**Depends on:** T28
**Requirement:** HUB-02, HUB-03

**Done when:**
- [ ] Zero PaymentService in OrderPaymentApi
- [ ] `./mvnw test -pl sm-shop -Dtest=OrderPaymentApiRoutingTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=OrderPaymentApiRoutingTest`

**Commit:** `refactor(shop): OrderPaymentApi via checkout service`

---

### T34: Hub — route `OrderTotalApi` and `OrderShippingApi`

**What:** Same pattern as T33 for totals and shipping quotes.
**Where:** `OrderTotalApi.java`, `OrderShippingApi.java`
**Depends on:** T28
**Requirement:** HUB-02

**Done when:**
- [ ] Delegation complete
- [ ] `./mvnw test -pl sm-shop -Dtest=OrderTotalShippingApiRoutingTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=OrderTotalShippingApiRoutingTest`

**Commit:** `refactor(shop): order total and shipping APIs via checkout`

---

### T35: Thin `OrderFacadeImpl` — remove checkout sm-core deps

**What:** Checkout methods delegate to CheckoutApplicationService; reduce injections to ≤4 for checkout paths.
**Where:** `OrderFacadeImpl.java`
**Depends on:** T31, T33, T34
**Requirement:** HUB-01, HUB-03

**Done when:**
- [ ] No ProductService/PaymentService on checkout code paths
- [ ] `./mvnw test -pl sm-shop -Dtest=OrderFacadeThinTest`

**Tests:** unit
**Gate:** `./mvnw test -pl sm-shop -Dtest=OrderFacadeThinTest`

**Commit:** `refactor(shop): thin order facade for checkout delegation`

---

### T36: Consolidate duplicate `OrderFacadeImpl` packages

**What:** Merge `facade/v1/OrderFacadeImpl` into single thin delegate or deprecate v1 path.
**Where:** `sm-shop/.../order/facade/`
**Depends on:** T35
**Requirement:** GAP-ORD-01

**Done when:**
- [ ] Single canonical OrderFacade for checkout
- [ ] `./mvnw test -pl sm-shop -Dtest=OrderFacadeConsolidationTest`

**Tests:** unit
**Gate:** `./mvnw test -pl sm-shop -Dtest=OrderFacadeConsolidationTest`

**Commit:** `refactor(shop): consolidate order facade implementations`

---

### T37: Cart merge on login (P2)

**What:** `mergeAnonymousCart` in shoppingcart-service; BFF calls on auth success.
**Where:** `shoppingcart-service`, `CustomerFacade` hook
**Depends on:** T13
**Requirement:** CART-08

**Done when:**
- [ ] Merge integration test passes
- [ ] `./mvnw test -pl shoppingcart-service -Dtest=CartMergeIntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl shoppingcart-service -Dtest=CartMergeIntegrationTest`

**Commit:** `feat(shoppingcart-service): anonymous cart merge on login`

---

### T38: Correlation ID + health indicators Wave6

**What:** Health for catalog/order/customer deps; propagate `X-Correlation-Id` on all wave6 clients.
**Where:** `shoppingcart-service`, `order-service`, `Wave6ClientConfig`
**Depends on:** T13, T21
**Requirement:** STR-05, STR-06

**Done when:**
- [ ] Actuator health shows dependency status
- [ ] `./mvnw test -pl shoppingcart-service,order-service -Dtest=*HealthIndicatorTest`

**Tests:** integration
**Gate:** `./mvnw test -pl shoppingcart-service,order-service -Dtest=*HealthIndicatorTest`

**Commit:** `feat(wave6): correlation and health indicators`

---

### T39: Pact consumer — sm-shop Wave6 [P]

**What:** `Wave6ConsumerPactTest` for cart, order read, totals, checkout commit.
**Where:** `sm-shop/src/test/.../pact/`
**Depends on:** T38
**Requirement:** STR-01

**Done when:**
- [ ] Consumer pacts generated
- [ ] `./mvnw test -pl sm-shop -Dtest=Wave6ConsumerPactTest`

**Tests:** pact
**Gate:** `./mvnw test -pl sm-shop -Dtest=Wave6ConsumerPactTest`

**Commit:** `test(shop): wave6 pact consumer tests`

---

### T40: Pact provider — shoppingcart-service [P]

**What:** `ShoppingCartProviderPactTest`.
**Where:** `shoppingcart-service/src/test/.../pact/`
**Depends on:** T11
**Requirement:** STR-02

**Done when:**
- [ ] Provider verifies consumer pacts
- [ ] `./mvnw test -pl shoppingcart-service -Dtest=ShoppingCartProviderPactTest`

**Tests:** pact
**Gate:** `./mvnw test -pl shoppingcart-service -Dtest=ShoppingCartProviderPactTest`

**Commit:** `test(shoppingcart-service): pact provider`

---

### T41: Pact provider — order-service [P]

**What:** `OrderProviderPactTest` for read + internal totals + commit.
**Where:** `order-service/src/test/.../pact/`
**Depends on:** T24
**Requirement:** STR-03, STR-04

**Done when:**
- [ ] Provider verifies
- [ ] `./mvnw test -pl order-service -Dtest=OrderProviderPactTest`

**Tests:** pact
**Gate:** `./mvnw test -pl order-service -Dtest=OrderProviderPactTest`

**Commit:** `test(order-service): pact provider`

---

### T42: JaCoCo coverage gates Wave6 [P]

**What:** Add jacoco thresholds for shoppingcart-service, order-service, sm-shop strangler package.
**Where:** module `pom.xml` files
**Depends on:** T10, T18
**Requirement:** STR-01

**Done when:**
- [ ] `./mvnw verify -pl shoppingcart-service,order-service,sm-shop -DfailIfNoTests=false`

**Tests:** coverage
**Gate:** `./mvnw verify -pl shoppingcart-service,order-service`

**Commit:** `build(wave6): jacoco coverage gates`

---

### T43: `docker-compose-wave6.yml`

**What:** Full topology: reference, tax, wave2, catalog, customer, integration, cart, order, sm-shop with strangler-wave6 profile.
**Where:** root `docker-compose-wave6.yml`, Dockerfiles
**Depends on:** T32, T38
**Requirement:** STR-07

**Done when:**
- [ ] `docker compose -f docker-compose-wave6.yml config` valid
- [ ] Health checks pass after `up`

**Tests:** manual
**Gate:** `docker compose -f docker-compose-wave6.yml config`

**Commit:** `infra(wave6): docker compose topology`

---

### T44: Wave6 integration test suite

**What:** Cross-service test: add to cart → totals → place order (saga) → read order.
**Where:** `sm-shop/src/test/.../integration/Wave6E2EIntegrationTest.java`
**Depends on:** T43
**Requirement:** CHK-01

**Done when:**
- [ ] E2E passes against compose topology (or Testcontainers)
- [ ] `./mvnw test -pl sm-shop -Dtest=Wave6E2EIntegrationTest -DfailIfNoTests=false`

**Tests:** e2e
**Gate:** `./mvnw test -pl sm-shop -Dtest=Wave6E2EIntegrationTest`

**Commit:** `test(shop): wave6 end-to-end integration`

---

### T45: Update STATE.md + ROADMAP — Wave 6 complete

**What:** Mark Onda 6 Execute complete; document flags, ports, rollback runbooks.
**Where:** `.specs/project/STATE.md`, `ROADMAP.md`
**Depends on:** T39, T40, T41, T44
**Requirement:** all P1

**Done when:**
- [ ] STATE.md AD-020+ recorded
- [ ] `./mvnw clean install` passes (full reactor when all waves merged)

**Tests:** full reactor
**Gate:** `./mvnw clean install`

**Commit:** `docs(project): mark onda 6 wave complete`

---

## Summary

| Phase | Tasks | Count |
|-------|-------|-------|
| Gate + contracts | T1–T5 | 5 |
| Totals / cycle break | T6 | 1 |
| ShoppingCart | T7–T14 | 8 |
| Order read | T15–T21 | 7 |
| Saga + outbox | T22–T27 | 6 |
| Checkout BFF | T28–T32 | 5 |
| Hub + strangler | T33–T38 | 6 |
| Gate | T39–T45 | 7 |
| **Total** | **T1–T45** | **45** |

### Extended TLC tasks (T46–T62) — fine-grained Execute helpers

| Task | What | Depends |
|------|------|---------|
| T46 | Unit tests `CartLineSnapshot` builder | T2 |
| T47 | Unit tests `OrderSnapshot` builder | T2 |
| T48 | `CartTotalsClientRestTemplateImpl` | T5 |
| T49 | `OrderServiceClientRestTemplateImpl` | T5 |
| T50 | `ShoppingCartServiceClientRestTemplateImpl` | T5 |
| T51 | `CheckoutCommitClientRestTemplateImpl` | T5 |
| T52 | Mapper `ReadableShoppingCartMapper` in cart-service | T11 |
| T53 | Mapper `ReadableOrderMapper` in order-service | T19 |
| T54 | JWT security config cart-service (copy wave1 pattern) | T10 |
| T55 | JWT security config order-service | T18 |
| T56 | `wave6.totals.http.enabled` property + tests | T6 |
| T57 | Inventory validation stub in checkout (catalog HTTP) | T28 |
| T58 | Email outbox consumer wiring | T23 |
| T59 | Chaos test kill integration mid-saga | T27 |
| T60 | ArchUnit: no OrderService in shoppingcart-service | T8 |
| T61 | ArchUnit: no PaymentService in order-service commit | T24 |
| T62 | Rollback drill script `scripts/wave6-rollback-drill.sh` | T32 |

**Grand total: 62 TLC tasks** (T1–T62)

---

## Compozy mapping

See `.compozy/tasks/onda-6-shoppingcart-order/_tasks.md` for 16 Compozy tasks covering T1–T62.
