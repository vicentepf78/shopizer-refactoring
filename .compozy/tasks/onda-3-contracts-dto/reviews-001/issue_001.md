---
provider: manual
pr:
round: 1
round_created_at: 2026-07-26T22:06:00Z
status: resolved
file: sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/category/CategoryApi.java
line: 78
severity: critical
author: claude-code
provider_ref:
---

# Issue 001: Category endpoints NPE when lang=_all is requested

## Review Comment

`LanguageUtils.getRESTLanguage` intentionally returns `null` when the client passes `lang=_all` (see comment at line 178: "if language is null then underlying facade must load all languages"). `CategoryFacadeImpl.getById` still branches on `language == null` to load all languages (lines 257–262).

After the Onda 3 migration, Category endpoints call `LanguageCode.of(language.getCode())` in the controller before the facade runs (e.g. lines 77–78, 94–95, 131–132, 147–148). When `language` is `null`, `language.getCode()` throws `NullPointerException` before the facade's all-languages branch can execute. This is a behavioral regression violating CHK-01/CHK-02 (checkout/REST parity) and TNT acceptance criteria.

The same pattern appears in other migrated APIs: `SearchApi.java` (61, 75), `ProductVariationApi.java` (185), `ShoppingCartApi.java` (244, 280), `OrderApi.java` (383, 458), and others.

Suggested fix: introduce an optional/sentinel `LanguageCode` (e.g. `LanguageCode.ALL` or `Optional<LanguageCode>`), or guard at the API boundary — only call `LanguageCode.of(...)` when `language != null` and pass `null`/`Optional.empty()` to facades that support all-languages. Add a regression test exercising `?lang=_all` on at least one Category endpoint.

## Triage

- Decision: `valid`
- Root cause: Onda 3 migration added `LanguageCode.of(language.getCode())` at the Category API boundary without guarding for `null` language. `LanguageUtils.getRESTLanguage` intentionally returns `null` when `lang=_all`, so the controller NPEs before `CategoryFacadeImpl` can take its all-languages branch.
- Fix: Guard at the API boundary with `language != null ? LanguageCode.of(...) : null`. Minimal companion change in `CategoryFacadeImpl.resolveLanguage` to return `null` when `LanguageCode` is `null` (required for end-to-end behavior; facade already branches on `language != null` in `getById`).
- Test: `CategoryApiTest.getById_withNullLanguage_passesNullLanguageCodeForAllLanguages` exercises the `_all` path (null `Language` argument).
