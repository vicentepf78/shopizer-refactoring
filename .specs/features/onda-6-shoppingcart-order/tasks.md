# Onda 6 — Tasks ShoppingCart + Order

**Design:** `.specs/features/onda-6-shoppingcart-order/design.md`
**Spec:** `.specs/features/onda-6-shoppingcart-order/spec.md`
**Status:** Aprovado — Execute bloqueado até gate Ondas 3–5
**Testes:** `.specs/codebase/TESTING.md`
**Pré-requisito:** Execute Ondas 3, 4, 5 completo (snapshots, PoC saga/outbox, catalog, customer, integration-service)

---

## Plano de execução

### Fase 0: Gate

```
Onda3-T48 ∧ Onda4-T42 ∧ Onda5-T36 ──→ T1
```

### Fase 1: Contratos + API Totals (fundação quebra de ciclo)

```
T1 ──→ T2 ──┬──→ T3 [P]
            └──→ T4 [P]
T2,T3,T4 ──→ T5 ──→ T6
```

### Fase 2: Trilha ShoppingCart

```
T6 ──→ T7 ──→ T8 ──→ T9 ──→ T10 ──→ T11 ──→ T12 ──→ T13 ──→ T14
```

### Fase 3: Order core + caminho read

```
T6 ──→ T15 ──→ T16 ──→ T17 ──→ T18 ──→ T19 ──→ T20 ──→ T21
```

### Fase 4: Saga + outbox

```
T17 ──→ T22 ──→ T23 ──→ T24 ──→ T25 ──→ T26 ──→ T27
```

### Fase 5: Checkout Application Service

```
T25,T14 ──→ T28 ──→ T29 ──→ T30 ──→ T31 ──→ T32
```

### Fase 6: Decomposição hub + Strangler

```
T21,T32 ──→ T33 ──→ T34 ──→ T35 ──→ T36 ──→ T37 ──→ T38
```

### Fase 7: Gate de integração

```
T38 ──→ T39 ──┬──→ T40 [P]
              ├──→ T41 [P]
              └──→ T42 [P]
T39,T40,T41,T42 ──→ T43 ──→ T44 ──→ T45
```

**Marcos:** `TOT-ready` = T6 | `SC-ready` = T14 | `OR-read-ready` = T21 | `CHK-ready` = T32

---

## Detalhamento de tasks

### T1: Verificação gate — artefatos Ondas 3–5

**O quê:** Verificar `OrderSnapshot`, PoC saga/outbox, esqueleto `CheckoutApplicationService`, serviços catalog/customer/integration existem e testes passam.
**Onde:** Script CI `scripts/wave6-gate.sh` (novo)
**Depende de:** Onda 3 T48, Onda 4 T42, Onda 5 T36
**Requisito:** CHK-01 (pré-requisito)

**Pronto quando:**
- [ ] Script de gate sai com 0
- [ ] Documentado em `.specs/project/STATE.md`

**Testes:** nenhum
**Gate:** `./scripts/wave6-gate.sh`

**Commit:** `chore(wave6): add ondas 3-5 gate script`

---

### T2: DTOs cart/order em `shopizer-api-contracts`

**O quê:** Adicionar pacotes `CartLineSnapshot`, `ReadableShoppingCart`, `PersistableCartLine`, `CartTotalsRequest`, `CartTotalsResponse`, `OrderSnapshot`, `ReadableOrder`.
**Onde:** `shopizer-api-contracts/.../cart/`, `.../order/`, `.../checkout/`
**Depende de:** T1
**Reutiliza:** tipos snapshot Onda 3 onde presentes
**Requisito:** CART-07, ORD-01, HUB-04

**Pronto quando:**
- [ ] Zero imports `com.salesmanager.core.model` nos novos DTOs
- [ ] `./mvnw compile -pl shopizer-api-contracts`

**Testes:** unitários (serialização)
**Gate:** `./mvnw test -pl shopizer-api-contracts -Dtest=Wave6ContractsSerializationTest`

**Commit:** `feat(contracts): add wave6 cart order checkout DTOs`

---

### T3: Interfaces client — cart, order, totals [P]

**O quê:** `ShoppingCartServiceClient`, `OrderServiceClient`, `CartTotalsClient`, `CheckoutCommitClient`.
**Onde:** `shopizer-api-contracts/.../client/`
**Depende de:** T1
**Requisito:** CART-03, ORD-06

