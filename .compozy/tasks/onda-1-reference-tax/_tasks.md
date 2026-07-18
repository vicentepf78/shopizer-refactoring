---
schema_version: "compozy.tasks/v2"
workflow: onda-1-reference-tax
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
    - from: task_02
      to: task_03
    - from: task_03
      to: task_04
    - from: task_03
      to: task_05
    - from: task_04
      to: task_06
    - from: task_05
      to: task_07
    - from: task_06
      to: task_07
    - from: task_03
      to: task_08
    - from: task_06
      to: task_08
    - from: task_07
      to: task_08
    - from: task_08
      to: task_09
    - from: task_09
      to: task_10
---

# Onda 1 — Reference + Tax Task List

Lista consolidada (modo C) da Wave 1: 10 tasks Compozy mapeadas a partir das TLC T1–T30 e da **Ordem de build** (passos 1–10) do TechSpec.

## Mapeamento TLC → task

| Task | Título | TLC | Build Order |
|------|--------|-----|-------------|
| task_01 | Scaffold shopizer-api-contracts e DTOs comuns | T1–T2 | 1 |
| task_02 | DTOs Reference/Tax e interfaces de client nos contracts | T3–T5 | 2 |
| task_03 | Wire sm-shop-model → shopizer-api-contracts | T6 | 3 |
| task_04 | Extrair sm-reference-core e rewire sm-core | T7–T9 | 4 |
| task_05 | Extrair sm-tax-core, guard 409 e rewire sm-core | T10–T12, T16 | 5 |
| task_06 | reference-service Boot, facades e REST | T13–T15 | 6 |
| task_07 | tax-service Boot, JWT, client Reference e REST | T17–T20 | 7 |
| task_08 | Strangler monolito: RestTemplate, adapters e beans condicionais | T21–T24 | 8 |
| task_09 | Contratos Pact provider e consumer | T25–T27 | 9 |
| task_10 | Docker Compose, health, correlation e gate do reactor | T28–T30 | 10 |

## Paralelismo

- Após `task_03`: `task_04` e `task_05` podem correr em paralelo (sem aresta entre elas).
- `task_07` exige `task_05` e `task_06`.
- `task_08` exige `task_03`, `task_06` e `task_07`.
- Cauda sequencial: `task_08` → `task_09` → `task_10`.

## Fonte

- PRD: `_prd.md`
- TechSpec: `_techspec.md` (Ordem de build 1–10)
- TLC (referência WHAT): `.specs/features/onda-1-reference-tax/tasks.md` — não modificar
