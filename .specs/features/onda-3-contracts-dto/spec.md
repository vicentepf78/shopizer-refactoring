# Onda 3 — Especificação Contracts DTO + Checkout Application Service

**ID da feature:** `onda-3-contracts-dto`  
**Fase:** Specify / Design (Execute bloqueado até tasks aprovadas)  
**Complexidade:** Large (refatoração cross-cutting no monólito, sem novos serviços)  
**Fonte:** [MIGRATION-MASTER-PLAN.md](../../../docs/decomposition/MIGRATION-MASTER-PLAN.md) § Onda 3  
**Pré-requisito:** Execute Onda 2 completo (gate revalidado 2026-07-26)

---

## Declaração do problema

As Ondas 1 e 2 extraíram reference, tax, content, search e merchant em serviços implantáveis com contratos DTO e adapters Strangler BFF. Porém o core do monólito permanece fortemente acoplado:

- **Interfaces facade** passam JPA `MerchantStore` e `Language` (B-001) — 20+ interfaces, `AbstractDataPopulator` hard-wired.
- **ReferencesApi** ainda retorna entidades JPA `Language` / `Currency` (B-002).
- **Plugins de integração** (`PaymentModule`, `ShippingQuoteModule`) aceitam grafos completos de entidade — bloqueia integration-service (Onda 5).
- **Hub checkout** — `OrderFacadeImpl` injeta 12+ serviços; `processOrder` é um método transacional com ciclo order↔payments.
- **Search** usa `ProductIndexPayload` interim (AD-009); `SearchItem` ainda em `shopizer-commons` (OQ-06).

A Onda 3 é a **onda de contratos e refatoração interna** com **sem novos microserviços**. Desbloqueia Ondas 4–6 estabelecendo DTOs snapshot, identificadores tenant, redesign DTO integração, checkout application service e base outbox.

---

## Objetivos

- [ ] `ProductSnapshot`, `OrderSnapshot`, `CustomerSnapshot` em `shopizer-api-contracts`
- [ ] `MerchantStoreId` / `LanguageCode` em interfaces facade P1 (6 facades)
- [ ] `PaymentModuleV2` / `ShippingQuoteModuleV2` com contextos DTO; plugins legacy via bridge
- [ ] `CheckoutApplicationService` extraído de `OrderFacadeImpl`
- [ ] `CHECKOUT_OUTBOX` + `processOrder` em estágios atrás de feature flag
- [ ] `SearchItem` migrado para api-contracts; Pact atualizado
- [ ] B-002 fechado — ReferencesApi retorna DTOs legíveis
- [ ] Plano migração facade para Ondas 4–6 publicado
- [ ] `./mvnw clean install` verde; zero JPA novo em api-contracts

---

## Fora de escopo

| Feature | Motivo |
| ------- | ------ |
| Novos serviços Spring Boot | AD-W3-001 / plano mestre |
| Docker Compose Onda 3 | Sem deployables |
| Extração catalog-service / customer-service | Onda 4 |
| Extração integration-service | Onda 5 |
| order-service / shoppingcart-service | Onda 6 |
| Saga distribuída completa / Kafka | Onda 6+ |
| Migrar todas as 76 facades | Faseado — apenas P1 na Onda 3 |
| Reescrever plugins Stripe/PayPal/USPS para V2 | Opcional; bridge suficiente |
| Split database por domínio | AD-003 |
| Extração cálculo tax | AD-002 |
| Rewrite MerchantStoreArgumentResolver (~450 refs) | Permanece no BFF |
| Quick wins merge Mapper/Populator | Paralelo |

---

## Requisitos

### CTR — Base contracts

| ID | Requisito | Prioridade |
| ---- | --------- | ---------- |
| CTR-01 | `shopizer-api-contracts` SHALL NOT importar `com.salesmanager.core.model` | P1 |
| CTR-02 | Novos DTOs snapshot SHALL ser Jackson-serializáveis com nomes de campo estáveis para Pact | P1 |
| CTR-03 | Mappers/builders SHALL ficar em `sm-core` ou `sm-shop`, não no JAR contracts | P1 |
| CTR-04 | Módulo contracts SHALL publicar packages snapshot: `catalog`, `order`, `customer`, `tenant` | P1 |

### TNT — Identificadores tenant

