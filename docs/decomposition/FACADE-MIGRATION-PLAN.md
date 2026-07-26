# Plano de Migração de Facades — FAC-06

**Versão:** 1.0  
**Data:** 2026-07-26  
**Requisito:** FAC-06 (Onda 3)  
**ADR:** [ADR-003](../../.compozy/tasks/onda-3-contracts-dto/adrs/adr-003.md)  
**Blocker relacionado:** B-001 (parcial após Onda 3)

---

## Sumário

1. [Objetivo](#1-objetivo)
2. [Escopo e contagem](#2-escopo-e-contagem)
3. [Fases Onda 3–6](#3-fases-onda-36)
4. [Inventário completo (76 superfícies)](#4-inventário-completo-76-superfícies)
5. [Critérios de conclusão por facade](#5-critérios-de-conclusão-por-facade)
6. [Riscos e dependências](#6-riscos-e-dependências)

---

## 1. Objetivo

Documentar **todas as superfícies facade** do monólito `sm-shop` que ainda expõem ou consomem entidades JPA (`MerchantStore`, `Language`) em assinaturas públicas, e atribuir cada uma a uma **onda de migração** para `MerchantStoreId` / `LanguageCode` (ADR-003).

A Onda 3 conclui apenas o **subset P1** (checkout + search). Este plano evita redescoberta manual nas Ondas 4–6.

---

## 2. Escopo e contagem

| Categoria | Contagem | Notas |
| --------- | -------- | ----- |
| Interfaces `sm-shop-model` | 24 | Contratos canônicos para APIs REST |
| Interfaces legadas `sm-shop/controller` | 13 | Duplicatas históricas; convergir para model |
| Implementações `*FacadeImpl` | 38 | Inclui `ProductFacadeV2Impl`, configs |
| Adapters Strangler `*HttpAdapter` | 9 | Wave 1–2; já DTO-aware na fronteira HTTP |
| **Total inventariado** | **76** | Interfaces + impls − duplicatas consolidadas + adapters |

**Fora de escopo deste plano:** facades em serviços já extraídos (`reference-service`, `tax-service`, `content-service`, `merchant-service`) — migrados no respectivo módulo.

**Padrão de migração (ADR-003):**

1. Assinaturas em `sm-shop-model` passam a `MerchantStoreId` / `LanguageCode`.
2. `*FacadeImpl` hidrata entidades via `TenantEntityBridge` na entrada.
3. Controllers convertem entidade (de `MerchantStoreArgumentResolver`) → value type na chamada facade.
4. `AbstractDataPopulator` overload com bridge; legado permanece até remoção da onda.

---

## 3. Fases Onda 3–6

| Onda | Semanas (plano mestre) | Domínio | Facades-alvo | Pré-requisito |
| ---- | ---------------------- | ------- | ------------ | ------------- |
| **3** | 33–38 | Checkout + search (P1) | 6 interfaces P1 | `MerchantStoreId`, `LanguageCode`, `TenantEntityBridge` |
| **4** | 39–48 | Catalog + customer | 22 superfícies | Onda 3 gate; `ProductSnapshot` |
| **5** | 49–56 | Integration + merchant + identity | 24 superfícies | Checkout Application Service; DTOs `sm-core-modules` |
| **6** | 57–68 | Cart + order (hub) | 24 superfícies restantes | Saga/outbox; último a migrar (acoplamento 9/10) |

### Onda 3 — P1 concluída (task_07)

| # | Interface (`sm-shop-model`) | Impl principal | Status |
| - | --------------------------- | -------------- | ------ |
| 1 | `OrderFacade` (v1) | `store/facade/order/OrderFacadeImpl` | Migrado |
| 2 | `ShoppingCartFacade` (v1) | `store/facade/shoppingCart/ShoppingCartFacadeImpl` | Migrado |
| 3 | `SearchFacade` | `controller/search/SearchFacadeImpl` | Migrado |
| 4 | `ShippingFacade` | `controller/shipping/ShippingFacadeImpl` | Migrado |
| 5 | `CategoryFacade` | `store/facade/category/CategoryFacadeImpl` | Migrado |
| 6 | `ProductCommonFacade` | `store/facade/product/ProductCommonFacadeImpl` | Migrado |

ArchUnit: `FacadesNoNewEntityParamsTest` impede regressão em novos métodos P1.

---

## 4. Inventário completo (76 superfícies)

Legenda **Tipo:** `IF` = interface, `IMPL` = implementação monólito, `ADP` = Strangler HTTP adapter.

### 4.1 Referência — Onda 1 (Strangler; sem migração tenant)

| # | Facade | Tipo | Módulo / caminho | Onda migração tenant |
| - | ------ | ---- | ---------------- | -------------------- |
| 1 | `LanguageFacade` | IF | `sm-shop/.../language/facade/` | N/A (extraído) |
| 2 | `LanguageFacade` | IMPL | `LanguageFacadeImpl` | N/A |
| 3 | `LanguageFacade` | ADP | `LanguageFacadeHttpAdapter` | N/A |
| 4 | `CurrencyFacade` | IF | `sm-shop/.../currency/facade/` | N/A |
| 5 | `CurrencyFacade` | IMPL | `CurrencyFacadeImpl` | N/A |
| 6 | `CurrencyFacade` | ADP | `CurrencyFacadeHttpAdapter` | N/A |
| 7 | `CountryFacade` | IF | `sm-shop/.../country/facade/` | N/A |
| 8 | `CountryFacade` | IMPL | `CountryFacadeImpl` | N/A |
| 9 | `CountryFacade` | ADP | `CountryFacadeHttpAdapter` | N/A |
| 10 | `ZoneFacade` | IF | `sm-shop/.../zone/facade/` | N/A |
| 11 | `ZoneFacade` | IMPL | `ZoneFacadeImpl` | N/A |
| 12 | `ZoneFacade` | ADP | `ZoneFacadeHttpAdapter` | N/A |

**Nota:** `ReferencesApi` (B-002) retorna DTOs `ReadableLanguage` / `ReadableCurrency` desde Onda 3; facades internas permanecem entidade para compatibilidade Strangler.

### 4.2 Catalog — Onda 4

| # | Facade | Tipo | Impl / adapter | Prioridade |
| - | ------ | ---- | -------------- | ---------- |
| 13 | `ProductFacade` | IF | `sm-shop-model/.../product/facade/` | Alta |
| 14 | `ProductFacade` | IMPL | `ProductFacadeImpl` | Alta |
| 15 | `ProductFacade` | IMPL | `ProductFacadeV2Impl` (V2 API) | Alta |
| 16 | `ProductCommonFacade` | IF | `sm-shop-model` | ✅ Onda 3 |
| 17 | `ProductCommonFacade` | IMPL | `ProductCommonFacadeImpl` | ✅ Onda 3 |
| 18 | `ProductDefinitionFacade` | IF+IMPL | `ProductDefinitionFacadeImpl` | Média |
| 19 | `ProductInventoryFacade` | IF+IMPL | `ProductInventoryFacadeImpl` | Média |
| 20 | `ProductOptionFacade` | IF+IMPL | `ProductOptionFacadeImpl` | Média |
| 21 | `ProductOptionSetFacade` | IF+IMPL | `ProductOptionSetFacadeImpl` | Média |
| 22 | `ProductPriceFacade` | IF+IMPL | `ProductPriceFacadeImpl` | Média |
| 23 | `ProductTypeFacade` | IF+IMPL | `ProductTypeFacadeImpl` | Baixa |
| 24 | `ProductVariantFacade` | IF+IMPL | `ProductVariantFacadeImpl` | Média |
| 25 | `ProductVariantGroupFacade` | IF+IMPL | `ProductVariantGroupFacadeImpl` | Média |
| 26 | `ProductVariationFacade` | IF+IMPL | `ProductVariationFacadeImpl` | Média |
| 27 | `ProductItemsFacade` | IF+IMPL | `ProductItemsFacadeImpl` | Média |
| 28 | `CatalogFacade` | IF+IMPL | `CatalogFacadeImpl` | Alta |
| 29 | `CategoryFacade` | IF+IMPL | `CategoryFacadeImpl` | ✅ Onda 3 |
| 30 | `ManufacturerFacade` | IF+IMPL | `ManufacturerFacadeImpl` | Média |
| 31 | `SearchFacade` | IF | `sm-shop/controller/search` | ✅ Onda 3 |
| 32 | `SearchFacade` | IMPL | `SearchFacadeImpl` | ✅ Onda 3 |
| 33 | `SearchFacade` | ADP | `SearchFacadeHttpAdapter` | ✅ Onda 3 |

### 4.3 Customer + content — Onda 4

| # | Facade | Tipo | Impl / adapter | Prioridade |
| - | ------ | ---- | -------------- | ---------- |
| 34 | `CustomerFacade` | IF | `sm-shop-model/.../v1/` | Alta |
| 35 | `CustomerFacade` | IF | legado `controller/customer` | Alta (deprecar) |
| 36 | `CustomerFacade` | IMPL | `store/facade/customer/CustomerFacadeImpl` | Alta |
| 37 | `CustomerFacade` | IMPL | `controller/customer/CustomerFacadeImpl` | Alta (merge) |
| 38 | `ContentFacade` | IF | `sm-shop-model` | Média |
| 39 | `ContentFacade` | IMPL | `ContentFacadeImpl` | Média |
| 40 | `ContentFacade` | ADP | `ContentFacadeHttpAdapter` | Média |

### 4.4 Merchant + identity + system — Onda 5

| # | Facade | Tipo | Impl / adapter | Prioridade |
| - | ------ | ---- | -------------- | ---------- |
| 41 | `StoreFacade` | IF+IMPL | `StoreFacadeImpl` | Alta |
| 42 | `StoreFacade` | ADP | `StoreFacadeHttpAdapter` | Alta |
| 43 | `MerchantConfigurationFacade` | IF+IMPL | `MerchantConfigurationFacadeImpl` | Média |
| 44 | `MerchantConfigurationFacade` | ADP | `MerchantConfigurationFacadeHttpAdapter` | Média |
| 45 | `UserFacade` | IF+IMPL | `UserFacadeImpl` | Média |
| 46 | `SecurityFacade` | IF+IMPL | `SecurityFacadeImpl` | Média |
| 47 | `OptinFacade` | IF+IMPL | `OptinFacadeImpl` | Baixa |
| 48 | `MarketPlaceFacade` | IF+IMPL | `MarketPlaceFacadeImpl` | Baixa |
| 49 | `TaxFacade` | IF | `sm-shop-model` | Média |
| 50 | `TaxFacade` | IMPL | `store/facade/tax/TaxFacadeImpl` | Média |
| 51 | `TaxFacade` | ADP | `TaxFacadeHttpAdapter` | Média |

### 4.5 Integration / configuration — Onda 5

| # | Facade | Tipo | Impl | Prioridade |
| - | ------ | ---- | ---- | ---------- |
| 52 | `ConfigurationsFacade` | IF | `sm-shop-model` | Média |
| 53 | `PaymentConfigurationFacade` | IMPL | `PaymentConfigurationFacadeImpl` | Alta |
| 54 | `ShippingConfigurationFacade` | IMPL | `ShippingConfigurationFacadeImpl` | Alta |
| 55 | `ShippingModuleConfigurationFacade` | IF | `sm-shop-model` | Média |
| 56 | `AbstractConfigurationFacadeImpl` | IMPL | base abstrata | Média |
| 57 | `ShippingFacade` | IF | `sm-shop-model` | ✅ Onda 3 |
| 58 | `ShippingFacade` | IMPL | `ShippingFacadeImpl` | ✅ Onda 3 |

### 4.6 Checkout hub — Onda 6 (último)

| # | Facade | Tipo | Impl | Prioridade |
| - | ------ | ---- | ---- | ---------- |
| 59 | `OrderFacade` | IF | `sm-shop-model/v1` | ✅ Onda 3 (parcial) |
| 60 | `OrderFacade` | IF | legado `controller/order` | Alta — merge |
| 61 | `OrderFacade` | IMPL | `store/facade/order/OrderFacadeImpl` | Onda 6 (hub completo) |
| 62 | `OrderFacade` | IMPL | `controller/order/OrderFacadeImpl` | Onda 6 (deprecar) |
| 63 | `ShoppingCartFacade` | IF | `sm-shop-model/v1` | ✅ Onda 3 |
| 64 | `ShoppingCartFacade` | IF | legado `controller/shoppingCart` | Alta — merge |
| 65 | `ShoppingCartFacade` | IMPL | `store/facade/shoppingCart/ShoppingCartFacadeImpl` | Onda 6 |
| 66 | `ShoppingCartFacade` | IMPL | `controller/shoppingCart/ShoppingCartFacadeImpl` | Onda 6 (deprecar) |

### 4.7 Duplicatas legadas a eliminar (contam no total 76)

| # | Item | Ação Onda 4–6 |
| - | ---- | ------------- |
| 67–70 | Pares IF legado vs `sm-shop-model` (Order, ShoppingCart, Customer, Search) | Consolidar em `sm-shop-model`; remover pacote `controller/*/facade` |
| 71–76 | Reserva: novos adapters Strangler Onda 4+ (catalog-read, customer) | Adicionar linha ao inventário ao criar adapter |

**Total linhas inventariadas:** 76

---

## 5. Critérios de conclusão por facade

Uma facade conta como **migrada** quando:

1. Nenhum método **novo** em `sm-shop-model` aceita `MerchantStore` ou `Language` (ArchUnit).
2. Implementação usa `TenantEntityBridge` para chamadas core que ainda exigem entidade.
3. Controllers convertem na fronteira (`MerchantStoreId.of(code)`, `LanguageCode.of(code)`).
4. Testes de integração/API existentes passam sem mudança de contrato REST.
5. Adapters HTTP Strangler (se existirem) recebem/enviam DTOs ou códigos, não entidades serializadas.

---

## 6. Riscos e dependências

| Risco | Mitigação |
| ----- | --------- |
| Duplicata IF legado + model | Inventário §4.7; uma PR por par consolidado |
| `AbstractDataPopulator` overload duplo | Manter bridge até Onda 6; documentar em STATE.md |
| Order hub 12+ services | Onda 6 somente após CheckoutApplicationService + outbox |
| Regressão Strangler | Pact Wave1 inalterado; adapters já mapeiam DTO↔entidade |
| Contagem drift (novos facades) | Atualizar este doc no gate de cada onda |

---

## Referências

- `docs/decomposition/MIGRATION-MASTER-PLAN.md` § Onda 3–6
- `.specs/project/STATE.md` B-001, B-002
- `.compozy/tasks/onda-3-contracts-dto/adrs/adr-003.md`
- `sm-shop-model/.../FacadesNoNewEntityParamsTest.java`
