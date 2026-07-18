---
status: pending
title: Extrair sm-tax-core, guard 409 e rewire sm-core
type: backend
complexity: high
---

# Extrair sm-tax-core, guard 409 e rewire sm-core

## Overview
Consolida TLC T10–T12 e T16. Extrai repositories e services CRUD de TaxClass/TaxRate para `sm-tax-core`, implementa o guard DELETE→409 (`TAX_CLASS_IN_USE`) e a semântica `existsTaxRate` → `{exists:false}` sem throw (TAX-09), e rewire `sm-core` mantendo `TaxService*` (cálculo) in-process (TechSpec **Ordem de build** passo 5; ADR-003). Paralelizável com task_04 após task_03.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST criar `sm-tax-core` e mover `TaxClassRepository` / `TaxRateRepository` e services TaxClass/TaxRate (não `TaxService*`).
2. MUST implementar guard de delete: product count > 0 → `TaxClassInUseException` mapeável a HTTP 409 (OQ-03; TechSpec **Interfaces principais** / **Convenções de erro**).
3. MUST corrigir `existsTaxRate` para retornar false sem lançar quando code ausente (TAX-09).
4. MUST rewire `sm-core` para depender de `sm-tax-core` e manter `TaxService`/`TaxServiceImpl` no sm-core (TAX-07, ADR-003).
5. MUST preservar regressão de cálculo: `TaxRateIntegrationTest` (ou equivalente) verde em profile monolito.
6. SHOULD adicionar `ProductRepository.countByTaxClassId` (ou query equivalente) para o guard.
</requirements>

## Subtasks
- [ ] 5.1 Scaffold `sm-tax-core` + mover tax repositories
- [ ] 5.2 Mover TaxClass/TaxRate services (excluir TaxService*)
- [ ] 5.3 Implementar guard 409 + semântica existsTaxRate
- [ ] 5.4 Rewire `sm-core` e validar TaxService permanece
- [ ] 5.5 Testes unitários do guard/exists + regressão sm-shop TaxRate IT

## Implementation Details
Ver TechSpec: **Interfaces principais** (delete guard), **Convenções de erro**, **Modelos de dados** (tabelas TAX_*), **Ordem de build** passo 5, ADR-003.

### Relevant Files
- `sm-core/src/main/java/com/salesmanager/core/business/repositories/tax/TaxClassRepository.java` — a mover
- `sm-core/src/main/java/com/salesmanager/core/business/repositories/tax/TaxRateRepository.java` — a mover
- `sm-core/src/main/java/com/salesmanager/core/business/services/tax/TaxClassServiceImpl.java` — a mover + guard
- `sm-core/src/main/java/com/salesmanager/core/business/services/tax/TaxRateServiceImpl.java` — a mover + exists fix
- `sm-core/src/main/java/com/salesmanager/core/business/services/tax/TaxServiceImpl.java` — permanece
- `sm-core/pom.xml` — rewire

### Dependent Files
- `sm-tax-core/` — novo módulo (a criar)
- `sm-shop` testes de tax rate / checkout — regressão TAX-07
- Product repository (count by tax class) — suporte ao guard 409

### Related ADRs
- [ADR-002: Shared DB schema for Wave 1](adrs/adr-002.md) — FKs TAX_RATE/PRODUCT retidas
- [ADR-003: Tax admin only; calculation stays in monolith](adrs/adr-003.md) — TaxService fica no sm-core

## Deliverables
- Módulo `sm-tax-core` com CRUD tax (sem calculate)
- Guard 409 + existsTaxRate corrigido
- `sm-core` rewired com TaxService in-process
- Unit tests 80%+ no guard/exists **(REQUIRED)**
- Integration/regressão TaxRate **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] Delete TaxClass com products associados → `TaxClassInUseException`
  - [ ] Delete TaxClass sem products → remove com sucesso
  - [ ] `existsTaxRate` com code ausente → false, sem exception
  - [ ] `existsTaxRate` com code existente → true
- Integration tests:
  - [ ] `@DataJpaTest` smoke nos tax repositories
  - [ ] `./mvnw test -pl sm-core` verde
  - [ ] `./mvnw test -pl sm-shop -Dtest=TaxRateIntegrationTest` verde (TAX-07)
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- `TaxService*` permanece em `sm-core`
- HTTP 409 semantics prontas para o tax-service (task_07)
- Paralelismo com task_04 respeitado (sem dependência mútua)
