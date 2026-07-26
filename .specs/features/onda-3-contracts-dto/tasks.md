# Onda 3 — Contracts DTO + Checkout Application Service Tasks

**Design:** `.specs/features/onda-3-contracts-dto/design.md`  
**Spec:** `.specs/features/onda-3-contracts-dto/spec.md`  
**Status:** Aprovado — pronto para Execute (após gate Onda 2)  
**Pré-requisito:** Execute Onda 2 completo (`onda-2-content-search-merchant` T1–T54)

---

## Plano de execução

### Fase 1: Base de contracts (sequencial)

```
Wave2-T54 ──→ T1 ──→ T2 ──→ T3 ──→ T4 ──→ T5 ──→ T6
```

### Fase 2: Tracks paralelas

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
**Track B (snapshots Order/Customer):** T13–T16  
**Track C (Integração):** T21–T29  
**Track D (Facades + REF):** T30–T38

### Fase 3: Convergência checkout

```
T12,T16,T29,T38 ──→ T39 ──→ T40 ──→ T41 ──→ T42 ──→ T43
```

### Fase 4: Outbox + gate

```
T43 ──→ T44 ──→ T45 ──→ T46 ──→ T47 ──→ T48
```

**Marcos:** `SNP-ready` = T16; `INT-ready` = T29; `CHK-ready` = T43; `Wave3-complete` = T48

---

## Decomposição de tarefas

### T1: MerchantStoreId value type

**O quê:** Adicionar imutável `MerchantStoreId` em `com.salesmanager.contracts.tenant`.  
**Onde:** `shopizer-api-contracts/.../tenant/MerchantStoreId.java`  
**Depende de:** Wave 2 T54  
**Requisito:** TNT-01, CTR-04

**Concluído quando:**
- [ ] Rejeita código null/vazio
- [ ] Teste Jackson round-trip passa

**Testes:** `MerchantStoreIdTest`  
**Gate:** `./mvnw test -pl shopizer-api-contracts -Dtest=MerchantStoreIdTest`  
**Commit:** `feat(contracts): add MerchantStoreId`

---

### T2: LanguageCode value type

**O quê:** Adicionar imutável `LanguageCode` no package tenant de contracts.  
**Onde:** `shopizer-api-contracts/.../tenant/LanguageCode.java`  
**Depende de:** T1  
**Requisito:** TNT-02

**Concluído quando:**
- [ ] Factory `of(String)` valida não vazio
- [ ] equals/hashCode no código

**Testes:** `LanguageCodeTest`  
**Gate:** `./mvnw test -pl shopizer-api-contracts -Dtest=LanguageCodeTest`

---

### T3: Snapshot package scaffolding

**O quê:** Criar packages vazios `catalog`, `order`, `customer` com package-info / tipos base se necessário.  
**Onde:** `shopizer-api-contracts/`  
**Depende de:** T2  
**Requisito:** CTR-04

**Concluído quando:**
- [ ] Módulo compila com novos packages

**Gate:** `./mvnw compile -pl shopizer-api-contracts`

---

### T4: TenantEntityBridge interface

**O quê:** Definir bridge para resolver `MerchantStore` e `Language` a partir de códigos.  
**Onde:** `sm-shop/.../tenant/TenantEntityBridge.java`  
**Depende de:** T2  
**Requisito:** TNT-03

**Concluído quando:**
- [ ] Interface documentada; stub impl compila

**Gate:** `./mvnw compile -pl sm-shop -am`

---

### T5: TenantEntityBridgeImpl

**O quê:** Implementar bridge usando `MerchantStoreService` / `LanguageService` (in-process).  
**Onde:** `sm-shop/.../tenant/TenantEntityBridgeImpl.java`  
**Depende de:** T4  
**Requisito:** TNT-03

**Concluído quando:**
- [ ] Resolve store DEFAULT e language en em teste

**Testes:** `TenantEntityBridgeImplTest`  
**Gate:** `./mvnw test -pl sm-shop -Dtest=TenantEntityBridgeImplTest`

---

### T6: AbstractDataPopulator tenant overload

