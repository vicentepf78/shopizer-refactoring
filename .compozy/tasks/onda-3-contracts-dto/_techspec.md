# TechSpec: Wave 3 — Contracts DTO + Checkout Application Service

**PRD:** [_prd.md](_prd.md)  
**Authoritative TLC (HOW):** `.specs/features/onda-3-contracts-dto/design.md`  
**Feature slug:** `onda-3-contracts-dto`  
**Date:** 2026-07-26  
**Status:** Ready for `cy-create-tasks`

---

## Executive summary

Wave 3 is a **monolith-only refactoring wave** (ADR-001): no new Spring Boot apps, no Docker services, no Strangler HTTP URLs. It delivers cross-cutting **DTO contracts** in `shopizer-api-contracts`, **integration module V2** in `sm-core-modules`, a **CheckoutApplicationService** extracted from `OrderFacadeImpl`, and a **local transactional outbox** for `processOrder` (ADR-005).

**Primary trade-off:** Accept temporary bridge layers (entity hydration, dual PaymentModule interfaces, ProductIndexPayload + ProductSnapshot) to avoid big-bang rewrites while closing blockers B-001 (partial), B-002, and AD-009 evolution.

**Hard prerequisite:** Wave 2 Execute complete (`onda-2-content-search-merchant` gate green).

---

## System architecture

### Component view (unchanged deployment topology)

```mermaid
flowchart TB
    subgraph clients [Clients]
        ADMIN[Admin UI]
        STOREFRONT[Storefront]
    end

    subgraph monolith [sm-shop + sm-core — Wave 3 changes here]
        API[REST Controllers — frozen paths]
        FAC[Facades — P1 signatures use tenant IDs]
        CAS[CheckoutApplicationService NEW]
        OUT[CheckoutOutboxDispatcher NEW]
        BRIDGE[TenantEntityBridge NEW]
        BUILD[ProductSnapshotBuilder NEW]
        OFI[OrderFacadeImpl — thinned]
    end

    subgraph contracts [shopizer-api-contracts]
        SNAP[ProductSnapshot / OrderSnapshot / CustomerSnapshot]
        TNT[MerchantStoreId / LanguageCode]
        SRCH[SearchItem migrated]
    end

    subgraph modules [sm-core-modules]
        PMV2[PaymentModuleV2 / ShippingQuoteModuleV2]
        DTO[Integration DTO contexts]
    end

    subgraph wave12 [Wave 1+2 services — unchanged]
        REF[reference-service]
        CNT[content-service]
        SRCHSVC[search-service]
        MCH[merchant-service]
        TAX[tax-service]
    end

    STOREFRONT --> API --> FAC
    FAC --> CAS
    CAS --> OUT
    FAC --> BRIDGE
    BUILD --> SNAP
    FAC -.-> contracts
    CAS --> PMV2
    API --> wave12
```

| Component | Module | Responsibility |
| --------- | ------ | -------------- |
| Snapshot DTOs | `shopizer-api-contracts` | Serializable cross-boundary projections |
| Snapshot builders | `sm-core`, `sm-shop` | JPA → DTO mapping (MapStruct/manual) |
| Tenant value types | `shopizer-api-contracts` | Store/lang identifiers |
| `TenantEntityBridge` | `sm-shop` | Code → `MerchantStore`/`Language` for in-process services |
| Integration DTOs + V2 | `sm-core-modules` | Plugin contracts without JPA |
| Legacy bridges | `sm-core` | V1 plugins via entity adapters |
| `CheckoutApplicationService` | `sm-core/.../checkout` | Place-order orchestration |
| `CheckoutOutbox` | `sm-core` + Flyway/Liquibase script | Staged checkout events |
| Facade interfaces | `sm-shop-model` | P1 migrated signatures |

### Principles (inherited + Wave 3)

1. **Contracts = DTOs only** — L-002; no JPA in `shopizer-api-contracts`.
2. **Frozen REST paths** — no checkout URL changes.
3. **Feature flags** for behavioral switches (`checkout.outbox.enabled`).
4. **Behavioral parity** — integration tests are the gate.
5. **No new HTTP client patterns** — Waves 1–2 Strangler untouched.
6. **Shared DB** — AD-003; outbox table in `SALESMANAGER`.

---

## Implementation design

### Key interfaces

