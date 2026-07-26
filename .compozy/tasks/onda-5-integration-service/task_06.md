---
status: pending
title: Public + internal REST APIs (I-ready)
type: backend
complexity: high
---

# Public + internal REST APIs (I-ready)

## Overview
Consolidates TLC T20–T22. Adds public payment methods endpoint, internal payment and shipping APIs with `X-Internal-Token` filter. Achieves **I-ready** milestone.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST expose public accepted payment methods — T20, PAY-04.
2. MUST expose `/internal/v1/payments/*` with token auth — T21, PAY-07..12.
3. MUST expose `/internal/v1/shipping/quote` and `/summary` — T22, SHP-01..07.
4. MUST NOT register Order JPA repository in integration-service.
5. MUST map gateway errors to `TransactionResult` or 502 per TechSpec.
6. MUST pass integration test: process payment without Order UPDATE — I-ready.
</requirements>

## Subtasks
- [ ] 6.1 Public payment methods controller (T20)
- [ ] 6.2 Internal payment controller + token filter (T21)
- [ ] 6.3 Internal shipping controller (T22)
- [ ] 6.4 I-ready integration tests

## Related ADRs
- [ADR-002](adrs/adr-002.md)
- [ADR-006](adrs/adr-006.md)

## Deliverables
- Internal + public API controllers
- `InternalPaymentIntegrationTest` asserting no Order writes **(REQUIRED)**
- Token filter tests **(REQUIRED)**

## Success Criteria
- I-ready milestone met
- Health endpoint UP on :8086
