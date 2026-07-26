---
provider: manual
pr:
round: 2
round_created_at: 2026-07-26T22:52:00Z
status: resolved
file: sm-shop/src/main/java/com/salesmanager/shop/store/facade/product/ProductCommonFacadeImpl.java
line: 498
severity: medium
author: claude-code
provider_ref:
---

# Issue 012: P1 facades lack null LanguageCode bridge guard

## Review Comment

Only `CategoryFacadeImpl.resolveLanguage` returns `null` when `LanguageCode` is `null`. Other P1 implementations pass `null` straight to `TenantEntityBridge.resolveLanguage`, which calls `languageCode.getCode()` and NPEs: `ProductCommonFacadeImpl` (498), `ShippingFacadeImpl` (410), `SearchFacadeImpl` (102), `ShoppingCartFacadeImpl` v1 (49), `OrderFacadeImpl` v1 (68).

Fixing API boundaries (issue_002) without aligning these facades will shift the NPE from controller to bridge.

**Suggested fix:** extract a shared `resolveLanguage(LanguageCode)` helper (null → null) used by all P1 facades, or teach `TenantEntityBridge` to accept null and document the contract.

## Triage

- Decision: `valid`
- Root cause: `ProductCommonFacadeImpl.resolveLanguage` forwarded a null `LanguageCode` to `TenantEntityBridge.resolveLanguage`, which dereferences the code and throws NPE. API boundary fixes (issue_002) allow null language codes to reach the facade layer.
- Fix: Added the same null guard already used in `CategoryFacadeImpl.resolveLanguage` — return `null` when `languageCode` is null, before calling the bridge.
- Verification: `ProductCommonFacadeImplTest.getProduct_withNullLanguageCode_doesNotCallBridgeResolveLanguage` confirms the bridge is not invoked; `./mvnw -pl sm-shop -am verify -DfailIfNoTests=false` passed (154 tests, 0 failures).
