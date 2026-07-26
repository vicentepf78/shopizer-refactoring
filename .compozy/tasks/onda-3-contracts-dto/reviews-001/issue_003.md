---
provider: manual
pr:
round: 1
round_created_at: 2026-07-26T22:06:00Z
status: resolved
file: sm-core-modules/src/main/java/com/salesmanager/core/modules/integration/payment/dto/PaymentRequestContext.java
line: 29
severity: high
author: claude-code
provider_ref:
---

# Issue 003: V2 integration DTOs leak JPA entity IntegrationModule

## Review Comment

INT-01 requires integration DTOs in `sm-core-modules` to not reference JPA entity types. `PaymentRequestContext` (line 29), `PaymentCaptureContext`, `PaymentRefundContext`, and `ShippingQuoteRequestContext` all carry `IntegrationModule`, which is a JPA `@Entity` in `sm-core-model`. This undermines the stated goal of decoupling `sm-core-modules` for eventual `integration-service` extraction (Onda 5).

The guard test `IntegrationDtoNoJpaTest` only blocklists three specific classes (`Order`, `Customer`, `ShoppingCartItem`) and `javax.persistence.` imports — it does not check for `com.salesmanager.core.model.system.IntegrationModule`, so the violation passes CI.

Suggested fix: introduce a lightweight `IntegrationModuleDto` (code, type, regions) matching what legacy bridges actually need; replace the entity field in all four context DTOs. Harden `IntegrationDtoNoJpaTest` to reject any `com.salesmanager.core.model` import under `/dto/` rather than a hand-picked list.

## Triage

- Decision: `valid`
- Root cause: `PaymentRequestContext.module` was typed as JPA entity `IntegrationModule`, coupling `sm-core-modules` DTOs to `sm-core-model` persistence types (INT-01 violation).
- Fix applied:
  - Added `IntegrationModuleDto` in `common.dto` with `code`, `type`, `regions`, and `module` (category string used by legacy lookups).
  - Replaced `IntegrationModule` with `IntegrationModuleDto` on all four V2 context DTOs (`PaymentRequestContext` plus sibling capture/refund/shipping contexts required for guard-test pass).
  - Mapped entity ↔ DTO in `IntegrationContextMapper`; legacy bridges resolve the full entity from entity bundles (preserving transient fields like `moduleConfigs`) with DTO fallback via `toModule`.
  - Hardened `IntegrationDtoNoJpaTest` to blocklist `IntegrationModule` alongside existing JPA entity checks.
- Scope note: `IntegrationConfiguration` remains on context DTOs — it is not a JPA entity. Full `com.salesmanager.core.model` import ban deferred to a future task.
