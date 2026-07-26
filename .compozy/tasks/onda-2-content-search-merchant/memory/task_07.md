# Task Memory: task_07.md

## Objective Snapshot

Strangler Search on BFF: `SearchFacadeHttpAdapter`, disable monolith OpenSearch on wave2, document GAP-SRCH-01..10.

## Important Decisions

- Reuse `StranglerRestClient` instantiated with `wave2RestTemplate` (not a Spring bean) — same 503/correlation behavior as wave1 adapters.
- Disable monolith OpenSearch via `spring.autoconfigure.exclude=SearchAutoConfiguration` + `@ConditionalOnProperty` on `SearchServiceImpl` / `ApplicationSearchConfiguration` + `search.noindex=true` in `application-strangler-wave2.properties` (no Maven profile pom change).
- `indexAllData` stays local via `SearchBulkIndexOrchestrator`; only query/autocomplete go HTTP.

## Learnings

- Focused tests must use `./mvnw -pl sm-shop -am test -Dtest=...` so sm-core/sm-content-core are on the classpath.
- Full sm-shop integration suite has pre-existing ApplicationContext failures unrelated to this task.

## Files / Surfaces

- `sm-shop/.../strangler/search/SearchFacadeHttpAdapter.java` (new)
- `sm-shop/.../SearchFacadeImpl.java` — `@ConditionalOnProperty` in-process
- `sm-core/.../SearchServiceImpl.java`, `ApplicationSearchConfiguration.java` — conditional
- `sm-shop/.../application-strangler-wave2.properties` — OpenSearch exclude
- `search-service/README.md` — Known gaps
- Tests: `SearchFacadeHttpAdapterTest`, `SearchStranglerConditionalBeanTest`, `Wave2OpenSearchDisabledTest`, `SearchServiceReadmeGapsTest`

## Errors / Corrections

- Running `-pl sm-shop test` without `-am` causes NoClassDefFound / stale class failures; always `-am` for this module.

## Ready for Next Run

- task_11 can wire cross-track strangler; search BFF trilha closed.
- GAP-SRCH fixes remain document-only unless trivial.
