---
status: pending
title: Merge cart no login + correlation/health Wave6
type: backend
complexity: medium
---

# Merge cart no login + correlation/health Wave6

## Visão geral
TLC T37–T38. Merge de carrinho anônimo no login de cliente; actuator health para deps wave6; correlation ID em todas as chamadas RestTemplate Wave6.

<requirements>
1. MUST implementar mergeAnonymousCart em shoppingcart-service — T37, CART-08.
2. MUST conectar hook login cliente BFF para chamar merge — T37.
3. MUST adicionar health indicators para deps catalog/order/customer/integration — T38, STR-05.
4. MUST propagar X-Correlation-Id em todos os clients wave6 — T38, STR-06.
</requirements>

## Entregáveis
- API merge cart + hook BFF
- Health indicators + testes correlation **(REQUIRED)**

## Critérios de sucesso
- `CartMergeIntegrationTest` verde
- Health mostra DOWN quando dependência indisponível
