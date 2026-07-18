---
status: pending
title: Contratos Pact provider e consumer
type: test
complexity: medium
---

# Contratos Pact provider e consumer

## Overview
Consolida TLC T25–T27. Adiciona verificação Pact provider em `reference-service` e `tax-service` e testes consumer no monolito Strangler, cobrindo todos os endpoints migrados P1 para que drift de schema falhe no CI (TechSpec **Ordem de build** passo 9; STR-02, TAX-08, ADR-005). Artefatos Pact locais são aceitáveis na Wave 1.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST adicionar Pact provider tests para os 5 endpoints do reference-service (STR-02).
2. MUST adicionar Pact provider tests para endpoints tax class + tax rate do tax-service (TAX-08).
3. MUST adicionar Pact consumer tests no `sm-shop` cobrindo contratos reference + tax usados pelos adapters Strangler.
4. MUST falhar a verificação do consumer quando um campo obrigatório do provider for removido/alterado (gate de drift).
5. SHOULD usar provider states mínimos e assertar apenas campos que o consumer precisa (TechSpec **Riscos conhecidos**).
6. MUST gerar/consumir pacts sob paths locais convencionados (ex.: `target/pacts`).
</requirements>

## Subtasks
- [ ] 9.1 Pact provider reference-service (5 endpoints)
- [ ] 9.2 Pact provider tax-service (class + rate)
- [ ] 9.3 Pact consumer Wave1 no sm-shop
- [ ] 9.4 Prova negativa: quebra de campo falha verificação consumer
- [ ] 9.5 Documentar comando gate Pact na task verify

## Implementation Details
Ver TechSpec: **Abordagem de testes** (Contratos / gate), **Pontos de integração** (Artefatos Pact), **Ordem de build** passo 9, ADR-005. Endpoints cobertos = listas do TechSpec **Endpoints da API**.

### Relevant Files
- `reference-service/src/main/java/...` — providers REST (task_06)
- `tax-service/src/main/java/...` — providers REST (task_07)
- `sm-shop/.../strangler/` — consumers HTTP (task_08)
- `shopizer-api-contracts/` — shape dos DTOs wire

### Dependent Files
- `reference-service/src/test/java/.../contract/ReferenceProviderPactTest.java` — a criar
- `tax-service/src/test/java/.../contract/TaxProviderPactTest.java` — a criar
- `sm-shop/src/test/java/.../contract/Wave1ConsumerPactTest.java` — a criar
- Artefatos em `target/pacts` (ou path Pact do projeto)

### Related ADRs
- [ADR-005: Contract DTOs / no JPA in REST responses; Pact](adrs/adr-005.md) — gates Pact obrigatórios

## Deliverables
- Provider tests reference + tax
- Consumer test monolito
- Prova de falha por drift documentada
- Contract tests com cobertura dos endpoints P1 **(REQUIRED)**
- Gates Maven dos três módulos **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] N/A específico — foco em contrato; helpers de state podem ter smoke unitário se criados
- Integration tests:
  - [ ] `ReferenceProviderPactTest` verifica GET country/zones/languages/currency/measures
  - [ ] `TaxProviderPactTest` verifica CRUD/unique paths tax class e tax rate (estados mínimos)
  - [ ] `Wave1ConsumerPactTest` passa contra pacts locais
  - [ ] Remover campo obrigatório no provider (teste controlado) → consumer verification falha
- Test coverage target: >=80% (código de suporte a contrato introduzido)
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- Pacts gerados e consumidos localmente
- Drift de schema detectável no CI antes do deploy