**Pronto quando:**
- [ ] Interfaces compilam com DTOs da T2
- [ ] `./mvnw compile -pl shopizer-api-contracts`

**Testes:** nenhum
**Gate:** `./mvnw compile -pl shopizer-api-contracts`

**Commit:** `feat(contracts): add wave6 HTTP client interfaces`

---

### T4: DTOs saga checkout [P]

**O quê:** `CheckoutCommitRequest`, `CheckoutCommitResponse`, `SagaStepStatus`, `PaymentStatusUpdate`.
**Onde:** `shopizer-api-contracts/.../checkout/`
**Depende de:** T1
**Requisito:** CHK-02, CHK-09

**Pronto quando:**
- [ ] Campo chave idempotência documentado em Javadoc
- [ ] `./mvnw compile -pl shopizer-api-contracts`

**Testes:** unit
**Gate:** `./mvnw test -pl shopizer-api-contracts -Dtest=CheckoutCommitDtoTest`

**Commit:** `feat(contracts): add checkout saga DTOs`

---

### T5: Properties Strangler Wave6 + RestTemplate

**O quê:** Profile `strangler-wave6`; `wave6.*.base-url`, três feature flags; `Wave6ClientConfig` + interceptor de correlação.
**Onde:** `sm-shop/.../strangler/config/Wave6ClientConfig.java`, `application-strangler-wave6.properties`
**Depende de:** T2, T3, T4
**Requisito:** CART-04, ORD-05, CHK-07

**Pronto quando:**
- [ ] Properties coexistem com wave1/wave2
- [ ] `./mvnw test -pl sm-shop -Dtest=Wave6ClientConfigTest`

**Testes:** unit
**Gate:** `./mvnw test -pl sm-shop -Dtest=Wave6ClientConfigTest`

**Commit:** `feat(shop): add wave6 strangler properties and clients`

---

### T6: API cart totals — fronteira monólito (`TOT-ready`)

**O quê:** Extrair lógica `calculateShoppingCartTotal` para `CartTotalsService`; expor `POST /internal/v1/orders/totals` em sm-shop (temporário) ou módulo sm-order-core; conectar `ShoppingCartCalculationServiceImpl` a client HTTP quando flag setada.
**Onde:** `sm-core/.../order/totals/`, `sm-shop/.../internal/CartTotalsController.java`
**Depende de:** T5
**Requisito:** CART-03, OQ-01

**Pronto quando:**
- [ ] Cálculo cart usa HTTP quando `wave6.totals.http.enabled=true`
- [ ] Paridade teste integração com totais in-process
- [ ] `./mvnw test -pl sm-core,sm-shop -Dtest=CartTotalsParityTest`

