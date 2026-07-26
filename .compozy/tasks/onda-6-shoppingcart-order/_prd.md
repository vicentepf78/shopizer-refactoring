# PRD: Onda 6 — ShoppingCart + Order

**Slug da feature:** `onda-6-shoppingcart-order`
**Fonte da verdade:** TLC em `.specs/features/onda-6-shoppingcart-order/` (Opção A — autoritativa; escopo congelado)
**Status:** Pronto para TechSpec
**Data:** 2026-07-26

---

## Visão geral

Após as Ondas 1–5 estabelecerem padrões Strangler, snapshots DTO, serviços catalog/customer/integration e saga/outbox em `processOrder`, a Onda 6 extrai os **últimos e mais arriscados domínios**: shopping cart e order. Visitantes da vitrine, clientes concluindo checkout e admins de loja devem continuar usando os caminhos REST existentes enquanto a persistência do carrinho e o ciclo de vida do pedido migram para runtimes dedicados.

Hoje order é o orquestrador central (acoplamento **9/10**): `OrderFacadeImpl` injeta **12 serviços sm-core**, `processOrder` roda em transação global AOP, e dois ciclos críticos bloqueiam extração ingênua — **order↔payments** e **order↔shoppingcart** (`ShoppingCartCalculationServiceImpl` → `OrderService.calculateShoppingCartTotal`). Sem especificação faseada, os times enfrentam cutover big-bang de checkout sem rollback.

Este PRD define o **o quê e o porquê de negócio** de `shoppingcart-service` e `order-service`, com **Checkout Application Service** como fronteira de checkout no BFF (entregável da Onda 3). O como técnico fica para a TechSpec e os ADRs.

**Usuários primários:** compradores da vitrine, clientes em checkout, admins de loja visualizando pedidos, engenheiros de plataforma operando cutover faseado e rollback.

---

## Objetivos

- Entregar **shoppingcart-service** e **order-service** como capabilities independentemente implantáveis atrás do BFF existente.
- Preservar jornadas de CRUD de carrinho, leitura/listagem/histórico de pedidos e place-order com contratos REST equivalentes.
- **Quebrar o ciclo cart↔order** via API HTTP de totais — o domínio de carrinho não deve chamar `OrderService` in-process.
- Executar checkout como **saga choreography + transactional outbox** (Onda 3), não transação monolítica de DB.
- **Decompor o hub de checkout** — `OrderFacadeImpl` de 12 serviços colapsa em `CheckoutApplicationService` + facades finas.
- **Faseamento, feature flags e rollback** explícitos por domínio (`wave6.shoppingcart`, `wave6.order`, `wave6.checkout.saga`).
- Testes de contrato (Pact) para superfícies P1 antes de declarar a Onda 6 concluída.
- **Bloqueado até o Execute das Ondas 3, 4, 5 estar completo.**

### Resultados de negócio

| Resultado | Indicador |
|---------|-----------|
| Decomposição do monólito completa | Cart + order fora do runtime sm-core |
| Caminho de receita protegido | Checkout saga com compensação; rollback < 5 min |
| Eliminação de ciclos | Sem dependência in-process order↔cart nos serviços extraídos |
| Manutenibilidade do hub | Orquestração de checkout em um application service |
| Segurança operacional | Rollback por flag; runbooks para marcos SC/OR/CHK |

---

## Histórias de usuário

### Visitante da vitrine — shopping cart (P1 / CART)

Como **visitante da vitrine**, quero gerenciar meu carrinho via endpoints `/api/v1/cart` existentes, para preparar o checkout quando o carrinho rodar fora do monólito.

**Aceite (negócio):**

1. Adicionar/atualizar/remover itens de linha com escopo de loja e sessão/cliente.
2. Validação de produto usa capability de catalog (HTTP) — não grafo JPA de produto do monólito.
3. Totais do carrinho vêm da capability de order via HTTP — não cálculo in-process de order.
4. Flag Strangler desligada preserva comportamento legado in-process.
5. Falha remota retorna 503 claro com correlation id — sem fallback silencioso.

