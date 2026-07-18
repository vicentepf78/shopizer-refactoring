---
status: pending
title: Scaffold shopizer-api-contracts e DTOs comuns
type: backend
complexity: medium
---

# Scaffold shopizer-api-contracts e DTOs comuns

## Overview
Consolida TLC T1–T2. Cria o módulo Maven `shopizer-api-contracts` no reactor e migra os wrappers DTO comuns (`Entity`, `ShopEntity`, listas, `EntityExists`) para `com.salesmanager.contracts.common`, sem dependência de JPA/`sm-core-model`. Fundação obrigatória para todos os contratos da Wave 1 (TechSpec **Ordem de build** passo 1 e início do passo 2).

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST criar o módulo `shopizer-api-contracts` no root reactor com parent Shopizer e deps apenas de anotações Jackson + Bean Validation API (sem Spring Boot, sem `sm-core-model`).
2. MUST listar o módulo em `pom.xml` raiz antes dos services Wave 1.
3. MUST migrar os DTOs comuns listados no TechSpec seção **Modelos de dados** (`Entity`, `ShopEntity`, `ReadableList`, `ReadableEntityList`, `EntityExists`) para `com.salesmanager.contracts.common`.
4. MUST garantir `./mvnw validate` e `./mvnw compile -pl shopizer-api-contracts` verdes.
5. SHOULD reutilizar o padrão de JAR fino de `sm-core-modules/pom.xml` como template.
6. MUST NOT introduzir imports de `com.salesmanager.core.model`.
</requirements>

## Subtasks
- [ ] 1.1 Scaffold do módulo `shopizer-api-contracts` e registro no reactor
- [ ] 1.2 Configurar dependências mínimas (jackson-annotations, validation-api)
- [ ] 1.3 Migrar wrappers DTO comuns para `contracts.common`
- [ ] 1.4 Validar compile do módulo isolado
- [ ] 1.5 Smoke test de serialização Jackson dos wrappers comuns

## Implementation Details
Ver TechSpec: **Arquitetura do sistema** (`shopizer-api-contracts`), **Modelos de dados** (wrappers comuns), **Ordem de build** passo 1. Fontes atuais em `sm-shop-model/.../model/entity/`.

### Relevant Files
- `pom.xml` — registrar módulo no reactor
- `sm-core-modules/pom.xml` — template de JAR fino
- `sm-shop-model/src/main/java/com/salesmanager/shop/model/entity/Entity.java` — fonte do wrapper base
- `sm-shop-model/src/main/java/com/salesmanager/shop/model/entity/ShopEntity.java` — fonte ShopEntity
- `sm-shop-model/src/main/java/com/salesmanager/shop/model/entity/ReadableEntityList.java` — fonte lista paginada
- `sm-shop-model/src/main/java/com/salesmanager/shop/model/entity/EntityExists.java` — fonte exists

### Dependent Files
- `shopizer-api-contracts/pom.xml` — novo módulo (a criar)
- `shopizer-api-contracts/src/main/java/com/salesmanager/contracts/common/*` — DTOs comuns (a criar)

### Related ADRs
- [ADR-005: Contract DTOs / no JPA in REST responses; Pact](adrs/adr-005.md) — contracts livres de JPA

## Deliverables
- Módulo `shopizer-api-contracts` no reactor
- Package `com.salesmanager.contracts.common` com 5 wrappers
- Unit tests com 80%+ coverage dos wrappers serializáveis **(REQUIRED)**
- Integration/compile gate do módulo **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] `Entity` serializa/deserializa `id` via Jackson sem campos JPA
  - [ ] `EntityExists` serializa `{exists: true|false}` corretamente
  - [ ] `ReadableEntityList` preserva lista e metadados de paginação no JSON
- Integration tests:
  - [ ] `./mvnw validate -pl shopizer-api-contracts` passa
  - [ ] `./mvnw compile -pl shopizer-api-contracts` passa sem dep transitiva de `sm-core-model`
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- Módulo listado no root `<modules>`
- Zero dependência de `sm-core-model` no classpath do contracts