```java
// shopizer-api-contracts
package com.salesmanager.contracts.tenant;

public final class MerchantStoreId implements Serializable {
  private final String code;
  // factory, validation, getters
}

public final class LanguageCode implements Serializable {
  private final String code;
}
```

```java
// shopizer-api-contracts
package com.salesmanager.contracts.catalog;

public class ProductSnapshot implements Serializable {
  private int schemaVersion = 1;
  private Long productId;
  private String storeCode;
  private String sku;
  private String defaultLanguage;
  // localized fields, pricing, inventory summaries, categories, images
}
```

```java
// shopizer-api-contracts
package com.salesmanager.contracts.order;

public class OrderSnapshot implements Serializable { /* checkout-relevant order state */ }
public class CustomerSnapshot implements Serializable { /* id, email, billing/delivery DTO refs */ }
```

```java
// sm-core-modules
package com.salesmanager.core.modules.integration.payment.model;

public interface PaymentModuleV2 {
  void validateModuleConfiguration(IntegrationConfiguration cfg, IntegrationStoreContext store)
      throws IntegrationException;
  TransactionResult authorize(PaymentRequestContext ctx) throws IntegrationException;
  TransactionResult capture(PaymentCaptureContext ctx) throws IntegrationException;
  // refund, initTransaction equivalents
}
```

```java
// sm-core
package com.salesmanager.core.business.services.checkout;

public interface CheckoutApplicationService {
  Order placeOrder(CheckoutCommand command) throws ServiceException;
}

public class CheckoutCommand {
  private MerchantStoreId storeId;
  private LanguageCode language;
  private CustomerSnapshot customer;
  private List<ShoppingCartItem> items; // internal entities until Wave 6
  private Payment payment;
  private OrderTotalSummary summary;
}
```

```java
// sm-core
package com.salesmanager.core.business.services.checkout.outbox;

public interface CheckoutOutboxRepository {
  void append(CheckoutOutboxEvent event);
  List<CheckoutOutboxEvent> findPending(int limit);
}
```

### Data models

| DTO | Package | Notes |
| --- | ------- | ----- |
| `ProductSnapshot` | `contracts.catalog` | Supersedes payload semantics (ADR-002) |
| `OrderSnapshot` | `contracts.order` | Status, totals, line items as nested DTOs |
| `CustomerSnapshot` | `contracts.customer` | No lazy collections |
| `SearchItem` | `contracts.search` | Migrated from `modules.commons.search` |
| `PaymentRequestContext` | `modules.integration.payment.dto` | Amount, line items as `PaymentLineItemDto` |
| `ShippingQuoteRequestContext` | `modules.integration.shipping.dto` | Delivery/origin as address DTOs |
| `IntegrationStoreContext` | `modules.integration.common` | Store code, currency, locale |

### ProductIndexPayload evolution

```
Product (JPA)
    → ProductSnapshotBuilder.build()
    → ProductSnapshot
    → ProductIndexPayloadMapper.toPayload()  // schemaVersion 2
    → SearchIndexClient.index()
```

`search-service` index handler: accept v1 and v2; normalize to internal document model.

### Checkout flow (after extraction)

```
OrderApi / OrderPaymentApi
    → OrderFacadeImpl (validation, DTO mapping)
    → CheckoutApplicationService.placeOrder(CheckoutCommand)
        → stage PAYMENT_REQUESTED (outbox if enabled)
        → PaymentService (V2 path when available)
        → stage PAYMENT_CONFIRMED
        → CustomerService / OrderService.create
        → stage ORDER_PERSISTED
        → Inventory decrement
        → stage INVENTORY_DECREMENTED
    → ReadableOrder mapping (unchanged)
```

### Facade migration (Phase 1)

| Facade | Change |
| ------ | ------ |
| `OrderFacade` | `MerchantStore` → `MerchantStoreId`, `Language` → `LanguageCode` |
| `ShoppingCartFacade` | Same |
| `SearchFacade` | Same |
| `ShippingFacade` | Same |
| `CategoryFacade` | Read methods only |
| `ProductCommonFacade` | Read + `getProduct` paths |

Implementations (`*FacadeImpl`) call `TenantEntityBridge` at method entry.

### ReferencesApi fix (B-002)

| Endpoint | Before | After |
| -------- | ------ | ----- |
| `GET .../languages` | `List<Language>` entity | `List<ReadableLanguage>` |
| `GET .../currencies` | `List<Currency>` entity | `List<ReadableCurrency>` |

