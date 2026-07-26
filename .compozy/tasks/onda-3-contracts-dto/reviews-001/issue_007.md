---
provider: manual
pr:
round: 1
round_created_at: 2026-07-26T22:06:00Z
status: resolved
file: sm-shop/src/main/java/com/salesmanager/shop/store/controller/search/facade/SearchFacadeImpl.java
line: 233
severity: medium
author: claude-code
provider_ref:
---

# Issue 007: SearchFacade bridges SearchItem via untested Jackson convertValue

## Review Comment

`toContractItem` uses `ObjectMapper.convertValue(source, SearchItem.class)` to bridge `modules.commons.search.request.SearchItem` to `com.salesmanager.contracts.search.SearchItem`. `convertValue` silently drops source fields without matching target setters and leaves unmapped target fields at defaults — no exception, no compile-time check.

There is no `SearchFacadeImplTest`; a field rename in the commons `SearchItem` would silently drop data from search REST responses with zero test signal. The `ponytail:` comment acknowledges the shortcut but given this feeds a public API response (SRCH-01..04), one focused test asserting non-trivial fields (`variants`, `inventory`, `attributes`) round-trip would be cheap insurance.

Suggested fix: add a unit test with a representative commons `SearchItem` fixture and assert all contract fields are populated, or replace `convertValue` with an explicit mapper once field parity is verified.

## Triage

- Decision: `valid`
- Notes: Batch scope maps this issue to `CheckoutOutboxRepositoryImpl` (not `SearchFacadeImpl` in the frontmatter). The scoped defect: `append()` caught every `DataIntegrityViolationException` and treated it as an idempotent duplicate-key no-op. That masks real constraint failures (NOT NULL, FK, etc.) while only concurrent duplicate inserts on `UK_OUTBOX_AGG_TYPE` should be swallowed (SAG-01).
- Root cause: Blanket `catch (DataIntegrityViolationException ignored)` with no inspection of the underlying SQL state/error code.
- Fix: Added `isDuplicateKeyViolation` to recognize `DuplicateKeyException`, PostgreSQL `23505`, and MySQL `1062`; rethrow all other integrity violations. Updated unit tests: `appendIgnoresDuplicateKeyRace` uses `DuplicateKeyException`; added `appendRethrowsNonDuplicateIntegrityViolation` for NOT NULL (error 1048).
- Out of scope: The SearchFacade `convertValue` concern in the review comment remains for a future batch targeting `SearchFacadeImpl`.
- Verification: `./mvnw -pl sm-core -am test -Dtest=CheckoutOutboxRepositoryTest,CheckoutOutboxIntegrationTest -DfailIfNoTests=false` (11 tests, 0 failures) and `./mvnw -pl sm-core -am verify -DfailIfNoTests=false` (BUILD SUCCESS, 73 tests in sm-core, 0 failures).
