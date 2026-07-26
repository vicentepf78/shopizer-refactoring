# Onda 6 — Especificação ShoppingCart + Order

**ID da feature:** `onda-6-shoppingcart-order`
**Fase:** Specify + Design (Execute bloqueado até gate Ondas 3–5 verde)
**Complexidade:** XLarge (2 serviços + fronteira checkout + saga; acoplamento 9/10)
**Fonte:** [MIGRATION-MASTER-PLAN.md](../../../docs/decomposition/MIGRATION-MASTER-PLAN.md) § Onda 6
**Exploração:** Análise de acoplamento — hub order, ciclo cart↔order, `processOrder` transacional (2026-07-04)

---

## Declaração do problema

A Onda 6 é a **última e mais arriscada onda de extração**. Order é o orquestrador central (dificuldade **9/10**): seis dependências outbound de domínio, um `processOrder` transacional abrangendo payments, shipping, tax, catalog, customer e cart, e um hub de checkout em `sm-shop` que injeta **12 serviços sm-core** mais `CustomerFacade` e `ShoppingCartFacade`. Dois ciclos críticos bloqueiam splits ingênuos:

- **order ↔ payments** — `OrderServiceImpl.processOrder` → `paymentService.processPayment`; `PaymentServiceImpl` → `orderService.saveOrUpdate`
- **order ↔ shoppingcart** — `ShoppingCartCalculationServiceImpl` → `orderService.calculateShoppingCartTotal`; `OrderServiceImpl` → `shoppingCartService`

Sem especificação formal, os times ou extraem cart e order juntos em big-bang (superfície de rollback inaceitável) ou adiam indefinidamente enquanto o monólito permanece autoridade de checkout.

As Ondas 3–5 entregam os pré-requisitos: snapshots DTO, Checkout Application Service, saga/outbox em `processOrder`, serviços catalog/customer/integration. Esta spec define **o que** extrair (`shoppingcart-service`, `order-service`), **o que** permanece no BFF (`CheckoutApplicationService`) e **como** fasear cutover com feature flags e rollback explícitos.

---

## Objetivos

- [ ] `shoppingcart-service` e `order-service` implantáveis como aplicações Spring Boot independentes
- [ ] BFF monólito consome ambos via HTTP Strangler; caminhos REST congelados para APIs cart e order
- [ ] **Ciclo cart↔order quebrado** — totais de cart via contrato HTTP, não `OrderService` in-process
- [ ] **Checkout Application Service** é a única fronteira de orquestração de checkout em `sm-shop`
- [ ] `processOrder` roda como **saga choreography** com **transactional outbox** (padrão Onda 3), não transação global AOP
- [ ] Decomposição do hub: `OrderFacadeImpl` reduzido a delegação; bypass APIs (`OrderPaymentApi`, `OrderTotalApi`, `OrderShippingApi`) roteadas pela fronteira checkout
- [ ] Feature flags por domínio com rollback documentado (`wave6.*`)
- [ ] Cobertura Pact para cart P1, totals, order read, checkout commit
- [ ] Zero entidades JPA em respostas JSON REST migradas

---

## Fora de escopo

| Funcionalidade | Motivo |
|---------|--------|
| Split físico de database por serviço | AD-003 herdado — schema `SALESMANAGER` compartilhado na transição |
| Mover `CheckoutApplicationService` para deployable próprio | AD-024 — permanece no BFF na Onda 6; extração opcional pós-onda |
| Cálculo remoto de tax dentro de `order-service` | OQ-03 / ADR-006 — linhas de tax fornecidas pelo BFF via `tax-service` |
| Extração completa de catalog write | Escopo Onda 4 |
| Reescrever plugins payment/shipping | Onda 5 `integration-service` |
| Eliminar `MerchantStoreArgumentResolver` (~450 refs) | BFF mantém resolver; passa `MerchantStoreId` / snapshots |
| Analytics, reporting, exports BI de order | Fora do caminho crítico de checkout |
| Split greenfield `InitializationDatabaseImpl` | Serviços assumem DB populado |
| Depreciação API V1 | Roadmap Fase 4 |

---

## Histórias de usuário

### P1: Shopping Cart — CRUD e session cart ⭐ MVP

