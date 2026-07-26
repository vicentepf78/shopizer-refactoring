---
status: pending
title: Docker Compose wave6, E2E, STATE/ROADMAP gate
type: infra
complexity: medium
---

# Docker Compose wave6, E2E, STATE/ROADMAP gate

## Overview
TLC T43–T45. Full `docker-compose-wave6.yml`; Wave6E2EIntegrationTest (cart → totals → place order → read order); update STATE.md and ROADMAP.md marking Onda 6 complete.

<requirements>
1. MUST add docker-compose-wave6.yml with all dependent services — T43.
2. MUST pass `docker compose -f docker-compose-wave6.yml config` — T43.
3. MUST implement Wave6E2EIntegrationTest — T44, CHK-01.
4. MUST update STATE.md (AD-020+) and ROADMAP.md — T45.
5. SHOULD run `./mvnw clean install` when full reactor includes all waves.
</requirements>

## Deliverables
- docker-compose-wave6.yml + Dockerfiles
- E2E test
- STATE.md + ROADMAP.md updates

## Tests
- `./mvnw test -pl sm-shop -Dtest=Wave6E2EIntegrationTest`
- `docker compose -f docker-compose-wave6.yml config`

## Success Criteria
- E2E green against compose topology
- Onda 6 marked complete in project docs
