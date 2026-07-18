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

## Shared Learnings

- JaCoCo 0.8.8 check at 80% line coverage is configured on `shopizer-api-contracts` (`verify` phase).

## Open Risks

## Handoffs

- task_04/task_05: extract cores; contracts already available via reactor/`sm-shop-model`.
- task_08: strangler adapters can import contracts types transitively from `sm-shop`.
