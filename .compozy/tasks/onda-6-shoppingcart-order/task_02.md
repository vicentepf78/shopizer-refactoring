---
status: pending
title: API cart totals — quebra ciclo cart↔order (TOT-ready)
type: backend
complexity: high
---

# API cart totals — quebra ciclo cart↔order (TOT-ready)

## Visão geral
TLC T6, T56. Extrair `calculateShoppingCartTotal` para `CartTotalsService`; expor `POST /internal/v1/orders/totals`; conectar `ShoppingCartCalculationServiceImpl` a HTTP quando `wave6.totals.http.enabled=true`. Marco **TOT-ready**.

<requirements>
1. MUST quebrar chamada in-process `ShoppingCartCalculationServiceImpl` → `OrderService.calculateShoppingCartTotal` quando flag on — CART-03, OQ-01.
2. MUST aceitar `CartTotalsRequest` e retornar `CartTotalsResponse` com semântica de `OrderTotalSummary`.
3. MUST preservar paridade byte-a-byte com totais legados em teste de integração.
4. MUST proteger endpoint interno com `X-Internal-Token`.
5. MUST marcar TOT-ready em STATE.md quando completo.
</requirements>

## Subtarefas
- [ ] 2.1 Extrair `CartTotalsService` da lógica OrderServiceImpl (T6)
- [ ] 2.2 `CartTotalsController` interno em sm-shop ou sm-order-core (T6)
- [ ] 2.3 Wiring client HTTP em ShoppingCartCalculationServiceImpl (T6)
- [ ] 2.4 Property `wave6.totals.http.enabled` + testes (T56)

## Detalhes de implementação
Fonte: `sm-core/.../shoppingcart/ShoppingCartCalculationServiceImpl.java` linha 73; `OrderServiceImpl.calculateShoppingCartTotal`.

### Arquivos relevantes
- `sm-core/.../order/OrderServiceImpl.java`
- `sm-core/.../shoppingcart/ShoppingCartCalculationServiceImpl.java`

### ADRs relacionados
- [ADR-007: Faseamento cart antes de order](adrs/adr-007.md)

## Entregáveis
- CartTotalsService + API interna + wiring de flag
- `CartTotalsParityTest` **(REQUIRED)**

## Testes
- Integração: `./mvnw test -pl sm-shop -Dtest=CartTotalsParityTest`

## Critérios de sucesso
- Teste de paridade verde com flag on/off
- Marco TOT-ready registrado
