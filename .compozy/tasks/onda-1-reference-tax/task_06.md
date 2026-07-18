---
status: completed
title: reference-service Boot, facades e REST
type: backend
complexity: high
---

# reference-service Boot, facades e REST

## Overview
Consolida TLC T13–T15. Entrega o executável `reference-service` (porta 8081) com facades/populators retornando DTOs de contracts e os 5 endpoints públicos espelhados (`/country`, `/zones`, `/languages`, `/currency`, `/measures`) — TechSpec **Ordem de build** passo 6 e **Endpoints da API** reference-service. Sem JWT nas rotas P1.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST criar módulo Boot executable `reference-service` com `server.port=8081`, datasource e actuator (REF-01, STR-05).
2. MUST depender de `sm-reference-core` + `shopizer-api-contracts`.
3. MUST portar facades/populators de reference para retornar DTOs contracts (não entidades JPA) — REF-03..REF-06.
4. MUST expor os 5 paths REST listados no TechSpec **Endpoints da API** reference-service, preservando query params `lang`/`store`.
5. MUST retornar 200 + `[]` para `/zones?code=` desconhecido (OQ-02).
6. MUST NOT exigir JWT nas rotas P1 de reference.
7. SHOULD incluir Ehcache local alinhado ao comportamento atual (TechSpec **Decisões-chave**).
</requirements>

## Subtasks
- [x] 6.1 Scaffold Boot app + packaging + health mínimo
- [x] 6.2 Portar facades e populators (Country/Zone/Language/Currency)
- [x] 6.3 Implementar `ReferencesController` com 5 endpoints
- [x] 6.4 Garantir respostas só-DTO (incl. `ReadableCurrency`)
- [x] 6.5 IT de API: 5 endpoints + zones desconhecido → 200 `[]`

## Implementation Details
Ver TechSpec: **Endpoints da API** (reference-service), **Configuração** (`server.port=8081`), **Abordagem de testes** (IT reference-service), **Ordem de build** passo 6. Reutilizar comportamento de `ReferencesApi` do monolito como espelho de paths.

### Relevant Files
- `sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/references/ReferencesApi.java` — paths a espelhar
- `sm-shop/src/main/java/com/salesmanager/shop/store/controller/country/facade/CountryFacadeImpl.java` — fonte facade
- `sm-shop/src/main/java/com/salesmanager/shop/store/controller/zone/facade/ZoneFacadeImpl.java` — fonte facade
- `sm-shop/src/main/java/com/salesmanager/shop/store/controller/language/facade/LanguageFacadeImpl.java` — fonte facade
- `sm-shop/src/main/java/com/salesmanager/shop/store/controller/currency/facade/CurrencyFacadeImpl.java` — fonte facade
- `sm-shop/src/main/java/com/salesmanager/shop/populator/references/ReadableCountryPopulator.java` — populator
- `sm-reference-core/` — services/repos (task_04)
- `shopizer-api-contracts/` — DTOs wire

### Dependent Files
- `reference-service/` — novo módulo (a criar)
- Root `pom.xml` — registrar módulo
- Consumers futuros: tax-service client e adapters Strangler (task_07/task_08)

### Related ADRs
- [ADR-001: TLC-sourced Compozy PRD for Wave 1 Reference+Tax](adrs/adr-001.md) — piloto reference
- [ADR-005: Contract DTOs / no JPA in REST responses; Pact](adrs/adr-005.md) — respostas DTO-only
- [ADR-006: GeoZone excluded from Wave 1](adrs/adr-006.md) — não expor GeoZone

## Deliverables
- JAR executable `reference-service` na 8081
- 5 endpoints públicos com JSON DTO
- Unit tests de facades + IT de API com 80%+ coverage **(REQUIRED)**
- `GET /actuator/health` UP no contexto mínimo **(REQUIRED)**

## Tests
- Unit tests:
  - [x] LanguageFacade retorna `ReadableLanguage` só com id/code/sortOrder
  - [x] CurrencyFacade retorna `ReadableCurrency` (não entidade Currency)
  - [x] CountryFacade inclui zones aninhadas no DTO
- Integration tests:
  - [x] GET `/api/v1/country` → 200 lista `ReadableCountry`
  - [x] GET `/api/v1/zones?code=XX` (desconhecido) → 200 `[]`
  - [x] GET `/api/v1/languages`, `/currency`, `/measures` → 200 DTOs
  - [x] Resposta JSON não contém campos tipicamente JPA (ex.: hibernateLazyInitializer)
  - [x] `ReferenceServiceApplicationTest` / health UP
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- `./mvnw package -pl reference-service` produz jar
- Paridade de paths com monolito (STR-04) para reference público
