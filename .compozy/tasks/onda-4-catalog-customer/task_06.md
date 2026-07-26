---
status: pending
title: ProductSnapshot builder + search v2 migration
type: backend
complexity: high
---

# ProductSnapshot builder + search v2 migration

## Overview
Consolidates TLC T21–T23. Replaces `ProductIndexPayloadBuilder` with `ProductSnapshotBuilder` in monolith; extends search-service to accept ProductSnapshot v2; updates SearchIndexProducerHttp.

<requirements>
1. MUST implement ProductSnapshotBuilder producing v2 from catalog read model — T21.
2. MUST extend search-service index service for v2 deserialization — T22.
3. MUST update SearchIndexProducerHttp to send v2 — T23.
4. MUST maintain temporary v1 acceptance in search during migration.
5. MUST NOT require catalog-service uptime for monolith write-side indexing (builder local).
</requirements>

## Subtasks
- [ ] 6.1 ProductSnapshotBuilder + unit tests (T21)
- [ ] 6.2 search-service v2 intake (T22)
- [ ] 6.3 Producer migration (T23)

## Related ADRs
- [ADR-003](adrs/adr-003.md)

## Deliverables
- Builder + producer migration
- Tests for v2 index round-trip **(REQUIRED)**

## Tests
- `./mvnw test -pl sm-core -Dtest=*ProductSnapshotBuilder*Test`
- `./mvnw test -pl search-service -Dtest=*Index*Test`
- `./mvnw test -pl sm-shop -Dtest=*SearchIndexProducer*Test`

## Success Criteria
- Index event produces v2 snapshot
- search-service indexes v2 document
