---
status: pending
title: catalog-service Boot, clients, public read REST (CAT-ready)
type: backend
complexity: high
---

# catalog-service Boot, clients, public read REST (CAT-ready)

## Overview
Consolidates TLC T9–T13. Delivers `catalog-service` (:8086): Boot app, reference/merchant HTTP clients, public GET REST mirroring ProductApi/CategoryApi/manufacturer/inventory/price, internal ProductSnapshot API, JWT on private reads. **CAT-ready** milestone.

<requirements>
1. MUST scaffold Boot app with JPA + sm-catalog-core — T9.
2. MUST integrate ReferenceServiceClient + MerchantServiceClient — T10.
3. MUST port public GET product/category/manufacturer/inventory/price endpoints — T11.
4. MUST expose `GET /internal/v1/products/{id}/snapshot` with schemaVersion 422 — T12.
5. MUST replicate JWT security for routes requiring auth today — T13.
6. MUST return 503 when dependencies down; no JPA in JSON.
</requirements>

## Subtasks
- [ ] 3.1 Boot scaffold (T9)
- [ ] 3.2 HTTP clients (T10)
- [ ] 3.3 Public read controllers (T11)
- [ ] 3.4 Internal snapshot API (T12)
- [ ] 3.5 Security (T13)

## Related ADRs
- [ADR-002](adrs/adr-002.md)
- [ADR-003](adrs/adr-003.md)
- [ADR-006](adrs/adr-006.md) — no admin writes

## Deliverables
- Deployable catalog-service
- Integration tests product list + category tree + snapshot **(REQUIRED)**

## Tests
- `./mvnw test -pl catalog-service`
- Parity: GET product list vs monolith baseline fixture

## Success Criteria
- CAT-ready milestone achieved
- Port 8086 health UP with MySQL
