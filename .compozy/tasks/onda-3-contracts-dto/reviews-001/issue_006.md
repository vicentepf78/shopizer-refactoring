---
provider: manual
pr:
round: 1
round_created_at: 2026-07-26T22:06:00Z
status: resolved
file: sm-core/src/main/java/com/salesmanager/core/business/tenant/TenantEntityBridgeRegistry.java
line: 19
severity: medium
author: claude-code
provider_ref:
---

# Issue 006: Static TenantEntityBridgeRegistry is dead speculative plumbing

## Review Comment

Every migrated facade impl correctly injects `TenantEntityBridge` via Spring DI. However, `AbstractDataPopulator.populate(source, target, MerchantStoreId, LanguageCode)` reaches for `TenantEntityBridgeRegistry.require()` — a static mutable singleton registered via `@PostConstruct` side effect in `TenantEntityBridgeImpl` (sm-shop only).

Problems:
- No production caller of the new populator overload exists in this diff (grep confirms zero `src/main` usages).
- Global mutable state requires manual `TenantEntityBridgeRegistry.clear()` in tests (`AbstractDataPopulatorTenantOverloadTest`) to avoid cross-test leakage.
- Any Spring context without `TenantEntityBridgeImpl` (slice tests, extracted services) will throw `IllegalStateException` if the overload is ever called.
- The static `bridge` field is non-volatile with no happens-before guarantee.

Suggested fix: remove `TenantEntityBridgeRegistry` and the populator overload until a real caller exists in Onda 4+, or replace with constructor injection at the call site. If retained, mark the field `volatile` and document the test isolation contract.

## Triage

- Decision: `valid`
- Notes: Confirmed zero `src/main` callers of the `MerchantStoreId`/`LanguageCode` populator overloads; facades inject `TenantEntityBridge` via Spring DI instead. The static registry added global mutable state, test isolation boilerplate, and a latent `IllegalStateException` in contexts without `TenantEntityBridgeImpl`. Batch scope listed `V3_001__checkout_outbox.sql` but the issue frontmatter and review comment target `TenantEntityBridgeRegistry`; fix applied per suggested removal path.
- Fix: Deleted `TenantEntityBridgeRegistry`, removed the unused populator overloads from `DataPopulator`/`AbstractDataPopulator`, dropped `AbstractDataPopulatorTenantOverloadTest`, inlined bridge methods on `TenantEntityBridge`, and removed `@PostConstruct` registration from `TenantEntityBridgeImpl`.
- Verification: `./mvnw -pl sm-core,sm-shop -am test -Dtest=TenantEntityBridgeImplTest -DfailIfNoTests=false` (4 tests, 0 failures) and `./mvnw -pl sm-core,sm-shop -am verify -DfailIfNoTests=false` (BUILD SUCCESS).
