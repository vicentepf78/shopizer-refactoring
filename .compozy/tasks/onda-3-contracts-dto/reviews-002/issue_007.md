---
provider: manual
pr:
round: 2
round_created_at: 2026-07-26T22:52:00Z
status: resolved
file: shopizer-api-contracts/src/main/java/com/salesmanager/contracts/search/SearchItem.java
line: 12
severity: medium
author: claude-code
provider_ref:
---

# Issue 007: SearchItem and ProductIndexPayload lack @JsonIgnoreProperties

## Review Comment

Snapshot DTOs received `@JsonIgnoreProperties(ignoreUnknown = true)` in reviews-001 issue_008, but `SearchItem` (public search REST response, SRCH-01) and `ProductIndexPayload` (versioned internal index contract) do not. With default Jackson settings, any additive JSON field from search-service or a newer index document shape will cause `UnrecognizedPropertyException` on strangler deserialization (`SearchFacadeHttpAdapter`) or `InternalIndexController` ingestion.

**Suggested fix:** add `@JsonIgnoreProperties(ignoreUnknown = true)` to both types and extend `SearchDtoJacksonTest` / index payload tests with unknown-field deserialization cases.

## Triage

- Decision: `valid`
- Notes: `SearchItem` is deserialized from search-service JSON in `SearchFacadeHttpAdapter` without a custom `ObjectMapper`. Default Jackson fails on additive fields (`UnrecognizedPropertyException`). Snapshot DTOs already use `@JsonIgnoreProperties(ignoreUnknown = true)`; `SearchItem` was the gap called out for this issue file. `ProductIndexPayload` is mentioned in the review comment but is out of scope for this batch (`issue_007` targets `SearchItem.java` only).
- Fix: add `@JsonIgnoreProperties(ignoreUnknown = true)` to `SearchItem` and cover unknown-field deserialization in `SearchDtoJacksonTest`.
