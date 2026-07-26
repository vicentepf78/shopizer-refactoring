# Onda 4 — Especificação Catalog + Customer

**ID da feature:** `onda-4-catalog-customer`
**Fase:** Specify → Design → Tasks (Execute bloqueado até conclusão da Onda 3)
**Complexidade:** Grande (2 serviços deployáveis + Strangler + migração de contratos)
**Fonte:** [MIGRATION-MASTER-PLAN.md](../../../docs/decomposition/MIGRATION-MASTER-PLAN.md) § Onda 4
**Pré-requisito:** Onda 3 — fundações de `ProductSnapshot`, `CustomerSnapshot`, `LanguageCode`, `MerchantStoreId`, Checkout Application Service

---

## Declaração do problema

Catalog é o **domínio de maior acoplamento aferente** em `sm-core` (10 referências de serviço de entrada: order, cart, shipping, search, merchant, reference e outros). Extrair o CRUD de catalog de uma vez arrastaria metade do monólito para o primeiro corte. Customer é comparativamente isolado na camada de serviço (score 5/10), mas está **acoplado transacionalmente a order** (criação de customer durante checkout) e ao **merge de shopping cart** (carrinho de sessão + carrinho autenticado do customer em uma transação de DB).

A Onda 3 entrega DTOs cross-service (`ProductSnapshot`, `CustomerSnapshot`) e primitivas de tenant (`LanguageCode`, `MerchantStoreId`) que desbloqueiam a extração do read path. Sem uma spec formal da Onda 4, as equipes ou:

- Extraem writes de catalog cedo demais e recriam o grafo de bloqueadores upstream remotamente, ou
- Adiam a extração de customer indefinidamente porque o merge de cart não pode ser desemaranhado.

Esta spec define **extração faseada de leitura de catalog** e **extração de perfil de customer com desacoplamento explícito do merge de cart**, seguindo o padrão Strangler comprovado nas Ondas 1–2.

---

## Objetivos

- [ ] `catalog-service` deployável como aplicação Spring Boot servindo **APIs públicas e de storefront de leitura** para produtos, categorias, fabricantes, inventário e preços
- [ ] `customer-service` deployável servindo APIs de **perfil, endereços, opt-in e leitura/escrita de reviews** (excluindo orquestração de login/checkout)
- [ ] Monólito permanece **autoridade de escrita** para mutações admin de catalog (CRUD privado de produto) até onda posterior
- [ ] Monólito consome ambos os serviços via HTTP Strangler em **paths REST congelados**
- [ ] Zero entidades JPA nas respostas JSON dos endpoints migrados (critério Onda 1)
- [ ] `ProductIndexPayload` migra para **`ProductSnapshot`** na indexação de search (contrato Onda 3)
- [ ] Merge de cart desacoplado: monólito orquestra merge usando **`CustomerSnapshot`** do customer-service
- [ ] Testes Pact para superfícies P1 de leitura de catalog + perfil de customer
- [ ] Reutilizar `shopizer-api-contracts`, cores finos (`sm-catalog-core`, `sm-customer-core`), RestTemplate, replicação JWT

---

## Fora de escopo

| Funcionalidade | Motivo |
| -------------- | ------ |
| APIs admin de escrita de catalog em `catalog-service` | Plano mestre: escrita permanece no monólito temporariamente; score 7/10 extração completa |
| `shoppingcart-service` / extração de persistência de cart | Onda 6; merge de cart apenas **desacoplado**, não extraído |
| `order-service`, checkout, saga `processOrder` | Onda 6; Checkout Application Service da Onda 3 permanece no monólito |
| CRUD admin de `ProductTypeApi` em catalog-service | Adiado — listagem read-only de product-type PODE ser incluída; mutações permanecem no monólito |
| Autenticação de customer (`AuthenticateCustomerApi`, emissão JWT) | Autoridade de login permanece em `sm-shop` (padrão AD-006) |
| Database split por serviço | AD-003 / AD-022 — schema compartilhado `SALESMANAGER` |
| Consolidação completa Mapper/Populator (4 facades de produto) | Quick wins Fase 1; paralelo, não bloqueante |
| Redesign de DTOs `PaymentModule` / `ShippingQuoteModule` | Serviço de integração Onda 5 |
| Movimentação greenfield de `InitializationDatabaseImpl` | AD-004 — bootstrap permanece no monólito |
| Redesign de motor de preços / regras de promoção | Leitura de preços apenas; regras permanecem nos serviços do monólito |

