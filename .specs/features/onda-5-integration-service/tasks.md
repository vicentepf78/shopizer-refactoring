# Onda 5 — Integration Service Tasks

**Design:** `.specs/features/onda-5-integration-service/design.md`
**Spec:** `.specs/features/onda-5-integration-service/spec.md`
**Status:** Aprovado — Execute bloqueado em Onda 3 + Onda 4 parcial
**Testes:** `.specs/codebase/TESTING.md`
**Pré-requisito:** Execute Onda 3 (`onda-3-contracts-checkout`); leitura parcial catálogo Onda 4 (campos de frete `ProductSnapshot`)

---

## Execution Plan

### Fase 0: Gates (externos)

```
Onda3-T_complete ──→ Onda4-T_catalog_read_partial ──→ T1
```

### Fase 1: Contratos + Config Wave5

```
T1 ──→ T2 ──┬──→ T3 [P]
            └──→ T4 [P]
T2,T3,T4 ──→ T5
```

### Fase 2: Extração Core (2 trilhas paralelas)

```
T5 ──┬──→ T6 ──→ T7 ──→ T8 ──→ T9 ──→ T10 ──→ T11
     │
     └──→ T12 ──→ T13 ──→ T14 ──→ T15 ──→ T16
```

**Trilha A (Pagamento):** T6–T11
**Trilha B (Frete):** T12–T16

### Fase 3: Boot integration-service + APIs

```
T11,T16 ──→ T17 ──→ T18 ──→ T19 ──→ T20 ──→ T21 ──→ T22
```

### Fase 4: Fronteira stateless + Strangler

```
T22 ──→ T23 ──→ T24 ──→ T25 ──→ T26 ──→ T27 ──→ T28
```

### Fase 5: Integração & Gate

```
T28 ──→ T29 ──┬──→ T30 [P]
              └──→ T31 [P]
T30,T31 ──→ T32 ──→ T33 ──→ T34 ──→ T35 ──→ T36 ──→ T37 ──→ T38
```

**Marcos:**
- **P-ready:** T11 — orquestrador de pagamento + plugins compilam em `sm-integration-core`
- **S-ready:** T16 — orquestrador de frete + client catálogo
- **I-ready:** T22 — integration-service responde health + config admin

---

## Task Breakdown

### T1: Verificar artefatos de contrato integration da Onda 3

**What:** Assertir que `PaymentModuleV2`, `ShippingQuoteModuleV2`, `OrderSnapshot`, `CustomerSnapshot`, `CartLineSnapshot`, esqueleto do application service de checkout existem e compilam.
**Onde:** `sm-core-modules`, `shopizer-api-contracts`, `sm-shop/.../checkout/`
**Gate:** `./mvnw compile -pl sm-core-modules,shopizer-api-contracts,sm-shop -am`
**Depende de:** Onda 3 completa

### T2: DTOs integration em shopizer-api-contracts

**What:** Adicionar pacote `com.salesmanager.contracts.integration`: `PaymentProcessRequest`, `PaymentCaptureRequest`, `PaymentRefundRequest`, `PaymentInitRequest`, `TransactionResult`, `ShippingQuoteRequest`, `ShippingQuoteResponse`, `ShippingProductSnapshot`, `ShippingSummaryRequest`, `IntegrationModuleDto`, `PaymentMethodDto`, `ShippingOptionDto`, `DeliveryDto`.
**Gate:** Testes unitários serialização JSON; sem imports `com.salesmanager.core.model`.

### T3: Interface IntegrationServiceClient

**What:** Definir `IntegrationServiceClient` em `com.salesmanager.contracts.client` com métodos process/capture/refund/init/quote/summary.
**Depende de:** T2
**Gate:** Compila isolado.

### T4: Properties Strangler Wave5 e RestTemplate

**What:** Profile `strangler-wave5`, `wave5.integration-service.base-url`, `wave5.integration-service.internal-token`, `wave5.catalog-service.base-url`, `Wave5ClientConfig`, interceptor de correlation.
**Depende de:** T2
**Gate:** `Wave5ClientConfigTest` em sm-shop.

### T5: Registrar módulo Maven sm-integration-core

**What:** Criar pom `sm-integration-core`; depender de `sm-core-modules`, `sm-core-model`, `shopizer-api-contracts`; registrar no reactor.
**Depende de:** T1, T3, T4
**Gate:** `./mvnw compile -pl sm-integration-core -am`

### T6: Mover implementações plugins pagamento para sm-integration-core

**What:** Relocar `sm-core/.../modules/integration/payment/impl/*` para `sm-integration-core`; atualizar wiring bean `ModulesConfiguration`.
**Depende de:** T5
**Gate:** Testes unitários existentes de módulo pagamento passam no novo módulo.