**O quê:** Adicionar `populate(source, MerchantStoreId, LanguageCode)` defaultando para bridge.  
**Onde:** `sm-core/.../AbstractDataPopulator.java`  
**Depende de:** T5  
**Requisito:** TNT-04

**Concluído quando:**
- [ ] Populators existentes compilam inalterados
- [ ] Um populator usa novo overload em teste

**Gate:** `./mvnw compile -pl sm-core,sm-shop -am`

---

### T7: ProductSnapshot DTO

**O quê:** Definir `ProductSnapshot` e DTOs aninhados de variant/resumo de inventário.  
**Onde:** `shopizer-api-contracts/.../catalog/ProductSnapshot.java`  
**Depende de:** T6  
**Requisito:** SNP-01, CTR-02

**Concluído quando:**
- [ ] Sem imports JPA; teste Jackson passa

**Testes:** `ProductSnapshotJacksonTest`  
**Gate:** `./mvnw test -pl shopizer-api-contracts -Dtest=ProductSnapshotJacksonTest`

---

### T8: ProductSnapshotBuilder skeleton

**O quê:** Criar classe builder com assinatura `build(Product, MerchantStoreId, LanguageCode)`.  
**Onde:** `sm-core/.../catalog/ProductSnapshotBuilder.java`  
**Depende de:** T7  
**Requisito:** SNP-03

**Concluído quando:**
- [ ] Compila; retorna snapshot com id e sku

**Gate:** `./mvnw compile -pl sm-core -am`

---

### T9: ProductSnapshotBuilder full mapping

**O quê:** Mapear nome, descrição, pricing, categorias, imagens do grafo de produto.  
**Onde:** `ProductSnapshotBuilder.java`  
**Depende de:** T8  
**Requisito:** SNP-03, SNP-07

**Concluído quando:**
- [ ] Teste unitário com produto fixture cobre campos principais

**Testes:** `ProductSnapshotBuilderTest`  
**Gate:** `./mvnw test -pl sm-core -Dtest=ProductSnapshotBuilderTest`

---

### T10: ProductIndexPayloadMapper

**O quê:** Mapear `ProductSnapshot` → `ProductIndexPayload` com schemaVersion 2.  
**Onde:** `sm-shop/.../search/ProductIndexPayloadMapper.java`  
**Depende de:** T9  
**Requisito:** SNP-02

**Concluído quando:**
- [ ] schemaVersion 2 definido na saída

**Testes:** `ProductIndexPayloadMapperTest`

---

### T11: Refactor index producer to use snapshot pipeline

**O quê:** Atualizar `SearchIndexProducerHttp` / listener para construir snapshot primeiro.  
**Onde:** `sm-shop/.../strangler/search/`, `sm-core/.../IndexProductEventListener`  
**Depende de:** T10  
**Requisito:** SNP-02, AD-009

**Concluído quando:**
- [ ] Eventos de índice ainda chegam ao search-service em teste de integração

**Gate:** `./mvnw test -pl sm-shop -Dtest=*SearchIndex* -DfailIfNoTests=false`

---

### T12: search-service schema v2 acceptance

**O quê:** API interna de índice aceita payload schemaVersion 1 e 2.  
**Onde:** `search-service/.../index/`  
**Depende de:** T11  
**Requisito:** SNP-06

**Concluído quando:**
- [ ] Fixtures v1 e v2 indexam com sucesso

**Testes:** `IndexPayloadSchemaVersionTest`  
**Gate:** `./mvnw test -pl search-service -Dtest=IndexPayloadSchemaVersionTest`

---

### T13: OrderSnapshot DTOs

**O quê:** `OrderSnapshot`, `OrderLineSnapshot`, `OrderTotalSnapshot`.  
**Onde:** `shopizer-api-contracts/.../order/`  
**Depende de:** T6  
**Requisito:** SNP-04

**Concluído quando:**
- [ ] Testes Jackson passam

**Testes:** `OrderSnapshotJacksonTest`

---

### T14: CustomerSnapshot DTOs

**O quê:** `CustomerSnapshot`, `AddressSnapshot`.  
**Onde:** `shopizer-api-contracts/.../customer/`  
**Depende de:** T6  
**Requisito:** SNP-05