Use existing populators/mappers from Wave 1 reference strangler path.

### Database

```sql
CREATE TABLE CHECKOUT_OUTBOX (
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  AGGREGATE_ID VARCHAR(64) NOT NULL,
  EVENT_TYPE VARCHAR(64) NOT NULL,
  PAYLOAD JSON NOT NULL,
  STATUS VARCHAR(16) NOT NULL,
  CREATED_AT TIMESTAMP NOT NULL,
  PROCESSED_AT TIMESTAMP NULL,
  UNIQUE KEY UK_OUTBOX_AGG_TYPE (AGGREGATE_ID, EVENT_TYPE)
);
```

Ship as `sm-core/src/main/resources/db/migration/` or existing Shopizer schema script pattern.

### Configuration

```properties
# application.properties (sm-shop / sm-core)
checkout.outbox.enabled=false
checkout.outbox.dispatcher.interval-ms=5000
```

No `wave3.*` URLs.

---

## Build order

1. **Contracts foundation** — tenant types, snapshot DTO shells, Jackson tests (T1–T6).
2. **ProductSnapshot builder** + payload mapper (T7–T12).
3. **Order/Customer snapshots** (T13–T16).
4. **SearchItem migration** (T17–T20) — depends on search DTO stability.
5. **Integration DTOs + V2 interfaces** (T21–T26).
6. **Legacy plugin bridges** in Payment/Shipping services (T27–T29).
7. **Facade P1 migration** (T30–T36).
8. **ReferencesApi DTO fix** (T37–T38).
9. **CheckoutApplicationService extraction** (T39–T43).
10. **Outbox + staged processOrder** (T44–T47).
11. **Gate** — Pact, ArchUnit, `./mvnw clean install`, STATE.md (T48).

Parallel tracks after T6:
- **Track A:** ProductSnapshot + SearchItem (T7–T20)
- **Track B:** Integration modules (T21–T29)
- **Track C:** Facades + References (T30–T38)
- **Convergence:** Checkout + outbox (T39–T47) → gate (T48)

---

## Testing strategy

| Layer | Scope |
| ----- | ----- |
| Unit | Snapshot serializers, tenant type validation, DTO→entity adapters |
| Integration | `CheckoutApplicationServicePlaceOrderTest` — flag on/off |
| Integration | Outbox rows written per stage when enabled |
| Pact | Update search consumer/provider for `SearchItem` in contracts |
| ArchUnit | `no_core_model_in_contracts`, `facades_no_new_entity_params` |
| Regression | Existing `OrderTest`, `PaymentService` tests green |

Gate: `./mvnw clean install`

---

## Risks and mitigations

| Risk | Mitigation |
| ---- | ---------- |
| Large monolith diff | Feature flags; phased facades |
| Checkout regression | Copy existing flow to CAS verbatim first; then refactor stages |
| Plugin adapter bugs | Golden-path test with MoneyOrderPayment (simplest plugin) |
| Pact drift on SearchItem move | Coordinate search-service + sm-shop consumer in one PR slice |
| Outbox table in shared DB | Migration idempotent; flag off in production profile until Wave 6 |

---

## TLC mapping

| Compozy task | TLC tasks | Requirements |
| ------------ | --------- | ------------ |
| task_01 | T1–T6 | CTR, TNT |
| task_02 | T7–T12 | SNP |
| task_03 | T13–T16 | SNP |
| task_04 | T17–T20 | SRCH |
| task_05 | T21–T24 | INT |
| task_06 | T25–T29 | INT |
| task_07 | T30–T34 | FAC |
| task_08 | T35–T38 | FAC, REF |
| task_09 | T39–T43 | CHK |
| task_10 | T44–T48 | SAG, GAT |

---

## References

- ADRs: `adrs/adr-001.md` … `adr-005.md`
- TLC: `.specs/features/onda-3-contracts-dto/`
- Master plan: `docs/decomposition/MIGRATION-MASTER-PLAN.md` § Onda 3
- Key sources:
  - `sm-shop/.../order/facade/OrderFacadeImpl.java`
  - `sm-core/.../order/OrderServiceImpl.java`
  - `sm-core-modules/.../PaymentModule.java`
  - `shopizer-api-contracts/.../search/ProductIndexPayload.java`
