# Wave 3 — Contracts DTO + Checkout Application Service Tasks

**Design:** `.specs/features/onda-3-contracts-dto/design.md`  
**Spec:** `.specs/features/onda-3-contracts-dto/spec.md`  
**Status:** Approved — ready for Execute (after Wave 2 gate)  
**Prerequisite:** Wave 2 Execute complete (`onda-2-content-search-merchant` T1–T54)

---

## Execution Plan

### Phase 1: Contracts foundation (Sequential)

```
Wave2-T54 ──→ T1 ──→ T2 ──→ T3 ──→ T4 ──→ T5 ──→ T6
```

### Phase 2: Parallel tracks

```
T6 ──┬──→ T7 ──→ T8 ──→ T9 ──→ T10 ──→ T11 ──→ T12 ──┬──→ T17 ──→ T18 ──→ T19 ──→ T20
     │                                                   │
     ├──→ T13 ──→ T14 ──→ T15 ──→ T16 ─────────────────┼──→ T39..T43
     │                                                   │
     ├──→ T21 ──→ T22 ──→ T23 ──→ T24 ──→ T25 ──→ T26 ──→ T27 ──→ T28 ──→ T29
     │                                                   │
     └──→ T30 ──→ T31 ──→ T32 ──→ T33 ──→ T34 ──→ T35 ──→ T36 ──→ T37 ──→ T38
```

**Track A (Product + Search):** T7–T12, T17–T20  
**Track B (Order/Customer snapshots):** T13–T16  
**Track C (Integration):** T21–T29  
**Track D (Facades + REF):** T30–T38

### Phase 3: Checkout convergence

```
T12,T16,T29,T38 ──→ T39 ──→ T40 ──→ T41 ──→ T42 ──→ T43
```

### Phase 4: Outbox + gate

```
T43 ──→ T44 ──→ T45 ──→ T46 ──→ T47 ──→ T48
```

**Milestones:** `SNP-ready` = T16; `INT-ready` = T29; `CHK-ready` = T43; `Wave3-complete` = T48

---

## Task Breakdown

### T1: MerchantStoreId value type

**What:** Add immutable `MerchantStoreId` in `com.salesmanager.contracts.tenant`.  
**Where:** `shopizer-api-contracts/.../tenant/MerchantStoreId.java`  
**Depends on:** Wave 2 T54  
**Requirement:** TNT-01, CTR-04

**Done when:**
- [ ] Rejects null/blank code
- [ ] Jackson round-trip test passes

**Tests:** `MerchantStoreIdTest`  
**Gate:** `./mvnw test -pl shopizer-api-contracts -Dtest=MerchantStoreIdTest`  
**Commit:** `feat(contracts): add MerchantStoreId`

---

### T2: LanguageCode value type

**What:** Add immutable `LanguageCode` in contracts tenant package.  
**Where:** `shopizer-api-contracts/.../tenant/LanguageCode.java`  
**Depends on:** T1  
**Requirement:** TNT-02

**Done when:**
- [ ] Factory `of(String)` validates non-blank
- [ ] equals/hashCode on code

**Tests:** `LanguageCodeTest`  
**Gate:** `./mvnw test -pl shopizer-api-contracts -Dtest=LanguageCodeTest`

---

### T3: Snapshot package scaffolding

**What:** Create empty packages `catalog`, `order`, `customer` with package-info / base types if needed.  
**Where:** `shopizer-api-contracts/`  
**Depends on:** T2  
**Requirement:** CTR-04

**Done when:**
- [ ] Module compiles with new packages

**Gate:** `./mvnw compile -pl shopizer-api-contracts`

---

### T4: TenantEntityBridge interface

**What:** Define bridge to resolve `MerchantStore` and `Language` from codes.  
**Where:** `sm-shop/.../tenant/TenantEntityBridge.java`  
**Depends on:** T2  
**Requirement:** TNT-03

**Done when:**
- [ ] Interface documented; impl stub compiles

**Gate:** `./mvnw compile -pl sm-shop -am`

---

### T5: TenantEntityBridgeImpl