**Concluído quando:**
- [ ] Sem tipos de coleção lazy

**Testes:** `CustomerSnapshotJacksonTest`

---

### T15: OrderSnapshotBuilder

**O quê:** Construir snapshot de order a partir da entidade `Order`.  
**Onde:** `sm-core/.../checkout/OrderSnapshotBuilder.java`  
**Depende de:** T13  
**Requisito:** SNP-07

**Testes:** `OrderSnapshotBuilderTest`

---

### T16: CustomerSnapshotBuilder

**O quê:** Construir snapshot de customer a partir da entidade `Customer`.  
**Onde:** `sm-core/.../checkout/CustomerSnapshotBuilder.java`  
**Depende de:** T14  
**Requisito:** SNP-07

**Testes:** `CustomerSnapshotBuilderTest`  
**Milestone:** SNP-ready

---

### T17: SearchItem in api-contracts

**O quê:** Mover/copiar `SearchItem` para package search de contracts.  
**Onde:** `shopizer-api-contracts/.../search/SearchItem.java`  
**Depende de:** T12  
**Requisito:** SRCH-01

**Concluído quando:**
- [ ] DTO compila; fixture JSON bate com legacy

**Testes:** `SearchItemJacksonTest`

---

### T18: Rewire sm-shop search imports

**O quê:** Atualizar SearchApi, SearchFacade, adapters para SearchItem de contracts.  
**Onde:** `sm-shop/.../search/`  
**Depende de:** T17  
**Requisito:** SRCH-02

**Gate:** `./mvnw compile -pl sm-shop -am`

---

### T19: Rewire search-service imports

**O quê:** Atualizar controllers/serviços search-service para SearchItem de contracts.  
**Onde:** `search-service/`  
**Depende de:** T17  
**Requisito:** SRCH-02

**Gate:** `./mvnw compile -pl search-service -am`

---

### T20: Atualizar testes Pact Search

**O quê:** Apontar Pact para SearchItem de contracts; verificar compatibilidade SRCH-04.  
**Onde:** `sm-shop/src/test/.../pact/`, `search-service/src/test/.../pact/`  
**Depende de:** T18, T19  
**Requisito:** SRCH-03, SRCH-04, GAT-02

**Gate:** `./mvnw test -pl sm-shop,search-service -Dtest=*Pact* -DfailIfNoTests=false`

---

### T21: IntegrationStoreContext DTO

**O quê:** Contexto comum de store para módulos de integração.  
**Onde:** `sm-core-modules/.../integration/common/dto/`  
**Depende de:** T6  
**Requisito:** INT-01

**Gate:** `./mvnw compile -pl sm-core-modules -am`

---

### T22: Payment integration DTOs

**O quê:** `PaymentRequestContext`, `PaymentCaptureContext`, `PaymentLineItemDto`, `TransactionResult`.  
**Onde:** `sm-core-modules/.../integration/payment/dto/`  
**Depende de:** T21  
**Requisito:** INT-01, INT-02

**Testes:** `PaymentDtoJacksonTest`

---

### T23: Shipping integration DTOs

**O quê:** `ShippingQuoteRequestContext`, DTOs de address e package.  
**Onde:** `sm-core-modules/.../integration/shipping/dto/`  
**Depende de:** T21  
**Requisito:** INT-01, INT-03

**Testes:** `ShippingDtoJacksonTest`

---

### T24: sm-core-modules depende de api-contracts

**O quê:** Adicionar dependência Maven para `MerchantStoreId` nos DTOs de integração.  
**Onde:** `sm-core-modules/pom.xml`  
**Depende de:** T22, T23  
**Requisito:** CTR-01

**Gate:** `./mvnw compile -pl sm-core-modules`

---

### T25: Interfaces PaymentModuleV2 e ShippingQuoteModuleV2

**O quê:** Definir interfaces V2 usando apenas DTOs.  
**Onde:** `sm-core-modules/.../payment/model/`, `.../shipping/model/`  
**Depende de:** T24  
**Requisito:** INT-02, INT-03

**Gate:** `./mvnw compile -pl sm-core-modules`

---

