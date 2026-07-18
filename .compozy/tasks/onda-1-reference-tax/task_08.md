---
status: pending
title: "Strangler monolito: RestTemplate, adapters e beans condicionais"
type: backend
complexity: high
---

# Strangler monolito: RestTemplate, adapters e beans condicionais

## Overview
Consolida TLC T21–T24. Configura o Strangler no `sm-shop`: RestTemplate + properties `wave1.*`, adapters HTTP para facades de reference e tax, e wiring `@ConditionalOnProperty` para que exatamente um bean por facade exista — remoto quando `wave1.strangler.enabled=true`, in-process quando false (TechSpec **Ordem de build** passo 8; STR-01). Controllers BFF permanecem; falhas de connect → 503 sem fallback silencioso.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST adicionar `Wave1ClientConfig` com RestTemplate (timeout configurável) e properties `wave1.strangler.enabled`, base-urls, timeout (TechSpec **Configuração**; ADR-004).
2. MUST implementar adapters HTTP para Country/Zone/Language/Currency facades com `@ConditionalOnProperty(wave1.strangler.enabled=true)` (REF-09).
3. MUST implementar `TaxFacadeHttpAdapter` encaminhando JWT + store/lang (TAX-10).
4. MUST garantir facades in-process ativas quando strangler off (`matchIfMissing=false`) — STR-01.
5. MUST mapear falhas de conexão/timeout downstream para HTTP 503 (sem fallback silencioso in-process).
6. MUST propagar `X-Correlation-Id` nos hops HTTP dos adapters.
7. MUST manter paths públicos de `ReferencesApi` / `TaxClassApi` / `TaxRatesApi` inalterados (STR-04).
</requirements>

## Subtasks
- [ ] 8.1 Config RestTemplate + properties wave1 + testes de binding
- [ ] 8.2 Adapters HTTP das 4 facades de reference
- [ ] 8.3 Adapter HTTP de TaxFacade (JWT forward)
- [ ] 8.4 Conditional beans: exatamente um bean por interface facade
- [ ] 8.5 ITs em profiles monolith e strangler + 503 em downstream down

## Implementation Details
Ver TechSpec: **Interfaces principais** (padrão CountryFacadeHttpAdapter), **Pontos de integração** (monolito→services), **Configuração**, **Ordem de build** passo 8, **Abordagem de testes** (profiles).

### Relevant Files
- `sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/references/ReferencesApi.java` — BFF entrypoint
- `sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/tax/TaxClassApi.java` — BFF tax
- `sm-shop/src/main/java/com/salesmanager/shop/store/controller/country/facade/CountryFacade.java` — interface a adaptar
- `sm-shop/src/main/java/com/salesmanager/shop/store/facade/tax/TaxFacadeImpl.java` — impl in-process a condicionar
- `shopizer-api-contracts/.../client/` — contratos client
- `reference-service/` / `tax-service/` — alvos HTTP (task_06/task_07)

### Dependent Files
- `sm-shop/.../strangler/config/Wave1ClientConfig.java` — a criar
- `sm-shop/.../strangler/reference/*FacadeHttpAdapter.java` — a criar
- `sm-shop/.../strangler/tax/TaxFacadeHttpAdapter.java` — a criar
- `sm-shop/src/main/resources/application*.properties` — wave1 props / profiles

### Related ADRs
- [ADR-004: RestTemplate Strangler clients](adrs/adr-004.md) — client HTTP
- [ADR-001: TLC-sourced Compozy PRD for Wave 1 Reference+Tax](adrs/adr-001.md) — piloto Strangler

## Deliverables
- Config wave1 + 5 adapters HTTP + wiring condicional
- Profiles `monolith` / `strangler` documentados
- Unit + ITs de adapters com 80%+ coverage **(REQUIRED)**
- Sem mudança de path REST externo **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] Properties `wave1.*` fazem bind corretamente em `Wave1ClientConfig`
  - [ ] Adapter reference propaga header `X-Correlation-Id`
  - [ ] Adapter tax encaminha header `Authorization`
- Integration tests:
  - [ ] Profile strangler: CountryFacade bean é HTTP adapter
  - [ ] Profile monolith (default): CountryFacade bean é impl in-process
  - [ ] Downstream reference down + strangler on → 503 no BFF
  - [ ] TaxFacadeHttpAdapter delega CRUD class/rate ao tax-service
  - [ ] `./mvnw test -pl sm-shop` passa em ambos profiles (subconjunto strangler + monolith)
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- Exatamente um bean por facade interface em cada profile
- Clients externos não mudam URLs (STR-04)