**What:** Implement bridge using `MerchantStoreService` / `LanguageService` (in-process).  
**Where:** `sm-shop/.../tenant/TenantEntityBridgeImpl.java`  
**Depends on:** T4  
**Requirement:** TNT-03

**Done when:**
- [ ] Resolves DEFAULT store and en language in test

**Tests:** `TenantEntityBridgeImplTest`  
**Gate:** `./mvnw test -pl sm-shop -Dtest=TenantEntityBridgeImplTest`

---

### T6: AbstractDataPopulator tenant overload

**What:** Add `populate(source, MerchantStoreId, LanguageCode)` defaulting to bridge.  
**Where:** `sm-core/.../AbstractDataPopulator.java`  
**Depends on:** T5  
**Requirement:** TNT-04

**Done when:**
- [ ] Existing populators compile unchanged
- [ ] One populator uses new overload in test

**Gate:** `./mvnw compile -pl sm-core,sm-shop -am`

---

### T7: ProductSnapshot DTO

**What:** Define `ProductSnapshot` and nested variant/inventory summary DTOs.  
**Where:** `shopizer-api-contracts/.../catalog/ProductSnapshot.java`  
**Depends on:** T6  
**Requirement:** SNP-01, CTR-02

**Done when:**
- [ ] No JPA imports; Jackson test passes

**Tests:** `ProductSnapshotJacksonTest`  
**Gate:** `./mvnw test -pl shopizer-api-contracts -Dtest=ProductSnapshotJacksonTest`

---

### T8: ProductSnapshotBuilder skeleton

**What:** Create builder class with `build(Product, MerchantStoreId, LanguageCode)` signature.  
**Where:** `sm-core/.../catalog/ProductSnapshotBuilder.java`  
**Depends on:** T7  
**Requirement:** SNP-03

**Done when:**
- [ ] Compiles; returns snapshot with id and sku

**Gate:** `./mvnw compile -pl sm-core -am`

---

### T9: ProductSnapshotBuilder full mapping

**What:** Map name, description, pricing, categories, images from product graph.  
**Where:** `ProductSnapshotBuilder.java`  
**Depends on:** T8  
**Requirement:** SNP-03, SNP-07

**Done when:**
- [ ] Unit test with fixture product covers main fields

**Tests:** `ProductSnapshotBuilderTest`  
**Gate:** `./mvnw test -pl sm-core -Dtest=ProductSnapshotBuilderTest`

---

### T10: ProductIndexPayloadMapper

**What:** Map `ProductSnapshot` → `ProductIndexPayload` with schemaVersion 2.  
**Where:** `sm-shop/.../search/ProductIndexPayloadMapper.java`  
**Depends on:** T9  
**Requirement:** SNP-02

**Done when:**
- [ ] schemaVersion 2 set on output

**Tests:** `ProductIndexPayloadMapperTest`

---

### T11: Refactor index producer to use snapshot pipeline

**What:** Update `SearchIndexProducerHttp` / listener to build snapshot first.  
**Where:** `sm-shop/.../strangler/search/`, `sm-core/.../IndexProductEventListener`  
**Depends on:** T10  
**Requirement:** SNP-02, AD-009

**Done when:**
- [ ] Index events still reach search-service in integration test

**Gate:** `./mvnw test -pl sm-shop -Dtest=*SearchIndex* -DfailIfNoTests=false`

---

### T12: search-service schema v2 acceptance

**What:** Internal index API accepts payload schemaVersion 1 and 2.  
**Where:** `search-service/.../index/`  
**Depends on:** T11  
**Requirement:** SNP-06

**Done when:**
- [ ] v1 and v2 fixtures index successfully

**Tests:** `IndexPayloadSchemaVersionTest`  
**Gate:** `./mvnw test -pl search-service -Dtest=IndexPayloadSchemaVersionTest`

---

### T13: OrderSnapshot DTOs

**What:** `OrderSnapshot`, `OrderLineSnapshot`, `OrderTotalSnapshot`.  
**Where:** `shopizer-api-contracts/.../order/`  
**Depends on:** T6  
**Requirement:** SNP-04

**Done when:**
- [ ] Jackson tests pass

**Tests:** `OrderSnapshotJacksonTest`

