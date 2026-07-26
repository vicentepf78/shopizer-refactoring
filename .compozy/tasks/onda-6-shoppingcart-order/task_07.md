---
status: pending
title: order-service Boot + read REST + internal totals (OR-read-ready partial)
type: backend
complexity: high
---

# order-service Boot + read REST + internal totals (OR-read-ready partial)

## Overview
TLC T18–T20, T53, T55. Spring Boot :8087; public order read APIs; internal totals endpoint; JWT + internal token filter.

<requirements>
1. MUST scaffold order-service :8087 — T18.
2. MUST implement GET order, list, status history — T19, ORD-01–03.
3. MUST implement `POST /internal/v1/orders/totals` — T20; retire sm-shop-only endpoint from task_02.
4. MUST add ReadableOrder mappers — T53; JWT — T55.
</requirements>

## Subtasks
- [ ] 7.1 Boot app (T18)
- [ ] 7.2 Public read REST (T19, T53)
- [ ] 7.3 Internal totals API (T20)
- [ ] 7.4 Security config (T55)

## Deliverables
- `order-service` deployable JAR
- `OrderReadApiIntegrationTest`, `InternalTotalsControllerTest` **(REQUIRED)**

## Success Criteria
- shoppingcart-service can call totals on order-service
- No JPA in JSON responses
