# Task Memory: task_02.md

Keep only task-local execution context here. Do not duplicate facts that are obvious from the repository, task file, PRD documents, or git history.

## Objective Snapshot

Migrate reference + tax DTOs into `shopizer-api-contracts` and define JPA-free client interfaces — complete.

## Important Decisions

- Skipped Address/PersistableAddress/ReadableAddress (not on Wave 1 reference endpoints).
- `ReadableLanguage` includes `sortOrder` per techspec/ADR-005.
- `TaxServiceClient` mirrors `TaxFacade` with `storeCode`/`langCode` Strings.
- `NamedEntity` in `com.salesmanager.contracts.catalog`.

## Learnings

- Bundle JaCoCo stays at 100% when serialization + anonymous client stubs exercise DTO getters/setters.

## Files / Surfaces

- `shopizer-api-contracts/.../reference/*` (Country/Zone entities, Readable*, SizeReferences, enums, ReadableCurrency)
- `shopizer-api-contracts/.../tax/*` (9 tax DTOs)
- `shopizer-api-contracts/.../catalog/NamedEntity.java`
- `shopizer-api-contracts/.../client/{Reference,Tax}ServiceClient.java`
- Tests under `reference/`, `tax/`, `client/`

## Errors / Corrections

## Ready for Next Run

Done. Hand off to task_03: wire `sm-shop-model` → contracts; optionally migrate Address DTOs if consumers need them.
