# Onda 5 — Integration Service Tasks

**Design:** `.specs/features/onda-5-integration-service/design.md`
**Spec:** `.specs/features/onda-5-integration-service/spec.md`
**Status:** Approved — Execute blocked on Onda 3 + Onda 4 partial
**Testing:** `.specs/codebase/TESTING.md`
**Prerequisite:** Onda 3 Execute (`onda-3-contracts-checkout`); Onda 4 partial catalog read (`ProductSnapshot` shipping fields)

---

## Execution Plan

### Phase 0: Gates (external)

```
Onda3-T_complete ──→ Onda4-T_catalog_read_partial ──→ T1
```

### Phase 1: Contracts + Wave5 Config

```
T1 ──→ T2 ──┬──→ T3 [P]
            └──→ T4 [P]
T2,T3,T4 ──→ T5
```

### Phase 2: Core Extraction (2 parallel tracks)

```
T5 ──┬──→ T6 ──→ T7 ──→ T8 ──→ T9 ──→ T10 ──→ T11
     │
     └──→ T12 ──→ T13 ──→ T14 ──→ T15 ──→ T16
```

**Track A (Payment):** T6–T11
**Track B (Shipping):** T12–T16

### Phase 3: integration-service Boot + APIs

```
T11,T16 ──→ T17 ──→ T18 ──→ T19 ──→ T20 ──→ T21 ──→ T22
```

### Phase 4: Stateless boundary + Strangler

```
T22 ──→ T23 ──→ T24 ──→ T25 ──→ T26 ──→ T27 ──→ T28
```

### Phase 5: Integration & Gate

```
T28 ──→ T29 ──┬──→ T30 [P]
              └──→ T31 [P]
T30,T31 ──→ T32 ──→ T33 ──→ T34 ──→ T35 ──→ T36 ──→ T37 ──→ T38
```

**Milestones:**
- **P-ready:** T11 — payment orchestrator + plugins compile in `sm-integration-core`
- **S-ready:** T16 — shipping orchestrator + catalog client
- **I-ready:** T22 — integration-service responds to health + admin config

---

## Task Breakdown

### T1: Verify Onda 3 integration contract artifacts

**What:** Assert `PaymentModuleV2`, `ShippingQuoteModuleV2`, `OrderSnapshot`, `CustomerSnapshot`, `CartLineSnapshot`, checkout application service skeleton exist and compile.
**Where:** `sm-core-modules`, `shopizer-api-contracts`, `sm-shop/.../checkout/`
**Gate:** `./mvnw compile -pl sm-core-modules,shopizer-api-contracts,sm-shop -am`
**Depends:** Onda 3 complete

### T2: Integration DTOs in shopizer-api-contracts

**What:** Add `com.salesmanager.contracts.integration` package: `PaymentProcessRequest`, `PaymentCaptureRequest`, `PaymentRefundRequest`, `PaymentInitRequest`, `TransactionResult`, `ShippingQuoteRequest`, `ShippingQuoteResponse`, `ShippingProductSnapshot`, `ShippingSummaryRequest`, `IntegrationModuleDto`, `PaymentMethodDto`, `ShippingOptionDto`, `DeliveryDto`.
**Gate:** Unit tests for JSON serialization; no `com.salesmanager.core.model` imports.

### T3: IntegrationServiceClient interface

**What:** Define `IntegrationServiceClient` in `com.salesmanager.contracts.client` with process/capture/refund/init/quote/summary methods.
**Depends:** T2
**Gate:** Compiles in isolation.

### T4: Wave5 Strangler properties and RestTemplate

**What:** Profile `strangler-wave5`, `wave5.integration-service.base-url`, `wave5.integration-service.internal-token`, `wave5.catalog-service.base-url`, `Wave5ClientConfig`, correlation interceptor.
**Depends:** T2
**Gate:** `Wave5ClientConfigTest` in sm-shop.

### T5: Register sm-integration-core Maven module

**What:** Create `sm-integration-core` pom; depend on `sm-core-modules`, `sm-core-model`, `shopizer-api-contracts`; register in reactor.
**Depends:** T1, T3, T4
**Gate:** `./mvnw compile -pl sm-integration-core -am`

### T6: Move payment plugin implementations to sm-integration-core

**What:** Relocate `sm-core/.../modules/integration/payment/impl/*` to `sm-integration-core`; update `ModulesConfiguration` bean wiring.
**Depends:** T5
**Gate:** Existing payment module unit tests pass in new module.

