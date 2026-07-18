# OQ Resolutions — Onda 2 Design (2026-07-04)

Decisões confirmadas assumindo recomendações padrão do Specify. Detalhes em `design.md`.

| ID | Decisão | Escolha |
|----|---------|---------|
| OQ-01 | Faseamento indexação Search | **HTTP producer + `ProductIndexPayload`** (Opção A) |
| OQ-02 | Product images no content-service | **Só `contentFileManager`** (Opção A) |
| OQ-03 | Legacy static serving | **Monólito thin proxy** (Opção B) |
| OQ-04 | Endpoints stub/deprecated | **Preservar byte-a-byte** (Opção A) |
| OQ-05 | Gaps de reindex | **Documentar** GAP-SRCH-01..10 (Opção A) |
| OQ-06 | SearchItem em commons | **Pact usa shopizer-commons** (Opção A) |

**Decisões adicionais (Design):**

| ID | Decisão |
|----|---------|
| AD-011 | Módulos thin `sm-content-core`, `sm-merchant-core` |
| AD-012 | `search-service` sem JPA |
| AD-013 | Internal APIs: network policy + `X-Internal-Token` (search) |
| AD-014 | Logo upload: blob primeiro, DB depois, com compensação |

**Status:** Ready for Tasks