### T7: Ponte adaptadora PaymentModuleV2

**What:** Criar `PaymentModuleV2Adapter` envolvendo entidades `PaymentModule` legadas usando mappers Onda 3 — ponytail: temporário até plugins implementarem V2 nativamente.
**Depende de:** T6
**Gate:** Teste unitário adapter com módulo legado mock.

### T8: Extrair PaymentOrchestrator de PaymentServiceImpl

**What:** Mover CRUD config, `getPaymentMethods`, `validateCreditCard`, resolução de módulo para `PaymentOrchestratorImpl` — excluir métodos de mutação de pedido inicialmente.
**Depende de:** T7
**Gate:** Testes unitários save/load config com criptografia.

### T9: Caminhos pagamento sem catálogo no orquestrador

**What:** Garantir que orquestrador de pagamento não tem dependência `OrderService`; injetar apenas `TransactionService`.
**Depende de:** T8
**Gate:** ArchUnit ou gate grep — sem `OrderService` em `sm-integration-core`.

### T10: Métodos de operação interna de pagamento

**What:** Implementar `process`, `capture`, `refund`, `init` no orquestrador usando `PaymentModuleV2` e retornando `TransactionResult`.
**Depende de:** T9
**Gate:** Teste integração com `MoneyOrderPayment` ou módulo mock.

### T11: Marcador P-ready — testes core pagamento

**What:** Baseline JaCoCo pacote orquestrador pagamento ≥ 70%.
**Depende de:** T10
**Gate:** `./mvnw test -pl sm-integration-core -Dtest=PaymentOrchestrator*`

### T12: Mover implementações plugins frete para sm-integration-core

**What:** Relocar shipping `impl/*`, `DefaultPackagingImpl`, preprocessors.
**Depende de:** T5
**Gate:** Testes módulo frete passam.

### T13: Ponte adaptadora ShippingQuoteModuleV2

**What:** Adaptador de `ShippingQuoteModule` legado para contratos DTO V2.
**Depende de:** T12
**Gate:** Teste unitário com `StorePickupShippingQuote`.

### T14: CatalogServiceClient para snapshots de frete

**What:** Client HTTP buscando lista `ShippingProductSnapshot` da API de leitura catalog-service (Onda 4); fallback GAP-INT-01 documentado.
**Depende de:** T13, Onda 4 parcial
**Gate:** Teste client com fixture catalog WireMock.

### T15: Extrair ShippingOrchestrator de ShippingServiceImpl

**What:** Mover montagem cotação, empacotamento, iteração módulos, `requiresShipping`, metadata para `ShippingOrchestratorImpl`; usar `ReferenceServiceClient` para países.
**Depende de:** T14
**Gate:** Teste unitário cotação com dois módulos.

### T16: Marcador S-ready — testes core frete

**What:** Baseline JaCoCo orquestrador frete ≥ 70%.
**Depende de:** T15
**Gate:** `./mvnw test -pl sm-integration-core -Dtest=ShippingOrchestrator*`

### T17: Scaffold Spring Boot integration-service

**What:** Novo módulo `integration-service`, porta 8086, scan `sm-integration-core`, segurança JWT, actuator.
**Depende de:** T11, T16
**Gate:** Context carrega `./mvnw -pl integration-service -am test -Dtest=IntegrationServiceApplicationTest`

### T18: Controllers REST admin pagamento

**What:** Espelhar endpoints config módulo privado `PaymentApi` em integration-service.
**Depende de:** T17
**Gate:** Testes MockMvc PAY-01..PAY-06.

### T19: Controllers REST admin frete

**What:** Espelhar endpoints `ShippingConfigurationApi`.
**Depende de:** T17
**Gate:** Testes MockMvc SHP-04.

### T20: Endpoint público métodos de pagamento

**What:** `GET` métodos de pagamento aceitos para loja.
**Depende de:** T18
**Gate:** Schema resposta pronto para Pact.

### T21: Controller REST interno pagamento

**What:** `/internal/v1/payments/*` com filtro `X-Internal-Token`.
**Depende de:** T18, T10
**Gate:** Teste integração process payment; assertar sem bean repositório Order.

### T22: Controller REST interno frete

**What:** `/internal/v1/shipping/quote` e `/summary` com auth token.
**Depende de:** T19, T15
**Gate:** I-ready — teste integração cotação.

### T23: Trim serviços payment/shipping sm-core

**What:** `PaymentServiceImpl`/`ShippingServiceImpl` delegam ao orquestrador quando extraído, ou stubs `@Deprecated` apontando guia de migração.
**Depende de:** T22
**Gate:** Monólito compila; testes existentes verdes em profile non-strangler.

