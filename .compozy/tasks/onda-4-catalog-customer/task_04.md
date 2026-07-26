---
status: pending
title: Extract sm-customer-core
type: backend
complexity: high
---

# Extract sm-customer-core

## Overview
Consolidates TLC T14–T17. Creates `sm-customer-core` with customer, optin, attribute services and repositories; wires sm-core delegation. Order-checkout customer creation paths remain in sm-core (GAP-CUS-01).

<requirements>
1. MUST scaffold `sm-customer-core` module — T14.
2. MUST move CustomerService, CustomerOptinService, attribute services (exclude order-only create) — T15.
3. MUST add CustomerSnapshot mapper — T16.
4. MUST wire sm-core delegation for profile paths — T17.
</requirements>

## Subtasks
- [ ] 4.1 Module + repositories (T14)
- [ ] 4.2 Service extraction (T15)
- [ ] 4.3 Mappers + snapshot (T16)
- [ ] 4.4 sm-core wire (T17)

## Related ADRs
- [ADR-004](adrs/adr-004.md)
- [ADR-005](adrs/adr-005.md)

## Deliverables
- sm-customer-core module
- Unit tests 80%+ **(REQUIRED)**

## Tests
- `./mvnw test -pl sm-customer-core`
- `./mvnw test -pl sm-core -Dtest=*Customer*Test -DfailIfNoTests=false`

## Success Criteria
- Profile CRUD works in-process via thin core
- Order create customer unchanged in monolith