**História de usuário**: Como visitante da vitrine, quero adicionar, atualizar e remover itens no meu carrinho via endpoints `/api/v1/cart` existentes, para que a preparação de checkout funcione quando o runtime de cart estiver fora do monólito.

**Por que P1**: Cart tem dificuldade 7/10 e deve ser extraído **antes** do cutover de order para quebrar o ciclo de cálculo via HTTP totals.

**Critérios de aceite**:

1. WHEN endpoints `GET/POST/PUT/DELETE` cart com header `store` THEN `shoppingcart-service` SHALL persistir `ShoppingCart` + itens de linha com escopo de loja e cliente/sessão
2. WHEN linha de cart referencia produto THEN `shoppingcart-service` SHALL validar disponibilidade via HTTP `catalog-service` (`ProductLineSnapshot`) — SHALL NOT carregar grafo JPA completo de `Product`
3. WHEN exibição do cart precisa de totais THEN BFF ou `shoppingcart-service` SHALL chamar `POST /internal/v1/orders/totals` em order-service (ou fronteira checkout) com `CartTotalsRequest` — SHALL NOT chamar in-process `OrderService.calculateShoppingCartTotal`
4. WHEN `wave6.shoppingcart.strangler.enabled=false` THEN comportamento in-process de cart do monólito SHALL permanecer inalterado
5. WHEN cart remoto indisponível THEN BFF SHALL retornar HTTP 503 com `X-Correlation-Id` — sem fallback silencioso

**Teste independente**: Implantar `shoppingcart-service` + dependências; adicionar item; ler cart; verificar totais via API totals; flag strangler alterna in-process vs remoto.

**Componentes fonte:**

| Papel | Caminho |
|------|------|
| Entities | `sm-core-model/.../shoppingcart/` |
| Services | `sm-core/.../services/shoppingcart/` |
| Calculation | `sm-core/.../shoppingcart/ShoppingCartCalculationServiceImpl.java` |
| API | `sm-shop/.../api/v1/shoppingCart/ShoppingCartApi.java` |
| Facade | `sm-shop/.../shoppingCart/ShoppingCartFacadeImpl.java` |

**IDs de requisito:** CART-01…CART-07

---

### P1: Order — leitura, listagem, histórico de status ⭐ MVP

**História de usuário**: Como admin de loja ou cliente, quero visualizar pedidos e histórico de status via APIs de order existentes, para que leituras funcionem quando a persistência de order é remota.

**Por que P1**: Caminhos de leitura são menor risco que `processOrder`; valida Strangler antes do cutover saga.

**Critérios de aceite**:

1. WHEN `GET` order por id/code THEN `order-service` SHALL retornar DTO `ReadableOrder` — sem entidade `Order` em JSON
2. WHEN admin lista pedidos com critérios THEN `order-service` SHALL suportar paginação/filtro equivalente ao monólito
3. WHEN histórico de status solicitado THEN `order-service` SHALL retornar lista `ReadableOrderStatusHistory`
4. WHEN snapshot de cliente necessário THEN `order-service` MAY chamar HTTP `customer-service` — SHALL usar `CustomerSnapshot`, não merge em transação global
5. WHEN `wave6.order.strangler.enabled=false` THEN caminhos de leitura permanecem in-process

**IDs de requisito:** ORD-01…ORD-06

---

### P1: Checkout — place order via saga ⭐ MVP (maior risco)

**História de usuário**: Como cliente concluindo checkout, quero fazer um pedido com pagamento e frete, para que a compra complete corretamente quando `processOrder` roda entre serviços remotos.

**Por que P1**: Caminho central de receita; exige saga/outbox da Onda 3 e integration-service da Onda 5.

**Critérios de aceite**:

1. WHEN `POST` checkout/place-order (caminhos existentes) THEN `CheckoutApplicationService` no BFF SHALL orquestrar: validar cart → computar totais → reservar/validar inventário → iniciar pagamento via `integration-service` → persistir pedido via endpoint saga `order-service` → limpar cart
2. WHEN passo saga `processOrder` falha THEN sistema SHALL executar ações compensatórias (void/refund de pagamento conforme capability do módulo, status order `CANCELLED`, cart não limpo)
3. WHEN pedido persistido THEN `order-service` SHALL escrever eventos de domínio em **transactional outbox** (`ORDER_OUTBOX`) na mesma transação DB da linha de pedido
4. WHEN relay outbox roda THEN eventos (`OrderPlaced`, `OrderPaid`, etc.) SHALL ser publicados para consumidores downstream (email, inventário)
5. WHEN `wave6.checkout.saga.enabled=false` THEN `orderService.processOrder` legado in-process SHALL executar (caminho rollback)
6. WHEN tax necessário THEN BFF SHALL chamar `tax-service` e passar linhas de tax em `OrderSnapshot` — order-service SHALL NOT chamar tax in-process (OQ-03)

