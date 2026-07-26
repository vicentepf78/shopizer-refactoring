---
provider: manual
pr:
round: 2
round_created_at: 2026-07-26T22:52:00Z
status: resolved
file: sm-shop/src/main/java/com/salesmanager/shop/store/facade/category/CategoryFacadeImpl.java
line: 100
severity: high
author: claude-code
provider_ref:
---

# Issue 003: Category hierarchy still breaks for lang=_all

## Review Comment

`CategoryApi` now passes `null` `LanguageCode` for `lang=_all`, and `resolveLanguage` correctly returns `null`, but only `getById` has an all-languages branch. `getCategoryHierarchy` (line 100) calls `categoryService.getListByDepth(parent, language, ...)` which dereferences `language.getId()` in `CategoryServiceImpl`. `getCategoryByFriendlyUrl` (line 346) calls `getBySeUrl(store, friendlyUrl, language)` which also NPEs on `language.getId()`.

`GET /api/v1/category` and friendly-URL lookups still regress with `?lang=_all` even after reviews-001 issue_001.

**Suggested fix:** when `language == null`, use no-language service overloads and/or `ReadableCategoryMapper` all-lang path; add facade tests for hierarchy and friendlyUrl with null `LanguageCode`.

## Triage

- Decision: `valid`
- Root cause: `resolveLanguage` correctly returns `null` for `lang=_all`, but `getCategoryHierarchy` still calls language-scoped `CategoryService` methods (`getListByDepth`, `getListByDepthFilterByFeatured`) that dereference `language.getId()`. `getCategoryByFriendlyUrl` calls `getBySeUrl(store, url, language)` and `ReadableCategoryPopulator`, both of which require a non-null `Language`.
- Fix: When `language == null`, route hierarchy through `getListByDepth(store, depth)` (with in-memory featured filtering) and friendly-URL lookup through `listBySeUrl` plus `ReadableCategoryMapper.convert(..., null)` — the same all-languages path already used by `getById`.
- Verification: `./mvnw -pl sm-shop -am test -Dtest=CategoryFacadeImplTest,CategoryApiTest -DfailIfNoTests=false`
