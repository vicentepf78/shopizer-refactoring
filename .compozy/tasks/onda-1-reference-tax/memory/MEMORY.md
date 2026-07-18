# Workflow Memory

Keep only durable, cross-task context here. Do not duplicate facts that are obvious from the repository, PRD documents, or git history.

## Current State

- `shopizer-api-contracts` is in the root reactor (listed before `sm-shop-model`) with package `com.salesmanager.contracts.common` holding the five common wrappers.

## Shared Decisions

- Contract wrappers live under `com.salesmanager.contracts.common`; originals in `sm-shop-model` stay until task_03 rewires consumers.
- Module declares only jackson-annotations + validation-api; MUST NOT add `sm-core-model`. Parent still injects shared compile deps (jackson-databind, mysql, ehcache, etc.) into every child — treat that as reactor inheritance, not a contracts-module dependency.

## Shared Learnings

- JaCoCo 0.8.8 check at 80% line coverage is configured on `shopizer-api-contracts` (`verify` phase).

## Open Risks

## Handoffs

- Next Wave 1 contract work (task_02+): add reference/tax DTOs and client interfaces to this module; do not pull JPA/`sm-core-model`.
- task_03: wire `sm-shop-model` → contracts and deprecate/re-export shop-model duplicates.
