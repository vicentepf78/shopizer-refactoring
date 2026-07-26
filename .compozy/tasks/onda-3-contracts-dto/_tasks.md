---
schema_version: "compozy.tasks/v2"
workflow: onda-3-contracts-dto
graph:
  nodes:
    - id: task_01
      file: task_01.md
    - id: task_02
      file: task_02.md
    - id: task_03
      file: task_03.md
    - id: task_04
      file: task_04.md
    - id: task_05
      file: task_05.md
    - id: task_06
      file: task_06.md
    - id: task_07
      file: task_07.md
    - id: task_08
      file: task_08.md
    - id: task_09
      file: task_09.md
    - id: task_10
      file: task_10.md
  edges:
    - from: task_01
      to: task_02
    - from: task_01
      to: task_03
    - from: task_01
      to: task_05
    - from: task_01
      to: task_07
    - from: task_02
      to: task_04
    - from: task_03
      to: task_09
    - from: task_05
      to: task_06
    - from: task_07
      to: task_08
    - from: task_02
      to: task_09
    - from: task_06
      to: task_10
    - from: task_04
      to: task_10
    - from: task_08
      to: task_10
    - from: task_09
      to: task_10
---

# Wave 3 — Contracts DTO + Checkout Application Service Task List

Consolidated task list (mode C) for Wave 3: **10 Compozy tasks** mapped from TLC T1–T48 and TechSpec build order.

**Hard external prerequisite:** Wave 2 Execute (`onda-2-content-search-merchant`) MUST be complete before starting any task — including `task_01`. No new microservices in this wave.

## TLC → Compozy mapping

| Task | Title | TLC | Type | Complexity |
|------|-------|-----|------|------------|
| task_01 | Tenant types and contracts foundation | T1–T6 | backend | medium |
| task_02 | ProductSnapshot and index payload evolution | T7–T12 | backend | high |
| task_03 | OrderSnapshot and CustomerSnapshot DTOs | T13–T16 | backend | medium |
| task_04 | SearchItem migration to api-contracts | T17–T20 | backend | medium |
| task_05 | Integration payment/shipping DTOs | T21–T24 | backend | high |
| task_06 | PaymentModuleV2/ShippingQuoteModuleV2 and bridges | T25–T29 | backend | high |
| task_07 | Facade P1 migration to tenant identifiers | T30–T34 | refactor | high |
| task_08 | ReferencesApi DTO fix and facade migration plan | T35–T38 | backend | medium |
| task_09 | CheckoutApplicationService extraction | T39–T43 | backend | high |
| task_10 | processOrder outbox, gate, and STATE update | T44–T48 | infra | medium |

## Milestones

- **SNP-ready:** end of `task_02` + `task_03` (snapshots compile and serialize).
- **INT-ready:** end of `task_06` (V2 payment path callable in tests).
- **CHK-ready:** end of `task_09` (facade delegates to application service).
- **Wave 3 complete:** end of `task_10` (`./mvnw clean install` green).

## Parallelism

After `task_01` (and Wave 2 gate):

- Track snapshots: `task_02` → `task_04`; `task_03` → `task_09`
- Track integration: `task_05` → `task_06`
- Track facades: `task_07` → `task_08`
- Convergence: `task_09` + `task_04` + `task_06` + `task_08` → `task_10`

## Source

- PRD: `_prd.md`
- TechSpec: `_techspec.md`
- ADRs: `adrs/adr-001.md` … `adr-005.md`
- TLC (authoritative WHAT): `.specs/features/onda-3-contracts-dto/tasks.md`
