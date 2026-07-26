---
status: pending
title: Shipping plugins + orchestrator + catalog client
type: backend
complexity: high
---

# Shipping plugins + orchestrator + catalog client

## Overview
Consolidates TLC T12–T16. Moves shipping plugins and packaging to `sm-integration-core`, adds V2 adapter, implements `CatalogServiceClient` for `ShippingProductSnapshot`, and extracts `ShippingOrchestratorImpl` from `ShippingServiceImpl`.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- Onda 4 partial catalog read API MUST exist before catalog client implementation
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST relocate shipping `impl/*`, `DefaultPackagingImpl`, preprocessors — T12.
2. MUST implement `ShippingQuoteModuleV2Adapter` — T13.
3. MUST implement HTTP client for catalog shipping snapshots — T14.
4. MUST extract quote assembly, `requiresShipping`, metadata to orchestrator — T15.
5. MUST use `ReferenceServiceClient` for country list — SHP-03.
6. MUST implement GAP-INT-01 fallback with WARN when catalog unavailable.
7. MUST achieve S-ready JaCoCo ≥70% on shipping orchestrator — T16.
</requirements>

## Subtasks
- [ ] 4.1 Move shipping plugins + packaging (T12)
- [ ] 4.2 V2 adapter (T13)
- [ ] 4.3 `CatalogServiceClient` + WireMock test (T14)
- [ ] 4.4 `ShippingOrchestratorImpl` (T15)
- [ ] 4.5 S-ready coverage (T16)

## Related ADRs
- [ADR-007](adrs/adr-007.md) — catalog HTTP

## Deliverables
- Shipping orchestrator + plugins in sm-integration-core
- Catalog client with fallback
- Tests: quote with 2 modules, empty digital cart **(REQUIRED)**

## Success Criteria
- S-ready milestone met
- Quote test uses HTTP catalog fixture, not in-process PricingService
