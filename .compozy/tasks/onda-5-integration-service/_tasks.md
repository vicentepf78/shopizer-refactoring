---
schema_version: "compozy.tasks/v2"
workflow: onda-5-integration-service
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
  edges:
    - from: task_01
      to: task_02
    - from: task_01
      to: task_04
    - from: task_02
      to: task_03
    - from: task_03
      to: task_05
    - from: task_04
      to: task_05
    - from: task_05
      to: task_06
    - from: task_06
      to: task_07
    - from: task_07
      to: task_08
    - from: task_08
      to: task_09
    - from: task_05
      to: task_10
    - from: task_09
      to: task_10
    - from: task_10
      to: task_11
    - from: task_08
      to: task_11
    - from: task_11
      to: task_12
---

# Onda 5 — Integration Service Task List

Consolidated Wave 5 task list: **12 Compozy tasks** mapped from TLC T1–T38 and TechSpec build order.

**External gates (hard):** Onda 3 Execute (`PaymentModuleV2`, checkout saga) AND Onda 4 partial catalog read (`ShippingProductSnapshot`) MUST be complete before `task_01`. Waves 1–2 patterns (RestTemplate, JWT, Pact, correlation) MUST be stable.

## TLC → task mapping

| Task | Title | TLC | Type | Complexity |
|------|-------|-----|------|------------|
| task_01 | Integration contracts, client, Wave5 Strangler config | T1–T5 | backend | medium |
| task_02 | Payment plugins + PaymentOrchestrator extract | T6–T8 | backend | high |
| task_03 | Stateless payment ops + P-ready tests | T9–T11 | backend | high |
| task_04 | Shipping plugins + orchestrator + catalog client | T12–T16 | backend | high |
| task_05 | integration-service Boot + admin REST | T17–T19 | backend | high |
| task_06 | Public + internal REST APIs (I-ready) | T20–T22 | backend | high |
| task_07 | Trim sm-core + stateless monolith boundary | T23–T24 | backend | medium |
| task_08 | Strangler payment/shipping facades | T25–T26 | backend | high |
| task_09 | Checkout + OrderShipping HTTP wiring | T27–T28 | backend | high |
| task_10 | Correlation ID + health indicators Wave5 | T29 | infra | medium |
| task_11 | Pact provider/consumer + IntegrationServiceClient | T30–T32 | test | medium |
| task_12 | Docker Compose, integration gate, STATE | T33–T38 | infra | medium |

## Milestones

- **P-ready:** end of `task_03` (payment orchestrator + plugins without OrderService).
- **S-ready:** end of `task_04` (shipping orchestrator + catalog client).
- **I-ready:** end of `task_06` (integration-service health + internal payment/quote APIs).
- Do not start `task_04` catalog client work until Onda 4 partial gate verified.
- Do not start `task_09` until `task_07` stateless boundary merged.

## Parallelism

After `task_01` (and external gates):

- Payment track: `task_02` → `task_03`
- Shipping track: `task_04` (parallel with task_02 after task_01)
- Converge: `task_05` requires P-ready + S-ready
- Tail: `task_11` → `task_12`; `task_10` parallel after `task_05` apps boot

## Source

- PRD: `_prd.md`
- TechSpec: `_techspec.md`
- ADRs: `adrs/adr-001.md` … `adr-007.md`
- TLC: `.specs/features/onda-5-integration-service/tasks.md`