| ID | Requisito | Prioridade |
| ---- | --------- | ---------- |
| TNT-01 | `MerchantStoreId` SHALL encapsular store code não vazio com igualdade por valor | P1 |
| TNT-02 | `LanguageCode` SHALL encapsular código ISO de language não vazio | P1 |
| TNT-03 | `TenantEntityBridge` SHALL hidratar `MerchantStore`/`Language` apenas no monólito | P1 |
| TNT-04 | `AbstractDataPopulator` SHALL aceitar primitivos tenant via overload | P1 |
| TNT-05 | ArchUnit SHALL falhar se novos métodos facade adicionam params `MerchantStore`/`Language` | P2 |
| TNT-06 | Controllers MAY ainda resolver entidades via argument resolver; conversão na fronteira facade | P1 |

### SNP — Snapshots

| ID | Requisito | Prioridade |
| ---- | --------- | ---------- |
| SNP-01 | `ProductSnapshot` SHALL ser projeção canônica de leitura de catálogo | P1 |
| SNP-02 | `ProductIndexPayload` SHALL mapear de `ProductSnapshot` com schemaVersion 2 | P1 |
| SNP-03 | `ProductSnapshotBuilder` SHALL construir de JPA `Product` sem expor entidades em contracts | P1 |
| SNP-04 | `OrderSnapshot` SHALL incluir status, totais, line items como DTOs aninhados | P1 |
| SNP-05 | `CustomerSnapshot` SHALL incluir id, email, DTOs billing/delivery address | P1 |
| SNP-06 | `search-service` SHALL aceitar payload índice schema v1 e v2 | P1 |
| SNP-07 | Builders snapshot SHALL ter testes unitários com entidades fixture | P1 |

### INT — Módulos integração

| ID | Requisito | Prioridade |
| ---- | --------- | ---------- |
| INT-01 | DTOs integração em `sm-core-modules` SHALL NOT referenciar tipos entidade JPA | P1 |
| INT-02 | `PaymentModuleV2` SHALL usar `PaymentRequestContext` e DTOs relacionados | P1 |
| INT-03 | `ShippingQuoteModuleV2` SHALL usar `ShippingQuoteRequestContext` | P1 |
| INT-04 | Plugins legacy V1 SHALL continuar funcionando sem mudanças de source | P1 |
| INT-05 | `PaymentServiceImpl` SHALL rotear a V2 quando plugin suporta ou via bridge | P1 |
| INT-06 | Pelo menos um caminho plugin SHALL ser testado integração via bridge V2 | P1 |

### CHK — Checkout application service

| ID | Requisito | Prioridade |
| ---- | --------- | ---------- |
| CHK-01 | `CheckoutApplicationService.placeOrder` SHALL ser entrada única de orquestração | P1 |
| CHK-02 | Caminhos REST públicos order e schemas SHALL permanecer inalterados | P1 |
| CHK-03 | `OrderFacadeImpl` SHALL delegar orquestração ao application service | P1 |
| CHK-04 | Colocação de pedido happy-path SHALL bater com comportamento pré-refactor | P1 |
| CHK-05 | Caminhos erro validação/payment conhecidos SHALL bater com pré-refactor | P1 |
| CHK-06 | Latência p95 checkout SHALL NOT exceder 2× baseline em env integração | P2 |

### SAG — Base saga / outbox

| ID | Requisito | Prioridade |
| ---- | --------- | ---------- |
| SAG-01 | Tabela `CHECKOUT_OUTBOX` SHALL existir em schema compartilhado | P1 |
| SAG-02 | Eventos outbox SHALL incluir PAYMENT_REQUESTED, PAYMENT_CONFIRMED, ORDER_PERSISTED, INVENTORY_DECREMENTED | P1 |
| SAG-03 | Escrita outbox SHALL ocorrer na mesma transação que passo de negócio quando flag habilitada | P1 |
| SAG-04 | `checkout.outbox.enabled` SHALL default false | P1 |
| SAG-05 | Dispatcher in-process SHALL marcar eventos processados (sem broker externo) | P1 |

### FAC — Migração facade

