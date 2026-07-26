# task_10 memory

## Objective

Wave 1 close-out: docker-compose topology, custom health indicators, X-Correlation-Id filters, reactor `./mvnw clean install` gate.

## Checklist

1. `docker-compose-wave1.yml` (mysql → reference → tax → sm-shop); `docker compose config` exit 0 — DONE (config exit 0)
2. `ReferenceHealthIndicator` (DB) + unit tests UP/DOWN — present
3. `TaxHealthIndicator` (DB + reference HTTP) + unit test DOWN when reference fails — present
4. `CorrelationIdFilter` in reference-service, tax-service, sm-shop (+ tax RestTemplate interceptor) — present
5. Correlation filter unit tests (generate / propagate) — present (3 modules)
6. Smoke checklist documented (compose header) — present
7. `./mvnw clean install` + TaxRateIntegrationTest + Wave1ConsumerPactTest green — DONE (2026-07-26, Java 21)
8. No Eureka/K8s discovery — compose uses WAVE1_* URLs only

## Pre-change signal

- Superseded: compose, health indicators, correlation filters, and unit tests already exist in the working tree from prior work on this task.

## Decisions

- Compose builds from module Dockerfiles that COPY prebuilt Boot jars (`target/*-service.jar`, `sm-shop` jar); document `./mvnw -pl … package` before `compose up`.
- Greenfield seed stays monolito (ADR-007); compose comments note seed prerequisite vs runtime depends_on order MySQL→ref→tax→shop.
- Tax health probes reference via `GET {base}/actuator/health` (not business API).
- Correlation: inbound OncePerRequestFilter + request wrapper so generated id is visible to StranglerRestClient / tax client; tax RestTemplate interceptor forwards header.
- AGENTS.md / CLAUDE.md absent at repo root — grounded on PRD/TechSpec/ADRs only.

## Touched surfaces

- `docker-compose-wave1.yml`, `reference-service/Dockerfile`, `tax-service/Dockerfile`, `sm-shop/Dockerfile.wave1`
- `reference-service/.../health/ReferenceHealthIndicator(+Test)`, `.../web/CorrelationIdFilter(+Test)`
- `tax-service/.../health/TaxHealthIndicator(+Test)`, `.../web/CorrelationIdFilter(+Test)`, `RestClientConfig` interceptor
- `sm-shop/.../filter/CorrelationIdFilter(+Test)`
- `management.endpoint.health.show-components=always` on reference + tax properties
- Pact deps in parent/module poms (needed for gate; originated task_09)

## Learnings

- `@ComponentScan("com.salesmanager.shop")` picks up test `@Configuration` inner classes in that package; keep wiring tests under `com.salesmanager.test.shop.*`.
- Centralize JaCoCo at `${jacoco.version}` (0.8.11) for Java 21 reactor gate.

## Completion note

- `docker compose config` exit 0; health/correlation unit tests green.
- `./mvnw clean install` reactor gate green on Java 21 (2026-07-26).
