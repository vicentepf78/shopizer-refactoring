---
status: pending
title: Rewire sm-core content e trim CMS de produto
type: backend
complexity: medium
---

# Rewire sm-core content e trim CMS de produto

## Overview
Consolida TLC T17. Remove do `sm-core` as classes de content já movidas, adiciona dependência de `sm-content-core` e deixa `shopizer-core-cms.xml` apenas com beans de produto. Fecha o rewire content do monólito após C-ready, preparando o Strangler Content.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST remover do `sm-core` as classes content/CMS content já presentes em `sm-content-core` — TLC T17.
2. MUST declarar dependência Maven `sm-content-core` em `sm-core`.
3. MUST trimar `shopizer-core-cms.xml` para beans de product file managers apenas.
4. MUST manter o monólito compilável e testes de sm-core verdes.
5. MUST NOT duplicar beans content entre sm-core e sm-content-core.
6. SHOULD validar que consumers in-process do monólito resolvem ContentService via o thin core.
</requirements>

## Subtasks
- [ ] 4.1 Remover classes content movidas de sm-core
- [ ] 4.2 Adicionar dep `sm-content-core` no pom sm-core
- [ ] 4.3 Trim CMS XML para product-only
- [ ] 4.4 Gate `./mvnw test -pl sm-core`
- [ ] 4.5 Smoke: ContentService disponível no contexto monólito

## Implementation Details
Ver TechSpec: **Análise de impacto** (`sm-core`), **Ordem de construção** passo 14. Coordenar com task_02/task_03 já concluídas.

### Relevant Files
- `sm-core/pom.xml` — deps
- `sm-core/src/main/resources/spring/shopizer-core-cms.xml` — trim product-only
- `sm-core/src/main/java/com/salesmanager/core/business/services/content/` — classes a remover/delegar
- `sm-content-core/` — módulo fonte da dep

### Dependent Files
- `sm-core/pom.xml` — dep sm-content-core
- `sm-shop/` — consumers indiretos de ContentService (regressão)

### Related ADRs
- [ADR-004: Módulos thin](adrs/adr-004.md) — rewire via thin core
- [ADR-008: Colocalização content](adrs/adr-008.md) — product CMS fica no monólito

## Deliverables
- sm-core delegando content a sm-content-core
- CMS XML product-only no sm-core
- Unit/integration tests sm-core com 80%+ nas áreas tocadas **(REQUIRED)**
- Gate `./mvnw test -pl sm-core` **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] Contexto Spring resolve ContentService a partir do thin core
- [ ] Beans product CMS permanecem no XML do sm-core
- Integration tests:
  - [ ] `./mvnw test -pl sm-core` verde
  - [ ] Ausência de classes duplicadas content no classpath sm-core source
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- Zero duplicação de ContentService entre módulos
- Monólito ainda opera content in-process até o Strangler (task_11)
