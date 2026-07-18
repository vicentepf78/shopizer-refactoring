---
status: pending
title: Merchant REST, snapshot interno e logo AD-014
type: backend
complexity: high
---

# Merchant REST, snapshot interno e logo AD-014

## Overview
Consolida TLC T35–T40. Porta Store/Config facades, REST de loja (~18 endpoints sem ProductType), `GET /config`, snapshot interno, orquestração de logo blob-first (AD-014) e gate do módulo merchant-service. Completa a trilha merchant antes do Strangler BFF.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST portar `StoreFacadeImpl` + populators usando Reference HTTP — TLC T35.
2. MUST portar `MerchantConfigurationFacade` e `GET /api/v1/config` — TLC T36.
3. MUST espelhar `MerchantStoreApi` (~18 endpoints ativos) **sem rotas ProductType** — TLC T37 / ADR-007.
4. MUST expor `GET /internal/v1/store/{code}` → `MerchantStoreSnapshot` — TLC T38.
5. MUST orquestrar logo: upload blob-first + compensate em falha DB; delete DB-first tolerando orphan blob WARN — TLC T39 / ADR-006.
6. MUST proteger loja default contra exclusão; MUST usar DTOs contracts no JSON.
7. MUST passar gate completo `./mvnw test -pl merchant-service` — TLC T40.
</requirements>

## Subtasks
- [ ] 10.1 Port StoreFacade + populators (T35)
- [ ] 10.2 Config facade + PublicConfigsController (T36)
- [ ] 10.3 MerchantStoreController REST sem ProductType (T37)
- [ ] 10.4 InternalStoreController snapshot (T38)
- [ ] 10.5 Logo orchestration AD-014 (T39)
- [ ] 10.6 Gate módulo merchant-service (T40)

## Implementation Details
Ver TechSpec: **Endpoints merchant-service**, **Ordem de construção** passos 25–26. Fontes: `StoreFacadeImpl`, `MerchantStoreApi`, `PublicConfigsApi`.

### Relevant Files
- `sm-shop/src/main/java/com/salesmanager/shop/store/controller/store/facade/StoreFacadeImpl.java` — facade
- `sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/store/MerchantStoreApi.java` — paths
- `sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/system/PublicConfigsApi.java` — config pública
- `sm-shop/src/test/java/com/salesmanager/test/shop/integration/store/MerchantStoreApiIntegrationTest.java` — paridade
- `shopizer-api-contracts/.../merchant/MerchantStoreSnapshot.java` — snapshot

### Dependent Files
- `merchant-service/.../facade/` — facades portadas
- `merchant-service/.../populator/` — populators
- `merchant-service/.../api/v1/store/` — REST store
- `merchant-service/.../api/v1/system/` — config
- `merchant-service/.../api/internal/InternalStoreController.java` — snapshot
- `merchant-service/.../facade/StoreFacadeImpl.java` — logo AD-014

### Related ADRs
- [ADR-006: Logo blob-first com compensação](adrs/adr-006.md) — orquestração
- [ADR-007: Sem ProductType](adrs/adr-007.md) — exclusão rotas
- [ADR-005: APIs internas](adrs/adr-005.md) — snapshot

## Deliverables
- REST merchant P1 + config + snapshot + logo
- Gate módulo merchant-service verde
- Unit tests facades 80%+ **(REQUIRED)**
- Integration tests store/config/logo/snapshot **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] StoreFacade create/update usa ReferenceServiceClient (mock)
  - [ ] Populators não serializam entidades JPA
- Integration tests:
  - [ ] Zero rotas ProductType registradas
  - [ ] GET /config retorna flags + social
  - [ ] Snapshot interno retorna MerchantStoreSnapshot por code
  - [ ] Upload logo: falha DB dispara compensate (delete blob)
  - [ ] Delete logo: falha content → orphan WARN sem rollback DB
  - [ ] Default store delete → erro de negócio
  - [ ] `./mvnw test -pl merchant-service` verde
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- Trilha merchant C-ready-consumidora completa
- Pronto para adapters Strangler (task_11)
