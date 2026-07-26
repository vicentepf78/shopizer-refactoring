---
status: pending
title: Delegação saga processOrder legado + testes compensação
type: backend
complexity: high
---

# Delegação saga processOrder legado + testes compensação

## Visão geral
TLC T26–T27, T59. Refatorar `OrderServiceImpl.processOrder` para delegar quando `wave6.checkout.saga.enabled`; estreitar pointcut AOP global; testes de compensação + teste de chaos.

<requirements>
1. MUST preservar processOrder legado quando flag saga false — T26, CHK-07.
2. MUST estreitar pointcut TransactionalAspectAwareService para checkout — GAP-CHK-02.
3. MUST passar SagaCompensationTest (falha payment → order CANCELLED, cart retido) — T27.
4. MUST adicionar teste de chaos matando integration mid-saga — T59.
</requirements>

## ADRs relacionados
- [ADR-003: Saga](adrs/adr-003.md)
- [ADR-008: Flag rollback](adrs/adr-008.md)

## Entregáveis
- Delegação saga OrderService
- `SagaCompensationTest`, teste chaos **(REQUIRED)**

## Critérios de sucesso
- Ambos caminhos de flag testados verdes
- Compensação não deixa pedidos pagos órfãos