---

### T14: CustomerSnapshot DTOs

**What:** `CustomerSnapshot`, `AddressSnapshot`.  
**Where:** `shopizer-api-contracts/.../customer/`  
**Depends on:** T6  
**Requirement:** SNP-05

**Done when:**
- [ ] No lazy collection types

**Tests:** `CustomerSnapshotJacksonTest`

---

### T15: OrderSnapshotBuilder

**What:** Build order snapshot from `Order` entity.  
**Where:** `sm-core/.../checkout/OrderSnapshotBuilder.java`  
**Depends on:** T13  
**Requirement:** SNP-07

**Tests:** `OrderSnapshotBuilderTest`

---

### T16: CustomerSnapshotBuilder

**What:** Build customer snapshot from `Customer` entity.  
**Where:** `sm-core/.../checkout/CustomerSnapshotBuilder.java`  
**Depends on:** T14  
**Requirement:** SNP-07

**Tests:** `CustomerSnapshotBuilderTest`  
**Milestone:** SNP-ready

---

### T17: SearchItem in api-contracts

**What:** Move/copy `SearchItem` to contracts search package.  
**Where:** `shopizer-api-contracts/.../search/SearchItem.java`  
**Depends on:** T12  
**Requirement:** SRCH-01

**Done when:**
- [ ] DTO compiles; JSON fixture matches legacy

**Tests:** `SearchItemJacksonTest`

---

### T18: Rewire sm-shop search imports

**What:** Update SearchApi, SearchFacade, adapters to contracts SearchItem.  
**Where:** `sm-shop/.../search/`  
**Depends on:** T17  
**Requirement:** SRCH-02

**Gate:** `./mvnw compile -pl sm-shop -am`

---

### T19: Rewire search-service imports

**What:** Update search-service controllers/services to contracts SearchItem.  
**Where:** `search-service/`  
**Depends on:** T17  
**Requirement:** SRCH-02

**Gate:** `./mvnw compile -pl search-service -am`

---

### T20: Update Search Pact tests

**What:** Point Pact to contracts SearchItem; verify SRCH-04 compatibility.  
**Where:** `sm-shop/src/test/.../pact/`, `search-service/src/test/.../pact/`  
**Depends on:** T18, T19  
**Requirement:** SRCH-03, SRCH-04, GAT-02

**Gate:** `./mvnw test -pl sm-shop,search-service -Dtest=*Pact* -DfailIfNoTests=false`

---

### T21: IntegrationStoreContext DTO

**What:** Common store context for integration modules.  
**Where:** `sm-core-modules/.../integration/common/dto/`  
**Depends on:** T6  
**Requirement:** INT-01

**Gate:** `./mvnw compile -pl sm-core-modules -am`

---

### T22: Payment integration DTOs

**What:** `PaymentRequestContext`, `PaymentCaptureContext`, `PaymentLineItemDto`, `TransactionResult`.  
**Where:** `sm-core-modules/.../integration/payment/dto/`  
**Depends on:** T21  
**Requirement:** INT-01, INT-02

**Tests:** `PaymentDtoJacksonTest`

---

### T23: Shipping integration DTOs

**What:** `ShippingQuoteRequestContext`, address and package DTOs.  
**Where:** `sm-core-modules/.../integration/shipping/dto/`  
**Depends on:** T21  
**Requirement:** INT-01, INT-03

**Tests:** `ShippingDtoJacksonTest`

---

### T24: sm-core-modules depends on api-contracts

**What:** Add Maven dependency for `MerchantStoreId` in integration DTOs.  
**Where:** `sm-core-modules/pom.xml`  
**Depends on:** T22, T23  
**Requirement:** CTR-01

**Gate:** `./mvnw compile -pl sm-core-modules`

---

### T25: PaymentModuleV2 and ShippingQuoteModuleV2 interfaces

**What:** Define V2 interfaces using DTOs only.  
**Where:** `sm-core-modules/.../payment/model/`, `.../shipping/model/`  
**Depends on:** T24  
**Requirement:** INT-02, INT-03

**Gate:** `./mvnw compile -pl sm-core-modules`

