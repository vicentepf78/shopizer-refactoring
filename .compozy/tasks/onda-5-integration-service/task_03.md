---
status: pending
title: Stateless payment ops + P-ready tests
type: backend
complexity: high
---

# Stateless payment ops + P-ready tests

## Overview
Consolidates TLC T9–T11. Implements `process`, `capture`, `refund`, `init` on `PaymentOrchestrator` returning `TransactionResult`; ensures no `OrderService` dependency; achieves P-ready test coverage.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST implement payment operations using `PaymentModuleV2` — T10.
2. MUST persist `Transaction` only — never update Order — T9, T10.
3. MUST return `TransactionResult` DTO for all outcomes including gateway failure — PAY-11.
4. MUST achieve JaCoCo ≥70% on payment orchestrator package — T11.
5. MUST document GAP-INT-05 if credit card regex moved as-is.
</requirements>

## Subtasks
- [ ] 3.1 Remove any OrderService wiring from payment core (T9)
- [ ] 3.2 Implement process/capture/refund/init (T10)
- [ ] 3.3 Integration test with mock gateway (T10)
- [ ] 3.4 P-ready coverage gate (T11)

## Related ADRs
- [ADR-002](adrs/adr-002.md) — stateless payment

## Deliverables
- Complete payment orchestrator operations
- `PaymentOrchestratorIntegrationTest` **(REQUIRED)**
- ArchUnit or static test banning OrderService **(REQUIRED)**

## Success Criteria
- P-ready milestone met
- Transaction saved; Order table untouched in tests
