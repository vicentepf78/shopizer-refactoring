---
status: pending
title: CHK-ready runbook + checkout cutover docs
type: docs
complexity: medium
---

# CHK-ready runbook + checkout cutover docs

## Overview
TLC T32, T62. Checkout cutover runbook; staging canary checklist; rollback drill script. Milestone **CHK-ready**.

<requirements>
1. MUST document `docs/runbooks/wave6-checkout-cutover.md` with valid flag combinations — T32, STR-07, ADR-008.
2. MUST configure docker-compose-wave6 staging profile for saga — T32.
3. MUST add `scripts/wave6-rollback-drill.sh` — T62.
4. MUST record CHK-ready in STATE.md.
</requirements>

## Related ADRs
- [ADR-008: Feature flags and rollback](adrs/adr-008.md)

## Deliverables
- Runbooks + rollback drill script
- Staging E2E checklist completed **(REQUIRED manual gate)**

## Success Criteria
- CHK-ready milestone in STATE.md
- Rollback drill completes < 5 min
