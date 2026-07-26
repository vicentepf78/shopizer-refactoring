---
status: pending
title: Pact provider/consumer + IntegrationServiceClient
type: test
complexity: medium
---

# Pact provider/consumer + IntegrationServiceClient

## Overview
Consolidates TLC T30–T32. Implements full `IntegrationServiceClientRestTemplateImpl`; adds `IntegrationProviderPactTest` on integration-service and `Wave5ConsumerPactTest` on sm-shop.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- Follow Wave1/Wave2 Pact patterns
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST implement `IntegrationServiceClientRestTemplateImpl` — T32.
2. MUST add provider pacts for payment config + shipping quote P1 — T30, STR-07.
3. MUST add consumer `Wave5ConsumerPactTest` in sm-shop — T31.
4. MUST preserve `X-Correlation-Id` in client — STR-05.
5. MUST run: `./mvnw -pl sm-shop,integration-service -am test -Dtest=Wave5ConsumerPactTest,IntegrationProviderPactTest -DfailIfNoTests=false`
</requirements>

## Subtasks
- [ ] 11.1 IntegrationServiceClientRestTemplateImpl (T32)
- [ ] 11.2 Provider pact tests (T30)
- [ ] 11.3 Consumer pact tests (T31)
- [ ] 11.4 Replace stub client from task_08

## Deliverables
- Full HTTP client implementation
- Pact provider + consumer tests **(REQUIRED)**

## Success Criteria
- Pact verification green
- Consumer contracts published to target/pacts
