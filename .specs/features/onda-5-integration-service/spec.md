# Onda 5 — Especificação Integration Service

**ID da feature:** `onda-5-integration-service`
**Fase:** Specify → Design → Tasks (Execute bloqueado)
**Complexidade:** Grande (1 serviço implantável + Strangler + registry de plugins)
**Fonte:** [MIGRATION-MASTER-PLAN.md](../../../docs/decomposition/MIGRATION-MASTER-PLAN.md) § Onda 5
**Ranking de acoplamento:** 9º de 10 domínios de extração (menor que o assumido originalmente; ainda bloqueia split payments da Onda 6)

---

## Declaração do problema

A orquestração de pagamento e cotação de frete hoje vive dentro de `sm-core` com **acoplamento MODEL disfarçado de plugins**: `PaymentModule` e `ShippingQuoteModule` em `sm-core-modules` aceitam entidades JPA (`Order`, `Customer`, `ShoppingCartItem`, `MerchantStore`). `PaymentServiceImpl` muta `Order` via `OrderService` após chamadas ao gateway, criando o **ciclo order ↔ payments** que bloqueia extração do serviço Order (Onda 6).

A orquestração de frete depende de preço/peso de catálogo (`PricingService`, `Product`), dados de referência (países) e configuração de sistema — mas não é dona do estado do pedido. A orquestração de pagamento depende de configuração de módulos de sistema e plugins de gateway sob `modules/integration/`.

Sem fronteira formal, a Onda 6 não consegue separar payments de order sem duplicar lógica de gateway ou quebrar checkout. A Onda 3 deve entregar contratos de módulo baseados em DTO e fundação de saga de checkout primeiro; a Onda 4 parcial deve expor caminhos de leitura de catálogo para snapshots de produto de frete.

Esta spec define **o que** `integration-service` é dono, **o que** permanece na camada BFF/checkout do monólito e **como** permanecer stateless em relação a entidades `Order`.

---

## Objetivos

- [ ] `integration-service` implantável como aplicação Spring Boot independente (:8086)
- [ ] Monólito consome integration via HTTP Strangler nas superfícies existentes de configuração de pagamento/frete e cotação
- [ ] Zero tipos de entidade JPA em respostas JSON REST para endpoints migrados
- [ ] **Orquestração** de pagamento e frete (registry de módulos, CRUD de config, chamadas gateway) de propriedade de integration-service
- [ ] **Stateless** em relação a `Order` — serviço retorna DTOs de transação/cotação; persistência de pedido permanece no monólito/saga checkout
- [ ] Contratos DTO `PaymentModuleV2` / `ShippingQuoteModuleV2` da Onda 3 usados nas fronteiras de runtime
- [ ] Quebrar caminho `PaymentServiceImpl` → `OrderService.saveOrUpdate` para novo fluxo strangler
- [ ] Testes de contrato (Pact) cobrindo endpoints P1 de config pagamento, cotações de frete e listagem de módulos
- [ ] Reutilizar `shopizer-api-contracts` para DTOs e clients HTTP compartilhados de integration

---

## Não-objetivos

| Funcionalidade | Motivo |
| ------- | ------ |
| Ownership de entidade Order / extração `OrderService` | Onda 6 |
| Cálculo de shopping cart | Onda 6 |
| Cálculo de imposto no checkout | Permanece monólito até split de order |
| CRUD completo de catálogo | Onda 4 — apenas snapshots de leitura para frete |
| Caminhos write `ProductType` / catálogo | Onda 4+ |
| Split de banco por serviço | AD-003 herdado — schema compartilhado durante extração runtime |
| Novos gateways de pagamento além dos plugins existentes | Sem novos provedores nesta onda |
| Stubs marketplace signup / payment | Incompletos; adiar |
| Feign/WebClient/service discovery | Padrão AD-005 — apenas RestTemplate |
| Substituir transação global AOP de checkout | Fundação saga/outbox da Onda 3; saga completa de order na Onda 6 |
| Upgrades SDK Stripe/Braintree | Manutenção; fora do escopo de decomposição |

---

## User Stories

### P1: Integration Service — configuração de módulo de pagamento ⭐ MVP

**História de usuário**: Como administrador de loja, quero configurar módulos de pagamento (Stripe, PayPal, money order, etc.) pelas APIs admin existentes, para que setup de gateway não dependa do runtime do monólito.

**Por que P1**: Configuração é a superfície de menor risco — sem mutação de pedido, valida padrão Strangler + JWT + DB compartilhado para domínio de integration.

**Critérios de aceite**:

1. WHEN `GET /api/v1/private/modules/payment` THEN `integration-service` SHALL retornar módulos de pagamento configurados como DTOs — SHALL NOT expor tipos JPA `IntegrationModule`
2. WHEN `POST /api/v1/private/modules/payment/{code}` com configuração de módulo THEN service SHALL validar, criptografar segredos e persistir via equivalente `MerchantConfigurationService`
3. WHEN `DELETE /api/v1/private/modules/payment/{code}` THEN service SHALL remover configuração merchant daquele módulo
4. WHEN `GET /api/v1/payment/config` (config pública da loja) THEN service SHALL retornar métodos de pagamento aceitos para a loja
5. WHEN rotas admin exigem auth THEN validação JWT SHALL seguir padrão Ondas 1–2 para `/private/**`
6. WHEN tenant é identificado THEN service SHALL resolver loja por code — sem exigir entidade `MerchantStore` no body da request

