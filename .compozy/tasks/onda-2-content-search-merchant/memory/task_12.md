# Task Memory: task_12.md

## Objective Snapshot

Correlation ID end-to-end Wave2 + custom health indicators (content/search/merchant) + correlationId in sm-shop 503 strangler errors.

## Important Decisions

- Reused Wave1 `CorrelationIdFilter` / `RestClientConfig.correlationInterceptor()` pattern across Wave2 services and sm-shop `Wave2ClientConfig`.
- Health indicators follow Wave1 composite style (single bean per service: `content`, `search`, `merchant` actuator component names).
- sm-shop `ErrorEntity.correlationId` populated from MDC in `RestErrorHandler.createErrorEntity`.

## Learnings

- Spring Boot strips `HealthIndicator` suffix for actuator component keys (`ContentHealthIndicator` → `components.content`).
- CMS health DOWN test needs path whose parent exists as a file (existing file path alone returns UP).

## Files / Surfaces

- `merchant-service/.../health/MerchantHealthIndicator.java` (new)
- Tests: CorrelationIdFilter, *HealthIndicatorTest, RestClientConfigTest, ActuatorHealthIntegrationTest (content/merchant)
- `sm-shop/.../ErrorEntity.java`, `RestErrorHandler.java`, `Wave2ClientConfigTest.java`
- Extended `SearchServiceIntegrationTest` health assertions

## Errors / Corrections

- Actuator jsonPath initially used `contentHealthIndicator`; fixed to `content`/`search`/`merchant`.
- CMS inaccessible test fixed: use `file/nested` path so `createDirectories` fails.

## Pronto para próxima execução

Task complete. Wave2 ops can use `/actuator/health` components and trace 503s via `correlationId`.
