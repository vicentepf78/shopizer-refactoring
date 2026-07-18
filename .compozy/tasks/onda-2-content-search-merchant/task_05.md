---
status: pending
title: search-service OpenSearch, query e índice (S-ready)
type: backend
complexity: high
---

# search-service OpenSearch, query e índice (S-ready)

## Overview
Consolida TLC T18–T22. Entrega o `search-service` (:8084) **sem JPA**, com bootstrap OpenSearch, `SearchQueryService`/`SearchIndexService`, API interna de índice com `X-Internal-Token` e REST público search/autocomplete (reindex admin → 501). Marco **S-ready** para o producer HTTP do monólito.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST scaffold `search-service` porta 8084 com deps commons + OpenSearch e **sem DataSource/JPA** — TLC T18 / ADR-003.
2. MUST migrar mappings/settings OpenSearch e `SearchModuleBootstrap` — TLC T19.
3. MUST extrair query/autocomplete de `SearchServiceImpl` retornando `SearchItem` (commons) — TLC T20 / ADR-010.
4. MUST implementar index/delete a partir de `ProductIndexPayload`, `InternalIndexController`, filter de token; `schemaVersion` ≠ 1 → 422; token inválido → 401 — TLC T21.
5. MUST expor `POST /api/v1/search`, `/autocomplete` e reindex privado → **501** — TLC T22 / ADR-011.
6. MUST falhar com 503 claro quando OpenSearch estiver down (sem fallback silencioso).
7. MUST passar testes de integração do search-service.
</requirements>

## Subtasks
- [ ] 5.1 Scaffold Boot sem JPA + package (T18)
- [ ] 5.2 Migrar config/bootstrap OpenSearch (T19)
- [ ] 5.3 SearchQueryServiceImpl query + autocomplete (T20)
- [ ] 5.4 SearchIndexServiceImpl + API interna índice (T21)
- [ ] 5.5 SearchController público + reindex 501 (T22)
- [ ] 5.6 Suite integração S-ready

## Implementation Details
Ver TechSpec: **Endpoints search-service**, **Interfaces** SearchQueryService/SearchIndexService, **Ordem de construção** passos 15–18. Fonte: `SearchServiceImpl` no sm-core.

### Relevant Files
- `sm-core/src/main/java/com/salesmanager/core/business/services/search/SearchServiceImpl.java` — lógica a extrair
- `sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/search/SearchApi.java` — paths públicos
- `sm-shop/src/test/java/com/salesmanager/test/shop/integration/search/SearchApiIntegrationTest.java` — paridade esperada
- `shopizer-api-contracts/.../search/` — ProductIndexPayload (task_01)

### Dependent Files
- `search-service/pom.xml` — novo módulo
- `search-service/.../SearchServiceApplication.java` — entrypoint
- `search-service/src/main/resources/search/` — mappings/settings
- `search-service/.../services/SearchQueryServiceImpl.java` — query
- `search-service/.../services/SearchIndexServiceImpl.java` — índice
- `search-service/.../api/internal/InternalIndexController.java` — índice interno
- `search-service/.../api/v1/SearchController.java` — REST público

### Related ADRs
- [ADR-002: ProductIndexPayload](adrs/adr-002.md) — schemaVersion
- [ADR-003: search-service sem JPA](adrs/adr-003.md) — só OpenSearch
- [ADR-005: APIs internas e token](adrs/adr-005.md) — X-Internal-Token
- [ADR-010: SearchItem em commons](adrs/adr-010.md) — sem mover schema
- [ADR-011: stubs/deprecated](adrs/adr-011.md) — reindex 501

## Deliverables
- `search-service` implantável sem DataSource
- Query + índice interno + REST público (S-ready)
- Unit tests 80%+ Query/Index services **(REQUIRED)**
- Integration tests index token/schema + search/autocomplete **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] SearchQueryServiceImpl zero imports sm-core-model
  - [ ] Index com payload válido chama OpenSearch client (mock)
  - [ ] OpenSearch down → exceção/503 mapeada
- Integration tests:
  - [ ] App context sobe sem DataSource
  - [ ] schemaVersion ≠ 1 → 422; token inválido → 401
  - [ ] POST search/autocomplete registrados; reindex → 501
  - [ ] `./mvnw test -pl search-service` verde
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- Marco **S-ready** atingido
- Sem JPA/MySQL no search-service