**Teste independente**: Implantar `integration-service` + `reference-service`; configurar módulo Stripe; listar métodos de pagamento; verificar config criptografada em `MERCHANT_CONFIGURATION`.

**Componentes-fonte:**

| Papel | Caminho |
|------|------|
| Service | `sm-core/.../services/payments/PaymentServiceImpl.java` |
| Module API | `sm-shop/.../api/v1/payment/PaymentApi.java` |
| Facade | `sm-shop/.../facade/payment/PaymentConfigurationFacadeImpl.java` |
| Contract | `sm-core-modules/.../payment/model/PaymentModule.java` |
| Plugins | `sm-core/.../modules/integration/payment/impl/*.java` |

**IDs de requisito:** PAY-01…PAY-06

---

### P1: Integration Service — configuração de frete e cotações ⭐ MVP

**História de usuário**: Como cliente da vitrine, quero cotações de frete para meu carrinho pelas APIs existentes, para que checkout mostre opções de transportadora sem o monólito executar plugins de frete in-process.

**Por que P1**: Frete não tem ciclo order↔payments; caminho de cotação é read-heavy e valida integração de snapshot de catálogo da Onda 4.

**Critérios de aceite**:

1. WHEN `POST /api/v1/auth/cart/{code}/shipping` com endereço de entrega THEN BFF SHALL montar DTO `ShippingQuoteRequest` e `integration-service` SHALL retornar `ReadableShippingQuote` com opções
2. WHEN módulos de frete estão configurados THEN service SHALL invocar plugins `ShippingQuoteModuleV2` com inputs DTO (entrega, pacotes, snapshots de produto)
3. WHEN `GET /api/v1/shipping/countries` THEN service SHALL retornar lista de países de entrega usando HTTP reference-service para localização
4. WHEN admin configura módulos/origem/empacotamento de frete THEN APIs privadas de configuração SHALL persistir settings equivalentes ao comportamento do monólito
5. WHEN peso/dimensões de produto são necessários THEN service SHALL buscar `ProductSnapshot` (ou subset de frete) da API de leitura de catálogo — SHALL NOT injetar `PricingService` in-process do monólito
6. WHEN frete não é necessário para itens do carrinho THEN service SHALL retornar cotação vazia com `requiresShipping=false`

**Teste independente**: Carrinho com produto físico; POST cotação de frete; receber opções UPS/custom; verificar ausência de entidade `Order` nos logs de integration-service.

**Componentes-fonte:**

| Papel | Caminho |
|------|------|
| Service | `sm-core/.../services/shipping/ShippingServiceImpl.java` |
| APIs | `sm-shop/.../api/v1/order/OrderShippingApi.java`, `ShippingConfigurationApi.java` |
| Facade | `sm-shop/.../facade/shipping/ShippingFacadeImpl.java` |
| Contract | `sm-core-modules/.../shipping/model/ShippingQuoteModule.java` |
| Plugins | `sm-core/.../modules/integration/shipping/impl/*.java` |

**IDs de requisito:** SHP-01…SHP-07

---

### P1: Integration Service — processamento de pagamento stateless ⭐ MVP

**História de usuário**: Como fluxo de checkout, quero autorização/captura/reembolso executados via integration-service retornando resultado de transação, para que atualizações de status do pedido permaneçam no application service de checkout e o ciclo order↔payments seja quebrado.

**Por que P1**: Valor central da Onda 5 — habilita split order/payments da Onda 6.

**Critérios de aceite**:

1. WHEN `POST /internal/v1/payments/process` com `PaymentProcessRequest` (snapshot customer, DTO payment, snapshots de linha de carrinho, id snapshot order, amount) THEN service SHALL invocar `PaymentModuleV2` apropriado e retornar `TransactionResult` — SHALL NOT chamar `OrderService.saveOrUpdate`
2. WHEN `POST /internal/v1/payments/capture` com referência de transação capturável THEN service SHALL capturar e retornar `TransactionResult`
3. WHEN `POST /internal/v1/payments/refund` com flag parcial e amount THEN service SHALL reembolsar via gateway e retornar `TransactionResult`
4. WHEN `POST /internal/v1/payments/init` para express checkout THEN service SHALL retornar token/transação de inicialização sem persistência de pedido
5. WHEN gateway falha THEN service SHALL retornar erro estruturado — saga checkout no monólito trata rollback de status do pedido
6. WHEN transação é bem-sucedida THEN service MAY persistir registro `Transaction` no DB compartilhado — SHALL NOT mutar linhas `Order`

**Teste independente**: Gateway mock; processar pagamento via API interna; verificar `Transaction` salva; verificar ausência de update `Order` em teste de integração de integration-service.

**IDs de requisito:** PAY-07…PAY-12

---

### P1: Strangler BFF — delegação de pagamento e frete ⭐ MVP

