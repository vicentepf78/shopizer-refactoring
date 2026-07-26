---
status: pending
title: Schema ORDER_OUTBOX + relay + endpoints saga commit
type: backend
complexity: high
---

# Schema ORDER_OUTBOX + relay + endpoints saga commit

## Visão geral
TLC T22–T25, T61. Tabela transactional outbox; scheduler relay; `POST /internal/v1/checkout/commit` com idempotência; `PATCH` payment status. ArchUnit: sem PaymentService no caminho de commit.

<requirements>
1. MUST criar migration ORDER_OUTBOX + repository — T22, CHK-03.
2. MUST implementar scheduler relay outbox — T23, CHK-10.
3. MUST implementar checkout commit com outbox OrderPlaced na mesma TX — T24, CHK-02, CHK-09.
4. MUST implementar atualização payment status + eventos OrderPaid/OrderCancelled — T25.
5. MUST ArchUnit: sem PaymentService no pacote commit de order-service — T61.
</requirements>

## ADRs relacionados
- [ADR-003: Saga](adrs/adr-003.md)
- [ADR-004: Outbox](adrs/adr-004.md)

## Entregáveis
- Outbox + APIs internas saga
- `CheckoutCommitIntegrationTest`, `OrderOutboxRelayTest`, teste ArchUnit **(REQUIRED)**

## Critérios de sucesso
- Commit idempotente verificado
- Relay outbox publica eventos
