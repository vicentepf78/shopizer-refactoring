---
status: pending
title: content-service Boot, JWT, REST e APIs internas (C-ready)
type: backend
complexity: high
---

# content-service Boot, JWT, REST e APIs internas (C-ready)

## Overview
Consolida TLC T8–T16. Entrega o app Spring Boot `content-service` (:8083) com JWT em `/private/**`, cliente Reference da Onda 1, controllers pages/boxes/files/admin, facade/mappers e APIs internas static+logo — marco **C-ready**. Desbloqueia clients merchant/logo e o proxy estático do BFF.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST scaffold `content-service` porta 8083 com `@ImportResource` do CMS XML — TLC T8.
2. MUST replicar cadeia JWT (padrão tax-service Onda 1) protegendo `/private/**` — TLC T9.
3. MUST integrar `ReferenceServiceClient` via `wave1.reference-service.base-url` (falha → 503) — TLC T10.
4. MUST expor REST pages, boxes, files e admin espelhando paths congelados; stubs deprecated preservam null/no-op — TLC T11–T14 / ADR-011.
5. MUST entregar APIs internas `GET /internal/v1/static/files/**` e `POST|DELETE /internal/v1/content/logo` — TLC T15 (**C-ready**).
6. MUST portar `ContentFacadeImpl` + populators/mappers retornando DTOs dos contracts — TLC T16.
7. MUST NOT expor entidades JPA no JSON; MUST usar store code como tenant.
8. MUST passar testes de integração do módulo content-service.
</requirements>

## Subtasks
- [ ] 3.1 Scaffold Boot + health actuator (T8)
- [ ] 3.2 Security JWT private/public (T9)
- [ ] 3.3 ReferenceServiceClient HTTP (T10)
- [ ] 3.4 Controllers pages/boxes/files/admin + stubs (T11–T14)
- [ ] 3.5 APIs internas static + logo C-ready (T15)
- [ ] 3.6 Port ContentFacade + mappers (T16)
- [ ] 3.7 Suite de integração do módulo

## Implementation Details
Ver TechSpec: **Endpoints content-service**, **Pontos de integração** (content→reference), **Ordem de construção** passos 9–13. Paths espelham `ContentApi` / `ContentAdministrationApi` no sm-shop.

### Relevant Files
- `sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/content/ContentApi.java` — contrato de paths
- `sm-shop/src/main/java/com/salesmanager/shop/store/facade/content/ContentFacadeImpl.java` — facade a portar
- `sm-shop-model/src/main/java/com/salesmanager/shop/store/controller/content/facade/ContentFacade.java` — interface
- `sm-content-core/` — deps de domínio (task_02)
- `shopizer-api-contracts/.../content/` — DTOs (task_01)

### Dependent Files
- `content-service/pom.xml` — novo módulo Boot
- `content-service/.../ContentServiceApplication.java` — entrypoint
- `content-service/.../security/` — JWT
- `content-service/.../api/v1/content/` — controllers REST
- `content-service/.../api/internal/` — static + logo
- `content-service/.../facade/content/` — facade portada
- `content-service/.../client/` — ReferenceServiceClient

### Related ADRs
- [ADR-005: APIs internas e X-Internal-Token](adrs/adr-005.md) — `/internal/v1/**`
- [ADR-008: Colocalização content](adrs/adr-008.md) — DB + blobs
- [ADR-011: Preservar stubs deprecated](adrs/adr-011.md) — paridade stubs

## Deliverables
- `content-service` implantável na porta 8083
- Superfície REST P1 + internas C-ready
- Unit tests com 80%+ coverage de facade/clients **(REQUIRED)**
- Integration tests pages/boxes/files/admin/internal/security **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] Facade retorna DTOs contracts (não entidades JPA)
  - [ ] ReferenceServiceClient resolve language por code (mock HTTP)
  - [ ] Stub admin retorna null/no-op como monólito
- Integration tests:
  - [ ] Private sem JWT → 401; public aberto
  - [ ] CRUD página com 2 idiomas retorna DTOs legíveis
  - [ ] Upload IMAGE persiste blob; GET static interno retorna bytes + content-type
  - [ ] Logo POST/DELETE interno funcional
  - [ ] `./mvnw test -pl content-service` verde
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- Marco **C-ready** atingido (static + logo internos)
- Paths REST alinhados a STR-04
