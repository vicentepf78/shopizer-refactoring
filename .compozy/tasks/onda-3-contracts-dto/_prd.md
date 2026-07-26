# PRD: Onda 3 — Contracts DTO + Checkout Application Service

**Slug da feature:** `onda-3-contracts-dto`  
**Fonte da verdade:** TLC em `.specs/features/onda-3-contracts-dto/` (Opção A — escopo congelado)  
**Status:** Pronto para TechSpec  
**Data:** 2026-07-26

---

## Visão geral

As Ondas 1 e 2 provaram que o Shopizer pode extrair domínios de baixo e médio risco atrás de um Strangler BFF com contratos DTO compartilhados. A Onda 3 é diferente: **sem novos microserviços implantáveis**. É uma **onda de contratos e refatoração interna** dentro do monólito que desbloqueia as Ondas 4–6 (leitura de catálogo, customer, hub de integração, shopping cart, order).

Hoje, ~94% das interfaces facade em `sm-shop-model` aceitam entidades JPA (`MerchantStore`, `Language`) como parâmetros. Os contratos de plugins de integração (`PaymentModule`, `ShippingQuoteModule`) aceitam grafos completos de domínio (`Order`, `Customer`, `ShoppingCartItem`). A orquestração de checkout está em `OrderFacadeImpl`, que injeta 12+ serviços core. `processOrder` executa pagamento, persistência de customer, criação de order, decremento de inventário e notificações em um único método transacional — um bloqueador para separar order e payments.

A Onda 3 entrega **snapshots DTO cross-service**, **value types de identificador de tenant**, **redesign de DTOs de módulos de integração**, um **Checkout Application Service** no monólito e uma **base saga/outbox** para `processOrder`. O `ProductIndexPayload` interim de Search evolui para um `ProductSnapshot` adequado; `SearchItem` sai de `shopizer-commons` e entra em `shopizer-api-contracts`.

**Usuários primários:** engenheiros de plataforma executando a decomposição; indiretamente, todos os consumidores de API que se beneficiam de contratos estáveis antes da extração de catálogo/order.

