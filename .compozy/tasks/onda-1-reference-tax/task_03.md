---
status: completed
title: Wire sm-shop-model → shopizer-api-contracts
type: backend
complexity: low
---

# Wire sm-shop-model → shopizer-api-contracts

## Overview
Consolida TLC T6. Liga `sm-shop-model` ao JAR `shopizer-api-contracts` (TechSpec **Ordem de build** passo 3) para que o monolito e futuros adapters consumam DTOs de contrato sem quebrar a compilação existente. DTOs duplicados em `com.salesmanager.shop.model.*` devem ser re-exportados ou marcados `@Deprecated` como aliases.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST adicionar dependência Maven `shopizer-api-contracts` em `sm-shop-model/pom.xml`.
2. MUST manter compatibilidade de compile do monolito (`./mvnw compile -pl sm-shop -am`) — STR-04.
3. SHOULD deprecar ou re-exportar DTOs duplicados em `com.salesmanager.shop.model.*` apontando para contracts.
4. MUST NOT alterar paths REST nesta task.
5. MUST garantir que tipos contracts sejam transitivamente visíveis a `sm-shop`.
</requirements>

## Subtasks
- [x] 3.1 Adicionar dep `shopizer-api-contracts` em `sm-shop-model`
- [x] 3.2 Aplicar aliases/@Deprecated nos DTOs duplicados de reference/tax/entity
- [x] 3.3 Validar compile de `sm-shop-model` e `sm-shop -am`
- [x] 3.4 Smoke: classe do monolito resolve tipo contracts no classpath

## Implementation Details
Ver TechSpec: **Análise de impacto** (`sm-shop-model`), **Ordem de build** passo 3. Preferir churn mínimo: dep Maven + aliases, sem reescrever todos os imports do monolito nesta task.

### Relevant Files
- `sm-shop-model/pom.xml` — ponto de wiring
- `sm-shop-model/src/main/java/com/salesmanager/shop/model/entity/` — DTOs a deprecar/re-exportar
- `sm-shop-model/src/main/java/com/salesmanager/shop/model/references/` — DTOs reference legados
- `sm-shop-model/src/main/java/com/salesmanager/shop/model/tax/` — DTOs tax legados
- `shopizer-api-contracts/` — fonte canônica pós task_02

### Dependent Files
- `sm-shop/pom.xml` — consome transitivamente via sm-shop-model
- Controllers/facades em `sm-shop` que importam `com.salesmanager.shop.model.*` — devem continuar compilando

### Related ADRs
- [ADR-005: Contract DTOs / no JPA in REST responses; Pact](adrs/adr-005.md) — DTOs canônicos no JAR contracts

## Deliverables
- `sm-shop-model` depende de `shopizer-api-contracts`
- Aliases/deprecations documentados nos packages legados
- Unit/smoke tests de classpath **(REQUIRED)**
- Compile gate monolito **(REQUIRED)**

## Tests
- Unit tests:
  - [x] Tipo contracts (`ReadableCountry` ou equivalente) é carregável a partir do classpath de `sm-shop-model`
  - [x] Alias/@Deprecated legado ainda resolve no compile (se re-export usado)
- Integration tests:
  - [x] `./mvnw compile -pl sm-shop-model` passa
  - [x] `./mvnw compile -pl sm-shop -am` passa
- Test coverage target: >=80% (código novo/alterado nesta task)
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- Monolito compila sem breaking change de API path
- Contracts disponível transitivamente para adapters futuros (task_08)
