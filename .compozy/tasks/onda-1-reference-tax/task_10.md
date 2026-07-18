---
status: completed
title: Docker Compose, health, correlation e gate do reactor
type: infra
complexity: medium
---

# Docker Compose, health, correlation e gate do reactor

## Overview
Consolida TLC T28–T30. Fecha a Wave 1 com topologia Docker Compose (MySQL → reference → tax → shop), health indicators customizados, propagação de `X-Correlation-Id` e gate `./mvnw clean install` do reactor completo (TechSpec **Ordem de build** passo 10; **Monitoramento e observabilidade**; STR-05).

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST adicionar `docker-compose-wave1.yml` com mysql, reference-service, tax-service e sm-shop; ordem de startup alinhada ao TechSpec **Dependências técnicas**.
2. MUST expor `/actuator/health` com componente DB em reference; tax deve checar DB + reference HTTP (STR-05).
3. MUST propagar/gerar `X-Correlation-Id` nos três apps (monolito + services).
4. MUST validar `docker compose -f docker-compose-wave1.yml config`.
5. MUST passar gate final `./mvnw clean install` incluindo regressão TaxRate (monolith) e Pact consumer.
6. SHOULD documentar smoke manual mínimo (health UP + um GET reference + um CRUD tax autenticado).
7. MUST NOT exigir Eureka/K8s discovery na Wave 1 (apenas URLs de config).
</requirements>

## Subtasks
- [x] 10.1 Criar `docker-compose-wave1.yml` e validar config
- [x] 10.2 Health indicators custom (reference DB; tax DB+reference)
- [x] 10.3 Filtros/propagação `X-Correlation-Id` nos 3 apps
- [x] 10.4 Gate `./mvnw clean install` + regressões Pact/TaxRate
- [x] 10.5 Checklist smoke de integração documentado

## Implementation Details
Ver TechSpec: **Monitoramento e observabilidade**, **Dependências técnicas**, **Configuração**, **Ordem de build** passo 10, **Abordagem de testes** (gate reactor). Seed permanece no monolito (ADR-007).

### Relevant Files
- `reference-service/` — health + correlation (task_06)
- `tax-service/` — health + correlation (task_07)
- `sm-shop/` — strangler + correlation (task_08)
- Root `pom.xml` — reactor completo Wave 1

### Dependent Files
- `docker-compose-wave1.yml` — a criar
- `reference-service/.../ReferenceHealthIndicator.java` (ou equivalente) — a criar
- `tax-service/.../TaxHealthIndicator.java` (ou equivalente) — a criar
- Filters de correlation id nos três módulos — a criar/adaptar

### Related ADRs
- [ADR-002: Shared DB schema for Wave 1](adrs/adr-002.md) — um MySQL na topologia
- [ADR-007: InitializationDatabaseImpl stays in the monolith](adrs/adr-007.md) — ordem de seed greenfield

## Deliverables
- Compose Wave 1 válido
- Health custom + correlation id
- Gate reactor verde
- Unit tests dos health indicators **(REQUIRED)**
- Evidence de `./mvnw clean install` **(REQUIRED)**

## Tests
- Unit tests:
  - [x] `ReferenceHealthIndicator` reporta UP quando DB ok / DOWN quando DB falha
  - [x] `TaxHealthIndicator` DOWN quando reference HTTP falha mesmo com DB UP
  - [x] Correlation filter gera ID quando header ausente e propaga quando presente
- Integration tests:
  - [x] `docker compose -f docker-compose-wave1.yml config` exit 0
  - [ ] `./mvnw clean install` passa no reactor
  - [ ] `TaxRateIntegrationTest` verde (profile monolith)
  - [ ] `Wave1ConsumerPactTest` verde
- Test coverage target: >=80% (indicators/filters novos)
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- Compose config válida e ordem MySQL→ref→tax→shop documentada
- Gate final da Wave 1 verde (REF/TAX/STR P1)
