---
status: pending
title: Strangler payment/shipping facades
type: backend
complexity: high
---

# Strangler payment/shipping facades

## Overview
Consolidates TLC T25–T26. Implements `PaymentFacadeHttpAdapter` and `ShippingFacadeHttpAdapter` with `@ConditionalOnProperty(wave5.strangler.enabled)`; maps 503 on remote failure without in-process fallback.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST implement HTTP adapters for `PaymentConfigurationFacade` — T25, STR-01.
2. MUST implement HTTP adapters for `ShippingFacade` / shipping configuration — T26.
3. MUST propagate `X-Correlation-Id` — STR-05.
4. MUST return 503 on connection/timeout errors — STR-02.
5. MUST use stub `IntegrationServiceClientRestTemplateImpl` until task_11 completes full client.
</requirements>

## Subtasks
- [ ] 8.1 PaymentConfigurationFacadeHttpAdapter (T25)
- [ ] 8.2 ShippingFacadeHttpAdapter (T26)
- [ ] 8.3 Adapter unit tests with MockRestServiceServer
- [ ] 8.4 Profile wiring in Wave5ClientConfig

## Related ADRs
- [ADR-001](adrs/adr-001.md)
- [ADR-006](adrs/adr-006.md)

## Deliverables
- Strangler facade adapters in sm-shop
- Adapter tests **(REQUIRED)**

## Success Criteria
- `wave5.strangler.enabled=true` routes to HTTP client
- `matchIfMissing=false` preserves in-process default
