---
schema_version: "compozy.tasks/v2"
workflow: onda-6-shoppingcart-order
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
    - id: task_11
      file: task_11.md
    - id: task_12
      file: task_12.md
    - id: task_13
      file: task_13.md
    - id: task_14
      file: task_14.md
    - id: task_15
      file: task_15.md
    - id: task_16
      file: task_16.md
  edges:
    - from: task_01
      to: task_02
    - from: task_02
      to: task_03
    - from: task_03
      to: task_04
    - from: task_04
      to: task_05
    - from: task_05
      to: task_06
    - from: task_05
      to: task_07
    - from: task_06
      to: task_08
    - from: task_07
      to: task_09
    - from: task_08
      to: task_10
    - from: task_09
      to: task_11
    - from: task_10
      to: task_12
    - from: task_11
      to: task_12
    - from: task_12
      to: task_13
    - from: task_12
      to: task_14
    - from: task_13
      to: task_15
    - from: task_14
      to: task_15
    - from: task_15
      to: task_16
---

# Onda 6 — ShoppingCart + Order Task List

Consolidated Wave 6 task list: **16 Compozy tasks** mapped from TLC T1–T62 and TechSpec build order.

**Hard prerequisite:** Ondas 3, 4, 5 Execute MUST be complete before `task_01` — snapshots, saga/outbox PoC, `CheckoutApplicationService` skeleton, catalog-service, customer-service, integration-service.

## TLC → Compozy mapping

| Task | Title | TLC | Type | Complexity |
|------|-------|-----|------|------------|
| task_01 | Gate Ondas 3–5 + Wave6 contracts and Strangler config | T1–T5, T46–T51 | backend | medium |
| task_02 | Cart totals API — break cart↔order cycle (`TOT-ready`) | T6, T56 | backend | high |
| task_03 | Extract sm-shoppingcart-core + catalog validation | T7–T9, T60 | backend | high |
| task_04 | shoppingcart-service Boot, REST, internal clear (`SC-ready` partial) | T10–T12, T52, T54 | backend | high |
| task_05 | ShoppingCart Strangler adapter + shadow mode (`SC-ready`) | T13–T14, T50 | backend | high |
| task_06 | Extract sm-order-core + cart totals service | T15–T17 | backend | high |
| task_07 | order-service Boot + read REST + internal totals (`OR-read-ready` partial) | T18–T20, T53, T55 | backend | high |
| task_08 | Order read Strangler adapter (`OR-read-ready`) | T21, T49 | backend | medium |
| task_09 | ORDER_OUTBOX schema + relay + saga commit endpoints | T22–T25, T61 | backend | high |
| task_10 | Legacy processOrder saga delegation + compensation tests | T26–T27, T59 | backend | high |
| task_11 | CheckoutApplicationService orchestration + tax at BFF (`CHK-ready` partial) | T28–T31, T57, T58 | backend | high |
| task_12 | Hub decomposition — thin OrderFacade + bypass APIs | T33–T36 | refactor | high |
| task_13 | Cart merge on login + correlation/health Wave6 | T37–T38 | backend | medium |
| task_14 | CHK-ready runbook + checkout cutover docs | T32, T62 | docs | medium |
| task_15 | Pact consumer/provider + JaCoCo Wave6 gates | T39–T42 | test | medium |
| task_16 | Docker Compose wave6, E2E, STATE/ROADMAP gate | T43–T45 | infra | medium |

## Milestones

- **TOT-ready:** end of `task_02` — totals HTTP live
- **SC-ready:** end of `task_05` — cart CRUD remote + strangler
- **OR-read-ready:** end of `task_08` — order reads remote
- **CHK-ready:** end of `task_14` — saga checkout + runbook

## Parallelism

After `task_05` (`TOT-ready` + cart track started):

- Track cart: `task_03` → `task_04` → `task_05`
- Track order: `task_06` → `task_07` → `task_08` (starts after `task_02`)

After `task_08` and `task_05`:

- Saga: `task_09` → `task_10`
- Checkout: `task_11` (needs `task_09`, `task_05`)
- Hub: `task_12` (needs `task_11`)

Tail: `task_13`, `task_14` → `task_15` → `task_16`

## Source

- PRD: `_prd.md`
- TechSpec: `_techspec.md`
- ADRs: `adrs/adr-001.md` … `adr-008.md`
- TLC: `.specs/features/onda-6-shoppingcart-order/tasks.md` — authoritative WHAT
