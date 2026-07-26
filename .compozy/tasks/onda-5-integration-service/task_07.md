---
status: pending
title: Trim sm-core + stateless monolith boundary
type: backend
complexity: medium
---

# Trim sm-core + stateless monolith boundary

## Overview
Consolidates TLC T23–T24. Trims or delegates `PaymentServiceImpl`/`ShippingServiceImpl` in sm-core; removes `orderService.saveOrUpdate` from payment path when `wave5.strangler.enabled`; ensures checkout saga owns order updates.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- Requires Onda 3 checkout saga to be functional
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST delegate or deprecate in-process payment/shipping services when extracted — T23.
2. MUST gate order writes behind `!wave5.strangler.enabled` for rollback — T24, ADR-017.
3. MUST keep monolith compiles and non-strangler tests green — T23.
4. MUST add test proving strangler payment does not call PaymentServiceImpl order save — T24.
</requirements>

## Subtasks
- [ ] 7.1 Trim sm-core services + update wiring (T23)
- [ ] 7.2 Stateless boundary flag + test (T24)
- [ ] 7.3 Update spring XML / component scan if needed

## Related ADRs
- [ADR-002](adrs/adr-002.md)
- [ADR-017](adrs/adr-017.md) in design.md

## Deliverables
- sm-core delegation stubs or conditional beans
- `StatelessPaymentBoundaryTest` in sm-shop **(REQUIRED)**

## Success Criteria
- `./mvnw test -pl sm-core,sm-shop -DfailIfNoTests=false` passes
- Strangler profile test shows no Order update from PaymentServiceImpl
