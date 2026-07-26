---
status: pending
title: ProductFacadeV2 + wiring guards (admin writes local)
type: backend
complexity: medium
---

# ProductFacadeV2 + wiring guards (admin writes local)

## Overview
Consolidates TLC T29–T30. ProductFacadeV2 read delegation via CatalogServiceClient; ArchUnit/wiring tests proving admin writes never route HTTP.

<requirements>
1. MUST delegate ProductFacadeV2Impl read paths to CatalogServiceClient when strangler on — T29.
2. MUST add Wave4WiringTest or ArchUnit rule: no HTTP on private product mutations — T30.
3. MUST document adapter matrix in code comment referencing AD-006.
4. MUST preserve V1/V2 behavioral parity for GET within documented GAP-CAT-01.
</requirements>

## Related ADRs
- [ADR-006](adrs/adr-006.md)
- OQ-05

## Deliverables
- V2 read delegation
- Wiring guard test **(REQUIRED)**

## Tests
- `./mvnw test -pl sm-shop -Dtest=*ProductFacadeV2*Test,*Wave4Wiring*Test`

## Success Criteria
- V2 GET uses HTTP when strangler on
- Private POST product never uses CatalogFacadeHttpAdapter
