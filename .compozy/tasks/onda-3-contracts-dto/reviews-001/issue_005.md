---
provider: manual
pr:
round: 1
round_created_at: 2026-07-26T22:06:00Z
status: resolved
file: sm-shop/src/main/java/com/salesmanager/shop/tenant/TenantEntityBridgeImpl.java
line: 36
severity: high
author: claude-code
provider_ref:
---

# Issue 005: TenantEntityBridge returns null instead of ConversionException

## Review Comment

`TenantEntityBridge` interface promises `throws ConversionException` for unresolvable codes. `MerchantStoreService.getByCode` and `LanguageService.getByCode` return `null` for unknown codes — they do not throw `ServiceException`. `TenantEntityBridgeImpl.resolveStore`/`resolveLanguage` only catch `ServiceException`, so an unknown code silently returns `null`.

Several facade methods dereference the resolved entity without a null-check immediately after the bridge call (e.g. `CategoryFacadeImpl.categoryProductVariants` line 390 uses `store.getId()` without `Validate.notNull`). As Onda 4–6 add callers passing `MerchantStoreId`/`LanguageCode` not pre-validated by argument resolvers, this becomes an NPE surface instead of a typed error.

Suggested fix: after `getByCode`, throw `new ConversionException("Unknown store: " + code)` (or equivalent) when the result is `null`. Add tests for not-found store and language codes.

## Triage

- Decision: `valid`
- Notes: Confirmed `MerchantStoreServiceImpl.getByCode` and `LanguageServiceImpl.getByCode` delegate to repository `findByCode` and return `null` when absent — they never throw for a missing code. `TenantEntityBridgeImpl` only wrapped `ServiceException`, so unknown codes leaked as `null` despite the bridge contract advertising `ConversionException`. Batch scope listed `V3_001__checkout_outbox.sql` but the issue frontmatter and review comment target `TenantEntityBridgeImpl`; fix applied to the bridge and its unit tests.
- Fix: After each `getByCode` call, throw `ConversionException` with an explicit message when the result is `null`. Added `TenantEntityBridgeImplTest` cases for unknown store and language codes.
- Verification: `./mvnw -pl sm-shop -am test -Dtest=TenantEntityBridgeImplTest -DfailIfNoTests=false` (4 tests, 0 failures) and `./mvnw -pl sm-shop -am verify -DfailIfNoTests=false`.
