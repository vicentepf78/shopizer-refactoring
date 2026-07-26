---
status: pending
title: Extract sm-catalog-core read services
type: backend
complexity: high
---

# Extract sm-catalog-core read services

## Overview
Consolidates TLC T5–T8. Creates `sm-catalog-core` with read repositories, read service methods, mappers, and wires sm-core delegation. Admin write methods remain in sm-core (ADR-006).

<requirements>
1. MUST scaffold `sm-catalog-core` Maven module in reactor — T5.
2. MUST move read methods from Product, Category, Manufacturer, Inventory, Pricing services — T6.
3. MUST add ReadableProduct/Category mappers without entity leakage — T7.
4. MUST wire sm-core to delegate reads to sm-catalog-core; writes unchanged — T8.
5. MUST pass sm-catalog-core and sm-core product read tests.
</requirements>

## Subtasks
- [ ] 2.1 Module scaffold + repositories (T5)
- [ ] 2.2 Read service extraction (T6)
- [ ] 2.3 Mappers (T7)
- [ ] 2.4 sm-core delegation (T8)

## Related ADRs
- [ADR-002](adrs/adr-002.md) — read-only boundary
- [ADR-004](adrs/adr-004.md) — thin core
- [ADR-006](adrs/adr-006.md) — writes stay monolith

## Deliverables
- `sm-catalog-core` module with read services
- Unit tests 80%+ on mappers/services **(REQUIRED)**

## Tests
- `./mvnw test -pl sm-catalog-core`
- `./mvnw test -pl sm-core -Dtest=*Product*Test -DfailIfNoTests=false`

## Success Criteria
- Read paths delegate; write paths local
- No circular Maven dependencies
