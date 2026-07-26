---
status: pending
title: Cart merge on login + correlation/health Wave6
type: backend
complexity: medium
---

# Cart merge on login + correlation/health Wave6

## Overview
TLC T37–T38. Anonymous cart merge on customer login; actuator health for wave6 dependencies; correlation ID on all Wave6 RestTemplate calls.

<requirements>
1. MUST implement mergeAnonymousCart in shoppingcart-service — T37, CART-08.
2. MUST hook BFF customer login to call merge — T37.
3. MUST add health indicators for catalog/order/customer/integration deps — T38, STR-05.
4. MUST propagate X-Correlation-Id on all wave6 clients — T38, STR-06.
</requirements>

## Deliverables
- Cart merge API + BFF hook
- Health indicators + correlation tests **(REQUIRED)**

## Success Criteria
- `CartMergeIntegrationTest` green
- Health shows DOWN when dependency unavailable