**Testes:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=CartTotalsParityTest`

**Commit:** `feat(order): extract cart totals API and break cart-order cycle`

---

### T7: Scaffold `sm-shoppingcart-core`

**O quê:** Novo módulo Maven; mover repositórios cart.
**Onde:** `sm-shoppingcart-core/`, root `pom.xml`
**Depende de:** T2
**Reutiliza:** Onda 2 `sm-content-core` pattern
**Requisito:** CART-01

**Pronto quando:**
- [ ] Smoke `@DataJpaTest` passa
- [ ] `./mvnw test -pl sm-shoppingcart-core`

**Testes:** integration
**Gate:** `./mvnw test -pl sm-shoppingcart-core`

**Commit:** `feat(cart-core): scaffold and extract cart repositories`

---

### T8: Mover `ShoppingCartService` para sm-shoppingcart-core

**O quê:** Mover `ShoppingCartServiceImpl`; remover dependência direta `OrderService` — injetar `CartTotalsClient`.
**Onde:** `sm-shoppingcart-core/.../services/shoppingcart/`
**Depende de:** T6, T7
**Requisito:** CART-03

**Pronto quando:**
- [ ] Zero imports `OrderService` no módulo
- [ ] `./mvnw test -pl sm-shoppingcart-core`

**Testes:** unit + integration
**Gate:** `./mvnw test -pl sm-shoppingcart-core`

**Commit:** `feat(cart-core): extract ShoppingCartService with HTTP totals`

---

### T9: Client validação catalog em cart-core

**O quê:** `CatalogLineValidator` chamando catalog-service HTTP para `ProductLineSnapshot`.
**Onde:** `sm-shoppingcart-core/.../integration/`
**Depende de:** T8
**Requisito:** CART-02

**Pronto quando:**
- [ ] Produto inválido retorna erro estruturado
- [ ] `./mvnw test -pl sm-shoppingcart-core -Dtest=CatalogLineValidatorTest`

**Testes:** unit (mock catalog)
**Gate:** `./mvnw test -pl sm-shoppingcart-core -Dtest=CatalogLineValidatorTest`

**Commit:** `feat(cart-core): catalog HTTP validation for cart lines`

---

### T10: Scaffold app Boot `shoppingcart-service`

**O quê:** App Spring Boot :8086, JPA, JWT `/private/**`, health actuator.
**Onde:** `shoppingcart-service/`
**Depende de:** T8
**Requisito:** CART-01, STR-05

**Pronto quando:**
- [ ] `./mvnw package -pl shoppingcart-service`
- [ ] Context carrega com config DB compartilhado

**Testes:** integration
**Gate:** `./mvnw test -pl shoppingcart-service -Dtest=ShoppingCartServiceApplicationTest`

**Commit:** `feat(shoppingcart-service): scaffold application`

---

### T11: Controllers REST cart públicos

**O quê:** Espelhar caminhos `ShoppingCartApi`; mappers entidade cart ↔ DTO.
**Onde:** `shoppingcart-service/.../api/v1/`
**Depende de:** T10
**Requisito:** CART-01, CART-05, CART-06

**Pronto quando:**
- [ ] Caminhos CRUD registrados
- [ ] `./mvnw test -pl shoppingcart-service -Dtest=ShoppingCartApiIntegrationTest`

**Testes:** integration
**Gate:** `./mvnw test -pl shoppingcart-service -Dtest=ShoppingCartApiIntegrationTest`

**Commit:** `feat(shoppingcart-service): public cart REST endpoints`

---

### T12: API internal cleanup cart pós-checkout

**O quê:** `DELETE /internal/v1/carts/{id}/after-checkout` com `X-Internal-Token`.
**Onde:** `shoppingcart-service/.../api/internal/`
**Depende de:** T11
**Requisito:** CHK-06

**Pronto quando:**
- [ ] Token inválido → 401
- [ ] `./mvnw test -pl shoppingcart-service -Dtest=InternalCartControllerTest`

**Testes:** integration
**Gate:** `./mvnw test -pl shoppingcart-service -Dtest=InternalCartControllerTest`

**Commit:** `feat(shoppingcart-service): internal post-checkout cart clear`

---

### T13: Strangler `ShoppingCartFacadeHttpAdapter`

**O quê:** Adaptador HTTP em sm-shop; feature flag `wave6.shoppingcart.strangler.enabled`.
**Onde:** `sm-shop/.../strangler/cart/`
**Depende de:** T11, T5
**Requisito:** CART-04, STR-06

**Pronto quando:**
- [ ] Flag off → in-process; flag on → HTTP
- [ ] Falha remota → 503 + correlation id
- [ ] `./mvnw test -pl sm-shop -Dtest=ShoppingCartFacadeHttpAdapterTest`

**Testes:** unit + integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=ShoppingCartFacadeHttpAdapterTest`

**Commit:** `feat(shop): shopping cart strangler HTTP adapter`

---

### T14: Shadow mode cart + gate `SC-ready`

**O quê:** Shadow opcional read-remote comparando respostas; documentar runbook de cutover.
**Onde:** `sm-shop/.../strangler/cart/`, `docs/runbooks/wave6-cart-cutover.md`
**Depende de:** T13
**Requisito:** CART-04, STR-07

**Pronto quando:**
- [ ] Shadow mode registra divergências sem impacto ao usuário
- [ ] Runbook revisado
- [ ] Marco `SC-ready` marcado em STATE.md

**Testes:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=ShoppingCartShadowModeTest`

**Commit:** `feat(shop): cart strangler shadow mode and cutover runbook`

---

### T15: Scaffold `sm-order-core`

**O quê:** Novo módulo; repositórios order, `OrderTotalService`.
**Onde:** `sm-order-core/`, root `pom.xml`
**Depende de:** T2
**Requisito:** ORD-01

**Pronto quando:**
- [ ] Smoke `@DataJpaTest`
- [ ] `./mvnw test -pl sm-order-core`

**Testes:** integration
**Gate:** `./mvnw test -pl sm-order-core`

**Commit:** `feat(order-core): scaffold and extract order repositories`

---

### T16: Mover serviços read order para sm-order-core

**O quê:** Extrair get/list/history de `OrderServiceImpl`; sem `PaymentService` no caminho read.
**Onde:** `sm-order-core/.../services/order/`
**Depende de:** T15
**Requisito:** ORD-01, ORD-02, ORD-03

**Pronto quando:**
- [ ] Métodos read isolados
- [ ] `./mvnw test -pl sm-order-core -Dtest=OrderReadServiceTest`

**Testes:** unit
**Gate:** `./mvnw test -pl sm-order-core -Dtest=OrderReadServiceTest`

**Commit:** `feat(order-core): extract order read services`

---

### T17: `CartTotalsService` em sm-order-core

**O quê:** Totais stateless a partir de `CartTotalsRequest`; mover lógica da fronteira monólito T6.
**Onde:** `sm-order-core/.../services/order/totals/`
**Depende de:** T6, T15
**Requisito:** CART-03, ORD-04

**Pronto quando:**
- [ ] Testes paridade vs legado
- [ ] `./mvnw test -pl sm-order-core -Dtest=CartTotalsServiceTest`

**Testes:** unit
**Gate:** `./mvnw test -pl sm-order-core -Dtest=CartTotalsServiceTest`

**Commit:** `feat(order-core): cart totals calculation service`

---

### T18: Scaffold app Boot `order-service`

**O quê:** Spring Boot :8087, JPA, JWT, actuator, filtro internal token.
**Onde:** `order-service/`
**Depende de:** T16
**Requisito:** ORD-05, STR-05

**Pronto quando:**
- [ ] `./mvnw package -pl order-service`
- [ ] `./mvnw test -pl order-service -Dtest=OrderServiceApplicationTest`

**Testes:** integration
**Gate:** `./mvnw test -pl order-service -Dtest=OrderServiceApplicationTest`

**Commit:** `feat(order-service): scaffold application`

---

### T19: REST read order público

**O quê:** GET order, listar pedidos, histórico de status — espelhar caminhos monólito.
**Onde:** `order-service/.../api/v1/`
**Depende de:** T18
**Requisito:** ORD-01, ORD-02, ORD-03

**Pronto quando:**
- [ ] Sem JPA em respostas JSON
- [ ] `./mvnw test -pl order-service -Dtest=OrderReadApiIntegrationTest`

**Testes:** integration
**Gate:** `./mvnw test -pl order-service -Dtest=OrderReadApiIntegrationTest`

**Commit:** `feat(order-service): public order read endpoints`

---

### T20: API internal totals em order-service

**O quê:** `POST /internal/v1/orders/totals`; aposentar endpoint somente monólito da T6.
**Onde:** `order-service/.../api/internal/`
**Depende de:** T17, T18
**Requisito:** CART-03

**Pronto quando:**
- [ ] shoppingcart-service chama totals em order-service
- [ ] `./mvnw test -pl order-service -Dtest=InternalTotalsControllerTest`

**Testes:** integration
**Gate:** `./mvnw test -pl order-service -Dtest=InternalTotalsControllerTest`

**Commit:** `feat(order-service): internal cart totals API`

---

### T21: Adaptador strangler order read + `OR-read-ready`

**O quê:** `OrderFacadeHttpAdapter` para leituras; flag `wave6.order.strangler.enabled`.
**Onde:** `sm-shop/.../strangler/order/`
**Depende de:** T19, T5
**Requisito:** ORD-05

**Pronto quando:**
- [ ] Caminhos read remotos quando flag on
- [ ] Marco STATE.md `OR-read-ready`
- [ ] `./mvnw test -pl sm-shop -Dtest=OrderFacadeHttpAdapterTest`

**Testes:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=OrderFacadeHttpAdapterTest`

**Commit:** `feat(shop): order read strangler adapter`

---

### T22: Schema `ORDER_OUTBOX` + entidade

**O quê:** Migration Flyway/Liquibase; entidade JPA `OrderOutboxEntry`.
**Onde:** `order-service/src/main/resources/db/`, `sm-order-core/.../outbox/`
**Depende de:** T15
**Requisito:** CHK-03

**Pronto quando:**
- [ ] Migration aplica no DB compartilhado
- [ ] `./mvnw test -pl sm-order-core -Dtest=OrderOutboxRepositoryTest`

**Testes:** integration
**Gate:** `./mvnw test -pl sm-order-core -Dtest=OrderOutboxRepositoryTest`

**Commit:** `feat(order-core): order outbox schema and repository`

---

### T23: Scheduler relay outbox

**O quê:** Poller `@Scheduled`; publicar em Spring `ApplicationEventPublisher` (hooks email/inventário).
**Onde:** `order-service/.../outbox/OrderOutboxRelay.java`
**Depende de:** T22
**Requisito:** CHK-03, CHK-10

**Pronto quando:**
- [ ] Linhas não publicadas relayed; `published_at` setado
- [ ] `./mvnw test -pl order-service -Dtest=OrderOutboxRelayTest`

**Testes:** integration
**Gate:** `./mvnw test -pl order-service -Dtest=OrderOutboxRelayTest`

**Commit:** `feat(order-service): transactional outbox relay`

---

### T24: Saga — `CheckoutCommitHandler` persistir pedido

**O quê:** `POST /internal/v1/checkout/commit`; idempotência; persistir pedido + outbox `OrderPlaced` em uma transação.
**Onde:** `sm-order-core/.../checkout/`, `order-service/.../api/internal/`
**Depende de:** T22, T18
**Requisito:** CHK-02, CHK-03, CHK-09

**Pronto quando:**
- [ ] Chave idempotência duplicada retorna mesmo orderId
- [ ] `./mvnw test -pl order-service -Dtest=CheckoutCommitIntegrationTest`

**Testes:** integration
**Gate:** `./mvnw test -pl order-service -Dtest=CheckoutCommitIntegrationTest`

**Commit:** `feat(order-service): checkout commit saga step with outbox`

---

### T25: Saga — endpoint atualização payment status

**O quê:** `PATCH /internal/v1/orders/{id}/payment-status`; outbox `OrderPaid` / `OrderCancelled`.
**Onde:** `order-service/.../api/internal/`
**Depende de:** T24
**Requisito:** CHK-02, CHK-04

**Pronto quando:**
- [ ] Transições de status validadas
- [ ] `./mvnw test -pl order-service -Dtest=PaymentStatusUpdateTest`

**Testes:** integration
**Gate:** `./mvnw test -pl order-service -Dtest=PaymentStatusUpdateTest`

**Commit:** `feat(order-service): payment status saga step`

---

### T26: Remover `PaymentService` do caminho commit order em sm-core

**O quê:** Refatorar `OrderServiceImpl.processOrder` legado para delegar à saga quando flag on; estreitar pointcut AOP global para checkout.
**Onde:** `sm-core/.../order/OrderServiceImpl.java`, `shopizer-core-config.xml`
**Depende de:** T24
**Requisito:** CHK-07, GAP-CHK-02

**Pronto quando:**
- [ ] Caminho legado preservado quando flag saga off
- [ ] `./mvnw test -pl sm-core -Dtest=OrderServiceSagaDelegationTest`

**Testes:** unit
**Gate:** `./mvnw test -pl sm-core -Dtest=OrderServiceSagaDelegationTest`

**Commit:** `refactor(core): delegate processOrder to saga when enabled`

---

### T27: Testes compensação saga

**O quê:** Testar falha payment → order CANCELLED; cart não limpo.
**Onde:** `sm-shop/src/test/.../checkout/SagaCompensationTest.java`
**Depende de:** T25, T26
**Requisito:** CHK-02

**Pronto quando:**
- [ ] Caminhos compensação verdes
- [ ] `./mvnw test -pl sm-shop -Dtest=SagaCompensationTest`

**Testes:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=SagaCompensationTest`

**Commit:** `test(shop): saga compensation scenarios`

---

### T28: `CheckoutApplicationService` — scaffold orquestrador

**O quê:** Novo serviço em sm-shop; injetar client integration wave5, clients cart/order wave6, client tax.
**Onde:** `sm-shop/.../checkout/CheckoutApplicationService.java`
**Depende de:** T5, T14, T21
**Requisito:** CHK-01, HUB-01

**Pronto quando:**
- [ ] Serviço compila com todos os clients
- [ ] `./mvnw test -pl sm-shop -Dtest=CheckoutApplicationServiceTest`

**Testes:** unit (mocked deps)
**Gate:** `./mvnw test -pl sm-shop -Dtest=CheckoutApplicationServiceTest`

**Commit:** `feat(shop): checkout application service scaffold`

---

### T29: Checkout — tax no BFF (ADR-006)

**O quê:** `computeTaxLines()` chama tax-service; embutir em `OrderSnapshot` para commit.
**Onde:** `CheckoutApplicationService`
**Depende de:** T28
**Requisito:** CHK-08, OQ-03

**Pronto quando:**
- [ ] Linhas tax na requisição commit
- [ ] order-service não chama TaxService
- [ ] `./mvnw test -pl sm-shop -Dtest=CheckoutTaxIntegrationTest`

**Testes:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=CheckoutTaxIntegrationTest`

**Commit:** `feat(shop): checkout tax via tax-service at BFF`

---

### T30: Checkout — orquestração saga place-order completa

**O quê:** `placeOrder()` executa passos saga 1–8; flag `wave6.checkout.saga.enabled`.
**Onde:** `CheckoutApplicationService`
**Depende de:** T27, T29, T12, T24, T25
**Requisito:** CHK-01, CHK-02, CHK-04, CHK-05, CHK-06

**Pronto quando:**
- [ ] Happy path E2E em teste integração
- [ ] `./mvnw test -pl sm-shop -Dtest=CheckoutPlaceOrderIntegrationTest`

**Testes:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=CheckoutPlaceOrderIntegrationTest`

**Commit:** `feat(shop): checkout place-order saga orchestration`

---

### T31: Conectar checkout `OrderApi` ao CheckoutApplicationService

**O quê:** Substituir `orderFacade.processOrder` direto por `checkoutApplicationService.placeOrder` quando saga habilitada.
**Onde:** `sm-shop/.../api/v1/order/OrderApi.java`
**Depende de:** T30
**Requisito:** HUB-04, CHK-07

**Pronto quando:**
- [ ] Ambos caminhos de flag testados
- [ ] `./mvnw test -pl sm-shop -Dtest=OrderApiCheckoutRoutingTest`

**Testes:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=OrderApiCheckoutRoutingTest`

**Commit:** `feat(shop): route OrderApi checkout through application service`

---

### T32: Gate `CHK-ready` + checklist canary staging

**O quê:** Documentar rollout canary; habilitar saga no profile staging docker-compose-wave6.
**Onde:** `docs/runbooks/wave6-checkout-cutover.md`, `docker-compose-wave6.yml`
**Depende de:** T31
**Requisito:** CHK-07, STR-07

**Pronto quando:**
- [ ] Marco `CHK-ready` em STATE.md
- [ ] Runbook completo

**Testes:** manual checklist
**Gate:** Staging E2E place-order

**Commit:** `docs(wave6): checkout cutover runbook and CHK-ready gate`

---

### T33: Hub — rotear `OrderPaymentApi` pelo checkout

**O quê:** Remover injeção direta `PaymentService` da API; delegar a CheckoutApplicationService.
**Onde:** `OrderPaymentApi.java`
**Depende de:** T28
**Requisito:** HUB-02, HUB-03

**Pronto quando:**
- [ ] Zero PaymentService em OrderPaymentApi
- [ ] `./mvnw test -pl sm-shop -Dtest=OrderPaymentApiRoutingTest`

**Testes:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=OrderPaymentApiRoutingTest`

**Commit:** `refactor(shop): OrderPaymentApi via checkout service`

---

### T34: Hub — rotear `OrderTotalApi` e `OrderShippingApi`

**O quê:** Mesmo padrão T33 para totals e cotações de frete.
**Onde:** `OrderTotalApi.java`, `OrderShippingApi.java`
**Depende de:** T28
**Requisito:** HUB-02

**Pronto quando:**
- [ ] Delegação completa
- [ ] `./mvnw test -pl sm-shop -Dtest=OrderTotalShippingApiRoutingTest`

**Testes:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=OrderTotalShippingApiRoutingTest`

**Commit:** `refactor(shop): order total and shipping APIs via checkout`

---

### T35: `OrderFacadeImpl` fino — remover deps sm-core checkout

**O quê:** Métodos checkout delegam a CheckoutApplicationService; reduzir injeções para ≤4 em caminhos checkout.
**Onde:** `OrderFacadeImpl.java`
**Depende de:** T31, T33, T34
**Requisito:** HUB-01, HUB-03

**Pronto quando:**
- [ ] Sem ProductService/PaymentService em caminhos código checkout
- [ ] `./mvnw test -pl sm-shop -Dtest=OrderFacadeThinTest`

**Testes:** unit
**Gate:** `./mvnw test -pl sm-shop -Dtest=OrderFacadeThinTest`

**Commit:** `refactor(shop): thin order facade for checkout delegation`

---

### T36: Consolidar pacotes duplicados `OrderFacadeImpl`

**O quê:** Mesclar `facade/v1/OrderFacadeImpl` em delegate fino único ou deprecar caminho v1.
**Onde:** `sm-shop/.../order/facade/`
**Depende de:** T35
**Requisito:** GAP-ORD-01

**Pronto quando:**
- [ ] OrderFacade canônico único para checkout
- [ ] `./mvnw test -pl sm-shop -Dtest=OrderFacadeConsolidationTest`

**Testes:** unit
**Gate:** `./mvnw test -pl sm-shop -Dtest=OrderFacadeConsolidationTest`

**Commit:** `refactor(shop): consolidate order facade implementations`

---

### T37: Merge cart no login (P2)

**O quê:** `mergeAnonymousCart` em shoppingcart-service; BFF chama em sucesso de auth.
**Onde:** `shoppingcart-service`, `CustomerFacade` hook
**Depende de:** T13
**Requisito:** CART-08

**Pronto quando:**
- [ ] Teste integração merge passa
- [ ] `./mvnw test -pl shoppingcart-service -Dtest=CartMergeIntegrationTest`

**Testes:** integration
**Gate:** `./mvnw test -pl shoppingcart-service -Dtest=CartMergeIntegrationTest`

**Commit:** `feat(shoppingcart-service): anonymous cart merge on login`

---

### T38: Correlation ID + health indicators Wave6

**O quê:** Health para deps catalog/order/customer; propagar `X-Correlation-Id` em todos os clients wave6.
**Onde:** `shoppingcart-service`, `order-service`, `Wave6ClientConfig`
**Depende de:** T13, T21
**Requisito:** STR-05, STR-06

**Pronto quando:**
- [ ] Health actuator mostra status dependências
- [ ] `./mvnw test -pl shoppingcart-service,order-service -Dtest=*HealthIndicatorTest`

**Testes:** integration
**Gate:** `./mvnw test -pl shoppingcart-service,order-service -Dtest=*HealthIndicatorTest`

**Commit:** `feat(wave6): correlation and health indicators`

---

### T39: Pact consumer — sm-shop Wave6 [P]

**O quê:** `Wave6ConsumerPactTest` para cart, order read, totals, checkout commit.
**Onde:** `sm-shop/src/test/.../pact/`
**Depende de:** T38
**Requisito:** STR-01

**Pronto quando:**
- [ ] Pacts consumer gerados
- [ ] `./mvnw test -pl sm-shop -Dtest=Wave6ConsumerPactTest`

**Testes:** pact
**Gate:** `./mvnw test -pl sm-shop -Dtest=Wave6ConsumerPactTest`

**Commit:** `test(shop): wave6 pact consumer tests`

---

### T40: Pact provider — shoppingcart-service [P]

**O quê:** `ShoppingCartProviderPactTest`.
**Onde:** `shoppingcart-service/src/test/.../pact/`
**Depende de:** T11
**Requisito:** STR-02

**Pronto quando:**
- [ ] Provider verifica pacts consumer
- [ ] `./mvnw test -pl shoppingcart-service -Dtest=ShoppingCartProviderPactTest`

**Testes:** pact
**Gate:** `./mvnw test -pl shoppingcart-service -Dtest=ShoppingCartProviderPactTest`

**Commit:** `test(shoppingcart-service): pact provider`

---

### T41: Pact provider — order-service [P]

**O quê:** `OrderProviderPactTest` para read + internal totals + commit.
**Onde:** `order-service/src/test/.../pact/`
**Depende de:** T24
**Requisito:** STR-03, STR-04

**Pronto quando:**
- [ ] Provider verifica
- [ ] `./mvnw test -pl order-service -Dtest=OrderProviderPactTest`

**Testes:** pact
**Gate:** `./mvnw test -pl order-service -Dtest=OrderProviderPactTest`

**Commit:** `test(order-service): pact provider`

---

### T42: Gates cobertura JaCoCo Wave6 [P]

**O quê:** Adicionar thresholds jacoco para shoppingcart-service, order-service, pacote strangler sm-shop.
**Onde:** module `pom.xml` files
**Depende de:** T10, T18
**Requisito:** STR-01

**Pronto quando:**
- [ ] `./mvnw verify -pl shoppingcart-service,order-service,sm-shop -DfailIfNoTests=false`

**Testes:** coverage
**Gate:** `./mvnw verify -pl shoppingcart-service,order-service`

**Commit:** `build(wave6): jacoco coverage gates`

---

### T43: `docker-compose-wave6.yml`

**O quê:** Topologia completa: reference, tax, wave2, catalog, customer, integration, cart, order, sm-shop com profile strangler-wave6.
**Onde:** root `docker-compose-wave6.yml`, Dockerfiles
**Depende de:** T32, T38
**Requisito:** STR-07

**Pronto quando:**
- [ ] `docker compose -f docker-compose-wave6.yml config` valid
- [ ] Health checks passam após `up`

**Testes:** manual
**Gate:** `docker compose -f docker-compose-wave6.yml config`

**Commit:** `infra(wave6): docker compose topology`

---

### T44: Suite testes integração Wave6

**O quê:** Teste cross-service: add to cart → totals → place order (saga) → read order.
**Onde:** `sm-shop/src/test/.../integration/Wave6E2EIntegrationTest.java`
**Depende de:** T43
**Requisito:** CHK-01

**Pronto quando:**
- [ ] E2E passa contra topologia compose (ou Testcontainers)
- [ ] `./mvnw test -pl sm-shop -Dtest=Wave6E2EIntegrationTest -DfailIfNoTests=false`

**Testes:** e2e
**Gate:** `./mvnw test -pl sm-shop -Dtest=Wave6E2EIntegrationTest`

**Commit:** `test(shop): wave6 end-to-end integration`

---

### T45: Atualizar STATE.md + ROADMAP — Onda 6 completa

**O quê:** Marcar Execute Onda 6 completo; documentar flags, portas, runbooks rollback.
**Onde:** `.specs/project/STATE.md`, `ROADMAP.md`
**Depende de:** T39, T40, T41, T44
**Requisito:** all P1

**Pronto quando:**
- [ ] STATE.md AD-020+ registrado
- [ ] `./mvnw clean install` passa (reator completo quando todas ondas merged)

**Testes:** full reactor
**Gate:** `./mvnw clean install`

**Commit:** `docs(project): mark onda 6 wave complete`

---

## Resumo

| Fase | Tasks | Contagem |
|-------|-------|-------|
| Gate + contratos | T1–T5 | 5 |
| Totals / quebra de ciclo | T6 | 1 |
| ShoppingCart | T7–T14 | 8 |
| Order read | T15–T21 | 7 |
| Saga + outbox | T22–T27 | 6 |
| Checkout BFF | T28–T32 | 5 |
| Hub + strangler | T33–T38 | 6 |
| Gate | T39–T45 | 7 |
| **Total** | **T1–T45** | **45** |

### Tasks TLC estendidas (T46–T62) — auxiliares Execute granulares

| Task | O quê | Depende |
|------|------|---------|
| T46 | Testes unit `CartLineSnapshot` builder | T2 |
| T47 | Testes unit `OrderSnapshot` builder | T2 |
| T48 | `CartTotalsClientRestTemplateImpl` | T5 |
| T49 | `OrderServiceClientRestTemplateImpl` | T5 |
| T50 | `ShoppingCartServiceClientRestTemplateImpl` | T5 |
| T51 | `CheckoutCommitClientRestTemplateImpl` | T5 |
| T52 | Mapper `ReadableShoppingCartMapper` em cart-service | T11 |
| T53 | Mapper `ReadableOrderMapper` em order-service | T19 |
| T54 | Config security JWT cart-service (copiar padrão wave1) | T10 |
| T55 | Config security JWT order-service | T18 |
| T56 | Property `wave6.totals.http.enabled` + testes | T6 |
| T57 | Stub validação inventário em checkout (HTTP catalog) | T28 |
| T58 | Wiring consumer outbox email | T23 |
| T59 | Teste chaos matar integration mid-saga | T27 |
| T60 | ArchUnit: sem OrderService em shoppingcart-service | T8 |
| T61 | ArchUnit: sem PaymentService no commit order-service | T24 |
| T62 | Script drill rollback `scripts/wave6-rollback-drill.sh` | T32 |

**Total geral: 62 tasks TLC** (T1–T62)

---

## Mapeamento Compozy

Ver `.compozy/tasks/onda-6-shoppingcart-order/_tasks.md` para 16 tasks Compozy cobrindo T1–T62.