**História de usuário**: Como engenheiro de plataforma, quero adaptadores HTTP para facades de pagamento/frete atrás de `wave5.strangler.enabled`, para cortar over sem mudar caminhos REST da vitrine/admin.

**Critérios de aceite**:

1. WHEN `wave5.strangler.enabled=true` THEN `PaymentConfigurationFacade` e facades de frete SHALL delegar a `integration-service`
2. WHEN falha remota THEN SHALL retornar HTTP 503 com `correlationId` — sem fallback in-process silencioso
3. WHEN `OrderPaymentApi` processa pagamento THEN SHALL usar application service de checkout → client HTTP integration (não `PaymentService` in-process)
4. WHEN `OrderShippingApi` solicita cotação THEN SHALL montar request DTO a partir de facade de carrinho + snapshots de catálogo
5. WHEN header de correlation presente THEN SHALL propagar `X-Correlation-Id` para integration-service

**IDs de requisito:** STR-01…STR-06

---

### P2: Testes de contrato e observabilidade

**História de usuário**: Como desenvolvedor/operador, quero cobertura Pact e health indicators para dependências de integration-service.

**Critérios de aceite**:

1. Testes provider Pact para endpoints P1 de config pagamento e cotação frete
2. Consumer pact em `sm-shop` (`Wave5ConsumerPactTest`)
3. Actuator health reporta DB, registry de módulos, reference-service, catalog-service alcançáveis
4. Gate JaCoCo em `integration-service` e `sm-integration-core` conforme convenção do repositório

**IDs de requisito:** STR-07…STR-09

---

### P3: Topologia Docker local

**História de usuário**: Como desenvolvedor, quero `docker-compose-wave5.yml` estendendo topologia Ondas 1–4 com integration-service.

**IDs de requisito:** STR-10

---

## Rastreabilidade de requisitos

| ID | Prioridade | Resumo | Dependência Onda 3 |
|----|----------|---------|-------------------|
| PAY-01 | P1 | Listar módulos de pagamento | `IntegrationModuleDto` |
| PAY-02 | P1 | Salvar config módulo pagamento | `PersistableIntegrationConfig` |
| PAY-03 | P1 | Excluir config módulo pagamento | — |
| PAY-04 | P1 | Métodos de pagamento aceitos públicos | `PaymentMethodDto` |
| PAY-05 | P1 | JWT em rotas privadas | — |
| PAY-06 | P1 | Resolução tenant por store code | `MerchantStoreId` |
| PAY-07 | P1 | Processar pagamento stateless | `PaymentProcessRequest`, `OrderSnapshot` |
| PAY-08 | P1 | Capturar pagamento | `TransactionResult` |
| PAY-09 | P1 | Reembolsar pagamento | — |
| PAY-10 | P1 | Init transação (express) | — |
| PAY-11 | P1 | Mapeamento erro gateway | — |
| PAY-12 | P1 | Persistência apenas Transaction | — |
| SHP-01 | P1 | Cotação frete carrinho | `ShippingQuoteRequest` |
| SHP-02 | P1 | Invocação plugin com DTOs | `ShippingQuoteModuleV2` |
| SHP-03 | P1 | Países de entrega | HTTP reference |
| SHP-04 | P1 | Config admin frete | — |
| SHP-05 | P1 | Snapshots produto catálogo | `ProductSnapshot` (Onda 4) |
| SHP-06 | P1 | Cotação vazia quando não necessário | — |
| SHP-07 | P1 | DTO resumo frete | `ShippingSummaryDto` |
| STR-01 | P1 | Facades Strangler | properties `wave5.*` |
| STR-02 | P1 | 503 em falha remota | — |
| STR-03 | P1 | OrderPaymentApi → checkout + HTTP | Checkout app service |
| STR-04 | P1 | Montagem DTO OrderShippingApi | — |
| STR-05 | P1 | Propagação correlation | — |
| STR-06 | P1 | Caminhos REST congelados | — |
| STR-07 | P2 | Pact provider | — |
| STR-08 | P2 | Health indicators | — |
| STR-09 | P2 | Gates JaCoCo | — |
| STR-10 | P3 | Docker Compose wave5 | — |

---

## Critérios de sucesso

- [ ] Health `integration-service` UP com registry de módulos carregado
- [ ] Paridade CRUD config pagamento com monólito para ≥ 2 módulos (ex.: moneyorder + stripe)
- [ ] Cotação de frete retorna opções para loja configurada com dados de snapshot de catálogo
- [ ] Processamento de pagamento via API interna não atualiza tabela `Order`
- [ ] Pact verde para superfícies P1
- [ ] Profile `wave5.strangler.enabled` documentado no Compose
- [ ] Padrão documentado em `STATE.md` para reuso Onda 6

---

## Questões em aberto

Todas OQ-01…OQ-06 resolvidas em `context.md`. Não restam ambiguidades de produto bloqueadoras.

**Residual (não bloqueador):**

- Campos exatos de snapshot de catálogo para empacotamento — confirmar com entregável parcial Onda 4
- Se `TransactionService` move inteiramente para integration-service ou módulo thin compartilhado — ver ADR-003
