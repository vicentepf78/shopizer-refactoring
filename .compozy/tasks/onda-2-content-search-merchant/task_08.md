---
status: pending
title: Extrair sm-merchant-core sem ProductType
type: backend
complexity: high
---

# Extrair sm-merchant-core sem ProductType

## Overview
Consolida TLC T30–T32. Cria o thin `sm-merchant-core`, move repositórios e serviços de merchant store/config/log, remove a injeção morta de `ProductTypeService` e rewire o `sm-core`. Pode fan-out em paralelo às trilhas content/search após `task_01`.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST scaffold `sm-merchant-core` e mover repos merchant + MerchantConfiguration/MerchantLog — TLC T30.
2. MUST mover `MerchantStoreService*`, `MerchantConfiguration*`, `MerchantLog*` — TLC T31.
3. MUST remover injeção/uso de `ProductTypeService` do merchant core (MCH-06 / ADR-007) — TLC T31.
4. MUST rewire `sm-core` com dep `sm-merchant-core` e remover classes movidas — TLC T32.
5. MUST NOT extrair `ProductTypeApi` nem seeding de product-type.
6. MUST preservar hierarquia PARENT_ID / regras de loja default.
7. MUST passar `./mvnw test -pl sm-merchant-core` e `./mvnw test -pl sm-core`.
</requirements>

## Subtasks
- [ ] 8.1 Scaffold módulo + mover repositórios (T30)
- [ ] 8.2 Mover serviços merchant e dropar ProductType (T31)
- [ ] 8.3 Wire sm-core → sm-merchant-core (T32)
- [ ] 8.4 Registrar módulo no reactor
- [ ] 8.5 Gates de teste sm-merchant-core e sm-core

## Implementation Details
Ver TechSpec: **Arquitetura** (`sm-merchant-core`), **Ordem de construção** passo 22. Padrão thin core da Onda 1 / ADR-004.

### Relevant Files
- `sm-core/src/main/java/com/salesmanager/core/business/repositories/merchant/` — repos store
- `sm-core/src/main/java/com/salesmanager/core/business/repositories/system/MerchantConfigurationRepository.java` — config
- `sm-core/src/main/java/com/salesmanager/core/business/repositories/system/MerchantLogRepository.java` — log
- `sm-core/src/main/java/com/salesmanager/core/business/services/merchant/MerchantStoreService.java` — service
- `sm-core/src/main/java/com/salesmanager/core/business/services/merchant/MerchantStoreServiceImpl.java` — impl
- `sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/store/MerchantStoreApi.java` — superfície a NÃO portar ProductType

### Dependent Files
- `sm-merchant-core/pom.xml` — novo módulo
- `sm-merchant-core/src/main/java/.../repositories/` — repos movidos
- `sm-merchant-core/src/main/java/.../services/` — serviços movidos
- `sm-core/pom.xml` — dep + remoção classes
- `pom.xml` — registro reactor

### Related ADRs
- [ADR-004: thin cores](adrs/adr-004.md) — padrão módulo
- [ADR-007: Sem ProductType na Onda 2](adrs/adr-007.md) — exclusão explícita

## Deliverables
- Módulo `sm-merchant-core` sem ProductType
- sm-core rewired
- Integration tests 80%+ superfícies movidas **(REQUIRED)**
- Gates sm-merchant-core + sm-core **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] MerchantStoreService não referencia ProductTypeService
  - [ ] Default store delete protection permanece
- Integration tests:
  - [ ] `./mvnw test -pl sm-merchant-core` verde
  - [ ] `./mvnw test -pl sm-core` verde após rewire
  - [ ] Zero rotas/services ProductType no módulo merchant-core
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- ProductType fora do BC merchant Wave2
- Thin core utilizável pelo merchant-service
