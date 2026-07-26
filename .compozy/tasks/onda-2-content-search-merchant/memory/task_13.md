# Task Memory: task_13.md

## Objective Snapshot

Completed Wave2 Pact: consumer `sm-shop-wave2`, providers content/search/merchant; pacts in `pacts/sm-shop-wave2-*.json`; runbook `docs/PACT-WAVE2.md`.

## Important Decisions

- Multipart upload (`POST /api/v1/private/file`) excluded from Pact — covered by `ContentFilesIntegrationTest`; Pact JVM + MockMvc standalone cannot replay multipart reliably.
- Search index valid/invalid split into separate pact methods (same path, different bodies).
- Merchant pact: public store, config, snapshot, private GET, POST create (no ProductType).

## Learnings

- Use minimal JSON strings for `ProductIndexPayload` in consumer tests (full DTO serializes extra fields and breaks pact matching).
- `RestTemplate` throws on 422 — use tolerant error handler for schema-rejection consumer assertion.
- Merchant provider: `lenient()` on argument resolver stubs — not every interaction uses `MerchantStore`/`Language` params.

## Files / Surfaces

- `sm-shop/.../Wave2ConsumerPactTest.java`
- `content-service/.../ContentProviderPactTest.java`
- `search-service/.../SearchProviderPactTest.java`
- `merchant-service/.../MerchantProviderPactTest.java`
- `pacts/sm-shop-wave2-*.json`, `docs/PACT-WAVE2.md`
- pom.xml pact deps on three Wave2 services

## Ready for Next Run

- task_14+ can assume Wave2 pact gate green; extend contracts if new P1 endpoints added to strangler adapters.
