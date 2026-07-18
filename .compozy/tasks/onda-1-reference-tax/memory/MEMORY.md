# Workflow Memory

Keep only durable, cross-task context here. Do not duplicate facts that are obvious from the repository, PRD documents, or git history.

## Current State

- `shopizer-api-contracts` holds common wrappers plus reference/tax DTOs and `ReferenceServiceClient` / `TaxServiceClient`.
- `sm-shop-model` depends on `shopizer-api-contracts` (transitive to `sm-shop`); overlapping shop-model DTOs are `@Deprecated` aliases.
- `sm-reference-core` and `sm-tax-core` are extracted; `sm-core` depends on both. `TaxService*` (calculate) remains in `sm-core` (ADR-003).
- `reference-service` Boot app exists (:8081): depends on `sm-reference-core` + contracts; public P1 REST without JWT.
- `tax-service` Boot app exists (:8082): depends on `sm-tax-core` + contracts; private tax REST with JWT; HTTP client to reference via `wave1.reference-service.base-url`.
- `sm-shop` Strangler: `wave1.strangler.enabled` + profiles `monolith`/`strangler`; HTTP adapters under `com.salesmanager.shop.strangler`.

## Shared Decisions

- Contract wrappers live under `com.salesmanager.contracts.common`; shop-model duplicates stay as `@Deprecated` compile aliases (no inheritance re-export — nested types would break).
- Module declares only jackson-annotations + validation-api; MUST NOT add `sm-core-model`. Parent still injects shared compile deps (jackson-databind, mysql, ehcache, etc.) into every child — treat that as reactor inheritance, not a contracts-module dependency.
- Client interfaces use String `storeCode`/`langCode` (and reference iso/zone codes) — never `MerchantStore`/`Language` JPA.
- `ReadableLanguage` wire fields: id, code, sortOrder only.
- Slim `NamedEntity` lives in `com.salesmanager.contracts.catalog` for tax descriptions.
- Address DTOs were not migrated in task_02 (YAGNI for Wave 1 reference endpoints).
- `sm-reference-core` holds reference repos/CRUD plus the shared service slice (`SalesManagerEntityService*`, `CacheUtils`, `Constants`) so thin cores depend on `sm-core-model` without a circular dep on `sm-core`. Init/loader stay in `sm-core` (ADR-007).
- `sm-tax-core` depends on `sm-reference-core` for that generic slice (not on `sm-core`). Product delete-guard uses `ProductTaxClassCountRepository` in tax-core (query on PRODUCT) so tax-core never depends on catalog repos in sm-core.
- `TaxClassInUseException` lives in `sm-core-model` (`TAX_CLASS_IN_USE`) for HTTP 409 mapping in tax-service / sm-shop.

## Shared Learnings

- JaCoCo 0.8.8 check at 80% line coverage is configured on `shopizer-api-contracts` (`verify` phase).
- After thin-core extraction, `./mvnw test -pl sm-core` (and siblings) needs `-am` (or a prior install); otherwise Maven looks for the new artifact on `repo.spring.io` and 401s.
- Filtered `-Dtest=...` with `-am` needs `-DfailIfNoTests=false` or upstream modules without matching tests fail the reactor.
- Do not put version-less Spring Boot starters in parent `dependencyManagement` — that clears the Boot BOM version and breaks modules that declare those starters.
- On Java 21, JaCoCo needs ≥0.8.11 for Boot IT (0.8.8 hits “Unsupported class file major version 65”).
- `reference-service` resolves language from `lang` (or default); `store` is accepted for path parity only — strangler BFF should pass `lang` when calling it.

## Open Risks

- Monolito sm-shop still may map store-mismatch `UnauthorizedException` → 401; tax-service uses 403 per techspec — strangler adapters propagate tax-service status via `DownstreamHttpException`.
- sm-shop ControllerAdvice for `TaxClassInUseException` → 409 still pending if delete stays in-process when strangler is off.

## Handoffs

- task_08 done: profiles `monolith` / `strangler`; `wave1.strangler.enabled` toggles in-process vs HTTP facade beans; JaCoCo gate only on `sm-shop` strangler package (≥80%).
- task_09: Pact provider tests target `reference-service` 5 endpoints (and tax if in scope); consumer can assume strangler adapters call the same paths as reference/tax services.