**Por que importa:** Sem a Onda 3, as Ondas 4–6 repetem acoplamento MODEL nas fronteiras HTTP, a extração de integration-service permanece impossível e order/payments ficam em um ciclo bidirecional (issue #1 do plano mestre).

---

## Objetivos

- Introduzir **ProductSnapshot**, **OrderSnapshot** e **CustomerSnapshot** em `shopizer-api-contracts` com builders em `sm-core` / `sm-shop` (não no JAR de contracts — L-002).
- Substituir parâmetros de entidade por **MerchantStoreId** e **LanguageCode** nas interfaces facade P1; fornecer adapters bridge para que implementações possam hidratar entidades in-process durante a transição (resolve B-001).
- Redesenhar **PaymentModule** / **ShippingQuoteModule** para aceitar DTOs de integração; manter assinaturas legacy via adapter bridge até que todos os plugins migrem (Onda 5).
- Extrair **CheckoutApplicationService** de `OrderFacadeImpl` — entrada única de orquestração para o fluxo place-order sem alterar caminhos REST públicos.
- Adicionar base **outbox + processOrder em estágios** — não saga distribuída completa; suficiente para quebrar a suposição in-process order↔payments para a Onda 6.
- Migrar **SearchItem** (e tipos de resposta de search relacionados) para `shopizer-api-contracts`; alinhar `ProductIndexPayloadBuilder` com `ProductSnapshot` (evolui AD-009).
- Fechar **B-002**: `ReferencesApi` retorna DTOs `ReadableLanguage` / `ReadableCurrency`, não entidades JPA.
- Publicar um **plano de migração de interfaces facade** (inventário faseado de 76 facades) para as Ondas 4–6.
- **Pré-requisito rígido:** Execute da Onda 2 completo (gate verde, `docker-compose-wave2.yml`, suite Pact).

### Resultados de negócio

| Resultado | Indicador |
| --------- | --------- |
| Ondas 4–6 desbloqueadas | Pré-requisitos 1–5 do plano mestre atendidos no monólito |
| Higiene de contratos | Zero novos imports JPA em `shopizer-api-contracts` |
| Manutenibilidade do checkout | `OrderFacadeImpl` delega orquestração ao application service |
| Prontidão para integração | `PaymentModule` V2 invocável com contexto apenas DTO |
| Estabilidade do contrato de search | Pact usa `SearchItem` de api-contracts, não commons |

---

## Histórias de usuário

### Engenheiro de plataforma — contratos snapshot (P1 / SNP)

Como **engenheiro de plataforma**, quero DTOs snapshot versionados para dados de product, order e customer, para que leituras cross-service e payloads de índice não exijam `sm-core-model` no classpath do consumidor.

**Aceite:**

1. `ProductSnapshot` é a projeção canônica de leitura de catálogo; `ProductIndexPayload` delega ou mapea dele com bump de `schemaVersion`.
2. `OrderSnapshot` e `CustomerSnapshot` capturam campos relevantes ao checkout sem associações lazy JPA.
3. Snapshots serializam via Jackson com nomes de campo estáveis para Pact.
4. Builders ficam fora de `shopizer-api-contracts`.

**IDs de requisito:** SNP-01…SNP-07, CTR-01…CTR-03

### Engenheiro de plataforma — identificadores de tenant (P1 / TNT)

Como **engenheiro de plataforma**, quero que interfaces facade aceitem `MerchantStoreId` e `LanguageCode` em vez de entidades JPA, para que adapters HTTP Strangler em ondas futuras não vazem tipos de persistência.

**Aceite:**

1. Value types em `shopizer-api-contracts` com validação (código não vazio).
2. Facades P1 migradas: `OrderFacade`, `ShoppingCartFacade`, `SearchFacade`, `ShippingFacade`, `CategoryFacade`, `ProductCommonFacade`.
3. Helpers bridge hidratam `MerchantStore` / `Language` apenas em implementações do monólito.
4. `AbstractDataPopulator` ganha overload aceitando primitivos de tenant (retrocompatível).

**IDs de requisito:** TNT-01…TNT-06, FAC-01…FAC-05

### Engenheiro de plataforma — DTOs de integração (P1 / INT)

Como **engenheiro de plataforma**, quero que contratos de plugins de payment e shipping aceitem DTOs, para que a extração de `integration-service` na Onda 5 não arraste entidades `Order` através de fronteiras de processo.

**Aceite:**

1. Novos DTOs: `PaymentRequestContext`, `ShippingQuoteRequestContext`, etc. em `sm-core-modules`.
2. Interfaces paralelas `PaymentModuleV2` / `ShippingQuoteModuleV2`; registry resolve V2 quando o plugin implementa.
3. Plugins legacy continuam funcionando via adapter entidade→DTO em `PaymentServiceImpl` / `ShippingServiceImpl`.
4. Sem breaking change no bytecode de plugins Stripe/PayPal/USPS existentes na Onda 3.

**IDs de requisito:** INT-01…INT-06

### Visitante da vitrine — checkout inalterado (P1 / CHK)

Como **visitante da vitrine**, quero que o checkout se comporte exatamente como hoje, para que a refatoração de contratos não regredir a colocação de pedidos.

**Aceite:**

1. Caminhos REST públicos de order inalterados (padrão STR-04 da Onda 2).
2. `CheckoutApplicationService.placeOrder(...)` produz resultados de order idênticos no happy path e nos caminhos de erro conhecidos.
3. Sem violação de novo orçamento de latência voltada ao usuário (p95 ≤ 2× baseline).

**IDs de requisito:** CHK-01…CHK-06

### Engenheiro de plataforma — base processOrder (P1 / SAG)

Como **engenheiro de plataforma**, quero que `processOrder` registre eventos outbox por estágio, para que a Onda 6 possa separar confirmação de pagamento da persistência de order sem reescrever regras de negócio.

**Aceite:**

1. Tabela `CHECKOUT_OUTBOX` (ou equivalente) com chaves de evento idempotentes.
2. Estágios: `PAYMENT_REQUESTED`, `PAYMENT_CONFIRMED`, `ORDER_PERSISTED`, `INVENTORY_DECREMENTED` (mínimo).
3. Escrita outbox na mesma transação + passo de negócio para a Onda 3 (sem message broker ainda).
4. Feature flag `checkout.outbox.enabled` default false; testes cobrem ambos os caminhos.

**IDs de requisito:** SAG-01…SAG-05

### Consumidor de API — DTOs de referência (P1 / REF)

Como **consumidor de API**, quero que endpoints de lista de language e currency retornem DTOs legíveis, para que respostas públicas de referência batam com a higiene de contratos da Onda 1 (fecha B-002).

**IDs de requisito:** REF-01…REF-02

### Consumidor de search — schema estável (P2 / SRCH)

Como **mantenedor de search-service**, quero `SearchItem` em api-contracts, para que Pact não dependa de `shopizer-commons` (resolução OQ-06 da Onda 2).

**IDs de requisito:** SRCH-01…SRCH-04

### Engenheiro de plataforma — inventário de migração (P2 / FAC)

Como **engenheiro de plataforma**, quero um plano faseado documentado para as interfaces facade restantes, para que as Ondas 4–6 executem sem redescobrir 76 facades.

**IDs de requisito:** FAC-06

---

## Funcionalidades principais

### F1 — DTOs snapshot (MVP)

`ProductSnapshot`, `OrderSnapshot`, `CustomerSnapshot` em contracts; builders e mappers em módulos do monólito.

### F2 — Value types de tenant (MVP)

`MerchantStoreId`, `LanguageCode`; migração de assinaturas facade P1 com bridges.

### F3 — Redesign de módulos de integração (MVP)

DTO contexts + interfaces V2 de módulo + adapters legacy.

### F4 — Checkout Application Service (MVP)

Extrair orquestração de `OrderFacadeImpl`; facade fina delega.

### F5 — Base outbox processOrder (MVP)

Outbox local + passos em estágios atrás de feature flag.

### F6 — Migração SearchItem (Fase 2)

Mover tipos para contracts; atualizar search-service e Pact.

### F7 — Correção DTO ReferencesApi (MVP)

Conectar `ReadableLanguage` / `ReadableCurrency` em endpoints públicos de referência.

### F8 — Documento plano de migração facade (Fase 2)

Inventário + faseamento para Ondas 4–6.

---

## Restrições UX / API

- **Sem novos caminhos REST** para checkout ou referência na Onda 3.
- **Sem mudanças na UI admin** — apenas paridade comportamental.
- **Sem novos microserviços ou serviços Docker Compose** — diff apenas no monólito.
- DB compartilhado (AD-003) inalterado; tabela outbox na schema `SALESMANAGER`.

---

## Não-objetivos

| Excluído | Motivo |
| -------- | ------ |
| Implantar catalog-service, customer-service, order-service | Ondas 4–6 |
| Saga distribuída completa / message broker | Onda 6+; Onda 3 = apenas base |
| Migrar todas as 76 facades em uma onda | Faseado; apenas subset P1 |
| Reescrever todos os plugins payment/shipping para V2 | Bridge adapter; rewrites de plugin opcionais |
| Split de database por domínio | Onda futura |
| Extração de cálculo de tax | AD-002 — permanece no monólito |
| Quick wins (merge Mapper/Populator) | Paralelo, não bloqueante |
| Feign / service mesh | AD-005 — padrão RestTemplate continua |

---

## Rollout faseado

### MVP (Fase 1) — histórias P1

- DTOs snapshot + tipos tenant + migração facade P1
- Interfaces V2 de integração + adapters
- CheckoutApplicationService + base outbox (flag off por default)
- Correção DTO ReferencesApi

**Critérios de saída:** `./mvnw clean install` verde; Pact atualizado para migração SearchItem; B-001 parcialmente resolvido (facades P1); B-002 fechado.

### Fase 2

- SearchItem em api-contracts + alinhamento `ProductIndexPayload` → `ProductSnapshot`
- Plano de migração facade publicado
- Flag outbox habilitada no profile de teste de integração

### Fase 3

- Regra ArchUnit: sem novos `MerchantStore`/`Language` em novos métodos facade
- STATE.md atualizado; Specify da Onda 4 desbloqueado

---

## Métricas de sucesso

| Métrica | Target |
| ------- | ------ |
| Gate do reactor | `./mvnw clean install` verde |
| Pureza de contracts | Zero `com.salesmanager.core.model` em api-contracts |
| Paridade checkout | Testes de integração de colocação de pedido passam (flag on/off) |
| Migração facade P1 | 6 interfaces facade usam primitivos de tenant |
| Integração V2 | Pelo menos um caminho de plugin testado via adapter |
| B-002 | ReferencesApi retorna apenas DTOs |
| Documentação | TLC spec + 48 tasks + 5 ADRs |

---

## Rastreabilidade

| Fonte | Link |
| ----- | ---- |
| Plano mestre § Onda 3 | `docs/decomposition/MIGRATION-MASTER-PLAN.md` |
| Blockers B-001, B-002 | `.specs/project/STATE.md` |
| AD-009 ProductIndexPayload | `.specs/project/STATE.md` |
| Wave 2 OQ-06 SearchItem | `.specs/features/onda-2-content-search-merchant/design.md` |
| TLC spec | `.specs/features/onda-3-contracts-dto/spec.md` |

---

## Questões abertas (resolvidas no Design)

| ID | Questão | Resolução |
| ---- | ------- | --------- |
| OQ-01 | Substituir ou encapsular ProductIndexPayload? | **Encapsular** — ProductSnapshot canônico; payload mapea com schemaVersion 2 |
| OQ-02 | Migração facade big-bang vs faseada? | **Faseada** — facades adjacentes ao checkout P1 na Onda 3 |
| OQ-03 | Broker outbox agora? | **Não** — outbox same-DB; broker adiado Onda 6 |
| OQ-04 | Quebrar compat binária PaymentModule? | **Não** — interface V2 paralela + adapter |
| OQ-05 | Pacote CheckoutApplicationService? | **`sm-core/.../checkout`** — orquestração de domínio, não camada shop |