### T24: Fronteira pagamento stateless no monólito

**What:** Remover `orderService.saveOrUpdate` do caminho pagamento quando `wave5.strangler.enabled`; saga checkout executa update de pedido.
**Depende de:** T23, saga checkout Onda 3
**Gate:** Teste: pagamento via checkout não chama save in-process de pedido a partir de PaymentServiceImpl.

### T25: PaymentFacadeHttpAdapter

**What:** Adaptador Strangler para `PaymentConfigurationFacadeImpl`.
**Depende de:** T24
**Gate:** Teste adapter 503 em falha de conexão.

### T26: ShippingFacadeHttpAdapter

**What:** Adaptador Strangler para `ShippingFacadeImpl` e facade de configuração.
**Depende de:** T24
**Gate:** Teste adapter propaga correlation id.

### T27: Wiring client integration CheckoutApplicationService

**What:** `OrderPaymentApi` roteia via checkout service → `IntegrationServiceClient`; monta `PaymentProcessRequest` a partir de snapshots cart/order.
**Depende de:** T25, T26
**Gate:** Teste integração checkout payment E2E (gateway mock).

### T28: Montagem DTO OrderShippingApi

**What:** Montar `ShippingQuoteRequest` a partir de facade carrinho + snapshots catálogo; chamar client integration.
**Depende de:** T27
**Gate:** Teste E2E cotação frete.

### T29: Correlation ID e health indicators

**What:** `CorrelationIdFilter`; health para DB, reference-service, catalog-service, registry módulos.
**Depende de:** T28
**Gate:** Teste actuator health.

### T30: Testes Pact provider — integration-service

**What:** Pacts provider para config pagamento + cotação frete P1.
**Depende de:** T29
**Gate:** `./mvnw test -pl integration-service -Dtest=IntegrationProviderPactTest`

### T31: Wave5ConsumerPactTest em sm-shop

**What:** Contratos consumer para client strangler.
**Depende de:** T29
**Gate:** Pact publish/verify verde.

### T32: IntegrationServiceClientRestTemplateImpl

**What:** Implementação HTTP completa para BFF.
**Depende de:** T30, T31
**Gate:** Teste integração client contra TestRestTemplate.

### T33: Rewire ModulesConfiguration no monólito

**What:** Remover beans plugin duplicados de sm-core quando strangler habilitado; documentar rollback.
**Depende de:** T32
**Gate:** Profile `strangler-wave5` inicia sem duplicação de beans.

### T34: docker-compose-wave5.yml

**What:** Adicionar integration-service; env `WAVE5_INTEGRATION_BASE_URL`, `INTEGRATION_INTERNAL_TOKEN`; depende de catalog parcial.
**Depende de:** T33
**Gate:** `docker compose -f docker-compose-wave5.yml config`

### T35: Dockerfile.wave5 para integration-service

**What:** Temurin 11 JRE; copiar JAR pré-compilado.
**Depende de:** T34
**Gate:** Imagem builda com JAR empacotado.

### T36: Suite teste integração cross-service

**What:** Script ou job CI: reference + catalog (parcial) + integration + shop profile strangler.
**Depende de:** T35
**Gate:** Health 8086 UP; roundtrip amostra cotação + config.

### T37: Gates JaCoCo verify

**What:** Adicionar limites jacoco em `integration-service` e `sm-integration-core` no pom pai.
**Depende de:** T36
**Gate:** `./mvnw -pl integration-service,sm-integration-core verify`

### T38: Atualizar STATE.md e ROADMAP

**What:** Documentar AD-015..020, padrão wave5, GAP-INT-01..05, evidência de gate.
**Depende de:** T37
**Gate:** STATE.md lista onda-5-integration-service READY FOR EXECUTE ou COMPLETE conforme status real.

---

## Mapa de execução paralela

```
Fase 1: T1 → T2 → (T3 ∥ T4) → T5

Fase 2 (após T5):
  Pagamento:  T6 → T7 → T8 → T9 → T10 → T11
  Frete: T12 → T13 → T14 → T15 → T16

Fase 3: T17 → T18 → T19 → T20 → T21 → T22

Fase 4: T23 → T24 → T25 → T26 → T27 → T28

Fase 5: T29 → (T30 ∥ T31) → T32 → T33 → T34 → T35 → T36 → T37 → T38
```

**Regra subagent:** `[P]` → subagent paralelo na mesma fase. Fase 2 → **2 trilhas paralelas** (pagamento vs frete).

---

## Mapeamento Compozy

| TLC | Task Compozy |
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