---

## Histórias de usuário

### P1: Catalog Service — leituras de produto e categoria no storefront ⭐ MVP

**História de usuário**: Como visitante da loja, quero navegar produtos e categorias via APIs públicas existentes, para que a descoberta de produtos não dependa de serviços de catalog in-process no monólito.

**Por quê P1**: O read path de catalog é a superfície de maior tráfego e maior acoplamento que `ProductSnapshot` da Onda 3 desbloqueia sem mover writes.

**Critérios de aceite**:

1. WHEN `GET /api/v1/products/**` (lista pública, por id, por sku, relacionados, grupo) THEN `catalog-service` SHALL retornar `ReadableProduct` / DTOs de lista — SHALL NOT expor entidade `Product`
2. WHEN `GET /api/v1/category/**` (árvore, por id, contagens de produto) THEN `catalog-service` SHALL retornar DTOs `ReadableCategory` localizados por `lang`
3. WHEN `GET /api/v1/products/inventory/**` ou endpoints de preço (leitura pública) THEN `catalog-service` SHALL retornar DTOs de inventário/preço sem chamada cross a order/cart
4. WHEN contexto de tenant é necessário THEN `catalog-service` SHALL aceitar código `store` e resolver via `MerchantStoreId` / referência HTTP ao merchant-service (Onda 2) — SHALL NOT injetar entidade `MerchantStore`
5. WHEN `lang` é necessário THEN `catalog-service` SHALL resolver `LanguageCode` via HTTP ao `reference-service`
6. WHEN produto não encontrado THEN SHALL retornar HTTP 404 com mesmo envelope de erro do monólito
7. WHEN `catalog-service` indisponível e strangler habilitado THEN BFF SHALL retornar HTTP 503 com correlation id — sem fallback silencioso in-process

**Teste independente**: Deploy `catalog-service` + deps Ondas 1–2; `GET /api/v1/products?store=DEFAULT&lang=en` retorna produtos paginados; árvore de categorias corresponde ao baseline do monólito.

**Componentes fonte:**

| Papel | Caminho |
| ----- | ------- |
| Entidades | `sm-core-model/.../catalog/` |
| Serviços | `sm-core/.../services/catalog/product/`, `category/`, `inventory/`, `pricing/`, `manufacturer/` |
| APIs | `sm-shop/.../api/v1/product/ProductApi.java`, `CategoryApi.java`, `ProductInventoryApi.java`, `ProductPriceApi.java`, `ProductManufacturerApi.java`, `ProductGroupApi.java` |
| Facades | `ProductFacadeImpl`, `ProductCommonFacadeImpl`, `CategoryFacadeImpl` |
| DTOs | `sm-shop-model/.../model/catalog/` |

**Explicitamente FORA desta story:** APIs admin privadas `POST/PUT/DELETE` de produto — permanecem no monólito durante a Onda 4.

---

### P1: Catalog Service — API interna ProductSnapshot ⭐ MVP

**História de usuário**: Como engenheiro de plataforma, quero uma API HTTP versionada de `ProductSnapshot` dentro do catalog-service, para que indexação de search e leituras cross-service usem um contrato canônico da Onda 3.

**Critérios de aceite**:

1. WHEN `GET /internal/v1/products/{id}/snapshot?store=&lang=` com network policy THEN SHALL retornar `ProductSnapshot` com `schemaVersion`
2. WHEN `schemaVersion` solicitada não é suportada THEN SHALL retornar HTTP 422
3. WHEN monólito `ProductSnapshotBuilder` (substitui `ProductIndexPayloadBuilder`) monta documento de índice THEN MAY chamar API interna de snapshot OU montar in-process durante transição — Design escolhe único dono do builder
4. WHEN `search-service` recebe snapshot para indexação THEN SHALL aceitar schema v2 de `ProductSnapshot` (substitui `ProductIndexPayload` v1)

**IDs de requisito:** CAT-08, CAT-09, STR-07

---

### P1: Customer Service — perfil, endereços, opt-in ⭐ MVP

**História de usuário**: Como customer registrado, quero gerenciar meu perfil, endereços de entrega/cobrança e opt-in de marketing via APIs existentes, sem o monólito ser dono da persistência de customer para essas operações.

**Critérios de aceite**:

1. WHEN `GET /api/v1/customer/profile` (autenticado) THEN `customer-service` SHALL retornar `ReadableCustomer` — sem entidade `Customer` no JSON
2. WHEN `PUT /api/v1/customer/profile` ou endpoints de endereço THEN SHALL mutar linhas de customer com escopo de loja equivalente ao monólito
3. WHEN `POST /api/v1/customer/optin` THEN SHALL persistir opt-in via lógica de `CustomerOptinService` em `sm-customer-core`
4. WHEN resolução de country/zone/language necessária THEN SHALL chamar HTTP ao `reference-service`
5. WHEN `POST /api/v1/customer` (corpo de registro) é chamado em paths **admin privados** THEN MAY permanecer no monólito OU delegar conforme OQ-06 — **auto-registro público permanece no monólito**
6. WHEN customer não encontrado THEN HTTP 404; não autorizado THEN 401/403 conforme regras de segurança existentes

**Teste independente**: Token JWT de customer; atualizar perfil; adicionar endereço; verificar linha no DB; opt-in registrado.

**Componentes fonte:**

| Papel | Caminho |
| ----- | ------- |
| Entidades | `sm-core-model/.../customer/` |
| Serviços | `sm-core/.../services/customer/`, `optin/`, `attribute/` |
| APIs | `sm-shop/.../api/v1/customer/CustomerApi.java`, `CustomerNewsletterApi.java`, APIs de review (read path) |
| Facade | `CustomerFacadeImpl` |
| DTOs | `sm-shop-model/.../model/customer/` |

---

### P1: Desacoplamento do merge de cart ⭐ MVP

**História de usuário**: Como customer recorrente, quero que meu carrinho de sessão seja mesclado com meu carrinho salvo no login, sem `customer-service` participar da transação de banco do shopping cart.

**Por quê P1**: O plano mestre explicita "desacoplar merge de cart" como entregável de customer da Onda 4.

**Critérios de aceite**:

1. WHEN login bem-sucedido no monólito THEN `CustomerFacade` SHALL obter `CustomerSnapshot` do `customer-service` (ou cache) antes de chamar `ShoppingCartService.mergeShoppingCarts`
2. WHEN merge executa THEN `ShoppingCartService` SHALL NOT exigir entidade `Customer` in-process de `CustomerService` — usa id do snapshot + código da loja
3. WHEN `customer-service` está down durante merge THEN monólito SHALL falhar merge com erro claro OU caminho de degradação documentado (Design: fail closed preferido)
4. WHEN checkout de order cria customer na mesma transação (hoje) THEN comportamento inalterado na Onda 4 — acoplamento com order adiado para Onda 6

**IDs de requisito:** CUS-08, CUS-09, STR-08

---

### P1: Strangler BFF — adapters HTTP catalog + customer ⭐ MVP

**História de usuário**: Como engenheiro de plataforma, quero adapters HTTP com feature flag para facades de catalog e customer, para validar extração sem reescrever controllers.

**Critérios de aceite**:

1. WHEN `wave4.strangler.enabled=true` THEN adapters GET públicos de product/category/manufacturer/inventory/price SHALL delegar ao `catalog-service`
2. WHEN strangler habilitado THEN adapters de perfil/endereço/optin de customer SHALL delegar ao `customer-service`
3. WHEN strangler desabilitado THEN facades in-process SHALL comportar-se como hoje
4. WHEN APIs admin privadas de **escrita** de produto invocadas THEN SHALL permanecer in-process no monólito (sem delegação ao catalog-service)
5. WHEN falha remota THEN HTTP 503 + `X-Correlation-Id` — sem fallback silencioso
6. JWT encaminhado em rotas privadas de customer conforme padrão Ondas 1–2

**IDs de requisito:** STR-01…STR-06

---

### P2: Imagens de produto via content-service

**História de usuário**: Como admin de loja, quero imagens de produto/variante/opção enviadas pelos fluxos existentes, com blobs pertencentes ao content-service (adiamento Onda 2 OQ-02).

**Critérios de aceite**:

1. WHEN `ProductOptionFacadeImpl` / grupo de variante faz upload de imagem THEN monólito SHALL usar `ContentServiceClient` (já Onda 2) com `FileContentType` PRODUCT/VARIANT/PROPERTY
2. WHEN `catalog-service` serve DTOs de leitura de produto THEN URLs de imagem SHALL permanecer consistentes com semântica de `LocationImageConfig` / proxy estático
3. WHEN `/static/products/**` solicitado THEN monólito `StaticContentProxy` estendido OU redirect do catalog-service — detalhe em Design

