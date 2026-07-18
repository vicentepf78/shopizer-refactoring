---
status: pending
title: merchant-service Boot, JWT e clients Reference/Content
type: backend
complexity: high
---

# merchant-service Boot, JWT e clients Reference/Content

## Overview
Consolida TLC T33–T34. Scaffold do `merchant-service` (:8085) com cadeia JWT e clients HTTP para `reference-service` (Onda 1) e `content-service` (logo/C-ready). Bloqueia em `task_03` (C-ready) e `task_08` (merchant-core).

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST scaffold `merchant-service` porta 8085 com dep `sm-merchant-core` — TLC T33.
2. MUST replicar security JWT protegendo `/private/**` (padrão Onda 1) — TLC T33.
3. MUST implementar `ReferenceServiceClient` RestTemplate (`wave1.reference-service.base-url`) — TLC T34.
4. MUST implementar `ContentServiceClient` RestTemplate apontando às APIs internas de logo do content-service — TLC T34; **exige C-ready**.
5. MUST falhar com 503 quando reference/content remotos estiverem indisponíveis.
6. MUST NOT iniciar clients de logo antes do marco C-ready.
7. MUST passar testes de security e clients.
</requirements>

## Subtasks
- [ ] 9.1 Scaffold Boot merchant-service :8085 (T33)
- [ ] 9.2 Cadeia JWT private/public (T33)
- [ ] 9.3 ReferenceServiceClient HTTP (T34)
- [ ] 9.4 ContentServiceClient HTTP logo (T34)
- [ ] 9.5 Testes security + clients mock

## Implementation Details
Ver TechSpec: **Pontos de integração** (merchant→reference, merchant→content), **Ordem de construção** passos 23–24. Reutilizar padrões tax-service / content-service clients.

### Relevant Files
- `sm-merchant-core/` — domínio (task_08)
- `shopizer-api-contracts/.../client/ContentServiceClient.java` — contrato logo
- `shopizer-api-contracts/.../client/MerchantServiceClient.java` — contrato snapshot
- `content-service/.../api/internal/` — APIs logo (task_03 C-ready)

### Dependent Files
- `merchant-service/pom.xml` — novo módulo
- `merchant-service/.../MerchantServiceApplication.java` — entrypoint
- `merchant-service/.../security/` — JWT
- `merchant-service/.../client/ReferenceServiceClientRestTemplateImpl.java` — client ref
- `merchant-service/.../client/ContentServiceClientRestTemplateImpl.java` — client content

### Related ADRs
- [ADR-005: APIs internas](adrs/adr-005.md) — chamadas logo internas
- [ADR-006: Logo blob-first](adrs/adr-006.md) — client content usado na task_10
- [ADR-007: Sem ProductType](adrs/adr-007.md) — escopo merchant

## Deliverables
- merchant-service Boot + JWT
- Clients Reference e Content
- Unit tests clients 80%+ **(REQUIRED)**
- Integration test security JWT **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] ReferenceServiceClient resolve country/zone/language/currency (mock)
  - [ ] ContentServiceClient uploadLogo/deleteLogo chama paths internos corretos
  - [ ] Timeout/HTTP error → exceção mapeável a 503
- Integration tests:
  - [ ] Private sem JWT → 401
  - [ ] `./mvnw test -pl merchant-service -Dtest=MerchantSecurityIntegrationTest,*ServiceClientTest`
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- App sobe na 8085 com JWT
- Clients prontos para facades/logo (task_10)