**IDs de requisito:** CART-01…CART-07

### Cliente — place order (P1 / CHK)

Como **cliente**, quero concluir checkout com pagamento e frete, para que minha compra tenha sucesso quando `processOrder` atravessa serviços remotos.

**Aceite:**

1. Checkout orquestrado por Checkout Application Service no BFF.
2. Pagamento e frete via integration-service (Onda 5).
3. Imposto calculado no BFF via tax-service; order armazena linhas de imposto pré-computadas.
4. Falha na saga compensa (cancelar pedido, void de pagamento quando possível, carrinho não limpo).
5. Pedido bem-sucedido publica eventos via transactional outbox.
6. Flag de saga desligada reverte para `processOrder` legado in-process.

**IDs de requisito:** CHK-01…CHK-10

### Admin de loja — visualizar pedidos (P1 / ORD)

Como **admin de loja**, quero listar e visualizar pedidos e histórico de status via APIs existentes, para que leituras de pedido funcionem quando a persistência é remota.

**IDs de requisito:** ORD-01…ORD-06

### Engenheiro de plataforma — decomposição do hub (P1 / HUB)

Como **engenheiro de plataforma**, quero que APIs de checkout deleguem ao Checkout Application Service em vez de 12 serviços sm-core, para que o BFF seja mantível após a extração.

**IDs de requisito:** HUB-01…HUB-04

### Engenheiro de plataforma — rollout faseado (P1 / STR)

Como **engenheiro de plataforma**, quero feature flags independentes e runbooks de rollback para carrinho, leitura de pedido e checkout saga.

**IDs de requisito:** STR-05…STR-07, CART-04, ORD-05, CHK-07

### Desenvolvedor — confiança de contrato (P2 / STR)

Como **desenvolvedor**, quero testes Pact para carrinho, leitura de pedido, totais e checkout commit.

**IDs de requisito:** STR-01…STR-04

### Cliente recorrente — merge de carrinho (P2 / CART)

Como **cliente recorrente**, quero meu carrinho anônimo mesclado no login.

**IDs de requisito:** CART-08

---

## Capabilities principais

### F1 — ShoppingCart service (MVP)

Dono da persistência do carrinho e CRUD de itens de linha; valida produtos via HTTP de catalog; obtém totais via HTTP de order-service. Adaptador Strangler no BFF. Marco **SC-ready**.

### F2 — Order service read path (MVP)

Dono da persistência de pedidos para GET/list/history; respostas somente DTO. Adaptador Strangler. Marco **OR-read-ready**.

### F3 — Saga checkout (MVP — maior risco)

Endpoint de commit em order-service + outbox; BFF orquestra payment/shipping/tax/clear do carrinho. Marco **CHK-ready**.

### F4 — Checkout Application Service (MVP)

Fronteira única no BFF para place-order, payment, totals, cotações de frete; substitui injeções do hub.

### F5 — Decomposição do hub (MVP)

Rotear `OrderPaymentApi`, `OrderTotalApi`, `OrderShippingApi` pelo checkout service; `OrderFacadeImpl` fino.

### F6 — Contratos, Pact, Docker, runbooks (Fase 2–3)

Topologia Wave 6; drill de rollback; atualização de STATE.

---

## UX

| Persona | Objetivo |
|---------|------|
| Comprador | Carrinho e checkout inalterados na perspectiva da UI |
| Admin | Gestão de pedidos inalterada |
| Plataforma | Alternar flags; observar health; executar runbooks de rollback |

**Restrição:** Sem novas telas de vitrine/admin — apenas paridade comportamental. p95 checkout ≤ 2,5× baseline do monólito.

---

## Restrições técnicas de alto nível