**IDs de requisito:** CAT-10, CNT-W4-01

---

### P2: Testes de contrato (Pact) — Onda 4

**História de usuário**: Como desenvolvedor, quero cobertura Pact para endpoints de leitura de catalog e perfil de customer, para que drift de contrato falhe no CI antes do deploy.

**Critérios de aceite**:

1. WHEN schema de `ProductSnapshot` ou `ReadableProduct` quebra THEN consumer pact SHALL falhar
2. WHEN gate Full executa THEN SHALL incluir `Wave4ConsumerPactTest` + testes provider nos dois serviços
3. WHEN `schemaVersion` incrementa THEN fixtures pact SHALL fixar versões suportadas

**IDs de requisito:** STR-02, CAT-11, CUS-10

---

### P3: Observabilidade — Onda 4

**História de usuário**: Como operador, quero health checks e correlation IDs em catalog-service e customer-service.

**Critérios de aceite**:

1. WHEN `GET /actuator/health` THEN cada serviço reporta DB + deps HTTP (reference, merchant para catalog)
2. WHEN requisição processada THEN `X-Correlation-Id` propagado conforme Ondas 1–2
3. WHEN p95 de leitura de catalog excede 2× baseline do monólito THEN documentar tuning (connection pool, cache de snapshot)

**IDs de requisito:** STR-05

---

## Casos extremos

### Catalog

- WHEN produto tem variantes sem inventário THEN DTO de leitura SHALL corresponder à semântica de inventário vazio do monólito
- WHEN profundidade da árvore de categorias excede paginação THEN SHALL preservar comportamento lazy-load de `CategoryFacadeImpl`
- WHEN `ProductFacadeV2` e `ProductFacadeImpl` divergem THEN adapter strangler MUST mirar **paths públicos V1 primeiro**; delegação V2 documentada em Design
- WHEN apenas preço muda (sem evento de save de produto) THEN índice de search PODE ficar stale — aceitar conforme GAP-SRCH; reindex de snapshot é responsabilidade do producer no monólito
- WHEN metadados de arquivo de produto digital solicitados na API pública THEN SHALL NOT expor tokens de download sem auth — preservar regras do monólito

### Customer

- WHEN email duplicado por loja THEN semântica de conflito de registro inalterada (path de auth no monólito)
- WHEN bug do populator de address `stateProvince` (quick win) THEN fix PODE entrar na Onda 4 se trivial — não obrigatório para gate
- WHEN POST de review de customer permanece no monólito THEN `customer-service` PODE expor reviews somente leitura inicialmente — Design faseia write path
- WHEN corridas de merge (dois tabs no login) THEN idempotência de merge segue comportamento existente de `ShoppingCartService`

### Transversal

- WHEN `reference-service` ou `merchant-service` down THEN catalog/customer SHALL 503 em rotas que precisam de resolução
- WHEN migration de DB compartilhado executa THEN coordenar ownership — serviços catalog/customer usam mesmas tabelas que writers do monólito temporariamente

---

## Rastreabilidade de requisitos

