# OQ Resolutions — Onda 5 Design (2026-07-26)

Decisions confirmed for integration-service extraction. Details in `design.md`.

| ID | Decision | Choice |
|----|----------|--------|
| OQ-01 | Payment order mutation boundary | **Stateless integration-service** — returns `TransactionResult`; monolith/checkout saga persists order status (Option A) |
| OQ-02 | Shipping product data source | **Catalog read snapshots** via HTTP from catalog-service (Onda 4 partial) + `ShippingProductSnapshot` DTO (Option A) |
| OQ-03 | Plugin module hosting | **In-process registry** inside integration-service — `Map<String, PaymentModule>` / `ShippingQuoteModule` beans (Option A) |
| OQ-04 | Configuration persistence | **Shared MySQL** — `MERCHANT_CONFIGURATION`, `INTEGRATION_MODULE` metadata; AD-003 inherited (Option A) |
| OQ-05 | Admin payment/shipping config APIs | **Migrate to integration-service** — preserve frozen REST paths via Strangler (Option A) |
| OQ-06 | OrderPaymentApi / OrderShippingApi | **Remain on BFF** — delegate orchestration to checkout application service + integration HTTP clients (Option B) |

**Additional design decisions:**

| ID | Decision |
|----|----------|
| AD-015 | `integration-service` port **8086**; profile `strangler-wave5` |
| AD-016 | `PaymentModuleV2` / `ShippingQuoteModuleV2` in `sm-core-modules` — DTO-only boundaries (delivered in Onda 3) |
| AD-017 | Legacy `PaymentModule` adapters wrap V2 until all plugins migrated |
| AD-018 | Encryption service for module credentials stays in integration-service |
| AD-019 | Shipping packaging (`DefaultPackagingImpl`) co-located with shipping orchestration |
| AD-020 | No capture/refund without `orderId` reference — caller supplies `OrderSnapshot` id only |

**Prerequisites (hard gates):**

| Gate | Source | Blocks |
|------|--------|--------|
| Onda 3 Execute complete | `onda-3-contracts-checkout` | DTO contracts, checkout app service, saga/outbox foundation |
| Onda 4 partial — catalog read | `onda-4-catalog-customer` | `ProductSnapshot` / weight-dimension reads for shipping quotes |
| Onda 1–2 patterns | reference, strangler, Pact | HTTP clients, JWT, correlation |

**Status:** Ready for Tasks
