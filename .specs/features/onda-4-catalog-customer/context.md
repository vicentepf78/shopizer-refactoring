# OQ Resolutions — Onda 4 Design (2026-07-26)

Decisions confirmed for Specify/Design assuming Wave 3 (contracts) is complete. Details in `design.md`.

| ID | Decision | Choice |
|----|----------|--------|
| **OQ-01** | Catalog extraction phasing | **Read APIs first; admin writes stay in monolith** (Option A) |
| **OQ-02** | Cross-service product contract | **`ProductSnapshot` canonical**; migrate `ProductIndexPayload` → snapshot v2 (Option A) |
| **OQ-03** | Customer vs cart boundary | **`CustomerSnapshot` + monolith-orchestrated merge**; no shopping-cart service in Wave 4 (Option B) |
| **OQ-04** | Product images / digital files | **Extend content-service** with `productFileManager` paths; catalog calls content HTTP (Option A) |
| **OQ-05** | Catalog facade consolidation | **Strangler on existing V1 paths**; V2 mapper paths delegate same HTTP adapter (Option A) |
| **OQ-06** | Customer auth endpoints | **Login/register/password reset remain in monolith**; customer-service owns profile CRUD only (Option A) |

**Additional Design decisions:**

| ID | Decision |
|----|----------|
| AD-015 | One TLC/Compozy workflow for Catalog + Customer (same window, shared Strangler profile) |
| AD-016 | `sm-catalog-core` thin JAR — read services + mappers; writes stay in monolith `sm-core` |
| AD-017 | `sm-customer-core` thin JAR — customer domain without order/cart transaction coupling |
| AD-018 | `catalog-service` exposes `GET /internal/v1/products/{id}/snapshot` for search/BFF producers |
| AD-019 | Cart merge: `CustomerServiceClient.resolveForMerge(customerId, storeCode)` returns `CustomerSnapshot`; `ShoppingCartService.mergeShoppingCarts` stays monolith |
| AD-020 | Admin product mutations (POST/PUT/DELETE private product APIs) **not** routed to catalog-service in Wave 4 |
| AD-021 | `LanguageCode` / `MerchantStoreId` value types from Wave 3 required on all new HTTP boundaries |
| AD-022 | Shared DB schema (AD-003) continues; no per-service DB split in Wave 4 |

**Status:** Ready for Tasks