| ID de requisito | Story | Resumo | Fase | Status |
| --------------- | ----- | ------ | ---- | ------ |
| CAT-01 | P1 Catalog | `catalog-service` deployável Spring Boot | Execute | Planejado |
| CAT-02 | P1 Catalog | APIs públicas de leitura de produto paths congelados | Execute | Planejado |
| CAT-03 | P1 Catalog | APIs de leitura de categoria | Execute | Planejado |
| CAT-04 | P1 Catalog | Leitura de fabricante + grupo de produto | Execute | Planejado |
| CAT-05 | P1 Catalog | Leitura pública de inventário/preço | Execute | Planejado |
| CAT-06 | P1 Catalog | Zero JPA em respostas JSON | Execute | Planejado |
| CAT-07 | P1 Catalog | HTTP reference + merchant para tenant/lang | Execute | Planejado |
| CAT-08 | P1 Snapshot | API interna `ProductSnapshot` | Execute | Planejado |
| CAT-09 | P1 Snapshot | Índice de search usa ProductSnapshot v2 | Execute | Planejado |
| CAT-10 | P2 Images | Imagens de produto via content-service | Execute | Planejado |
| CAT-11 | P2 Pact | Provider pact de catalog | Execute | Planejado |
| CAT-12 | P1 Core | Serviços de leitura `sm-catalog-core` extraídos | Execute | Planejado |
| CUS-01 | P1 Customer | `customer-service` deployável | Execute | Planejado |
| CUS-02 | P1 Customer | Leitura/atualização de perfil | Execute | Planejado |
| CUS-03 | P1 Customer | CRUD de endereço | Execute | Planejado |
| CUS-04 | P1 Customer | Endpoints de opt-in | Execute | Planejado |
| CUS-05 | P1 Customer | Zero JPA em JSON | Execute | Planejado |
| CUS-06 | P1 Customer | HTTP reference para geo/lang | Execute | Planejado |
| CUS-07 | P1 Customer | Leitura de reviews (escrita opcional P2) | Execute | Planejado |
| CUS-08 | P1 Merge | CustomerSnapshot para merge de cart | Execute | Planejado |
| CUS-09 | P1 Merge | Merge ShoppingCart sem CustomerService in-process | Execute | Planejado |
| CUS-10 | P2 Pact | Provider pact de customer | Execute | Planejado |
| STR-01 | P1 Strangler | Feature flag `wave4.strangler.enabled` | Execute | Planejado |
| STR-02 | P2 Pact | Consumer pact Onda 4 em sm-shop | Execute | Planejado |
| STR-03 | AD-022 | Schema DB compartilhado | Execute | Planejado |
| STR-04 | P1 | Paths REST congelados | Execute | Planejado |
| STR-05 | P3 | Actuator health + correlation | Execute | Planejado |
| STR-06 | P1 | catalog→reference/merchant; customer→reference | Execute | Planejado |
| STR-07 | P1 | Migração do builder ProductSnapshot | Execute | Planejado |
| STR-08 | P1 | Orquestração de merge de cart no monólito | Execute | Planejado |

**Cobertura:** 30 total, 30 mapeados, 0 não mapeados

---

## Questões em aberto — Resolvidas ✅

Ver [context.md](./context.md) e [design.md](./design.md).

| ID | Decisão |
|----|---------|
| OQ-01 | Catalog read-first; writes no monólito |
| OQ-02 | ProductSnapshot canônico |
| OQ-03 | CustomerSnapshot + orquestração de merge no monólito |
| OQ-04 | Imagens de produto via content-service |
| OQ-05 | Strangler em paths V1; V2 delega no mesmo adapter |
| OQ-06 | Endpoints de auth permanecem no monólito |

---

## Critérios de sucesso

- [ ] `catalog-service` e `customer-service` passam health check e todos os endpoints P1 em integração
- [ ] Strangler produz respostas equivalentes ao baseline in-process (pact verdes)
- [ ] Nenhum endpoint migrado retorna tipos de entidade JPA no JSON
- [ ] Indexação de search aceita `ProductSnapshot` v2 do producer no monólito
- [ ] Teste de integração de merge de cart passa com customer-service remoto
- [ ] APIs admin de **escrita** de produto ainda funcionam somente no monólito
- [ ] Reator `./mvnw clean install` verde com módulos Onda 4
- [ ] Padrão documentado em STATE.md para Onda 5
- [ ] p95 de leitura pública ≤ 2× baseline do monólito

---

## Apêndice A — Scores de acoplamento (plano mestre)

| Domínio | Dificuldade | Aferente | Abordagem Onda 4 |
|---------|-------------|----------|------------------|
| catalog | 7/10 | 10 refs | Extração de API de leitura apenas |
| customer | 5/10 | order cria customer | Extração de perfil; txn de order adiada |

---

## Apêndice B — Arquivos-fonte principais

### Catalog

| Papel | Caminho |
|-------|---------|
| Serviço de produto | `sm-core/.../catalog/product/ProductServiceImpl.java` |
| Serviço de categoria | `sm-core/.../catalog/category/CategoryServiceImpl.java` |
| API de produto | `sm-shop/.../api/v1/product/ProductApi.java` |
| API de categoria | `sm-shop/.../api/v1/category/CategoryApi.java` |
| Facades | `sm-shop/.../facade/product/ProductFacadeImpl.java` |

### Customer

| Papel | Caminho |
|-------|---------|
| Serviço de customer | `sm-core/.../customer/CustomerServiceImpl.java` |
| Merge de cart | `sm-core/.../shoppingcart/ShoppingCartServiceImpl.java` (`mergeShoppingCarts`) |
| API de customer | `sm-shop/.../api/v1/customer/CustomerApi.java` |
| Facade | `sm-shop/.../facade/customer/CustomerFacadeImpl.java` |
