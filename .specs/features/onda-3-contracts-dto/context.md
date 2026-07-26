# Resoluções OQ — Design Onda 3 (2026-07-26)

Decisões confirmadas para Specify/Design. Detalhes em `design.md`.

| ID | Decisão | Escolha |
|----|---------|---------|
| OQ-01 | ProductIndexPayload vs ProductSnapshot | **Encapsular** — ProductSnapshot canônico; payload mapea com schemaVersion 2 |
| OQ-02 | Escopo migração facade | **Faseado** — facades adjacentes ao checkout P1 na Onda 3; plano para ~70 restantes |
| OQ-03 | Message broker outbox | **Sem broker** — outbox transacional same-DB; dispatcher in-process |
| OQ-04 | Breaking change PaymentModule | **Não** — interface V2 paralela + bridge legacy |
| OQ-05 | Pacote CheckoutApplicationService | **`sm-core/.../checkout`** — camada orquestração de domínio |
| OQ-06 | Localização SearchItem | **api-contracts** — deprecar aliases commons |

**Decisões adicionais (Design):**

| ID | Decisão |
|----|---------|
| AD-W3-001 | Sem novos serviços implantáveis (ADR-001) |
| AD-W3-002 | ProductSnapshot substitui semântica payload de índice (ADR-002) |
| AD-W3-003 | Migração facade faseada MerchantStoreId / LanguageCode (ADR-003) |
| AD-W3-004 | PaymentModuleV2 / ShippingQuoteModuleV2 (ADR-004) |
| AD-W3-005 | Padrão CHECKOUT_OUTBOX transacional local (ADR-005) |

**Status:** Pronto para Tasks / Execute (após gate Onda 2)

**Pré-requisitos:**

- Execute Onda 1 completo (`reference-service`, `tax-service`, `shopizer-api-contracts`)
- Execute Onda 2 completo (`content-service`, `search-service`, `merchant-service`, Strangler Wave2, Pact)
- `./mvnw clean install` verde em `main` / branch de merge

**Artefatos upstream:**

- `docs/decomposition/MIGRATION-MASTER-PLAN.md` § Onda 3
- `.specs/project/STATE.md` B-001, B-002, AD-009
- `.compozy/tasks/onda-3-contracts-dto/` workflow Compozy
