---
status: pending
title: Migração SearchItem para api-contracts
type: backend
complexity: medium
---

# Migração SearchItem para api-contracts

## Visão geral
Consolida TLC T17–T20. Move `SearchItem` e tipos de resposta de search relacionados de `shopizer-commons` para `shopizer-api-contracts`, resolvendo OQ-06 da Onda 2.

<critical>
- SEMPRE LER o PRD e a TechSpec antes de começar
- REFERENCIAR A TECHSPEC para detalhes de implementação — não duplicar aqui
- FOCAR NO "O QUÊ" — descrever o que precisa ser feito, não como
- MINIMIZAR CÓDIGO — mostrar código apenas para ilustrar estrutura atual ou áreas problemáticas
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST copiar/mover `SearchItem`, `SearchProductRequest` (se ainda não em contracts) para `com.salesmanager.contracts.search` — TLC T17.
2. MUST atualizar imports `search-service`, `sm-shop` SearchApi/Facade para package contracts — TLC T18.
3. MUST adicionar re-exports deprecados em commons (aliases finos opcionais) por uma release — TLC T19.
4. MUST atualizar testes Pact Wave2 para usar tipos contracts — TLC T20.
5. MUST preservar nomes de campo JSON byte-compatíveis com Pact existente.
</requirements>

## Subtarefas
- [ ] 4.1 DTOs search em api-contracts (T17)
- [ ] 4.2 Reescrita de imports em sm-shop e search-service (T18)
- [ ] 4.3 Aliases deprecados em commons se necessário (T19)
- [ ] 4.4 Atualizações consumer/provider Pact (T20)

## Detalhes de implementação
Ver TechSpec: **Estratégia de testes**. Depende de `task_02` para modelo de documento search alinhado.

### Arquivos relevantes
- `modules/shopizer-commons/` ou `modules.commons.search` — SearchItem atual
- `sm-shop/.../api/v1/search/SearchApi.java`
- `search-service/.../SearchController.java`
- `sm-shop/src/test/.../pact/` — testes Pact Wave2

### Arquivos dependentes
- `shopizer-api-contracts/.../search/SearchItem.java` — criar
- `shopizer-api-contracts/.../search/SearchProductRequest.java` — verificar/mover

### ADRs relacionados
- [ADR-002: ProductSnapshot](../adrs/adr-002.md) — alinhamento index/query

## Entregáveis
- SearchItem em api-contracts
- Todos os módulos compilam com novos imports
- Testes Pact verdes **(OBRIGATÓRIO)**

## Testes
- Testes unitários:
  - [ ] SearchItem Jackson compatível com fixtures JSON legacy
- Testes de integração:
  - [ ] `./mvnw test -pl sm-shop,search-service -Dtest=*Pact* -DfailIfNoTests=false`
- Meta de cobertura: >=80%
- Todos os testes devem passar

## Critérios de sucesso
- Todos os testes passando
- OQ-06 fechado
- Sem mudanças de caminho API search
