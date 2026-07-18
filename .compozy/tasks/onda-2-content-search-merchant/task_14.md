---
status: pending
title: Docker Compose, integração, gate e STATE
type: infra
complexity: medium
---

# Docker Compose, integração, gate e STATE

## Overview
Consolida TLC T51–T54. Empacota a topologia Docker Wave2, consolida a suite de integração Strangler, executa o gate `./mvnw clean install` do reactor e atualiza rastreabilidade/STATE do projeto. Cauda sequencial após observabilidade (`task_12`) e Pact (`task_13`).

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST criar `docker-compose-wave2.yml` com mysql, opensearch, content/search/merchant/reference + sm-shop; volume CMS só no content-service — TLC T51.
2. MUST consolidar suite de integração Wave2 (search/content/merchant strangler); documentar `@Ignore` ES opcional — TLC T52.
3. MUST passar `./mvnw clean install` no reactor Onda 1 + Onda 2 e manter Pact consumer verde — TLC T53.
4. MUST atualizar rastreabilidade: requirements → Verified; `STATE.md` Onda 2 Tasks complete; status design — TLC T54.
5. MUST validar `docker compose -f docker-compose-wave2.yml config`.
6. MUST mapear os 28 requirement IDs (CNT/SRCH/MCH/STR) sem gaps.
7. SHOULD reutilizar padrões compose da Onda 1 se existirem.
</requirements>

## Subtasks
- [ ] 14.1 docker-compose-wave2.yml + config válido (T51)
- [ ] 14.2 Suite integração Wave2 consolidada (T52)
- [ ] 14.3 Gate `./mvnw clean install` (T53)
- [ ] 14.4 Traceability + STATE.md + status design (T54)
- [ ] 14.5 Checklist final 28 requirements mapped

## Implementation Details
Ver TechSpec: **Ordem de construção** passo 30, **Abordagem de testes** gates finais. Artefatos TLC em `.specs/features/onda-2-content-search-merchant/` e `.specs/project/STATE.md` (atualizar só na Execute desta task).

### Relevant Files
- Compose Onda 1 existente (se houver) — template
- `content-service/`, `search-service/`, `merchant-service/`, `sm-shop/` — serviços
- `.specs/features/onda-2-content-search-merchant/spec.md` — requirements
- `.specs/project/STATE.md` — estado do projeto
- `.specs/features/onda-2-content-search-merchant/design.md` — status

### Dependent Files
- `docker-compose-wave2.yml` — topologia Wave2
- `*/src/test/java/**/*Wave2*Test.java` — suite integração
- `.specs/features/onda-2-content-search-merchant/spec.md` — Verified
- `.specs/project/STATE.md` — Onda 2 Tasks complete
- `.specs/features/onda-2-content-search-merchant/design.md` — status Approved/complete

### Related ADRs
- [ADR-001: workflow único](adrs/adr-001.md) — empacotamento Wave2
- [ADR-003: search sem JPA](adrs/adr-003.md) — OpenSearch no compose
- [ADR-008: colocalização content](adrs/adr-008.md) — volume CMS só content

## Deliverables
- docker-compose-wave2.yml válido
- Suite integração Wave2 + gate install verde
- STATE/traceability atualizados
- Integration tests da suite Wave2 **(REQUIRED)**
- Evidence do `./mvnw clean install` **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] N/A principal — foco em integração/gate (helpers compose se houver)
- Integration tests:
  - [ ] `docker compose -f docker-compose-wave2.yml config` exit 0
  - [ ] Suite `*Wave2*Test` / integração content+search+merchant+sm-shop verde
  - [ ] `./mvnw clean install` reactor completo passa
  - [ ] `Wave2ConsumerPactTest` permanece verde no install
  - [ ] Checklist: 28 requirements mapped; 0 unmapped
- Test coverage target: >=80% (código de teste/helpers tocados)
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- Topologia Wave2 reproduzível via Docker Compose
- Onda 2 pronta para declarar Tasks complete no STATE
- Gate de build do reactor verde
