---
status: pending
title: Correção DTO ReferencesApi e plano de migração facade
type: backend
complexity: medium
---

# Correção DTO ReferencesApi e plano de migração facade

## Visão geral
Consolida TLC T35–T38. Fecha blocker B-002 em ReferencesApi e publica inventário faseado para migrações facade restantes (FAC-06).

<critical>
- SEMPRE LER o PRD e a TechSpec antes de começar
- REFERENCIAR A TECHSPEC para detalhes de implementação — não duplicar aqui
- FOCAR NO "O QUÊ" — descrever o que precisa ser feito, não como
- MINIMIZAR CÓDIGO — mostrar código apenas para ilustrar estrutura atual ou áreas problemáticas
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST retornar `List<ReadableLanguage>` dos endpoints de lista de language — TLC T35, REF-01.
2. MUST retornar `List<ReadableCurrency>` dos endpoints de currency — TLC T36, REF-02.
3. MUST conectar populators/mappers existentes (padrão Onda 1) — TLC T37.
4. MUST adicionar `docs/decomposition/FACADE-MIGRATION-PLAN.md` inventário de 76 facades com fases Onda 4–6 — TLC T38, FAC-06.
5. MUST atualizar Pact Reference se tipos de resposta mudam.
</requirements>

## Subtarefas
- [ ] 8.1 Wiring DTO language ReferencesApi (T35)
- [ ] 8.2 Wiring DTO currency ReferencesApi (T36)
- [ ] 8.3 Teste integração ReferencesApi (T37)
- [ ] 8.4 Documento plano migração facade (T38)

## Detalhes de implementação
Ver TechSpec: **Correção ReferencesApi (B-002)**. `ReadableLanguage` / `ReadableCurrency` já existem em api-contracts da Onda 1.

### Arquivos relevantes
- `sm-shop/.../api/v1/references/ReferencesApi.java`
- `shopizer-api-contracts/.../reference/ReadableLanguage.java`
- `sm-shop/.../populator/references/`

### Arquivos dependentes
- `docs/decomposition/FACADE-MIGRATION-PLAN.md` — criar

### ADRs relacionados
- [ADR-003: Facades restantes adiadas](../adrs/adr-003.md)

## Entregáveis
- B-002 fechado em ReferencesApi
- Markdown plano migração facade
- Testes Pact/reference verdes **(OBRIGATÓRIO)**

## Testes
- Testes unitários:
  - [ ] JSON ReferencesApi não contém nomes de tipo entidade JPA
- Testes de integração:
  - [ ] `GET /api/v1/languages` retorna shape ReadableLanguage
- Meta de cobertura: >=80%
- Todos os testes devem passar

## Critérios de sucesso
- Todos os testes passando
- B-002 resolvido
- Documento FAC-06 publicado
