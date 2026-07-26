---
status: pending
title: Docker Compose, integration gate, STATE
type: infra
complexity: medium
---

# Docker Compose, integration gate, STATE

## Overview
Consolidates TLC T33–T38. Removes duplicate plugin beans when strangler enabled; adds `docker-compose-wave5.yml` and `Dockerfile.wave5`; runs cross-service integration gate; adds JaCoCo verify limits; updates STATE.md and ROADMAP.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST fix duplicate `paymentModules` beans in strangler profile — T33.
2. MUST add `docker-compose-wave5.yml` with integration-service :8086 — T34, STR-10.
3. MUST add `Dockerfile.wave5` expecting prebuilt JAR — T35.
4. MUST run cross-service health gate script/test — T36.
5. MUST add JaCoCo limits on integration-service + sm-integration-core — T37.
6. MUST update `.specs/project/STATE.md` and ROADMAP with Wave 5 evidence — T38.
7. MUST run `./mvnw clean install` before marking complete.
</requirements>

## Subtasks
- [ ] 12.1 Bean deduplication strangler profile (T33)
- [ ] 12.2 docker-compose-wave5.yml + Dockerfile (T34–T35)
- [ ] 12.3 Integration gate test/script (T36)
- [ ] 12.4 JaCoCo pom configuration (T37)
- [ ] 12.5 STATE.md + ROADMAP update (T38)

## Deliverables
- Docker topology for local Wave 5
- JaCoCo verify gates
- Updated STATE.md
- `docker compose -f docker-compose-wave5.yml config` passes **(REQUIRED)**

## Tests
- [ ] Cross-service: reference + catalog (partial) + integration + shop health
- [ ] `./mvnw clean install` green

## Success Criteria
- Wave 5 gate documented with evidence
- GAP-INT-01..05 listed in STATE
- All tests passing; coverage gates met
