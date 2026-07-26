---
status: pending
title: Extrair serviços de leitura sm-catalog-core
type: backend
complexity: high
---

# Extrair serviços de leitura sm-catalog-core

## Visão geral
Consolida TLC T5–T8. Cria `sm-catalog-core` com repositories de leitura, métodos de serviço de leitura, mappers e wiring de delegação sm-core. Métodos de write admin permanecem em sm-core (ADR-006).

<requirements>
1. MUST criar módulo Maven `sm-catalog-core` no reactor — T5.
2. MUST mover métodos de leitura de Product, Category, Manufacturer, Inventory, Pricing services — T6.
3. MUST adicionar mappers ReadableProduct/Category sem vazamento de entidade — T7.
4. MUST wire sm-core para delegar leituras a sm-catalog-core; writes inalterados — T8.
5. MUST passar testes de leitura de produto sm-catalog-core e sm-core.
</requirements>

## Subtarefas
- [ ] 2.1 Scaffold do módulo + repositories (T5)
- [ ] 2.2 Extração de serviços de leitura (T6)
- [ ] 2.3 Mappers (T7)
- [ ] 2.4 Delegação sm-core (T8)

## ADRs relacionados
- [ADR-002](adrs/adr-002.md) — fronteira read-only
- [ADR-004](adrs/adr-004.md) — thin core
- [ADR-006](adrs/adr-006.md) — writes permanecem no monólito

## Entregáveis
- Módulo `sm-catalog-core` com serviços de leitura
- Testes unitários 80%+ em mappers/serviços **(OBRIGATÓRIO)**

## Testes
- `./mvnw test -pl sm-catalog-core`
- `./mvnw test -pl sm-core -Dtest=*Product*Test -DfailIfNoTests=false`

## Critérios de sucesso
- Caminhos de leitura delegam; caminhos de write locais
- Sem dependências Maven circulares