### T26: LegacyPaymentModuleBridge

**O quê:** Adapter implementando V2 delegando a V1 com mapeamento de entidade.  
**Onde:** `sm-core/.../payments/LegacyPaymentModuleBridge.java`  
**Depende de:** T25  
**Requisito:** INT-04

**Testes:** `LegacyPaymentModuleBridgeTest`

---

### T27: LegacyShippingQuoteModuleBridge

**O quê:** Equivalente shipping de T26.  
**Onde:** `sm-core/.../shipping/LegacyShippingQuoteModuleBridge.java`  
**Depende de:** T25  
**Requisito:** INT-04

---

### T28: PaymentServiceImpl V2 routing

**O quê:** Rotear a V2/bridge nos caminhos authorize/capture/refund.  
**Onde:** `sm-core/.../payments/PaymentServiceImpl.java`  
**Depende de:** T26  
**Requisito:** INT-05

**Gate:** `./mvnw test -pl sm-core -Dtest=*Payment* -DfailIfNoTests=false`

---

### T29: ShippingServiceImpl V2 routing + plugin test

**O quê:** Roteamento V2; teste integração MoneyOrder ou StorePickup via bridge.  
**Onde:** `sm-core/.../shipping/ShippingServiceImpl.java`  
**Depende de:** T27, T28  
**Requisito:** INT-05, INT-06

**Milestone:** INT-ready

---

### T30: Assinaturas interface facade P1

**O quê:** Atualizar 6 interfaces facade para tipos tenant.  
**Onde:** `sm-shop-model/.../facade/`  
**Depende de:** T6  
**Requisito:** FAC-01, FAC-02, FAC-03

**Gate:** `./mvnw compile -pl sm-shop-model`

---

### T31: OrderFacadeImpl + ShoppingCartFacadeImpl

**O quê:** Implementar hidratação bridge nas facades order/cart.  
**Onde:** `sm-shop/.../order/`, `.../shoppingCart/`  
**Depende de:** T30, T5  
**Requisito:** FAC-01

---

### T32: SearchFacadeImpl + ShippingFacadeImpl

**O quê:** Tipos tenant nas facades search/shipping incluindo adapters HTTP Wave2.  
**Onde:** `sm-shop/.../search/`, `.../shipping/`  
**Depende de:** T30  
**Requisito:** FAC-02, FAC-04

---

### T33: CategoryFacadeImpl + ProductCommonFacadeImpl

**O quê:** Migração de caminhos de leitura para tipos tenant.  
**Onde:** `sm-shop/.../category/`, `.../product/`  
**Depende de:** T30  
**Requisito:** FAC-03

---

### T34: Correções call-site controller

**O quê:** Converter entidade → tenant na invocação facade nos controllers afetados.  
**Onde:** `sm-shop/.../api/v1/`  
**Depende de:** T31, T32, T33  
**Requisito:** TNT-06

**Gate:** `./mvnw compile -pl sm-shop -am`

---

### T35: ArchUnit facades_no_new_entity_params

**O quê:** Falhar build se novos métodos facade usam MerchantStore/Language.  
**Onde:** `sm-shop-model/src/test/` ou `sm-shop/src/test/`  
**Depende de:** T34  
**Requisito:** TNT-05

---

### T36: ReferencesApi ReadableLanguage

**O quê:** Conectar endpoints language a DTOs ReadableLanguage.  
**Onde:** `sm-shop/.../references/ReferencesApi.java`  
**Depende de:** T6  
**Requisito:** REF-01, B-002

---

### T37: ReferencesApi ReadableCurrency + testes

**O quê:** Conectar endpoints currency; teste de integração.  
**Onde:** `ReferencesApi.java`, tests  
**Depende de:** T36  
**Requisito:** REF-02

**Testes:** `ReferencesApiDtoTest`

---

### T38: FACADE-MIGRATION-PLAN.md

**O quê:** Documentar todas as 76 facades com atribuição de fase Onda 4–6.  
**Onde:** `docs/decomposition/FACADE-MIGRATION-PLAN.md`  
**Depende de:** T35  
**Requisito:** FAC-05, FAC-06

---

### T39: Interface CheckoutApplicationService

