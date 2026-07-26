# Resoluções OQ — Design Onda 4 (2026-07-26)

Decisões confirmadas para Specify/Design assumindo Onda 3 (contratos) completa. Detalhes em `design.md`.

| ID | Decisão | Escolha |
|----|----------|--------|
| **OQ-01** | Faseamento da extração de catálogo | **APIs de leitura primeiro; writes admin permanecem no monólito** (Opção A) |
| **OQ-02** | Contrato cross-service de produto | **`ProductSnapshot` canônico**; migrar `ProductIndexPayload` → snapshot v2 (Opção A) |
| **OQ-03** | Fronteira customer vs cart | **`CustomerSnapshot` + merge orquestrado no monólito**; sem shopping-cart service na Onda 4 (Opção B) |
| **OQ-04** | Imagens de produto / arquivos digitais | **Estender content-service** com paths `productFileManager`; catalog chama content HTTP (Opção A) |
| **OQ-05** | Consolidação de facades de catálogo | **Strangler nos paths V1 existentes**; paths mapper V2 delegam o mesmo adaptador HTTP (Opção A) |
| **OQ-06** | Endpoints de auth de customer | **Login/registro/reset de senha permanecem no monólito**; customer-service possui apenas CRUD de perfil (Opção A) |

**Decisões adicionais de Design:**

| ID | Decisão |
|----|----------|
| AD-015 | Um workflow TLC/Compozy para Catalog + Customer (mesma janela, profile Strangler compartilhado) |
| AD-016 | JAR thin `sm-catalog-core` — serviços de leitura + mappers; writes permanecem em `sm-core` do monólito |
| AD-017 | JAR thin `sm-customer-core` — domínio customer sem acoplamento transacional order/cart |
| AD-018 | `catalog-service` expõe `GET /internal/v1/products/{id}/snapshot` para producers search/BFF |
| AD-019 | Merge de carrinho: `CustomerServiceClient.resolveForMerge(customerId, storeCode)` retorna `CustomerSnapshot`; `ShoppingCartService.mergeShoppingCarts` permanece no monólito |
| AD-020 | Mutações admin de produto (POST/PUT/DELETE APIs privadas de produto) **não** roteadas para catalog-service na Onda 4 |
| AD-021 | Value types `LanguageCode` / `MerchantStoreId` da Onda 3 exigidos em todas as novas fronteiras HTTP |
| AD-022 | Schema DB compartilhado (AD-003) continua; sem split DB-per-service na Onda 4 |

**Status:** Pronto para Tasks
