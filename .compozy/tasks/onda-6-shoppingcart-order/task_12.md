---
status: pending
title: Decomposição hub — OrderFacade fino + bypass APIs
type: refactor
complexity: high
---

# Decomposição hub — OrderFacade fino + bypass APIs

## Visão geral
TLC T33–T36. Rotear OrderPaymentApi, OrderTotalApi, OrderShippingApi pelo CheckoutApplicationService; `OrderFacadeImpl` fino; consolidar pacotes duplicados OrderFacadeImpl.

<requirements>
1. MUST remover PaymentService direto de OrderPaymentApi — T33, HUB-02.
2. MUST rotear OrderTotalApi e OrderShippingApi pelo checkout — T34.
3. MUST reduzir injeções sm-core checkout de OrderFacadeImpl para ≤4 — T35, HUB-01, HUB-03.
4. MUST consolidar duplicata v1 OrderFacadeImpl — T36, GAP-ORD-01.
</requirements>

## ADRs relacionados
- [ADR-005: Decomposição hub](adrs/adr-005.md)

## Entregáveis
- Hub facades e bypass APIs refatorados
- `OrderFacadeThinTest`, testes de roteamento **(REQUIRED)**

## Critérios de sucesso
- ArchUnit ou checagem estática: sem PaymentService em OrderPaymentApi
- Testes de caracterização passam