**O quê:** Definir serviço + `CheckoutCommand`.  
**Onde:** `sm-core/.../checkout/`  
**Depende de:** T16, T12  
**Requisito:** CHK-01

---

### T40: Extrair orquestração de OrderFacadeImpl

**O quê:** Mover lógica place-order para `CheckoutApplicationServiceImpl`.  
**Onde:** `sm-core/.../checkout/`, `sm-shop/.../OrderFacadeImpl.java`  
**Depende de:** T39  
**Requisito:** CHK-03, CHK-04

---

### T41: Testes integração paridade checkout

**O quê:** Assertar que happy path bate com comportamento legacy.  
**Onde:** `sm-core/src/test/.../checkout/`  
**Depende de:** T40  
**Requisito:** CHK-04, CHK-05

**Testes:** `CheckoutApplicationServicePlaceOrderTest`

---

### T42: Delegação fina OrderFacadeImpl

**O quê:** Remover orquestração duplicada; facade mapea apenas DTOs.  
**Onde:** `OrderFacadeImpl.java`  
**Depende de:** T41  
**Requisito:** CHK-02, CHK-03

---

### T43: Varredura regressão OrderApi

**O quê:** Executar testes relacionados a order; corrigir regressões.  
**Depende de:** T42  
**Requisito:** CHK-05, CHK-06

**Milestone:** CHK-ready  
**Gate:** `./mvnw test -pl sm-shop,sm-core -Dtest=*Order* -DfailIfNoTests=false`

---

### T44: Schema CHECKOUT_OUTBOX + repository

**O quê:** Script de migração + repository JPA/JDBC.  
**Onde:** `sm-core/.../checkout/outbox/`  
**Depende de:** T43  
**Requisito:** SAG-01

---

### T45: Checkout em estágios com escritas outbox

**O quê:** Emitir eventos outbox por estágio no CAS quando flag habilitada.  
**Onde:** `CheckoutApplicationServiceImpl.java`  
**Depende de:** T44, T15  
**Requisito:** SAG-02, SAG-03

---

### T46: Payload outbox usa JSON OrderSnapshot

**O quê:** Serializar fragmentos snapshot na coluna payload outbox.  
**Onde:** outbox event builder  
**Depende de:** T45  
**Requisito:** SAG-02

---

### T47: Flag checkout.outbox.enabled + dispatcher

**O quê:** Property default false; dispatcher agendado marca processado.  
**Onde:** `application.properties`, dispatcher bean  
**Depende de:** T46  
**Requisito:** SAG-04, SAG-05

**Testes:** `CheckoutOutboxIntegrationTest` (flag on/off)

---

### T48: Gate reactor + STATE.md + ArchUnit contracts

**O quê:** Build completo; atualizar STATE; adicionar `ContractsMustNotDependOnCoreModel`.  
**Depende de:** T20, T29, T38, T47  
**Requisito:** GAT-01, GAT-02, GAT-03, CTR-01

**Concluído quando:**
- [ ] `./mvnw clean install` verde
- [ ] STATE.md lista Onda 3 completa
- [ ] B-001 parcial, B-002 resolvido

**Gate:** `./mvnw clean install`  
**Marco:** Onda 3 completa

---

## Mapeamento Compozy

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

## Mapa de execução paralela

```
Fase 1: Wave2-T54 → T1 → T2 → T3 → T4 → T5 → T6

Fase 2 (4 tracks após T6):
  Snapshots:  T7 → T8 → T9 → T10 → T11 → T12
  Order/Cust: T13 → T14 → T15 → T16
  Integração: T21 → T22 → T23 → T24 → T25 → T26 → T27 → T28 → T29
  Facades:    T30 → T31 → T32 → T33 → T34 → T35 → T36 → T37 → T38
  Search:     T17 → T18 → T19 → T20 (após T12)

Fase 3: T39 → T40 → T41 → T42 → T43

Fase 4: T44 → T45 → T46 → T47 → T48
```

**Regra subagent:** Tracks na Fase 2 podem rodar em paralelo após T6. Tasks de convergência T39+ exigem SNP-ready + INT-ready + facade compilando.

