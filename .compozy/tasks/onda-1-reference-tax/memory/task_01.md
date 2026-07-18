# Task Memory: task_01.md

Keep only task-local execution context here. Do not duplicate facts that are obvious from the repository, task file, PRD documents, or git history.

## Objective Snapshot

Scaffold `shopizer-api-contracts` and migrate common DTO wrappers to `com.salesmanager.contracts.common` — complete.

## Important Decisions

- Copied wrappers from shop-model; left shop-model classes in place for task_03.
- Module order: contracts before `sm-shop-model` for future reactor wiring.
- Added `dependencyManagement` entry for `shopizer-api-contracts` alongside module registration.
- JaCoCo line coverage gate 80% on module `verify`.

## Learnings

- Parent `<dependencies>` pollute child classpath (mysql/ehcache/mail); still zero `sm-core-model`.
- Jackson smoke tests need only ObjectMapper (databind inherited from parent).

## Files / Surfaces

- `pom.xml` (module + dependencyManagement)
- `shopizer-api-contracts/pom.xml`
- `shopizer-api-contracts/src/main/java/com/salesmanager/contracts/common/{Entity,ShopEntity,ReadableList,ReadableEntityList,EntityExists}.java`
- `shopizer-api-contracts/src/test/java/.../CommonDtoJacksonTest.java`

## Errors / Corrections

## Ready for Next Run

Done. Hand off to task_02 for reference/tax DTOs + client interfaces on this module.
