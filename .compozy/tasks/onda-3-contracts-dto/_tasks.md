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

# Onda 3 — Contracts DTO + Checkout Application Service Task List

Lista consolidada de tasks (modo C) para a Onda 3: **10 tasks Compozy** mapeadas das TLC T1–T48 e da ordem de construção do TechSpec.

**Pré-requisito externo rígido:** Execute da Onda 2 (`onda-2-content-search-merchant`) MUST estar completo antes de iniciar qualquer task — incluindo `task_01`. Sem novos microserviços nesta onda.

## Mapeamento TLC → Compozy

| Task | Título | TLC | Type | Complexity |
|------|--------|-----|------|------------|
| task_01 | Tipos tenant e base de contracts | T1–T6 | backend | medium |
| task_02 | ProductSnapshot e evolução do payload de índice | T7–T12 | backend | high |
| task_03 | DTOs OrderSnapshot e CustomerSnapshot | T13–T16 | backend | medium |
| task_04 | Migração SearchItem para api-contracts | T17–T20 | backend | medium |
| task_05 | DTOs de integração payment/shipping | T21–T24 | backend | high |
| task_06 | PaymentModuleV2/ShippingQuoteModuleV2 e bridges | T25–T29 | backend | high |
| task_07 | Migração facade P1 para identificadores tenant | T30–T34 | refactor | high |
| task_08 | Correção DTO ReferencesApi e plano de migração facade | T35–T38 | backend | medium |
| task_09 | Extração CheckoutApplicationService | T39–T43 | backend | high |
| task_10 | Outbox processOrder, gate e atualização STATE | T44–T48 | infra | medium |

## Milestones

- **SNP-ready:** fim de `task_02` + `task_03` (snapshots compilam e serializam).
- **INT-ready:** fim de `task_06` (caminho payment V2 invocável em testes).
- **CHK-ready:** fim de `task_09` (facade delega ao application service).
- **Onda 3 completa:** fim de `task_10` (`./mvnw clean install` verde).

## Paralelismo

Após `task_01` (e gate Onda 2):

- Track snapshots: `task_02` → `task_04`; `task_03` → `task_09`
- Track integração: `task_05` → `task_06`
- Track facades: `task_07` → `task_08`
- Convergência: `task_09` + `task_04` + `task_06` + `task_08` → `task_10`

## Fonte

- PRD: `_prd.md`
- TechSpec: `_techspec.md`
- ADRs: `adrs/adr-001.md` … `adr-005.md`
- TLC (WHAT autoritativo): `.specs/features/onda-3-contracts-dto/tasks.md`
