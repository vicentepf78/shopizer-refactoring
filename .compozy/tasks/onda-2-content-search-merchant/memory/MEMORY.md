# Workflow Memory

## Current State

Wave 2 search strangler on BFF complete (task_07): `SearchFacadeHttpAdapter` active when `wave2.strangler.enabled=true`; monolith OpenSearch auto-config excluded in `strangler-wave2` profile.

## Shared Decisions

- Search query/autocomplete delegate to search-service; bulk reindex stays in BFF via `SearchBulkIndexOrchestrator`.
- Remote search failures surface as 503 (`ServiceUnavailableException`) — no in-process fallback when strangler is on.

## Shared Learnings

- sm-shop strangler tests: use `./mvnw -pl sm-shop -am test` so dependent modules resolve.

## Open Risks

- GAP-SRCH-01..10 documented only; index can be stale until Onda 3+ fixes.

## Handoffs

- task_11 done: sm-shop Wave2 content+merchant strangler (ContentFacadeHttpAdapter, StaticContentProxy, ContentBlobClient, Store/MerchantConfig HTTP adapters, MerchantServiceClient+hydrator+resolver, conditional facade beans). Internal catalog blob API on content-service.
- task_10 done: merchant-service exposes store REST (~18 endpoints, no ProductType), GET /api/v1/config, GET /internal/v1/store/{code}, AD-014 logo orchestration; gate `./mvnw verify -pl merchant-service -am` with JaCoCo ≥80% on facade/populator/client.
- task_04 done: sm-core imports `shopizer-content-cms.xml` before product-only `shopizer-core-cms.xml`; ContentService resolves from sm-content-core via component scan.
