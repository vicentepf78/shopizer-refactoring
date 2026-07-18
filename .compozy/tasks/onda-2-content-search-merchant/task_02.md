---
status: pending
title: Extrair sm-content-core e CMS content
type: backend
complexity: high
---

# Extrair sm-content-core e CMS content

## Overview
Consolida TLC T5–T7. Cria o módulo thin `sm-content-core`, extrai repositórios e `ContentService` com backends CMS de conteúdo (sem product file managers) e adiciona `shopizer-content-cms.xml`. Base para o content-service e para o marco C-ready. Depende dos contracts content de `task_01`; Execute da Onda 1 já deve ter sido gateado.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST scaffold `sm-content-core` no reactor e mover `ContentRepository*`, `PageContentRepository` — TLC T5.
2. MUST mover `ContentService`/`ContentServiceImpl` e `modules/cms/content/` (exceto `product/`) + cache managers — TLC T6.
3. MUST criar `shopizer-content-cms.xml` apenas com `contentFileManager` e backends (infinispan/local/aws/gcp) — TLC T7.
4. MUST NOT incluir `productFileManager` nem pipeline `/static/products/**` no módulo (ADR-008).
5. MUST registrar o módulo no `pom.xml` raiz e permitir `@ImportResource` do XML CMS.
6. MUST manter schema DB compartilhado (sem migração física).
7. MUST passar `./mvnw test -pl sm-content-core`.
</requirements>

## Subtasks
- [ ] 2.1 Scaffold módulo + mover repositórios content (T5)
- [ ] 2.2 Mover ContentService e CMS content modules (T6)
- [ ] 2.3 Split `shopizer-content-cms.xml` content-only (T7)
- [ ] 2.4 Registrar módulo no reactor e smoke `@DataJpaTest`
- [ ] 2.5 Garantir zero beans de product file manager no módulo

## Implementation Details
Ver TechSpec: **Arquitetura** (`sm-content-core`), **Blobs**, **Ordem de construção** passos 6–8. Reutilizar padrão `sm-reference-core` da Onda 1 (ADR-004).

### Relevant Files
- `sm-core/src/main/java/com/salesmanager/core/business/repositories/content/` — repos a extrair
- `sm-core/src/main/java/com/salesmanager/core/business/services/content/ContentService.java` — interface
- `sm-core/src/main/java/com/salesmanager/core/business/services/content/ContentServiceImpl.java` — impl
- `sm-core/src/main/java/com/salesmanager/core/business/modules/cms/` — CMS (content vs product)
- `sm-core/src/main/resources/spring/shopizer-core-cms.xml` — XML monolítico a fatiar

### Dependent Files
- `sm-content-core/pom.xml` — novo módulo
- `sm-content-core/src/main/java/.../repositories/content/` — repos movidos
- `sm-content-core/src/main/java/.../services/content/` — serviços movidos
- `sm-content-core/src/main/resources/spring/shopizer-content-cms.xml` — XML content-only
- `pom.xml` — registro do módulo

### Related ADRs
- [ADR-004: Módulos thin sm-content-core / sm-merchant-core](adrs/adr-004.md) — padrão thin core
- [ADR-008: Colocalização content e contentFileManager-only](adrs/adr-008.md) — exclusão product managers

## Deliverables
- Módulo `sm-content-core` no reactor com repos + ContentService + CMS content
- XML CMS content-only importável
- Unit/integration tests com 80%+ coverage das superfícies movidas **(REQUIRED)**
- Gate `./mvnw test -pl sm-content-core` **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] ContentService métodos de pages/boxes não referenciam productFileManager
  - [ ] Beans CMS content resolvem via ImportResource (smoke)
- Integration tests:
  - [ ] `@DataJpaTest` smoke de ContentRepository passa
  - [ ] `./mvnw test -pl sm-content-core` verde
  - [ ] Grep/assert: zero `productFileManager` no módulo
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- `sm-content-core` compilável e testável isolado
- CMS product permanece fora do módulo
