# Task Memory: task_03.md

Keep only task-local execution context here. Do not duplicate facts that are obvious from the repository, task file, PRD documents, or git history.

## Objective Snapshot

Wire `sm-shop-model` → `shopizer-api-contracts` — complete.

## Important Decisions

- Prefer `@Deprecated` + javadoc links over inheritance aliases: nested shop-model types (`List<ReadableZone>`, tax hierarchies) would break if legacy classes extended contracts counterparts.
- Skip Address/PersistableAddress/ReadableAddress (not in contracts; task_02 YAGNI).
- Do not rewrite monolith imports in this task.
- Deprecated 25 overlapping types (common wrappers, reference, tax, NamedEntity) plus package-info on entity/references/tax.

## Learnings

- Online `dependency:tree -pl sm-shop` alone can fail on `repo.spring.io` auth; use `-am` reactor resolve or install sibling modules first.
- Deprecating `Entity`/`ShopEntity` surfaces expected `-Xlint:deprecation` noise in sm-shop-model/sm-shop; compile still succeeds (no `-Werror`).

## Files / Surfaces

- `sm-shop-model/pom.xml` — contracts + junit-jupiter test dep
- `@Deprecated` on duplicate DTOs under entity/, references/, tax/, catalog/NamedEntity
- `package-info.java` in entity/, references/, tax/
- `sm-shop-model/.../ContractsClasspathSmokeTest.java`

## Errors / Corrections

## Ready for Next Run

Done. Hand off: task_04/task_05 cores can depend on contracts via sm-shop-model wiring; adapters (task_08) get contracts transitively through sm-shop.
