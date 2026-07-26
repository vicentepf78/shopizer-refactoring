---
status: pending
title: Extrair sm-order-core + serviço cart totals
type: backend
complexity: high
---

# Extrair sm-order-core + serviço cart totals

## Visão geral
TLC T15–T17. Novo `sm-order-core` com repositórios order, serviços de leitura e `CartTotalsService` (totais stateless a partir de `CartTotalsRequest`). Mover lógica de totais da fronteira task_02 para order-core.

<requirements>
1. MUST fazer scaffold sm-order-core com repositórios order — T15.
2. MUST extrair serviços de leitura order (get/list/history) sem PaymentService — T16, ORD-01–03.
3. MUST implementar CartTotalsService com testes de paridade — T17, CART-03.
4. MUST manter linhas de tax como input na requisição de totais (pré-computadas opcional) conforme ADR-006.
</requirements>

## Subtarefas
- [ ] 6.1 Módulo + repositórios (T15)
- [ ] 6.2 OrderReadService (T16)
- [ ] 6.3 CartTotalsService (T17)

## ADRs relacionados
- [ADR-006: Tax no BFF](adrs/adr-006.md)

## Entregáveis
- Módulo `sm-order-core`
- `OrderReadServiceTest`, `CartTotalsServiceTest` **(REQUIRED)**

## Testes
- `./mvnw test -pl sm-order-core`

## Critérios de sucesso
- Paridade de totais vs legado
- Sem PaymentService no caminho de leitura
