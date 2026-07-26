# Task Memory: task_14.md

## Objective Snapshot

Wave 2 close-out: docker-compose-wave2.yml, integration suite docs, `./mvnw clean install` gate, traceability + STATE.

## Important Decisions

- Compose follows wave1 pattern (pre-built JARs, healthchecks, env URLs); CMS volume `wave2-cms-blobs` only on content-service (ADR-008).
- tax-service omitted from wave2 compose (task T51 scope); BFF still has WAVE1_TAX_BASE_URL for wave1 coexistence.
- JaCoCo strangler gate (80%) required extra unit tests for Wave2 adapters/clients added during this task.

## Learnings

- sm-shop strangler JaCoCo bundle was ~52% before task_14; expanded adapter/client tests brought it to ≥80%.
- OpenSearch in compose: `opensearchproject/opensearch:1.3.14`, security disabled for dev.

## Files / Surfaces

- `docker-compose-wave2.yml`, Dockerfiles (content/search/merchant + `sm-shop/Dockerfile.wave2`)
- `docs/WAVE2-INTEGRATION-TESTS.md`
- `.specs/features/onda-2-content-search-merchant/spec.md`, `design.md`, `.specs/project/STATE.md`
- sm-shop strangler test expansions (ContentBlobClient, hydrator, cached client, adapter coverage)

## Errors / Corrections

- Initial `./mvnw clean install` failed sm-shop JaCoCo (52% strangler); fixed with targeted Wave2 adapter tests.

## Pronto para próxima execução

- Onda 2 declared complete in STATE; next wave is Onda 3 Specify/Execute.
- Local smoke: `docker compose -f docker-compose-wave2.yml config` then package + up.
