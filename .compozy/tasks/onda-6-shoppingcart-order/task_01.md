---
status: pending
title: Gate Ondas 3–5 + Wave6 contracts and Strangler config
type: backend
complexity: medium
---

# Gate Ondas 3–5 + Wave6 contracts and Strangler config

## Overview
Consolidates TLC T1–T5, T46–T51. Verifies Ondas 3–5 gate; delivers cart/order/checkout DTOs and HTTP clients in `shopizer-api-contracts`; Wave6 Strangler profile and RestTemplate client stubs in sm-shop.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST add `scripts/wave6-gate.sh` verifying Ondas 3–5 artifacts and green tests — TLC T1.
2. MUST add cart/order/checkout DTOs (`CartLineSnapshot`, `OrderSnapshot`, `CartTotalsRequest/Response`, `CheckoutCommitRequest/Response`) — TLC T2, T46, T47.
3. MUST add client interfaces `ShoppingCartServiceClient`, `OrderServiceClient`, `CartTotalsClient`, `CheckoutCommitClient` — TLC T3.
4. MUST add profile `strangler-wave6`, properties `wave6.*`, three strangler flags + `wave6.totals.http.enabled` — TLC T5.
5. MUST implement RestTemplate stubs for all Wave6 clients — TLC T48–T51.
6. MUST NOT import `com.salesmanager.core.model` in contracts.
7. MUST NOT start without Ondas 3–5 gate passing.
</requirements>

## Subtasks
- [ ] 1.1 Gate script + STATE.md prerequisite note (T1)
- [ ] 1.2 DTO packages in shopizer-api-contracts (T2, T46, T47)
- [ ] 1.3 Client interfaces (T3)
- [ ] 1.4 Checkout saga DTOs (T4)
- [ ] 1.5 Wave6ClientConfig + properties (T5)
- [ ] 1.6 RestTemplate client stubs (T48–T51)

## Implementation Details
See TechSpec: **Key interfaces**, **Configuration**. Reuse Wave1/Wave2 `WaveNClientConfig` pattern. Extend Onda 3 snapshot types where they already exist.

### Relevant Files
- `shopizer-api-contracts/` — new cart/order/checkout packages
- `sm-shop/.../strangler/config/Wave6ClientConfig.java` — to create
- `sm-shop/src/main/resources/application-strangler-wave6.properties` — to create
- `scripts/wave6-gate.sh` — to create

### Related ADRs
- [ADR-001: One workflow](adrs/adr-001.md)
- [ADR-008: Feature flags](adrs/adr-008.md)

## Deliverables
- Gate script + contracts + Wave6 config
- Unit tests: DTO serialization, Wave6ClientConfig **(REQUIRED)**

## Tests
- Unit: `Wave6ContractsSerializationTest`, `Wave6ClientConfigTest`
- Gate: `./scripts/wave6-gate.sh && ./mvnw test -pl shopizer-api-contracts,sm-shop -Dtest=Wave6*Test`

## Success Criteria
- Gate script exits 0
- Contracts compile isolated
- All Wave6 tests passing
