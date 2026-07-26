---
status: pending
title: Cross-track integration checkpoint
type: backend
complexity: medium
---

# Cross-track integration checkpoint

## Overview
Consolidates convergence after CAT-ready (task_03) and CUS-ready (task_05). Verifies both services healthy together with Wave 1–2 dependencies before search migration and cart merge tasks proceed in parallel.

<requirements>
1. MUST verify catalog-service + customer-service start against shared Testcontainers MySQL.
2. MUST smoke: catalog GET product + customer GET snapshot internal APIs.
3. MUST verify reference-service + merchant-service reachable from catalog-service.
4. MUST document any parity gaps found vs monolith baseline.
5. MUST unblock task_06 and task_07 parallel execution after checkpoint pass.
</requirements>

## Deliverables
- `Wave4ServicesCheckpointTest` or equivalent **(REQUIRED)**
- Short checkpoint report in task memory (if using Compozy memory)

## Tests
- `./mvnw test -pl catalog-service,customer-service -Dtest=*Checkpoint*Test`

## Success Criteria
- Both services UP with deps
- CAT-ready + CUS-ready confirmed
- No blocker defects for downstream tasks
