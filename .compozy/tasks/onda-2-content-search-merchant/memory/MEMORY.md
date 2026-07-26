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

- task_11+: remaining cross-track strangler adapters; search-service README lists gaps.
