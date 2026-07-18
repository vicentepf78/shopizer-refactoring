---
status: pending
title: Pact providers e consumer Wave2
type: test
complexity: medium
---

# Pact providers e consumer Wave2

## Overview
Consolida TLC T49–T50. Adiciona verificação Pact provider nos três serviços (endpoints P1 content/search/merchant) e o consumer `Wave2ConsumerPactTest` em sm-shop. Garante estabilidade de contrato (STR-02) após o Strangler estar wired (`task_11`).

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST criar testes provider Pact para content-service cobrindo superfícies P1 pages/boxes/files — TLC T49.
2. MUST criar testes provider Pact para search-service (search/autocomplete + índice interno schema) — TLC T49.
3. MUST criar testes provider Pact para merchant-service (store/config/snapshot; sem ProductType) — TLC T49.
4. MUST criar `Wave2ConsumerPactTest` em sm-shop verificando contratos dos 3 serviços — TLC T50.
5. MUST reutilizar tooling Pact JVM da Onda 1.
6. MUST falhar CI se DTO/path P1 quebrar compatibilidade.
7. SHOULD espelhar layout de pastas `contract/*ProviderPactTest` da Onda 1.
</requirements>

## Subtasks
- [ ] 13.1 Provider Pact content-service (T49)
- [ ] 13.2 Provider Pact search-service (T49)
- [ ] 13.3 Provider Pact merchant-service (T49)
- [ ] 13.4 Consumer Wave2ConsumerPactTest em sm-shop (T50)
- [ ] 13.5 Gates Maven dos quatro módulos

## Implementation Details
Ver TechSpec: **Abordagem de testes** (Pact), **Ordem de construção** passo 29. Requisito STR-02 / SRCH-07.

### Relevant Files
- `content-service/`, `search-service/`, `merchant-service/` — providers
- `sm-shop/.../strangler/` — adapters consumer
- Padrão Pact Onda 1 em reference-service/tax-service/sm-shop (se existir)

### Dependent Files
- `content-service/src/test/java/**/contract/*ProviderPactTest.java`
- `search-service/src/test/java/**/contract/*ProviderPactTest.java`
- `merchant-service/src/test/java/**/contract/*ProviderPactTest.java`
- `sm-shop/src/test/java/**/contract/Wave2ConsumerPactTest.java`
- Contratos/pacts gerados sob pasta de build Pact do projeto

### Related ADRs
- [ADR-011: stubs deprecated](adrs/adr-011.md) — paridade stubs nos contratos
- [ADR-002: ProductIndexPayload](adrs/adr-002.md) — schema índice no Pact search
- [ADR-007: Sem ProductType](adrs/adr-007.md) — contratos merchant sem product-type

## Deliverables
- 3 provider Pact suites + 1 consumer Wave2
- Pact verification verde nos 4 módulos
- Contract tests cobrindo endpoints P1 **(REQUIRED)**
- Documentação mínima de como rodar Pact localmente **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] N/A — foco em contract tests (coverage aplicado aos fixtures/helpers se houver)
- Integration tests:
  - [ ] Provider content: list/get page e box; upload file path P1
  - [ ] Provider search: POST search/autocomplete; índice schemaVersion rejeitado
  - [ ] Provider merchant: store CRUD subset; GET config; snapshot; sem ProductType
  - [ ] Consumer sm-shop: interactions content/search/merchant usadas pelos adapters
  - [ ] `./mvnw test -pl content-service,search-service,merchant-service -Dtest=*ProviderPactTest`
  - [ ] `./mvnw test -pl sm-shop -Dtest=Wave2ConsumerPactTest`
- Test coverage target: >=80% (helpers/fixtures Pact quando aplicável)
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- STR-02 satisfeito para superfícies P1 Wave2
- Breaking change em DTO P1 falha no CI
