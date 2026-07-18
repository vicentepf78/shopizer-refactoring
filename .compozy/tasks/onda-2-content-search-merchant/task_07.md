---
status: pending
title: Strangler Search no monolito e documentação GAP-SRCH
type: backend
complexity: medium
---

# Strangler Search no monolito e documentação GAP-SRCH

## Overview
Consolida TLC T27–T29. Adiciona `SearchFacadeHttpAdapter`, desabilita o cliente OpenSearch do monólito no profile strangler-wave2 e documenta GAP-SRCH-01…10 no README do search-service. Fecha a trilha search no BFF antes do Strangler cross-track.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST implementar `SearchFacadeHttpAdapter` delegando query/autocomplete via HTTP; `indexAllData` local via orchestrator — TLC T27.
2. MUST condicionar beans SearchFacade com `@ConditionalOnProperty(wave2.strangler.enabled)` (`matchIfMissing` → in-process) — TLC T28.
3. MUST desabilitar bootstrap/cliente OpenSearch do monólito no profile strangler — TLC T28.
4. MUST documentar GAP-SRCH-01…10 em `search-service/README.md` — TLC T29 / TechSpec lacunas.
5. MUST retornar 503 com correlation id em falha remota; MUST NOT fazer fallback silencioso in-process quando strangler ligado.
6. MUST manter profile monolith com regressão verde.
</requirements>

## Subtasks
- [ ] 7.1 SearchFacadeHttpAdapter query/autocomplete (T27)
- [ ] 7.2 Wiring condicional + desabilitar OpenSearch monólito (T28)
- [ ] 7.3 Documentar GAP-SRCH-01…10 (T29)
- [ ] 7.4 Testes adapter + profile monolith/strangler
- [ ] 7.5 Verificar reindex admin usa orchestrator local

## Implementation Details
Ver TechSpec: **Matriz de adapters**, **Lacunas conhecidas**, **Ordem de construção** passos 20–21. Controllers `SearchApi` permanecem no BFF.

### Relevant Files
- `sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/search/SearchApi.java` — entrypoints
- `sm-shop/.../strangler/search/SearchBulkIndexOrchestrator.java` — reindex local (task_06)
- `sm-core/.../services/search/SearchServiceImpl.java` — facade in-process legado

### Dependent Files
- `sm-shop/.../strangler/search/SearchFacadeHttpAdapter.java` — adapter HTTP
- `sm-shop/.../SearchFacadeImpl.java` — condicionar in-process
- `sm-core/pom.xml` — OpenSearch condicional/remoção no strangler
- `search-service/README.md` — seção Known gaps

### Related ADRs
- [ADR-003: search sem JPA](adrs/adr-003.md) — OpenSearch só no search-service
- [ADR-010: SearchItem commons](adrs/adr-010.md) — schema query
- [ADR-011: stubs](adrs/adr-011.md) — reindex orquestrado no BFF

## Deliverables
- SearchFacadeHttpAdapter + OpenSearch monólito desabilitado no strangler
- README com 10 gaps documentados
- Unit/integration tests adapter 80%+ **(REQUIRED)**
- Regressão profile monolith **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] Adapter encaminha search/autocomplete com headers store/lang
  - [ ] Falha remota → 503 sem chamar impl in-process
- Integration tests:
  - [ ] `./mvnw test -pl sm-shop -Dtest=SearchFacadeHttpAdapterTest`
  - [ ] Profile `monolith` regressão verde
  - [ ] Profile strangler não conecta OpenSearch no monólito
  - [ ] README lista GAP-SRCH-01 até GAP-SRCH-10
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- Strangler search operacional; gaps documentados sem expandir escopo
