---
provider: manual
pr:
round: 1
round_created_at: 2026-07-26T22:06:00Z
status: resolved
file: sm-shop/src/test/java/com/salesmanager/shop/tenant/TenantEntityBridgeImplTest.java
line: 29
severity: low
author: claude-code
provider_ref:
---

# Issue 014: TenantEntityBridgeImplTest covers only happy-path resolveStore

## Review Comment

`TenantEntityBridgeImplTest` has a single test (`resolveStoreReturnsStoreForValidCode`). Missing coverage:
- `resolveLanguage` (entirely untested)
- Not-found store/language returning null (see issue 005)
- `ServiceException → ConversionException` wrapping branch in both methods

As Onda 4–6 facades depend on `TenantEntityBridge` for tenant-ID migration, this class is a critical boundary. The thin test suite leaves the error-contract untested.

Suggested fix: add tests for `resolveLanguage` success, not-found codes (expecting `ConversionException` after fix), and `ServiceException` wrapping.

## Triage

- Decision: `valid`
- Notes: Issue 005 already added `resolveLanguage` success, unknown store, and unknown language tests. The remaining gap was the `ServiceException → ConversionException` wrapping branch in both bridge methods — untested despite being the only error path when services fail at runtime.
- Fix: Added `resolveStoreWrapsServiceExceptionInConversionException` and `resolveLanguageWrapsServiceExceptionInConversionException`, asserting the thrown `ConversionException` carries the original `ServiceException` as cause.
- Verification: `./mvnw -pl sm-shop -am test -Dtest=TenantEntityBridgeImplTest -DfailIfNoTests=false`
