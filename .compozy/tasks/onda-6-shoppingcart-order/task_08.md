---
status: pending
title: Adaptador Strangler order read (OR-read-ready)
type: backend
complexity: medium
---

# Adaptador Strangler order read (OR-read-ready)

## Visão geral
TLC T21, T49. `OrderFacadeHttpAdapter` para caminhos de leitura; `wave6.order.strangler.enabled`. Marco **OR-read-ready**.

<requirements>
1. MUST delegar GET/list/history de order a order-service quando flag on — T21, ORD-05.
2. MUST manter caminhos checkout/write em CheckoutApplicationService (não neste adaptador).
3. MUST implementar `OrderServiceClientRestTemplateImpl` — T49.
4. MUST registrar OR-read-ready em STATE.md.
</requirements>

## Entregáveis
- Adaptador strangler order + impl client
- `OrderFacadeHttpAdapterTest` **(REQUIRED)**

## Critérios de sucesso
- Caminhos de leitura remotos quando flag on
- Marco OR-read-ready registrado