### T7: PaymentModuleV2 adapter bridge

**What:** Create `PaymentModuleV2Adapter` wrapping legacy `PaymentModule` entities using Onda 3 mappers — ponytail: temporary until plugins natively implement V2.
**Depends:** T6
**Gate:** Adapter unit test with mock legacy module.

### T8: Extract PaymentOrchestrator from PaymentServiceImpl

**What:** Move config CRUD, `getPaymentMethods`, `validateCreditCard`, module resolution to `PaymentOrchestratorImpl` — exclude order mutation methods initially.
**Depends:** T7
**Gate:** Unit tests for config save/load encryption.

### T9: Catalog-free payment paths in orchestrator

**What:** Ensure payment orchestrator has no `OrderService` dependency; inject `TransactionService` only.
**Depends:** T8
**Gate:** ArchUnit or grep gate — no `OrderService` in `sm-integration-core`.

### T10: Internal payment operation methods

**What:** Implement `process`, `capture`, `refund`, `init` on orchestrator using `PaymentModuleV2` and returning `TransactionResult`.
**Depends:** T9
**Gate:** Integration test with `MoneyOrderPayment` or mock module.

### T11: P-ready marker — payment core tests

**What:** JaCoCo baseline for payment orchestrator package ≥ 70%.
**Depends:** T10
**Gate:** `./mvnw test -pl sm-integration-core -Dtest=PaymentOrchestrator*`

### T12: Move shipping plugin implementations to sm-integration-core

**What:** Relocate shipping `impl/*`, `DefaultPackagingImpl`, preprocessors.
**Depends:** T5
**Gate:** Shipping module tests pass.

### T13: ShippingQuoteModuleV2 adapter bridge

**What:** Adapter from legacy `ShippingQuoteModule` to V2 DTO contracts.
**Depends:** T12
**Gate:** Unit test with `StorePickupShippingQuote`.

### T14: CatalogServiceClient for shipping snapshots

**What:** HTTP client fetching `ShippingProductSnapshot` list from catalog-service read API (Onda 4); fallback documented GAP-INT-01.
**Depends:** T13, Onda 4 partial
**Gate:** Client test with WireMock catalog fixture.

### T15: Extract ShippingOrchestrator from ShippingServiceImpl

**What:** Move quote assembly, packaging, module iteration, `requiresShipping`, metadata to `ShippingOrchestratorImpl`; use `ReferenceServiceClient` for countries.
**Depends:** T14
**Gate:** Unit test quote with two modules.

### T16: S-ready marker — shipping core tests

**What:** JaCoCo baseline shipping orchestrator ≥ 70%.
**Depends:** T15
**Gate:** `./mvnw test -pl sm-integration-core -Dtest=ShippingOrchestrator*`

### T17: integration-service Spring Boot scaffold

**What:** New module `integration-service`, port 8086, scan `sm-integration-core`, JWT security, actuator.
**Depends:** T11, T16
**Gate:** Context loads `./mvnw -pl integration-service -am test -Dtest=IntegrationServiceApplicationTest`

### T18: Payment admin REST controllers

**What:** Mirror `PaymentApi` private module config endpoints on integration-service.
**Depends:** T17
**Gate:** MockMvc tests per PAY-01..PAY-06.

### T19: Shipping admin REST controllers

**What:** Mirror `ShippingConfigurationApi` endpoints.
**Depends:** T17
**Gate:** MockMvc tests SHP-04.

### T20: Public payment methods endpoint

**What:** `GET` accepted payment methods for store.
**Depends:** T18
**Gate:** Pact-ready response schema.

### T21: Internal payment REST controller

**What:** `/internal/v1/payments/*` with `X-Internal-Token` filter.
**Depends:** T18, T10
**Gate:** Integration test process payment; assert no Order repository bean.

### T22: Internal shipping REST controller

**What:** `/internal/v1/shipping/quote` and `/summary` with token auth.
**Depends:** T19, T15
**Gate:** I-ready — quote integration test.

### T23: Trim sm-core payment/shipping services

**What:** `PaymentServiceImpl`/`ShippingServiceImpl` delegate to orchestrator when extracted, or `@Deprecated` stubs pointing to migration guide.
**Depends:** T22
**Gate:** Monolith compiles; existing tests green in non-strangler profile.

### T24: Stateless payment boundary in monolith

