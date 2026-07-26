# Resoluções OQ — Design Onda 6 (2026-07-26)

Decisões confirmadas para a onda final de extração. Detalhes em `design.md`.

| ID | Decisão | Escolha |
|----|----------|--------|
| OQ-01 | Quebra do ciclo cart↔order | **API de totals no checkout** — `CartTotalsClient` chama order-service (ou fronteira de checkout) para `OrderTotalSummary`; cart-service NÃO DEVE injetar `OrderService` in-process |
| OQ-02 | Consistência de `processOrder` | **Saga choreography + transactional outbox** (entregável Onda 3) — sem 2PC distribuído; passos compensatórios para falhas de payment/shipping |
| OQ-03 | Tax no checkout | **Permanece no BFF do monólito** no Execute da Onda 6 — `CheckoutApplicationService` chama `tax-service` HTTP; order-service recebe linhas de tax pré-computadas em `OrderSnapshot` (ADR-006) |
| OQ-04 | Hub `OrderFacadeImpl` (12 serviços) | **Decompor em Checkout Application Service + facades finas** — orquestração de checkout sai da facade; caminhos read/update divididos por concern |
| OQ-05 | Ordem de extração | **ShoppingCart primeiro (shadow), Order segundo (saga-ready)** — cutover de cart antes de `processOrder` remoto |
| OQ-06 | DB compartilhado | **Manter MySQL compartilhado** (AD-003 herdado) — split físico de DB adiado pós-Onda 6 |
| OQ-07 | Rollback | **Feature flags por domínio** `wave6.shoppingcart.strangler.enabled`, `wave6.order.strangler.enabled`, `wave6.checkout.saga.enabled` — rollback independente |
| OQ-08 | Bypass APIs (`OrderPaymentApi`, `OrderTotalApi`, `OrderShippingApi`) | **Rotear pelo Checkout Application Service** no BFF — sem injeção direta de sm-core service após decomposição do hub |

**Decisões adicionais de design:**

| ID | Decisão |
|----|----------|
| AD-020 | Um workflow Compozy `onda-6-shoppingcart-order` para ambos os serviços |
| AD-021 | Módulos thin `sm-shoppingcart-core` e `sm-order-core` |
| AD-022 | `shoppingcart-service` :8086, `order-service` :8087 |
| AD-023 | Tabela outbox `ORDER_OUTBOX` de order-service; relay in-process inicialmente |
| AD-024 | `CheckoutApplicationService` vive em `sm-shop` até extração opcional pós-Onda 6 |
| AD-025 | Pact cobre cart CRUD, cart totals, order read, checkout commit (início saga) |

**Pré-requisitos (assumidos completos):**

- Onda 3: `OrderSnapshot`, `CustomerSnapshot`, `ProductLineSnapshot`, esqueleto `CheckoutApplicationService`, PoC saga/outbox em `processOrder`
- Onda 4: `catalog-service` read + `customer-service` com `CustomerSnapshot`
- Onda 5: `integration-service` (payments/shipping stateless); DTOs `PaymentModule` / `ShippingQuoteModule`

**Status:** Pronto para Tasks
