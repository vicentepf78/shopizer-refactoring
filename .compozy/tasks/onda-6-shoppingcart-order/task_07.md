---
status: pending
title: order-service Boot + read REST + internal totals (OR-read-ready parcial)
type: backend
complexity: high
---

# order-service Boot + read REST + internal totals (OR-read-ready parcial)

## Visão geral
TLC T18–T20, T53, T55. Spring Boot :8087; APIs públicas de leitura de order; endpoint interno de totals; JWT + filtro internal token.

<requirements>
1. MUST fazer scaffold order-service :8087 — T18.
2. MUST implementar GET order, list, status history — T19, ORD-01–03.
3. MUST implementar `POST /internal/v1/orders/totals` — T20; aposentar endpoint somente sm-shop da task_02.
4. MUST adicionar mappers ReadableOrder — T53; JWT — T55.
</requirements>

## Subtarefas
- [ ] 7.1 Boot app (T18)
- [ ] 7.2 REST read público (T19, T53)
- [ ] 7.3 API internal totals (T20)
- [ ] 7.4 Config security (T55)

## Entregáveis
- JAR implantável `order-service`
- `OrderReadApiIntegrationTest`, `InternalTotalsControllerTest` **(REQUIRED)**

## Critérios de sucesso
- shoppingcart-service pode chamar totals em order-service
- Sem JPA em respostas JSON
