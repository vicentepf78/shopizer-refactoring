---
status: pending
title: Wiring HTTP checkout + OrderShipping
type: backend
complexity: high
---

# Wiring HTTP checkout + OrderShipping

## Visão geral
Consolida TLC T27–T28. Conecta `OrderPaymentApi` via `CheckoutApplicationService` → `IntegrationServiceClient`; conecta `OrderShippingApi` para montar `ShippingQuoteRequest` a partir de snapshots de carrinho + catálogo.

<critical>
- SEMPRE LER o PRD e a TechSpec antes de iniciar
- Depende da fronteira stateless task_07 e adaptadores task_08
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST rotear processamento de pagamento de `OrderPaymentApi` pelo checkout service — T27, STR-03.
2. MUST montar `PaymentProcessRequest` a partir de snapshots order/cart/customer — PAY-07.
3. MUST rotear `OrderShippingApi` pelo client integration com montagem DTO — T28, STR-04.
4. MUST preservar schemas de resposta (`ReadableTransaction`, `ReadableShippingSummary`).
5. MUST teste E2E pagamento checkout com integration-service mock — T27.
</requirements>

## Subtarefas
- [ ] 9.1 Injeção client integration em CheckoutApplicationService (T27)
- [ ] 9.2 Rewire OrderPaymentApi (T27)
- [ ] 9.3 Builder DTO OrderShippingApi (T28)
- [ ] 9.4 Testes E2E com TestRestTemplate ou WireMock

## ADRs relacionados
- [ADR-006](adrs/adr-006.md)
- [ADR-002](adrs/adr-002.md)

## Entregáveis
- Wiring Checkout + OrderShipping
- `CheckoutPaymentE2ETest` **(REQUIRED)**
- `OrderShippingQuoteE2ETest` **(REQUIRED)**

## Critérios de sucesso
- Fluxo de pagamento atualiza pedido apenas na saga checkout, não no caminho do client integration
- Cotação de frete retorna opções no profile strangler
