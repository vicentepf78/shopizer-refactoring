---
status: pending
title: ShoppingCart Strangler adapter + shadow mode (SC-ready)
type: backend
complexity: high
---

# ShoppingCart Strangler adapter + shadow mode (SC-ready)

## Overview
TLC T13–T14, T50. `ShoppingCartFacadeHttpAdapter` with `wave6.shoppingcart.strangler.enabled`; shadow read comparison; cutover runbook. Milestone **SC-ready**.

<requirements>
1. MUST implement HTTP adapter delegating to shoppingcart-service — T13, CART-04.
2. MUST return 503 on remote failure with correlation id — STR-06.
3. MUST implement shadow mode comparing in-process vs remote (log-only) — T14.
4. MUST document `docs/runbooks/wave6-cart-cutover.md` — T14, STR-07.
5. MUST implement `ShoppingCartServiceClientRestTemplateImpl` — T50.
</requirements>

## Subtasks
- [ ] 5.1 Facade HTTP adapter + flag (T13)
- [ ] 5.2 RestTemplate client impl (T50)
- [ ] 5.3 Shadow mode (T14)
- [ ] 5.4 Runbook + SC-ready STATE.md (T14)

## Related ADRs
- [ADR-007: Phasing](adrs/adr-007.md)
- [ADR-008: Rollback](adrs/adr-008.md)

## Deliverables
- Strangler adapter + runbook
- `ShoppingCartFacadeHttpAdapterTest`, `ShoppingCartShadowModeTest` **(REQUIRED)**

## Success Criteria
- SC-ready milestone in STATE.md
- Flag toggles in-process vs remote
