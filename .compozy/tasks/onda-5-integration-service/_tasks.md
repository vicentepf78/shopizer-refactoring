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

Lista consolidada da Onda 5: **12 tasks Compozy** mapeadas a partir do TLC T1–T38 e da ordem de construção do TechSpec.

**Gates externos (rígidos):** Execute da Onda 3 (`PaymentModuleV2`, saga checkout) E leitura parcial de catálogo da Onda 4 (`ShippingProductSnapshot`) DEVEM estar completos antes de `task_01`. Padrões Ondas 1–2 (RestTemplate, JWT, Pact, correlation) DEVEM estar estáveis.

## Mapeamento TLC → task

| Task | Título | TLC | Tipo | Complexidade |
|------|-------|-----|------|------------|
| task_01 | Contratos integration, client, config Strangler Wave5 | T1–T5 | backend | medium |
| task_02 | Plugins pagamento + extração PaymentOrchestrator | T6–T8 | backend | high |
| task_03 | Ops pagamento stateless + testes P-ready | T9–T11 | backend | high |
| task_04 | Plugins frete + orquestrador + client catálogo | T12–T16 | backend | high |
| task_05 | Boot integration-service + REST admin | T17–T19 | backend | high |
| task_06 | APIs REST públicas + internas (I-ready) | T20–T22 | backend | high |
| task_07 | Trim sm-core + fronteira stateless monólito | T23–T24 | backend | medium |
| task_08 | Facades Strangler pagamento/frete | T25–T26 | backend | high |
| task_09 | Wiring HTTP checkout + OrderShipping | T27–T28 | backend | high |
| task_10 | Correlation ID + health indicators Wave5 | T29 | infra | medium |
| task_11 | Pact provider/consumer + IntegrationServiceClient | T30–T32 | test | medium |
| task_12 | Docker Compose, gate integração, STATE | T33–T38 | infra | medium |

## Marcos

- **P-ready:** fim de `task_03` (orquestrador de pagamento + plugins sem OrderService).
- **S-ready:** fim de `task_04` (orquestrador de frete + client catálogo).
- **I-ready:** fim de `task_06` (health integration-service + APIs internas pagamento/cotação).
- Não iniciar trabalho de client catálogo em `task_04` até gate Onda 4 parcial verificado.
- Não iniciar `task_09` até fronteira stateless de `task_07` mergeada.

## Paralelismo

Após `task_01` (e gates externos):

- Trilha pagamento: `task_02` → `task_03`
- Trilha frete: `task_04` (paralelo com task_02 após task_01)
- Convergência: `task_05` exige P-ready + S-ready
- Cauda: `task_11` → `task_12`; `task_10` paralelo após apps Boot em `task_05`

## Fonte

- PRD: `_prd.md`
- TechSpec: `_techspec.md`
- ADRs: `adrs/adr-001.md` … `adr-007.md`
- TLC: `.specs/features/onda-5-integration-service/tasks.md`
