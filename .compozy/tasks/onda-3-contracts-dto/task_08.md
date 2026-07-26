---
status: pending
title: ReferencesApi DTO fix and facade migration plan
type: backend
complexity: medium
---

# ReferencesApi DTO fix and facade migration plan

## Overview
Consolidates TLC T35–T38. Closes blocker B-002 on ReferencesApi and publishes phased inventory for remaining facade migrations (FAC-06).

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST return `List<ReadableLanguage>` from language list endpoints — TLC T35, REF-01.
2. MUST return `List<ReadableCurrency>` from currency endpoints — TLC T36, REF-02.
3. MUST wire existing populators/mappers (Wave 1 pattern) — TLC T37.
4. MUST add `docs/decomposition/FACADE-MIGRATION-PLAN.md` inventory of 76 facades with Wave 4–6 phases — TLC T38, FAC-06.
5. MUST update Reference Pact if response types change.
</requirements>

## Subtasks
- [ ] 8.1 ReferencesApi language DTO wiring (T35)
- [ ] 8.2 ReferencesApi currency DTO wiring (T36)
- [ ] 8.3 Integration test ReferencesApi (T37)
- [ ] 8.4 Facade migration plan document (T38)

## Implementation Details
See TechSpec: **ReferencesApi fix (B-002)**. `ReadableLanguage` / `ReadableCurrency` already exist in api-contracts from Wave 1.

### Relevant Files
- `sm-shop/.../api/v1/references/ReferencesApi.java`
- `shopizer-api-contracts/.../reference/ReadableLanguage.java`
- `sm-shop/.../populator/references/`

### Dependent Files
- `docs/decomposition/FACADE-MIGRATION-PLAN.md` — create

### Related ADRs
- [ADR-003: Remaining facades deferred](../adrs/adr-003.md)

## Deliverables
- B-002 closed on ReferencesApi
- Facade migration plan markdown
- Pact/reference tests green **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] ReferencesApi JSON contains no JPA entity type names
- Integration tests:
  - [ ] `GET /api/v1/languages` returns ReadableLanguage shape
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- B-002 resolved
- FAC-06 document published
