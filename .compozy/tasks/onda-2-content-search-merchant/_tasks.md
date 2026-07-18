---
schema_version: "compozy.tasks/v2"
workflow: onda-2-content-search-merchant
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
    - from: task_03
      to: task_09
    - from: task_08
      to: task_09
    - from: task_09
      to: task_10
    - from: task_04
      to: task_11
    - from: task_07
      to: task_11
    - from: task_10
      to: task_11
    - from: task_03
      to: task_12
    - from: task_05
      to: task_12
    - from: task_09
      to: task_12
    - from: task_11
      to: task_13
    - from: task_12
      to: task_14
    - from: task_13
      to: task_14
---

# Onda 2 — Content, Search, Merchant Task List

Lista consolidada (modo C) da Wave 2: **14 tasks Compozy** mapeadas a partir das TLC T1–T54 e da **Sequenciamento de desenvolvimento / Ordem de construção** do TechSpec.

**Pré-requisito externo (gate rígido):** o Execute da Onda 1 (`onda-1-reference-tax`) MUST estar completo antes de iniciar qualquer task desta lista — incluindo `task_01`. Sem esse gate, contratos, RestTemplate/JWT/Pact e `reference-service` não estão estáveis.

## Mapeamento TLC → task

| Task | Título | TLC | Tipo | Complexidade |
|------|--------|-----|------|--------------|
| task_01 | Contracts Content/Search/Merchant e properties Strangler Wave2 | T1–T4 | backend | medium |
| task_02 | Extrair sm-content-core e CMS content | T5–T7 | backend | high |
| task_03 | content-service Boot, JWT, REST e APIs internas (C-ready) | T8–T16 | backend | high |
| task_04 | Rewire sm-core content e trim CMS de produto | T17 | backend | medium |
| task_05 | search-service OpenSearch, query e índice (S-ready) | T18–T22 | backend | high |
| task_06 | ProductIndexPayload, producers e listener de indexação | T23–T26 | backend | high |
| task_07 | Strangler Search no monolito e documentação GAP-SRCH | T27–T29 | backend | medium |
| task_08 | Extrair sm-merchant-core sem ProductType | T30–T32 | backend | high |
| task_09 | merchant-service Boot, JWT e clients Reference/Content | T33–T34 | backend | high |
| task_10 | Merchant REST, snapshot interno e logo AD-014 | T35–T40 | backend | high |
| task_11 | Strangler Content/Merchant, StaticContentProxy e wiring | T41–T46 | backend | high |
| task_12 | Correlation ID e health indicators Wave2 | T47–T48 | infra | medium |
| task_13 | Pact providers e consumer Wave2 | T49–T50 | test | medium |
| task_14 | Docker Compose, integração, gate e STATE | T51–T54 | infra | medium |

## Marcos

- **C-ready:** fim de `task_03` (APIs internas static + logo).
- **S-ready:** fim de `task_05` (API pública search + índice interno).
- Não iniciar clients de logo/blob (`task_09`/`task_10`/`task_11`) antes de C-ready; não iniciar producer HTTP (`task_06`) antes de S-ready + builder.

## Paralelismo

Após `task_01` (e gate Onda 1):

- Track content: `task_02` → `task_03` → `task_04`
- Track search: `task_05` → `task_06` → `task_07`
- Track merchant core: `task_08` (fan-out; junta com content em `task_09`)
- Observabilidade (`task_12`) pode avançar após apps Boot (`task_03`, `task_05`, `task_09`) em paralelo ao Strangler (`task_11`)
- Cauda: `task_11` → `task_13`; `task_12`+`task_13` → `task_14`

## Fonte

- PRD: `_prd.md`
- TechSpec: `_techspec.md`
- ADRs: `adrs/adr-001.md` … `adr-011.md`
- TLC (referência WHAT): `.specs/features/onda-2-content-search-merchant/tasks.md` — **não modificar**
