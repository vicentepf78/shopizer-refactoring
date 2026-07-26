# OQ Resolutions — Wave 3 Design (2026-07-26)

Decisions confirmed for Specify/Design. Details in `design.md`.

| ID | Decision | Choice |
|----|----------|--------|
| OQ-01 | ProductIndexPayload vs ProductSnapshot | **Wrap** — ProductSnapshot canonical; payload maps with schemaVersion 2 |
| OQ-02 | Facade migration scope | **Phased** — P1 checkout-adjacent facades in Wave 3; plan for remaining 70 |
| OQ-03 | Outbox message broker | **No broker** — same-DB transactional outbox; dispatcher in-process |
| OQ-04 | PaymentModule breaking change | **No** — V2 parallel interface + legacy bridge |
| OQ-05 | CheckoutApplicationService package | **`sm-core/.../checkout`** — domain orchestration layer |
| OQ-06 | SearchItem location | **api-contracts** — deprecate commons aliases |

**Additional decisions (Design):**

| ID | Decision |
|----|----------|
| AD-W3-001 | No new deployable services (ADR-001) |
| AD-W3-002 | ProductSnapshot supersedes index payload semantics (ADR-002) |
| AD-W3-003 | MerchantStoreId / LanguageCode phased facade migration (ADR-003) |
| AD-W3-004 | PaymentModuleV2 / ShippingQuoteModuleV2 (ADR-004) |
| AD-W3-005 | CHECKOUT_OUTBOX local transactional pattern (ADR-005) |

**Status:** Ready for Tasks / Execute (after Wave 2 gate)

**Prerequisites:**

- Wave 1 Execute complete (`reference-service`, `tax-service`, `shopizer-api-contracts`)
- Wave 2 Execute complete (`content-service`, `search-service`, `merchant-service`, Wave2 Strangler, Pact)
- `./mvnw clean install` green on `main` / merge branch

**Upstream artifacts:**

- `docs/decomposition/MIGRATION-MASTER-PLAN.md` § Onda 3
- `.specs/project/STATE.md` B-001, B-002, AD-009
- `.compozy/tasks/onda-3-contracts-dto/` Compozy workflow
