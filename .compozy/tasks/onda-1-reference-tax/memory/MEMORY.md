# Workflow Memory

Keep only durable, cross-task context here. Do not duplicate facts that are obvious from the repository, PRD documents, or git history.

## Current State

- `shopizer-api-contracts` holds common wrappers plus reference/tax DTOs and `ReferenceServiceClient` / `TaxServiceClient`.
- `sm-shop-model` depends on `shopizer-api-contracts` (transitive to `sm-shop`); overlapping shop-model DTOs are `@Deprecated` aliases.

## Shared Decisions

- Contract wrappers live under `com.salesmanager.contracts.common`; shop-model duplicates stay as `@Deprecated` compile aliases (no inheritance re-export — nested types would break).
- Module declares only jackson-annotations + validation-api; MUST NOT add `sm-core-model`. Parent still injects shared compile deps (jackson-databind, mysql, ehcache, etc.) into every child — treat that as reactor inheritance, not a contracts-module dependency.
- Client interfaces use String `storeCode`/`langCode` (and reference iso/zone codes) — never `MerchantStore`/`Language` JPA.
- `ReadableLanguage` wire fields: id, code, sortOrder only.
- Slim `NamedEntity` lives in `com.salesmanager.contracts.catalog` for tax descriptions.
- Address DTOs were not migrated in task_02 (YAGNI for Wave 1 reference endpoints).
- `sm-reference-core` holds reference repos/CRUD plus the shared service slice (`SalesManagerEntityService*`, `CacheUtils`, `Constants`) so thin cores depend on `sm-core-model` without a circular dep on `sm-core`. Init/loader stay in `sm-core` (ADR-007).

## Shared Learnings

- JaCoCo 0.8.8 check at 80% line coverage is configured on `shopizer-api-contracts` (`verify` phase).
- After thin-core extraction, `./mvnw test -pl sm-core` needs `-am` (or a prior install); otherwise Maven looks for the new artifact on `repo.spring.io` and 401s.

## Open Risks

- `sm-tax-core` (task_05) will also need `SalesManagerEntityServiceImpl` — today it arrives only via `sm-core` → `sm-reference-core`. Prefer extracting a neutral shared home or depending on the reference-core slice explicitly; do not reintroduce a cycle with `sm-core`.

## Handoffs

- task_05: mirror thin-core extraction for tax CRUD; resolve generic-service home (see Open Risks).
- task_06: `reference-service` depends on `sm-reference-core` + contracts.
- task_08: strangler adapters can import contracts types transitively from `sm-shop`.