---

### T26: LegacyPaymentModuleBridge

**What:** Adapter implementing V2 by delegating to V1 with entity mapping.  
**Where:** `sm-core/.../payments/LegacyPaymentModuleBridge.java`  
**Depends on:** T25  
**Requirement:** INT-04

**Tests:** `LegacyPaymentModuleBridgeTest`

---

### T27: LegacyShippingQuoteModuleBridge

**What:** Shipping equivalent of T26.  
**Where:** `sm-core/.../shipping/LegacyShippingQuoteModuleBridge.java`  
**Depends on:** T25  
**Requirement:** INT-04

---

### T28: PaymentServiceImpl V2 routing

**What:** Route to V2/bridge in authorize/capture/refund paths.  
**Where:** `sm-core/.../payments/PaymentServiceImpl.java`  
**Depends on:** T26  
**Requirement:** INT-05

**Gate:** `./mvnw test -pl sm-core -Dtest=*Payment* -DfailIfNoTests=false`

---

### T29: ShippingServiceImpl V2 routing + plugin test

**What:** V2 routing; integration test MoneyOrder or StorePickup via bridge.  
**Where:** `sm-core/.../shipping/ShippingServiceImpl.java`  
**Depends on:** T27, T28  
**Requirement:** INT-05, INT-06

**Milestone:** INT-ready

---

### T30: P1 facade interface signatures

**What:** Update 6 facade interfaces to tenant types.  
**Where:** `sm-shop-model/.../facade/`  
**Depends on:** T6  
**Requirement:** FAC-01, FAC-02, FAC-03

**Gate:** `./mvnw compile -pl sm-shop-model`

---

### T31: OrderFacadeImpl + ShoppingCartFacadeImpl

**What:** Implement bridge hydration in order/cart facades.  
**Where:** `sm-shop/.../order/`, `.../shoppingCart/`  
**Depends on:** T30, T5  
**Requirement:** FAC-01

---

### T32: SearchFacadeImpl + ShippingFacadeImpl

**What:** Tenant types in search/shipping facades including Wave2 HTTP adapters.  
**Where:** `sm-shop/.../search/`, `.../shipping/`  
**Depends on:** T30  
**Requirement:** FAC-02, FAC-04

---

### T33: CategoryFacadeImpl + ProductCommonFacadeImpl

**What:** Read-path migration to tenant types.  
**Where:** `sm-shop/.../category/`, `.../product/`  
**Depends on:** T30  
**Requirement:** FAC-03

---

### T34: Controller call-site fixes

**What:** Convert entity → tenant at facade invocation in affected controllers.  
**Where:** `sm-shop/.../api/v1/`  
**Depends on:** T31, T32, T33  
**Requirement:** TNT-06

**Gate:** `./mvnw compile -pl sm-shop -am`

---

### T35: ArchUnit facades_no_new_entity_params

**What:** Fail build if new facade methods use MerchantStore/Language.  
**Where:** `sm-shop-model/src/test/` or `sm-shop/src/test/`  
**Depends on:** T34  
**Requirement:** TNT-05

---

### T36: ReferencesApi ReadableLanguage

**What:** Wire language endpoints to ReadableLanguage DTOs.  
**Where:** `sm-shop/.../references/ReferencesApi.java`  
**Depends on:** T6  
**Requirement:** REF-01, B-002

---

### T37: ReferencesApi ReadableCurrency + tests

**What:** Wire currency endpoints; integration test.  
**Where:** `ReferencesApi.java`, tests  
**Depends on:** T36  
**Requirement:** REF-02

**Tests:** `ReferencesApiDtoTest`

---

### T38: FACADE-MIGRATION-PLAN.md

**What:** Document all 76 facades with Wave 4–6 phase assignment.  
**Where:** `docs/decomposition/FACADE-MIGRATION-PLAN.md`  
**Depends on:** T35  
**Requirement:** FAC-05, FAC-06

---

### T39: CheckoutApplicationService interface

**What:** Define service + `CheckoutCommand`.  
**Where:** `sm-core/.../checkout/`  
**Depends on:** T16, T12  
**Requirement:** CHK-01