| ID | Requisito | Prioridade |
| ---- | --------- | ---------- |
| FAC-01 | `OrderFacade` SHALL usar tipos identificador tenant | P1 |
| FAC-02 | `ShoppingCartFacade`, `SearchFacade`, `ShippingFacade` SHALL usar tipos tenant | P1 |
| FAC-03 | Caminhos leitura `CategoryFacade`, `ProductCommonFacade` SHALL usar tipos tenant | P1 |
| FAC-04 | Adapters HTTP Onda 2 SHALL compilar com assinaturas facade atualizadas | P1 |
| FAC-05 | Facades restantes SHALL ser inventariadas com atribuição de fase Onda 4–6 | P2 |
| FAC-06 | `FACADE-MIGRATION-PLAN.md` SHALL documentar migração faseada | P2 |

### SRCH — Contratos search

| ID | Requisito | Prioridade |
| ---- | --------- | ---------- |
| SRCH-01 | `SearchItem` SHALL ficar em `shopizer-api-contracts` | P2 |
| SRCH-02 | `search-service` e `sm-shop` SHALL importar SearchItem de contracts | P2 |
| SRCH-03 | Testes Pact SHALL usar SearchItem de contracts | P2 |
| SRCH-04 | Nomes de campo JSON SHALL permanecer compatíveis com Pact Onda 2 | P2 |

### REF — API References

| ID | Requisito | Prioridade |
| ---- | --------- | ---------- |
| REF-01 | Endpoints lista language SHALL retornar DTOs `ReadableLanguage` | P1 |
| REF-02 | Endpoints lista currency SHALL retornar DTOs `ReadableCurrency` | P1 |

### GAT — Gates

| ID | Requisito | Prioridade |
| ---- | --------- | ---------- |
| GAT-01 | `./mvnw clean install` SHALL passar na conclusão | P1 |
| GAT-02 | Suites Pact Onda 1+2 SHALL permanecer verdes | P1 |
| GAT-03 | `.specs/project/STATE.md` SHALL ser atualizado na conclusão Onda 3 | P2 |

**Contagem de requisitos: 49 IDs** (CTR-04, TNT-06, SNP-07, INT-06, CHK-06, FAC-06, GAT-03 incluídos)

---

## Histórias de usuário (resumo)

### P1 — Plataforma: contratos snapshot (SNP, CTR)

Como engenheiro de plataforma, preciso de DTOs snapshot versionados para que leituras cross-service não exijam `sm-core-model` no classpath do consumidor.

### P1 — Plataforma: tipos tenant (TNT, FAC)

Como engenheiro de plataforma, preciso que interfaces facade aceitem códigos store/lang para que adapters HTTP futuros não vazem tipos JPA (resolução parcial B-001).

### P1 — Plataforma: DTOs integração (INT)

Como engenheiro de plataforma, preciso que plugins payment/shipping aceitem DTOs para que extração integration-service Onda 5 seja viável.

### P1 — Visitante: paridade checkout (CHK)

Como visitante da vitrine, checkout deve se comportar exatamente como hoje enquanto orquestração move para application service.

### P1 — Plataforma: base outbox (SAG)

Como engenheiro de plataforma, preciso de processOrder em estágios com linhas outbox para que Onda 6 separe order e payments.

### P1 — Consumidor API: DTOs referência (REF)

Como consumidor de API, endpoints language/currency devem retornar DTOs (B-002).

### P2 — Mantenedor search (SRCH)

Como mantenedor search-service, SearchItem deve estar em api-contracts (OQ-06).

---

## Faseamento

### Fase 1 (MVP)

CTR, TNT, SNP, INT, CHK, SAG (flag off), REF, FAC P1

### Fase 2

Migração SRCH, plano FAC-06, outbox habilitado em profile de teste

### Fase 3

Enforcement ArchUnit, atualização STATE, Specify Onda 4 desbloqueado

---

## Rastreabilidade

| Seção PRD | IDs de requisito |
| --------- | ---------------- |
| Contratos snapshot | SNP-01..07, CTR-01..04 |
| Identificadores tenant | TNT-01..06, FAC-01..04 |
| DTOs integração | INT-01..06 |
| Serviço checkout | CHK-01..06 |
| Outbox | SAG-01..05 |
| SearchItem | SRCH-01..04 |
| Referências | REF-01..02 |
| Gates | GAT-01..03 |

Workflow Compozy: `.compozy/tasks/onda-3-contracts-dto/`
