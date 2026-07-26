---
status: pending
title: Correlation ID + health indicators Wave4
type: infra
complexity: medium
---

# Correlation ID + health indicators Wave4

## Overview
Consolidates TLC T31. Adds CorrelationIdFilter to catalog-service and customer-service; health indicators for db, reference, merchant (catalog).

<requirements>
1. MUST propagate X-Correlation-Id in Wave 4 services and RestTemplate interceptor — T31.
2. MUST add actuator health: catalog → db, referenceService, merchantService.
3. MUST add actuator health: customer → db, referenceService.
4. MUST match Wave 1–2 health response patterns.
</requirements>

## Deliverables
- Filters + health indicators
- Health integration tests **(REQUIRED)**

## Tests
- `./mvnw test -pl catalog-service,customer-service -Dtest=*Health*Test,*Correlation*Test`

## Success Criteria
- /actuator/health shows dependency components
- Correlation id present in cross-service logs (manual smoke OK)
