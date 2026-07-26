---
status: pending
title: Orquestração CheckoutApplicationService + tax no BFF (CHK-ready parcial)
type: backend
complexity: high
---

# Orquestração CheckoutApplicationService + tax no BFF (CHK-ready parcial)

## Visão geral
TLC T28–T31, T57, T58. `CheckoutApplicationService` completo em sm-shop: tax via tax-service (ADR-006), payment/shipping integration-service, passos saga, roteamento OrderApi.

<requirements>
1. MUST implementar CheckoutApplicationService com todos os clients Wave6 — T28, CHK-01, HUB-01.
2. MUST computar tax no BFF via tax-service; passar taxItems em OrderSnapshot — T29, CHK-08, ADR-006.
3. MUST implementar placeOrder() passos saga 1–8 completos — T30, CHK-01–06.
4. MUST rotear checkout OrderApi pelo service quando flag saga on — T31.
5. MUST conectar validação inventário catalog — T57; consumer outbox email — T58.
</requirements>

## ADRs relacionados
- [ADR-002: Fronteira checkout](adrs/adr-002.md)
- [ADR-006: Tax no BFF](adrs/adr-006.md)

## Entregáveis
- CheckoutApplicationService completo
- `CheckoutPlaceOrderIntegrationTest`, `CheckoutTaxIntegrationTest` **(REQUIRED)**

## Critérios de sucesso
- Caminho feliz E2E em teste de integração
- order-service não chama TaxService
