---
status: pending
title: Cart merge decoupling + CustomerFacade orchestration
type: backend
complexity: high
---

# Cart merge decoupling + CustomerFacade orchestration

## Overview
Consolidates TLC T24–T25. Refactors `ShoppingCartService.mergeShoppingCarts` to use `CustomerSnapshot`; CustomerFacade fetches snapshot from customer-service HTTP on login before merge.

<requirements>
1. MUST refactor mergeShoppingCarts to accept CustomerSnapshot or id primitives — T24.
2. MUST update CustomerFacade login flow to call CustomerServiceClient.getSnapshot — T25.
3. MUST fail closed when customer-service unavailable during merge (documented).
4. MUST NOT add HTTP calls inside ShoppingCartServiceImpl.
5. MUST pass cart merge integration test with strangler mock.
</requirements>

## Subtasks
- [ ] 7.1 ShoppingCartService signature refactor (T24)
- [ ] 7.2 CustomerFacade orchestration (T25)
- [ ] 7.3 Login+merge integration test

## Related ADRs
- [ADR-005](adrs/adr-005.md)

## Deliverables
- Merge refactor + facade orchestration
- Integration test login merge **(REQUIRED)**

## Tests
- `./mvnw test -pl sm-core -Dtest=*ShoppingCart*Merge*Test`
- `./mvnw test -pl sm-shop -Dtest=*CustomerFacade*Merge*Test`

## Success Criteria
- Merge works with snapshot input
- No in-process CustomerService required in merge path