- Integrar com capabilities das Ondas 1–5 (reference, tax, merchant, catalog, customer, integration).
- Preservar caminhos REST congelados (HUB-04).
- Sem entidades JPA em respostas JSON migradas.
- DB operacional compartilhado durante a extração (AD-003 herdado).
- Execute bloqueado até Ondas 3–5 completas.
- JWT em `/private/**` equivalente ao de hoje.

---

## Não-objetivos

| Excluído | Motivo |
|----------|--------|
| Execute antes das Ondas 3–5 | Pré-requisito rígido — snapshots, saga, catalog, customer, integration |
| Split físico DB-per-service | Pós-Onda 6 |
| Tax remoto dentro de order-service | ADR-006 — BFF dono do tax na Onda 6 |
| Extrair CheckoutApplicationService para deployable próprio | AD-024 — opcional pós-onda |
| Catalog write / ProductType | Escopo Onda 4 |
| Depreciação API V1 | Fase 4 |
| Reescrever ~450 refs do resolver MerchantStore | BFF mantém resolver |
| Split greenfield de bootstrap de DB | Assume DB populado |

---

## Rollout faseado

### Fase 0 — Gate

Ondas 3–5 verdes; PoC saga em `processOrder`; esqueleto CheckoutApplicationService.

### Fase 1 — MVP (P1)

- Totals HTTP (quebra de ciclo) — **TOT-ready**
- shoppingcart-service + strangler — **SC-ready**
- order-service reads + strangler — **OR-read-ready**
- Saga checkout + decomposição do hub — **CHK-ready**

### Fase 2

Suite Pact; Docker Compose wave6; gates JaCoCo.

### Fase 3

Health/correlation; runbooks de rollback; STATE.md; testes de chaos.

### Rollback

| Flag | Ação |
|------|--------|
| `wave6.checkout.saga.enabled=false` | processOrder legado (imediato) |
| `wave6.order.strangler.enabled=false` | Order in-process |
| `wave6.shoppingcart.strangler.enabled=false` | Cart in-process |

---

## Métricas de sucesso

| Métrica | Meta |
|--------|--------|
| Integração P1 | Cart + order read + checkout na topologia wave6 |
| Pact | Verde STR-01…04 |
| Ciclos | Zero OrderService em shoppingcart-service |
| Hub | OrderFacade checkout ≤ 4 deps sm-core diretas |
| Saga | Compensação verde em teste de chaos |
| Rollback | < 5 min com todas as flags false |

---

## Riscos

| Risco | Mitigação |
|------|------------|
| Falha big-bang de checkout | Flags faseadas; gate CHK-ready; canary |
| Inconsistência de carrinho em shadow | Shadow compare + reconciliação |
| Lag do outbox | Monitorar profundidade; alerta |
| Divergência de tax | Chamada única de tax no BFF (ADR-006) |
| Atraso Ondas 3–5 | Docs prontas; código bloqueado |

---

## Registros de decisão arquitetural

- [ADR-001: Um workflow Compozy para ShoppingCart + Order](adrs/adr-001.md)
- [ADR-002: Checkout Application Service como fronteira do BFF](adrs/adr-002.md)
- [ADR-003: Saga choreography para processOrder](adrs/adr-003.md)
- [ADR-004: Transactional outbox para eventos de order](adrs/adr-004.md)
- [ADR-005: Decomposição do hub OrderFacade](adrs/adr-005.md)
- [ADR-006: Cálculo de tax no BFF (adiado em order-service)](adrs/adr-006.md)
- [ADR-007: Faseamento ShoppingCart antes de Order cutover](adrs/adr-007.md)
- [ADR-008: Feature flags e plano de rollback](adrs/adr-008.md)

---

## Questões em aberto

Todas OQ-01…OQ-08 resolvidas em `.specs/features/onda-6-shoppingcart-order/context.md`. Sem ambiguidades bloqueantes.

Residual: formato do header de chave de idempotência (Design T39); relay outbox in-process vs worker (padrão ADR-004 in-process).
