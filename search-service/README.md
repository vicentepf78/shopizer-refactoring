# search-service

Wave 2 search REST service (OpenSearch query/index, no JPA). Port **8084**.

## Responsibilities

- Public product search and autocomplete (`POST /api/v1/search`, `/api/v1/search/autocomplete`)
- Internal index API (`/internal/v1/index*`) protected by `X-Internal-Token`
- OpenSearch bootstrap and index management (monolith does **not** connect when `wave2.strangler.enabled=true`)

## Known gaps (GAP-SRCH)

Document-only in Onda 2; fixes are optional unless trivially safe.

| ID | Gap | Onda 2 action |
|----|-----|---------------|
| GAP-SRCH-01 | `ProductService.update()` does not publish a reindex event | Document |
| GAP-SRCH-02 | Inventory/price-only changes do not trigger reindex | Document |
| GAP-SRCH-03 | Delete-image listener is a no-op for search | Document |
| GAP-SRCH-04 | Product reviews can become stale in the index | Document |
| GAP-SRCH-05 | NPE when manufacturer/category description missing for language | Document; trivial fix optional |
| GAP-SRCH-06 | Bulk reindex has no built-in rate limit | Mitigate via `wave2.search.index.reindex-delay-ms` on BFF orchestrator |
| GAP-SRCH-07 | HTTP index failure has no outbox/retry | Document (log-only in producer) |
| GAP-SRCH-08 | `addToCart` field unused in legacy index path | Included in index schema for forward compatibility |
| GAP-SRCH-09 | Category facets return null in BFF | Out of scope Onda 2 |
| GAP-SRCH-10 | `SearchItem` lives in shopizer-commons | Migrate to `shopizer-api-contracts` in Onda 3 |

## Local run

```bash
./mvnw -pl search-service -am spring-boot:run
```

Requires OpenSearch reachable via `search.*` properties in `application.properties`.
