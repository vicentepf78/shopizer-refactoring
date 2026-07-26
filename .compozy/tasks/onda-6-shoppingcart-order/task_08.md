---
status: pending
title: Order read Strangler adapter (OR-read-ready)
type: backend
complexity: medium
---

# Order read Strangler adapter (OR-read-ready)

## Overview
TLC T21, T49. `OrderFacadeHttpAdapter` for read paths; `wave6.order.strangler.enabled`. Milestone **OR-read-ready**.

<requirements>
1. MUST delegate order GET/list/history to order-service when flag on — T21, ORD-05.
2. MUST keep checkout/write paths on CheckoutApplicationService (not this adapter).
3. MUST implement `OrderServiceClientRestTemplateImpl` — T49.
4. MUST record OR-read-ready in STATE.md.
</requirements>

## Deliverables
- Order strangler adapter + client impl
- `OrderFacadeHttpAdapterTest` **(REQUIRED)**

## Success Criteria
- Read paths remote when flag on
- OR-read-ready milestone recorded
