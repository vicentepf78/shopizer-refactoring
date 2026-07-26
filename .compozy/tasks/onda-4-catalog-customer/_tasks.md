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

# Onda 4 — Lista de tasks Catalog + Customer

Lista consolidada (modo C) para a Onda 4: **15 tasks Compozy** mapeadas do TLC T1–T38 e da ordem de construção da TechSpec.

**Pré-requisito externo (gate rígido):** Execute da Onda 3 DEVE estar completo antes de qualquer task aqui — incluindo `task_01`. Requer `ProductSnapshot`, `CustomerSnapshot`, `LanguageCode`, `MerchantStoreId` em `shopizer-api-contracts`.

## Mapeamento TLC → task

| Task | Título | TLC | Tipo | Complexidade |
|------|--------|-----|------|--------------|
| task_01 | Contratos catalog/customer snapshots + config Strangler Wave4 | T1–T4 | backend | medium |
| task_02 | Extrair serviços de leitura sm-catalog-core | T5–T8 | backend | high |
| task_03 | catalog-service Boot, clients, REST read público (CAT-ready) | T9–T13 | backend | high |
| task_04 | Extrair sm-customer-core | T14–T17 | backend | high |
| task_05 | customer-service Boot, REST, snapshot (CUS-ready) | T18–T20 | backend | high |
| task_06 | ProductSnapshot builder + migração search v2 | T21–T23 | backend | high |
| task_07 | Desacoplamento merge de carrinho + orquestração CustomerFacade | T24–T25 | backend | high |
| task_08 | Imagens de produto via content-service (P2) | T26 | backend | medium |
| task_09 | Adaptadores HTTP Strangler catalog + customer | T27–T30 | backend | high |
| task_10 | Checkpoint de integração cross-track | T10,T13,T20 | backend | medium |
| task_11 | Correlation ID + health indicators Wave4 | T31 | infra | medium |
| task_12 | Gates JaCoCo verify módulos Wave4 | T32 | test | low |
| task_13 | Providers Pact + Wave4ConsumerPactTest | T33–T34 | test | medium |
| task_14 | ProductFacadeV2 + guards de wiring (writes admin locais) | T29–T30 | backend | medium |
| task_15 | Docker Compose wave4, gate, STATE | T35–T38 | infra | medium |

## Marcos

- **CAT-ready:** fim de `task_03` (leitura pública de catálogo + ProductSnapshot interno).
- **CUS-ready:** fim de `task_05` (REST profile de customer + CustomerSnapshot interno).
- Não iniciar `task_06` (migração search) antes de CAT-ready + contratos de `task_01`.
- Não iniciar `task_07` (merge de carrinho) antes de CUS-ready.
- Não iniciar adaptadores strangler (`task_09`) antes do checkpoint `task_10`.

## Paralelismo

Após `task_01` (e gate da Onda 3):

- Trilha catalog: `task_02` → `task_03`
- Trilha customer: `task_04` → `task_05` (fan-out de `task_01`)
- Convergência: `task_10` → `task_06` + `task_07` (merge/search podem paralelizar após checkpoint)
- Strangler: `task_09` + `task_14` após `task_10`
- Cauda: `task_11` + `task_12` → `task_13` → `task_15`; `task_08` paralelo após CAT-ready

## Fonte

- PRD: `_prd.md`
- TechSpec: `_techspec.md`
- ADRs: `adrs/adr-001.md` … `adr-007.md`
- TLC (referência WHAT): `.specs/features/onda-4-catalog-customer/tasks.md` — não modificar no Execute Compozy sem revisão TLC