**IDs de requisito:** CHK-01…CHK-10

---

### P1: Decomposição do hub — facades finas ⭐ MVP

**História de usuário**: Como engenheiro de plataforma, quero que APIs de checkout parem de injetar 12 serviços sm-core diretamente, para que a fronteira BFF seja mantível após a extração.

**Critérios de aceite**:

1. WHEN `OrderApi`, `OrderPaymentApi`, `OrderTotalApi`, `OrderShippingApi` tratam operações relacionadas a checkout THEN SHALL delegar somente a `CheckoutApplicationService`
2. WHEN operações de order não-checkout (leitura, histórico) THEN `OrderFacade` MAY delegar a adaptador HTTP `order-service`
3. WHEN decomposição do hub completa THEN `OrderFacadeImpl` SHALL NOT injetar `PaymentService`, `ShippingService`, `ProductService` diretamente para caminhos de checkout

**IDs de requisito:** HUB-01…HUB-04

---

### P2: Merge de cart no login

**História de usuário**: Como cliente recorrente, quero meu carrinho anônimo mesclado quando faço login.

**IDs de requisito:** CART-08

---

### P2: Testes de contrato (Pact)

**História de usuário**: Como desenvolvedor, quero testes Pact para cart, totals, order read e checkout commit.

**IDs de requisito:** STR-01…STR-04

---

### P3: Observabilidade e runbooks de rollback

**História de usuário**: Como operador, quero health checks, correlation IDs e rollback documentado para cada flag Wave 6.

**IDs de requisito:** STR-05…STR-07

---

## Resumo de requisitos funcionais

| ID | Área | Prioridade | Resumo |
|----|------|----------|---------|
| CART-01 | Cart | P1 | CRUD itens de linha, escopo sessão/cliente |
| CART-02 | Cart | P1 | Validação de produto via HTTP catalog |
| CART-03 | Cart | P1 | Totais via HTTP order-service (quebra de ciclo) |
| CART-04 | Cart | P1 | Flag Strangler `wave6.shoppingcart.strangler.enabled` |
| CART-05 | Cart | P1 | Códigos promo / atributos de cart preservados |
| CART-06 | Cart | P1 | Endpoints mini-cart e contagem de cart |
| CART-07 | Cart | P1 | Sem JPA em JSON |
| CART-08 | Cart | P2 | Merge cart anônimo no login |
| ORD-01 | Order | P1 | Get order por id |
| ORD-02 | Order | P1 | Listar/buscar pedidos (admin) |
| ORD-03 | Order | P1 | Histórico de status |
| ORD-04 | Order | P1 | Breakdown de totais de pedido (leitura) |
| ORD-05 | Order | P1 | Flag Strangler `wave6.order.strangler.enabled` |
| ORD-06 | Order | P1 | API interna saga para checkout commit |
| CHK-01 | Checkout | P1 | Orquestração CheckoutApplicationService |
| CHK-02 | Checkout | P1 | Saga choreography para processOrder |
| CHK-03 | Checkout | P1 | Transactional outbox na persistência de order |
| CHK-04 | Checkout | P1 | Pagamento via integration-service |
| CHK-05 | Checkout | P1 | Cotação de frete via integration-service |
| CHK-06 | Checkout | P1 | Clear cart após commit bem-sucedido |
| CHK-07 | Checkout | P1 | Flag rollback saga `wave6.checkout.saga.enabled` |
| CHK-08 | Checkout | P1 | Linhas de tax do BFF (não order-service) |
| CHK-09 | Checkout | P1 | Checkout idempotente com client token |
| CHK-10 | Checkout | P1 | Notificação email via consumer outbox async |
| HUB-01 | Hub | P1 | Decompor caminhos checkout OrderFacadeImpl |
| HUB-02 | Hub | P1 | Rotear bypass APIs pelo checkout |
| HUB-03 | Hub | P1 | Reduzir injeções diretas sm-core |
| HUB-04 | Hub | P1 | Preservar caminhos REST congelados |
| STR-01 | Strangler | P2 | Pact consumer (sm-shop) |
| STR-02 | Strangler | P2 | Pact provider cart |
| STR-03 | Strangler | P2 | Pact provider order |
| STR-04 | Strangler | P2 | Pact checkout commit |
| STR-05 | Ops | P3 | Actuator health por serviço |
| STR-06 | Ops | P3 | Propagação Correlation ID |
| STR-07 | Ops | P3 | Runbook rollback por flag |

