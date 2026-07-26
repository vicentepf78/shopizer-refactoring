---
schema_version: "compozy.tasks/v2"
workflow: onda-4-catalog-customer
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
  edges:
    - from: task_01
      to: task_02
    - from: task_02
      to: task_03
    - from: task_03
      to: task_04
    - from: task_01
      to: task_05
    - from: task_05
      to: task_06
    - from: task_06
      to: task_07
    - from: task_01
      to: task_08
    - from: task_08
      to: task_09
    - from: task_04
      to: task_10
    - from: task_07
      to: task_10
    - from: task_09
      to: task_10
    - from: task_10
      to: task_11
    - from: task_10
      to: task_12
    - from: task_11
      to: task_13
    - from: task_12
      to: task_13
    - from: task_09
      to: task_14
    - from: task_13
      to: task_15
    - from: task_14
      to: task_15
---

# Onda 4 — Catalog + Customer Task List

Consolidated list (mode C) for Wave 4: **15 Compozy tasks** mapped from TLC T1–T38 and TechSpec build order.

**External prerequisite (hard gate):** Onda 3 Execute MUST be complete before any task here — including `task_01`. Requires `ProductSnapshot`, `CustomerSnapshot`, `LanguageCode`, `MerchantStoreId` in `shopizer-api-contracts`.

## TLC → task mapping

| Task | Title | TLC | Type | Complexity |
|------|--------|-----|------|--------------|
| task_01 | Contracts catalog/customer snapshots + Wave4 Strangler config | T1–T4 | backend | medium |
| task_02 | Extract sm-catalog-core read services | T5–T8 | backend | high |
| task_03 | catalog-service Boot, clients, public read REST (CAT-ready) | T9–T13 | backend | high |
| task_04 | Extract sm-customer-core | T14–T17 | backend | high |
| task_05 | customer-service Boot, REST, snapshot (CUS-ready) | T18–T20 | backend | high |
| task_06 | ProductSnapshot builder + search v2 migration | T21–T23 | backend | high |
| task_07 | Cart merge decoupling + CustomerFacade orchestration | T24–T25 | backend | high |
| task_08 | Product images via content-service (P2) | T26 | backend | medium |
| task_09 | Strangler catalog + customer HTTP adapters | T27–T30 | backend | high |
| task_10 | Cross-track integration checkpoint | T10,T13,T20 | backend | medium |
| task_11 | Correlation ID + health indicators Wave4 | T31 | infra | medium |
| task_12 | JaCoCo verify gates Wave4 modules | T32 | test | low |
| task_13 | Pact providers + Wave4ConsumerPactTest | T33–T34 | test | medium |
| task_14 | ProductFacadeV2 + wiring guards (admin writes local) | T29–T30 | backend | medium |
| task_15 | Docker Compose wave4, gate, STATE | T35–T38 | infra | medium |

## Milestones

- **CAT-ready:** end of `task_03` (public catalog read + internal ProductSnapshot).
- **CUS-ready:** end of `task_05` (customer profile REST + internal CustomerSnapshot).
- Do not start `task_06` (search migration) before CAT-ready + contracts from `task_01`.
- Do not start `task_07` (cart merge) before CUS-ready.
- Do not start strangler adapters (`task_09`) before `task_10` checkpoint.

## Parallelism

After `task_01` (and Onda 3 gate):

- Track catalog: `task_02` → `task_03`
- Track customer: `task_04` → `task_05` (fan-out from `task_01`)
- Converge: `task_10` → `task_06` + `task_07` (merge/search can parallel after checkpoint)
- Strangler: `task_09` + `task_14` after `task_10`
- Tail: `task_11` + `task_12` → `task_13` → `task_15`; `task_08` parallel after CAT-ready

## Source

- PRD: `_prd.md`
- TechSpec: `_techspec.md`
- ADRs: `adrs/adr-001.md` … `adr-007.md`
- TLC (reference WHAT): `.specs/features/onda-4-catalog-customer/tasks.md` — do not modify from Compozy execute without TLC review
