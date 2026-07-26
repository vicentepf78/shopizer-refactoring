---
provider: manual
pr:
round: 2
round_created_at: 2026-07-26T22:52:00Z
status: resolved
file: sm-core/src/main/java/com/salesmanager/core/business/services/shipping/LegacyShippingQuoteModuleBridge.java
line: 35
severity: critical
author: claude-code
provider_ref:
---

# Issue 001: Shipping V2 bridge strips IntegrationModule transient state

## Review Comment

Unlike `LegacyPaymentModuleBridge`, which keeps the full `IntegrationModule` in `LegacyPaymentEntityBundle` and prefers `entities.getIntegrationModule()` via `resolveModule()`, the shipping bridge always calls `IntegrationContextMapper.toModule(context.getModule())`. `IntegrationModuleDto` only carries code/type/regions/module string — `moduleConfigs`, `regionsSet`, and `details` are lost on every quote request.

USPS and UPS shipping plugins read `module.getModuleConfigs()` for host/scheme/port/uri and UPS also uses `module.getRegionsSet()` and `module.getDetails()`. Pre-V2, `ShippingServiceImpl` passed the hydrated `shippingModule` from `getShippingMethods()`; after V2 routing the delegate receives an empty shell, causing quote failures or `IntegrationException` on region checks.

**Suggested fix:** mirror the payment pattern — add `IntegrationModule` to `LegacyShippingEntityBundle`, pass the full entity from `ShippingServiceImpl`, and resolve module from the bundle with DTO fallback only when null.

## Triage

- Decision: `valid`
- Root cause: `LegacyShippingQuoteModuleBridge.getShippingQuotes()` always reconstructed the module from `IntegrationModuleDto` via `IntegrationContextMapper.toModule()`, which copies only code/type/regions/module and drops transient fields (`moduleConfigs`, `regionsSet`, `details`) populated by `IntegrationModulesLoader`.
- Fix: Added `integrationModule` to `LegacyShippingEntityBundle`, passed the hydrated `shippingModule` from `ShippingServiceImpl.getShippingQuote()`, and added `resolveModule()` in the bridge to prefer the bundle entity with DTO fallback (matching `LegacyPaymentModuleBridge`).
- Verification: `./mvnw -pl sm-core -am test -Dtest=LegacyShippingQuoteModuleBridgeTest -DfailIfNoTests=false` and `./mvnw -pl sm-core -am verify -DfailIfNoTests=false` — BUILD SUCCESS.
