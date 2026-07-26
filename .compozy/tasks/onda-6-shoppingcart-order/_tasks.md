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

Lista consolidada da Onda 6: **16 tasks Compozy** mapeadas a partir das TLC T1–T62 e da ordem de construção da TechSpec.

**Pré-requisito rígido:** Execute das Ondas 3, 4, 5 DEVE estar completo antes de `task_01` — snapshots, PoC saga/outbox, esqueleto `CheckoutApplicationService`, catalog-service, customer-service, integration-service.

## Mapeamento TLC → Compozy

| Task | Título | TLC | Tipo | Complexidade |
|------|-------|-----|------|------------|
| task_01 | Gate Ondas 3–5 + contratos Wave6 e config Strangler | T1–T5, T46–T51 | backend | medium |
| task_02 | API cart totals — quebra ciclo cart↔order (`TOT-ready`) | T6, T56 | backend | high |
| task_03 | Extrair sm-shoppingcart-core + validação catalog | T7–T9, T60 | backend | high |
| task_04 | shoppingcart-service Boot, REST, internal clear (`SC-ready` parcial) | T10–T12, T52, T54 | backend | high |
| task_05 | Adaptador Strangler ShoppingCart + shadow mode (`SC-ready`) | T13–T14, T50 | backend | high |
| task_06 | Extrair sm-order-core + serviço cart totals | T15–T17 | backend | high |
| task_07 | order-service Boot + read REST + internal totals (`OR-read-ready` parcial) | T18–T20, T53, T55 | backend | high |
| task_08 | Adaptador Strangler order read (`OR-read-ready`) | T21, T49 | backend | medium |
| task_09 | Schema ORDER_OUTBOX + relay + endpoints saga commit | T22–T25, T61 | backend | high |
| task_10 | Delegação saga processOrder legado + testes compensação | T26–T27, T59 | backend | high |
| task_11 | Orquestração CheckoutApplicationService + tax no BFF (`CHK-ready` parcial) | T28–T31, T57, T58 | backend | high |
| task_12 | Decomposição hub — OrderFacade fino + bypass APIs | T33–T36 | refactor | high |
| task_13 | Merge cart no login + correlation/health Wave6 | T37–T38 | backend | medium |
| task_14 | Runbook CHK-ready + docs cutover checkout | T32, T62 | docs | medium |
| task_15 | Pact consumer/provider + gates JaCoCo Wave6 | T39–T42 | test | medium |
| task_16 | Docker Compose wave6, E2E, gate STATE/ROADMAP | T43–T45 | infra | medium |

## Marcos

- **TOT-ready:** fim de `task_02` — totals HTTP live
- **SC-ready:** fim de `task_05` — cart CRUD remoto + strangler
- **OR-read-ready:** fim de `task_08` — leituras de order remotas
- **CHK-ready:** fim de `task_14` — checkout saga + runbook

## Paralelismo

Após `task_05` (`TOT-ready` + trilha cart iniciada):

- Trilha cart: `task_03` → `task_04` → `task_05`
- Trilha order: `task_06` → `task_07` → `task_08` (inicia após `task_02`)

Após `task_08` e `task_05`:

- Saga: `task_09` → `task_10`
- Checkout: `task_11` (precisa `task_09`, `task_05`)
- Hub: `task_12` (precisa `task_11`)

Cauda: `task_13`, `task_14` → `task_15` → `task_16`

## Fonte

- PRD: `_prd.md`
- TechSpec: `_techspec.md`
- ADRs: `adrs/adr-001.md` … `adr-008.md`
- TLC: `.specs/features/onda-6-shoppingcart-order/tasks.md` — O QUÊ autoritativo
