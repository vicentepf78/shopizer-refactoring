---
status: pending
title: SearchItem migration to api-contracts
type: backend
complexity: medium
---

# SearchItem migration to api-contracts

## Overview
Consolidates TLC T17–T20. Moves `SearchItem` and related search response types from `shopizer-commons` to `shopizer-api-contracts`, resolving Wave 2 OQ-06.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST copy/move `SearchItem`, `SearchProductRequest` (if not already in contracts) to `com.salesmanager.contracts.search` — TLC T17.
2. MUST update `search-service`, `sm-shop` SearchApi/Facade imports to contracts package — TLC T18.
3. MUST add deprecation re-exports in commons (optional thin aliases) for one release — TLC T19.
4. MUST update Wave2 Pact tests to use contracts types — TLC T20.
5. MUST preserve JSON field names byte-compatible with existing Pact.
</requirements>

## Subtasks
- [ ] 4.1 Search DTOs in api-contracts (T17)
- [ ] 4.2 Import rewires in sm-shop and search-service (T18)
- [ ] 4.3 Commons deprecation aliases if needed (T19)
- [ ] 4.4 Pact consumer/provider updates (T20)

## Implementation Details
See TechSpec: **Testing strategy**. Depends on `task_02` for aligned search document model.

### Relevant Files
- `modules/shopizer-commons/` or `modules.commons.search` — current SearchItem
- `sm-shop/.../api/v1/search/SearchApi.java`
- `search-service/.../SearchController.java`
- `sm-shop/src/test/.../pact/` — Wave2 Pact tests

### Dependent Files
- `shopizer-api-contracts/.../search/SearchItem.java` — create
- `shopizer-api-contracts/.../search/SearchProductRequest.java` — verify/move

### Related ADRs
- [ADR-002: ProductSnapshot](../adrs/adr-002.md) — index/query alignment

## Deliverables
- SearchItem in api-contracts
- All modules compile with new imports
- Pact tests green **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] SearchItem Jackson compatibility with legacy JSON fixtures
- Integration tests:
  - [ ] `./mvnw test -pl sm-shop,search-service -Dtest=*Pact* -DfailIfNoTests=false`
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- OQ-06 closed
- No search API path changes
