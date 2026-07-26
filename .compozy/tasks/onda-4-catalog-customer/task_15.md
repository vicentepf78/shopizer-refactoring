---
status: pending
title: Docker Compose wave4, gate, STATE
type: infra
complexity: medium
---

# Docker Compose wave4, gate, STATE

## Overview
Consolidates TLC T35–T38. Packages docker-compose-wave4.yml, Wave4 integration suite, reactor `./mvnw clean install` gate, updates STATE.md and requirement traceability to Verified.

<requirements>
1. MUST create `docker-compose-wave4.yml` extending Wave 2 with catalog:8086 + customer:8087 — T35.
2. MUST consolidate `*Wave4*Integration*Test` suite — T36.
3. MUST pass `./mvnw clean install` full reactor — T37.
4. MUST update STATE.md, spec traceability 30/30 Verified, design status — T38.
5. MUST validate `docker compose -f docker-compose-wave4.yml config`.
6. MUST map all 30 requirement IDs (CAT/CUS/STR) without gaps.
</requirements>

## Subtasks
- [ ] 15.1 docker-compose-wave4.yml (T35)
- [ ] 15.2 Integration suite (T36)
- [ ] 15.3 Reactor gate (T37)
- [ ] 15.4 STATE + traceability (T38)

## Related ADRs
- [ADR-001](adrs/adr-001.md)

## Deliverables
- docker-compose-wave4.yml
- Integration suite + install evidence **(REQUIRED)**
- STATE.md updated

## Tests
- `docker compose -f docker-compose-wave4.yml config`
- `./mvnw test -pl sm-shop -Dtest=*Wave4*Integration*Test`
- `./mvnw clean install`
- `./mvnw test -pl sm-shop -Dtest=Wave4ConsumerPactTest -DfailIfNoTests=false`

## Success Criteria
- Wave 4 topology reproducible
- Reactor gate green
- 30/30 requirements Verified in spec.md
- Onda 4 ready to declare Execute complete in STATE