**What:** Remove `orderService.saveOrUpdate` from payment path when `wave5.strangler.enabled`; checkout saga performs order update.
**Depends:** T23, Onda 3 checkout saga
**Gate:** Test: payment via checkout does not call in-process order save from PaymentServiceImpl.

### T25: PaymentFacadeHttpAdapter

**What:** Strangler adapter for `PaymentConfigurationFacadeImpl`.
**Depends:** T24
**Gate:** Adapter test 503 on connection failure.

### T26: ShippingFacadeHttpAdapter

**What:** Strangler adapter for `ShippingFacadeImpl` and configuration facade.
**Depends:** T24
**Gate:** Adapter test propagates correlation id.

### T27: CheckoutApplicationService integration client wiring

**What:** `OrderPaymentApi` routes through checkout service → `IntegrationServiceClient`; build `PaymentProcessRequest` from cart/order snapshots.
**Depends:** T25, T26
**Gate:** Integration test checkout payment E2E (mock gateway).

### T28: OrderShippingApi DTO assembly

**What:** Build `ShippingQuoteRequest` from cart facade + catalog snapshots; call integration client.
**Depends:** T27
**Gate:** Shipping quote E2E test.

### T29: Correlation ID and health indicators

**What:** `CorrelationIdFilter`; health for DB, reference-service, catalog-service, module registry.
**Depends:** T28
**Gate:** Actuator health test.

### T30: Pact provider tests — integration-service

**What:** Provider pacts for payment config + shipping quote P1.
**Depends:** T29
**Gate:** `./mvnw test -pl integration-service -Dtest=IntegrationProviderPactTest`

### T31: Wave5ConsumerPactTest in sm-shop

**What:** Consumer contracts for strangler client.
**Depends:** T29
**Gate:** Pact publish/verify green.

### T32: IntegrationServiceClientRestTemplateImpl

**What:** Full HTTP client implementation for BFF.
**Depends:** T30, T31
**Gate:** Client integration test against TestRestTemplate.

### T33: Rewire ModulesConfiguration in monolith

**What:** Remove duplicate plugin beans from sm-core when strangler enabled; document rollback.
**Depends:** T32
**Gate:** Profile `strangler-wave5` starts without bean duplication.

### T34: docker-compose-wave5.yml

**What:** Add integration-service; env `WAVE5_INTEGRATION_BASE_URL`, `INTEGRATION_INTERNAL_TOKEN`; depends on catalog partial.
**Depends:** T33
**Gate:** `docker compose -f docker-compose-wave5.yml config`

### T35: Dockerfile.wave5 for integration-service

**What:** Temurin 11 JRE; copy prebuilt JAR.
**Depends:** T34
**Gate:** Image builds with packaged JAR.

### T36: Cross-service integration test suite

**What:** Script or CI job: reference + catalog (partial) + integration + shop strangler profile.
**Depends:** T35
**Gate:** Health 8086 UP; sample quote + config roundtrip.

### T37: JaCoCo verify gates

**What:** Add jacoco limits on `integration-service` and `sm-integration-core` in parent pom.
**Depends:** T36
**Gate:** `./mvnw -pl integration-service,sm-integration-core verify`

### T38: Update STATE.md and ROADMAP

**What:** Document AD-015..020, wave5 pattern, GAP-INT-01..05, gate evidence.
**Depends:** T37
**Gate:** STATE.md lists onda-5-integration-service READY FOR EXECUTE or COMPLETE per actual status.

---

## Parallel Execution Map

```
Phase 1: T1 → T2 → (T3 ∥ T4) → T5

Phase 2 (after T5):
  Payment:  T6 → T7 → T8 → T9 → T10 → T11
  Shipping: T12 → T13 → T14 → T15 → T16

Phase 3: T17 → T18 → T19 → T20 → T21 → T22

Phase 4: T23 → T24 → T25 → T26 → T27 → T28

Phase 5: T29 → (T30 ∥ T31) → T32 → T33 → T34 → T35 → T36 → T37 → T38
```

**Subagent rule:** `[P]` → parallel subagent in same phase. Phase 2 → **2 parallel tracks** (payment vs shipping).

---

## Compozy Mapping

| TLC | Compozy task |
|-----|--------------|
| T1–T5 | task_01 |
| T6–T11 | task_02, task_03 |
| T12–T16 | task_04 |
| T17–T22 | task_05, task_06 |
| T23–T24 | task_07 |
| T25–T28 | task_08, task_09 |
| T29 | task_10 |
| T30–T32 | task_11 |
| T33–T38 | task_12 |
