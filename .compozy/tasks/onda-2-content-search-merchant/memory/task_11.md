# Task Memory: task_11.md

## Objective Snapshot

Wire sm-shop Wave2 strangler for content + merchant: HTTP facades, static proxy, catalog blob client, resolver hydrator, conditional beans (T41–T46).

## Important Decisions

- `StranglerRestClient` instantiated with `wave2RestTemplate` (same pattern as SearchFacadeHttpAdapter); no wave2-specific rest client bean.
- Catalog PROPERTY/VARIANT blobs: `InternalCatalogBlobController` on content-service + `ContentBlobClient` in sm-shop; facades use optional `@Autowired(required=false)`.
- Resolver uses `MerchantServiceClient` + hydrator when bean present; `StoreFacadeHttpAdapter.get()` also hydrates from snapshot.
- In-process facades gated with `@ConditionalOnProperty(wave2.strangler.enabled=false, matchIfMissing=true)`.

## Learnings

- Public content paths forward correlation only; JWT on `/private/**` (test: `ContentFacadeHttpAdapterTest.privateEndpoint_forwardsJwtAndCorrelation`).
- `./mvnw -pl sm-shop -am test` green after wiring; content-service tests green with new internal blob controller.

## Files / Surfaces

- sm-shop strangler: content/*, merchant/*, Wave2ClientConfig, conditional on ContentFacadeImpl/StoreFacadeImpl/MerchantConfigurationFacadeImpl
- sm-shop: ImagesController, FilesController, ProductOptionFacadeImpl, ProductVariantGroupFacadeImpl, MerchantStoreArgumentResolver
- content-service: InternalCatalogBlobController

## Pronto para próxima execução

- task_12+ can assume Wave2 BFF adapters wired; Pact/compose validation may follow in later tasks.
