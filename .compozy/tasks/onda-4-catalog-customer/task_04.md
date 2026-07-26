---
status: pending
title: Extrair sm-customer-core
type: backend
complexity: high
---

# Extrair sm-customer-core

## Visão geral
Consolida TLC T14–T17. Cria `sm-customer-core` com serviços customer, optin, attribute e repositories; wiring de delegação sm-core. Caminhos de criação de customer no checkout permanecem em sm-core (GAP-CUS-01).

<requirements>
1. MUST criar módulo `sm-customer-core` — T14.
2. MUST mover CustomerService, CustomerOptinService, attribute services (excluir create só de order) — T15.
3. MUST adicionar mapper CustomerSnapshot — T16.
4. MUST wire delegação sm-core para caminhos de profile — T17.
</requirements>

## Subtarefas
- [ ] 4.1 Módulo + repositories (T14)
- [ ] 4.2 Extração de serviços (T15)
- [ ] 4.3 Mappers + snapshot (T16)
- [ ] 4.4 Wire sm-core (T17)

## ADRs relacionados
- [ADR-004](adrs/adr-004.md)
- [ADR-005](adrs/adr-005.md)

## Entregáveis
- Módulo sm-customer-core
- Testes unitários 80%+ **(OBRIGATÓRIO)**

## Testes
- `./mvnw test -pl sm-customer-core`
- `./mvnw test -pl sm-core -Dtest=*Customer*Test -DfailIfNoTests=false`

## Critérios de sucesso
- CRUD de profile funciona in-process via thin core
- Criação customer em order inalterada no monólito
