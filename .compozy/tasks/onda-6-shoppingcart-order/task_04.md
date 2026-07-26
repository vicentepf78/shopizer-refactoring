---
status: pending
title: shoppingcart-service Boot, REST, internal clear (SC-ready partial)
type: backend
complexity: high
---

# shoppingcart-service Boot, REST, internal clear (SC-ready partial)

## Overview
TLC T10–T12, T52, T54. Spring Boot app :8086; public cart REST mirroring `ShoppingCartApi`; internal post-checkout cart clear; JWT on `/private/**`.

<requirements>
1. MUST scaffold `shoppingcart-service` on port 8086 — T10.
2. MUST implement cart CRUD REST with DTO mappers — T11, CART-01, CART-07.
3. MUST implement `DELETE /internal/v1/carts/{id}/after-checkout` — T12, CHK-06.
4. MUST replicate JWT security pattern from wave1 services — T54.
5. MUST NOT expose JPA entities in JSON.
</requirements>

## Subtasks
- [ ] 4.1 Boot app + JPA + actuator (T10)
- [ ] 4.2 Public cart controllers + mappers (T11, T52)
- [ ] 4.3 Internal clear API (T12)
- [ ] 4.4 JWT config (T54)

## Deliverables
- `shoppingcart-service` deployable JAR
- `ShoppingCartApiIntegrationTest`, `InternalCartControllerTest` **(REQUIRED)**

## Tests
- `./mvnw test -pl shoppingcart-service -Dtest=ShoppingCartApiIntegrationTest,InternalCartControllerTest`

## Success Criteria
- All cart-service tests pass
- Health endpoint UP
