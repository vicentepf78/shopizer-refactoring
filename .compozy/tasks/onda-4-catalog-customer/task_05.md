---
status: pending
title: customer-service Boot, REST, snapshot (CUS-ready)
type: backend
complexity: high
---

# customer-service Boot, REST, snapshot (CUS-ready)

## Overview
Consolidates TLC T18–T20. Delivers `customer-service` (:8087): Boot, profile/address/optin REST, reference client, internal CustomerSnapshot, JWT. Excludes AuthenticateCustomerApi (OQ-06). **CUS-ready** milestone.

<requirements>
1. MUST scaffold Boot + JPA + sm-customer-core — T18.
2. MUST port CustomerApi profile/address/optin sections — T19.
3. MUST expose `GET /internal/v1/customers/{id}/snapshot` — T20.
4. MUST replicate JWT for private routes — T20.
5. MUST NOT expose login/register/password endpoints.
</requirements>

## Subtasks
- [ ] 5.1 Boot scaffold (T18)
- [ ] 5.2 Profile REST + reference client (T19)
- [ ] 5.3 Internal snapshot + security (T20)

## Related ADRs
- [ADR-005](adrs/adr-005.md)
- OQ-06 auth stays monolith

## Deliverables
- Deployable customer-service
- Integration tests profile update + snapshot **(REQUIRED)**

## Tests
- `./mvnw test -pl customer-service`

## Success Criteria
- CUS-ready milestone
- Port 8087 health UP
