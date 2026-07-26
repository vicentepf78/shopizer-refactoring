---
status: pending
title: Extract sm-shoppingcart-core + catalog validation
type: backend
complexity: high
---

# Extract sm-shoppingcart-core + catalog validation

## Overview
TLC T7–T9, T60. New `sm-shoppingcart-core` module with cart repositories and `ShoppingCartServiceImpl` using `CartTotalsClient` (no `OrderService`). Catalog line validation via HTTP.

<requirements>
1. MUST scaffold `sm-shoppingcart-core` with cart repositories — T7.
2. MUST move `ShoppingCartServiceImpl`; zero `OrderService` imports — T8, CART-03.
3. MUST add `CatalogLineValidator` calling catalog-service — T9, CART-02.
4. MUST add ArchUnit rule: no OrderService in sm-shoppingcart-core — T60.
</requirements>

## Subtasks
- [ ] 3.1 Maven module + repositories (T7)
- [ ] 3.2 ShoppingCartService with CartTotalsClient (T8)
- [ ] 3.3 CatalogLineValidator HTTP (T9)
- [ ] 3.4 ArchUnit test (T60)

## Implementation Details
Pattern: `sm-content-core` from Onda 2. Entities stay in `sm-core-model`.

### Relevant Files
- `sm-core/.../services/shoppingcart/ShoppingCartServiceImpl.java`
- `sm-core/.../repositories/shoppingcart/`

## Deliverables
- `sm-shoppingcart-core` module
- `./mvnw test -pl sm-shoppingcart-core` green **(REQUIRED)**

## Success Criteria
- ArchUnit passes
- No OrderService in module
