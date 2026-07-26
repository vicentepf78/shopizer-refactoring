---
provider: manual
pr:
round: 2
round_created_at: 2026-07-26T22:52:00Z
status: resolved
file: sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/references/ReferencesApi.java
line: 1
severity: high
author: claude-code
provider_ref:
---

# Issue 004: References country/zones NPE when lang=_all

## Review Comment

`ReferencesApi` passes the resolved `Language` entity directly to `countryFacade.getListCountryZones` and `zoneFacade.getZones`. With `?lang=_all`, language is `null`, so `CountryServiceImpl.listCountryZones` NPEs on `language.getId()` before population. Even if the service tolerated null, `ReadableCountryPopulator` and `ReadableZonePopulator` call `language.getId()` when matching zone descriptions.

This is a behavioral regression on `/api/v1/country` and `/api/v1/zones` for clients using the documented `_all` sentinel (B-002 scope).

**Suggested fix:** null-guard at `ReferencesApi` or facade layer and skip language filtering (return all descriptions / first available), consistent with category all-languages behavior.

## Triage

- Decision: `valid`
- Root cause: `LanguageUtils.getRESTLanguage` returns `null` for `lang=_all`, but `ReferencesApi` forwarded that null to facades/services/populators that require a non-null `Language` (`CountryServiceImpl.listCountryZones`, `ZoneServiceImpl.getZones`, `ReadableCountryPopulator`, `ReadableZonePopulator`).
- Fix: Guard at the API boundary with `resolveLanguage(Language)` — when language is null, resolve via `languageUtils.getServiceLanguage(null)` (system default), matching `reference-service` `LanguageResolver` behavior for `_all`/blank `lang`. Country and zone DTOs expose a single localized `name`, so default-language resolution is the correct parity path (unlike category endpoints that return multi-language payloads).
- Test: `ReferencesApiTest.getCountry_withNullLanguage_resolvesDefaultLanguageBeforeFacade` and `getZones_withNullLanguage_resolvesDefaultLanguageBeforeFacade`.
- Verification: `./mvnw -pl sm-shop -am test -Dtest=ReferencesApiTest -DfailIfNoTests=false`