---

### T40: Extract orchestration from OrderFacadeImpl

**What:** Move place-order logic to `CheckoutApplicationServiceImpl`.  
**Where:** `sm-core/.../checkout/`, `sm-shop/.../OrderFacadeImpl.java`  
**Depends on:** T39  
**Requirement:** CHK-03, CHK-04

---

### T41: Checkout parity integration tests

**What:** Assert happy path matches legacy behavior.  
**Where:** `sm-core/src/test/.../checkout/`  
**Depends on:** T40  
**Requirement:** CHK-04, CHK-05

**Tests:** `CheckoutApplicationServicePlaceOrderTest`

---

### T42: OrderFacadeImpl thin delegation

**What:** Remove duplicated orchestration; facade maps DTOs only.  
**Where:** `OrderFacadeImpl.java`  
**Depends on:** T41  
**Requirement:** CHK-02, CHK-03

---

### T43: OrderApi regression sweep

**What:** Run order-related tests; fix regressions.  
**Depends on:** T42  
**Requirement:** CHK-05, CHK-06

**Milestone:** CHK-ready  
**Gate:** `./mvnw test -pl sm-shop,sm-core -Dtest=*Order* -DfailIfNoTests=false`

---

### T44: CHECKOUT_OUTBOX schema + repository

**What:** Migration script + JPA/JDBC repository.  
**Where:** `sm-core/.../checkout/outbox/`  
**Depends on:** T43  
**Requirement:** SAG-01

---

### T45: Staged checkout with outbox writes

**What:** Emit outbox events per stage in CAS when flag enabled.  
**Where:** `CheckoutApplicationServiceImpl.java`  
**Depends on:** T44, T15  
**Requirement:** SAG-02, SAG-03

---

### T46: Outbox payload uses OrderSnapshot JSON

**What:** Serialize snapshot fragments in outbox payload column.  
**Where:** outbox event builder  
**Depends on:** T45  
**Requirement:** SAG-02

---

### T47: checkout.outbox.enabled flag + dispatcher

**What:** Property default false; scheduled dispatcher marks processed.  
**Where:** `application.properties`, dispatcher bean  
**Depends on:** T46  
**Requirement:** SAG-04, SAG-05

**Tests:** `CheckoutOutboxIntegrationTest` (flag on/off)

---

### T48: Reactor gate + STATE.md + ArchUnit contracts

**What:** Full build; update STATE; add `ContractsMustNotDependOnCoreModel`.  
**Depends on:** T20, T29, T38, T47  
**Requirement:** GAT-01, GAT-02, GAT-03, CTR-01

**Done when:**
- [ ] `./mvnw clean install` green
- [ ] STATE.md lists Wave 3 complete
- [ ] B-001 partial, B-002 resolved

**Gate:** `./mvnw clean install`  
**Milestone:** Wave 3 complete

---

## Compozy mapping

| Compozy | TLC |
| ------- | --- |
| task_01 | T1–T6 |
| task_02 | T7–T12 |
| task_03 | T13–T16 |
| task_04 | T17–T20 |
| task_05 | T21–T24 |
| task_06 | T25–T29 |
| task_07 | T30–T34 |
| task_08 | T35–T38 |
| task_09 | T39–T43 |
| task_10 | T44–T48 |

---

## Parallel Execution Map

```
Phase 1: Wave2-T54 → T1 → T2 → T3 → T4 → T5 → T6

Phase 2 (4 tracks after T6):
  Snapshots:  T7 → T8 → T9 → T10 → T11 → T12
  Order/Cust: T13 → T14 → T15 → T16
  Integration: T21 → T22 → T23 → T24 → T25 → T26 → T27 → T28 → T29
  Facades:    T30 → T31 → T32 → T33 → T34 → T35 → T36 → T37 → T38
  Search:     T17 → T18 → T19 → T20 (after T12)

Phase 3: T39 → T40 → T41 → T42 → T43

Phase 4: T44 → T45 → T46 → T47 → T48
```

**Subagent rule:** Tracks in Phase 2 may run in parallel after T6. Convergence tasks T39+ require SNP-ready + INT-ready + facade compile.
