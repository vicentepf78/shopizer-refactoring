---
provider: manual
pr:
round: 2
round_created_at: 2026-07-26T22:52:00Z
status: resolved
file: sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/search/SearchApi.java
line: 61
severity: high
author: claude-code
provider_ref:
---

# Issue 002: Migrated APIs still NPE on lang=_all

## Review Comment

reviews-001 issue_001 fixed `CategoryApi` with `language != null ? LanguageCode.of(...) : null`, but other Onda-3 migrated endpoints still call `LanguageCode.of(language.getCode())` unconditionally. `LanguageUtils.getRESTLanguage` returns `null` for `lang=_all`, so `language.getCode()` throws NPE before the facade runs.

**Affected callers:** `SearchApi` (61, 75), `OrderApi` (383, 458), `ShoppingCartApi` (244, 280), `ShippingExpeditionApi` (64, 74), `ProductReviewApi` (129), `ProductApi` (318), `ProductApiV2` (231), `ProductVariationApi` (185).

**Suggested fix:** extract the `CategoryApi` `languageCode(Language)` helper and use it at every migrated API boundary; add regression tests exercising `?lang=_all` per facade group.

## Triage

- Decision: `valid`
- Root cause: Onda 3 migration added `LanguageCode.of(language.getCode())` at the Search API boundary without guarding for `null` language. `LanguageUtils.getRESTLanguage` intentionally returns `null` when `lang=_all`, so the controller NPEs before `SearchFacade` runs.
- Fix: Guard at the API boundary with `languageCode(Language)` (same pattern as `CategoryApi`). Minimal companion change in `SearchFacadeImpl.resolveLanguage` to accept `null` `LanguageCode` and fall back to the store default language for search/autocomplete (search indexes are language-scoped; legacy facade also required a language code).
- Verification: `./mvnw -pl sm-shop -am test -Dtest=SearchApiTest -DfailIfNoTests=false`
