# task_08 memory

## Objective

Strangler in sm-shop: Wave1ClientConfig + RestTemplate, 4 reference HTTP adapters + TaxFacadeHttpAdapter, conditional beans (exactly one per facade), connect/timeout → 503, propagate X-Correlation-Id / Authorization. No public path changes.

## Decisions

- Shared `StranglerRestClient` for correlation/JWT headers + 503/`DownstreamHttpException` mapping (one place for all 5 adapters).
- Language/Currency adapters map contracts Readable* → JPA entity shells so facade interfaces stay unchanged.
- Country/Zone adapters deserialize into shop-model Readable* (JSON-compatible with contracts).
- In-process facades: `@ConditionalOnProperty(havingValue=false, matchIfMissing=true)`; HTTP adapters: `havingValue=true`.
- JaCoCo gate on `com.salesmanager.shop.strangler/**` only (excludes config), minimum 80% lines — not whole sm-shop.

## Touched surfaces

- `sm-shop/.../strangler/**` (config, support, reference adapters, tax adapter)
- In-process facade impls + `RestErrorHandler` (503/downstream handlers)
- `application.properties`, `application-monolith.properties`, `application-strangler.properties`
- `sm-shop/pom.xml` (strangler-scoped JaCoCo)

## Learnings

- MockRestServiceServer `withException` requires `IOException` (e.g. `ConnectException`), not `ResourceAccessException`.
- Full ShopApplication ITs not needed for bean-type gate — lightweight `AnnotationConfigApplicationContext` suffices.

## Status

Implementation + verify green; ready to mark task complete after tracking/commit.
