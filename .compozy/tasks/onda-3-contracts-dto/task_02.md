---
status: pending
title: ProductSnapshot and index payload evolution
type: backend
complexity: high
---

# ProductSnapshot and index payload evolution

## Overview
Consolidates TLC T7–T12. Delivers canonical `ProductSnapshot` DTO, builder from JPA `Product`, and `ProductIndexPayload` mapper with `schemaVersion` 2 per ADR-002.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST define `ProductSnapshot` and nested DTOs in `shopizer-api-contracts` — TLC T7.
2. MUST implement `ProductSnapshotBuilder` in monolith (sm-core or sm-shop) mapping from `Product` + store + language — TLC T8–T9.
3. MUST refactor existing index producer to build snapshot first, then map to `ProductIndexPayload` — TLC T10.
4. MUST bump `schemaVersion` default to 2 when snapshot-backed — TLC T11.
5. MUST update `search-service` index handler to accept schema v1 and v2 — TLC T12.
6. MUST NOT add JPA dependencies to api-contracts.
</requirements>

## Subtasks
- [ ] 2.1 `ProductSnapshot` DTO + Jackson tests (T7)
- [ ] 2.2 `ProductSnapshotBuilder` from catalog services (T8–T9)
- [ ] 2.3 `ProductIndexPayloadMapper` from snapshot (T10–T11)
- [ ] 2.4 search-service v2 index acceptance (T12)

## Implementation Details
See TechSpec: **ProductIndexPayload evolution**, ADR-002. Source: existing `ProductIndexPayloadBuilder` / `SearchIndexProducerHttp` from Wave 2.

### Relevant Files
- `shopizer-api-contracts/.../search/ProductIndexPayload.java`
- `sm-core/.../events/products/` — index listeners
- `sm-shop/.../strangler/search/` — HTTP producer
- `search-service/.../index/` — internal index API

### Dependent Files
- `shopizer-api-contracts/.../catalog/ProductSnapshot.java` — create
- `sm-core/.../catalog/ProductSnapshotBuilder.java` — create
- `sm-shop/.../search/ProductIndexPayloadMapper.java` — create

### Related ADRs
- [ADR-002: ProductSnapshot supersedes ProductIndexPayload](../adrs/adr-002.md)

## Deliverables
- ProductSnapshot DTO family
- Builder + payload mapper
- search-service backward-compatible index handler
- Unit tests 80%+ on DTOs and mapper **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] Snapshot builder maps SKU, name, store code
  - [ ] Payload schemaVersion 2 when built from snapshot
  - [ ] v1 payload still deserializes
- Integration tests:
  - [ ] Index producer posts v2 payload to search-service test slice
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- SNP-ready milestone
- AD-009 evolution path implemented