---

## Rollout faseado

### Fase 0 — Gate (sem código Wave 6)

- Execute Ondas 3, 4, 5 completo
- PoC saga/outbox verde em `processOrder` no monólito
- Esqueleto `CheckoutApplicationService` merged

### Fase 1 — Contratos + quebra de ciclo (fundação MVP)

- `CartTotalsRequest`/`CartTotalsResponse`, `OrderSnapshot`, clients cart/order
- Properties Strangler Wave 6
- API Totals na fronteira order (no monólito primeiro, depois order-service)

### Fase 2 — Extração ShoppingCart (shadow → cutover)

- `sm-shoppingcart-core`, `shoppingcart-service`
- Strangler shadow: dual-write ou read-remote/write-local conforme ADR-007
- **Marco `SC-ready`**: cart CRUD remoto, totals HTTP

### Fase 3 — Extração order read

- `sm-order-core`, APIs read `order-service`
- **Marco `OR-read-ready`**: order GET/list remoto

### Fase 4 — Cutover saga checkout (maior risco)

- Endpoint saga em `order-service`
- Orquestração completa `CheckoutApplicationService`
- **Marco `CHK-ready`**: place-order via saga com flag rollback

### Fase 5 — Decomposição hub + hardening

- Facades finas, Pact, Docker Compose wave6, atualização STATE

### Plano de rollback

| Flag | Ação de rollback |
|------|-----------------|
| `wave6.checkout.saga.enabled=false` | Reverter para `processOrder` in-process (imediato) |
| `wave6.order.strangler.enabled=false` | Leituras/escritas order no monólito |
| `wave6.shoppingcart.strangler.enabled=false` | Cart no monólito |
| Todas false | Caminho checkout monólito completo — serviços Wave 6 ociosos |

---

## Métricas de sucesso

| Métrica | Meta |
|--------|--------|
| Endpoints P1 disponíveis | Cart + order read + checkout na topologia de integração |
| Paridade de contrato | Pact verde para STR-01…STR-04 |
| Eliminação de ciclos | Zero import `OrderService` em `shoppingcart-service` |
| Redução do hub | `OrderFacadeImpl` ≤ 4 injeções sm-core para checkout (delega a CheckoutApplicationService) |
| Confiabilidade saga | 0 pedidos perdidos em teste chaos (falha payment → compensado) |
| Latência | p95 checkout ≤ 2,5× baseline monólito (aceitável para onda final) |
| Tempo de rollback | < 5 min para desabilitar todas as flags `wave6.*` |

---

## Riscos

| Risco | Mitigação |
|------|------------|
| Falha big-bang de checkout | Flags faseadas; rollback saga; gate CHK-ready antes cutover produção |
| Inconsistência dual-write cart | Shadow mode + job reconciliação; faseamento ADR-007 |
| Lag relay outbox | Monitorar profundidade outbox; alerta > 100 pendentes |
| Divergência tax remoto vs local | BFF dono da chamada tax; fonte única na requisição checkout (ADR-006) |
| integration-service indisponível | Checkout falha rápido 503; sem pedido parcial sem estado de pagamento |
| Conflitos migration DB compartilhado | Coordenação Flyway/Liquibase — order-service dono somente migrações ORDER_* |

---

## Questões em aberto

Todas OQ-01…OQ-08 resolvidas em `context.md`. Sem ambiguidades de produto bloqueantes.

Residual (não bloqueante):

- Formato exato da chave de idempotência para checkout (`Idempotency-Key` header vs body) — decidir em Design T39
- Relay outbox: in-process vs worker standalone — padrão in-process (ADR-023)
