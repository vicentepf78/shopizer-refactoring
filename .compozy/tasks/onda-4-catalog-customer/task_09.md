---
status: pending
title: Strangler catalog + customer HTTP adapters
type: backend
complexity: high
---

# Strangler catalog + customer HTTP adapters

## Overview
Consolidates TLC T27–T28. Implements CatalogFacadeHttpAdapter (read only) and CustomerFacadeHttpAdapter (profile/address/optin). Auth methods stay in-process.

<requirements>
1. MUST delegate ProductFacade/ProductCommonFacade/CategoryFacade **read** methods to catalog-service — T27.
2. MUST delegate CustomerFacade profile/address/optin to customer-service — T28.
3. MUST return 503 on remote failure without in-process fallback when strangler on.
4. MUST forward JWT + X-Correlation-Id on private routes.
5. MUST NOT delegate private admin product POST/PUT/DELETE — AD-006.
</requirements>

## Related ADRs
- [ADR-002](adrs/adr-002.md)
- [ADR-006](adrs/adr-006.md)

## Deliverables
- CatalogFacadeHttpAdapter + CustomerFacadeHttpAdapter
- Integration tests per adapter **(REQUIRED)**

## Tests
- `./mvnw test -pl sm-shop -Dtest=*CatalogFacadeHttp*Test,*CustomerFacadeHttp*Test`

## Success Criteria
- Strangler on: GET product remote; POST product local
- Strangler off: in-process behavior preserved
