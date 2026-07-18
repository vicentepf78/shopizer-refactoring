---
status: completed
title: Extrair sm-reference-core e rewire sm-core
type: backend
complexity: high
---

# Extrair sm-reference-core e rewire sm-core

## Overview
Consolida TLC T7–T9. Extrai repositories e services CRUD de Country/Zone/Language/Currency para o módulo fino `sm-reference-core`, adiciona o overload `LanguageService.toLocale(Language, String countryCode)` (REF-08), e rewire `sm-core` para depender do novo módulo mantendo init/loader no monolito core (TechSpec **Ordem de build** passo 4; ADR-007).

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST criar `sm-reference-core` dependendo apenas de `sm-core-model` (fino) — ver TechSpec **Arquitetura do sistema**.
2. MUST mover os 4 repositories de reference e os services CRUD Country/Zone/Language/Currency (sem init/loader).
3. MUST adicionar overload `toLocale(Language, String countryCode)` em LanguageService (REF-08).
4. MUST atualizar `sm-core` para depender de `sm-reference-core` e remover classes movidas.
5. MUST manter `InitializationDatabaseImpl` / packages init/loader em `sm-core` (ADR-007).
6. MUST NOT mover GeoZone APIs (ADR-006 — entidades podem permanecer no model).
7. SHOULD incluir smoke `@DataJpaTest` para pelo menos um repository extraído.
</requirements>

## Subtasks
- [x] 4.1 Scaffold `sm-reference-core` + mover repositories de reference
- [x] 4.2 Mover services CRUD reference (excluir init/loader)
- [x] 4.3 Implementar overload `toLocale(Language, String countryCode)`
- [x] 4.4 Rewire `sm-core` (dep + remoção de classes movidas)
- [x] 4.5 Smoke `@DataJpaTest` + regressão `./mvnw test -pl sm-core`

## Implementation Details
Ver TechSpec: **Arquitetura** (`sm-reference-core`), **Modelos de dados** (tabelas COUNTRY/ZONE/LANGUAGE/CURRENCY), **Ordem de build** passo 4, **Análise de impacto**. Inventário TLC T7–T9.

### Relevant Files
- `sm-core/src/main/java/com/salesmanager/core/business/repositories/reference/country/CountryRepository.java` — a mover
- `sm-core/src/main/java/com/salesmanager/core/business/services/reference/language/LanguageService.java` — API + REF-08
- `sm-core/src/main/java/com/salesmanager/core/business/services/reference/language/LanguageServiceImpl.java` — impl a mover
- `sm-core/src/main/java/com/salesmanager/core/business/services/reference/init/` — permanece em sm-core
- `sm-core/pom.xml` — rewire de dependência
- `sm-core-model/` — entidades JPA shared

### Dependent Files
- `sm-reference-core/` — novo módulo (a criar)
- Callers em `sm-core` / `sm-shop` que usam Country/Zone/Language/Currency services — devem resolver via dep do core

### Related ADRs
- [ADR-002: Shared DB schema for Wave 1](adrs/adr-002.md) — schema compartilhado
- [ADR-006: GeoZone excluded from Wave 1](adrs/adr-006.md) — sem API GeoZone
- [ADR-007: InitializationDatabaseImpl stays in the monolith](adrs/adr-007.md) — seed/init permanece

## Deliverables
- Módulo `sm-reference-core` com repos + services CRUD
- `sm-core` rewired e compilando
- Overload REF-08 presente
- Unit/integration tests com 80%+ coverage do código movido/alterado **(REQUIRED)**
- Gate `./mvnw test -pl sm-reference-core` e `./mvnw test -pl sm-core` **(REQUIRED)**

## Tests
- Unit tests:
  - [x] `toLocale(Language, String countryCode)` produz Locale esperado para language+country codes válidos
  - [x] Service CRUD Language/Country não depende de MerchantStore na nova assinatura de toLocale
- Integration tests:
  - [x] `@DataJpaTest` smoke em `CountryRepository` (ou equivalente) no sm-reference-core
  - [x] `./mvnw test -pl sm-core` — testes existentes verdes
  - [x] Init/loader ainda presente e compilável em sm-core
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- Quatro repositories e services CRUD em `sm-reference-core`
- Init/loader não movidos
- `sm-core` depende de `sm-reference-core`
