---
status: pending
title: Correlation ID + health indicators Wave5
type: infra
complexity: medium
---

# Correlation ID + health indicators Wave5

## Overview
Consolidates TLC T29. Adds correlation filter to integration-service; health indicators for DB, module registry, reference-service, catalog-service; RestTemplate interceptor verification in sm-shop.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST add `CorrelationIdFilter` to integration-service — STR-08.
2. MUST add health indicators: datasource, paymentModules bean, reference ping, catalog ping.
3. MUST verify sm-shop RestTemplate propagates `X-Correlation-Id` to integration-service — STR-05.
4. MUST expose indicators on `/actuator/health` — STR-08.
</requirements>

## Subtasks
- [ ] 10.1 CorrelationIdFilter + tests
- [ ] 10.2 Custom HealthIndicators
- [ ] 10.3 Correlation propagation integration test

## Deliverables
- Health + correlation infrastructure
- `IntegrationServiceHealthTest` **(REQUIRED)**

## Success Criteria
- Actuator health shows UP/DOWN per dependency
- Correlation id appears in integration-service logs when called from BFF
