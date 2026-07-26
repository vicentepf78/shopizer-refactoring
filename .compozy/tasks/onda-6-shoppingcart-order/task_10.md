---
status: pending
title: Legacy processOrder saga delegation + compensation tests
type: backend
complexity: high
---

# Legacy processOrder saga delegation + compensation tests

## Overview
TLC T26–T27, T59. Refactor `OrderServiceImpl.processOrder` to delegate when `wave6.checkout.saga.enabled`; narrow global AOP pointcut; compensation tests + chaos test.

<requirements>
1. MUST preserve legacy processOrder when saga flag false — T26, CHK-07.
2. MUST narrow TransactionalAspectAwareService pointcut for checkout — GAP-CHK-02.
3. MUST pass SagaCompensationTest (payment fail → order CANCELLED, cart retained) — T27.
4. MUST add chaos test killing integration mid-saga — T59.
</requirements>

## Related ADRs
- [ADR-003: Saga](adrs/adr-003.md)
- [ADR-008: Rollback flag](adrs/adr-008.md)

## Deliverables
- OrderService saga delegation
- `SagaCompensationTest`, chaos test **(REQUIRED)**

## Success Criteria
- Both flag paths tested green
- Compensation leaves no orphan paid orders
