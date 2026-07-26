---
status: pending
title: ProductSnapshot e evolução do payload de índice
type: backend
complexity: high
---

# ProductSnapshot e evolução do payload de índice

## Visão geral
Consolida TLC T7–T12. Entrega DTO canônico `ProductSnapshot`, builder a partir de JPA `Product` e mapper `ProductIndexPayload` com `schemaVersion` 2 conforme ADR-002.

<critical>
- SEMPRE LER o PRD e a TechSpec antes de começar
- REFERENCIAR A TECHSPEC para detalhes de implementação — não duplicar aqui
- FOCAR NO "O QUÊ" — descrever o que precisa ser feito, não como
- MINIMIZAR CÓDIGO — mostrar código apenas para ilustrar estrutura atual ou áreas problemáticas
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST definir `ProductSnapshot` e DTOs aninhados em `shopizer-api-contracts` — TLC T7.
2. MUST implementar `ProductSnapshotBuilder` no monólito (sm-core ou sm-shop) mapeando de `Product` + store + language — TLC T8–T9.
3. MUST refatorar producer de índice existente para construir snapshot primeiro, depois mapear para `ProductIndexPayload` — TLC T10.
4. MUST bump `schemaVersion` default para 2 quando snapshot-backed — TLC T11.
5. MUST atualizar handler de índice `search-service` para aceitar schema v1 e v2 — TLC T12.
6. MUST NOT adicionar dependências JPA em api-contracts.
</requirements>

## Subtarefas
- [ ] 2.1 DTO `ProductSnapshot` + testes Jackson (T7)
- [ ] 2.2 `ProductSnapshotBuilder` a partir de serviços de catálogo (T8–T9)
- [ ] 2.3 `ProductIndexPayloadMapper` a partir de snapshot (T10–T11)
- [ ] 2.4 Aceitação índice v2 em search-service (T12)

## Detalhes de implementação
Ver TechSpec: **Evolução ProductIndexPayload**, ADR-002. Fonte: `ProductIndexPayloadBuilder` / `SearchIndexProducerHttp` existentes da Onda 2.

### Arquivos relevantes
- `shopizer-api-contracts/.../search/ProductIndexPayload.java`
- `sm-core/.../events/products/` — listeners de índice
- `sm-shop/.../strangler/search/` — producer HTTP
- `search-service/.../index/` — API interna de índice

### Arquivos dependentes
- `shopizer-api-contracts/.../catalog/ProductSnapshot.java` — criar
- `sm-core/.../catalog/ProductSnapshotBuilder.java` — criar
- `sm-shop/.../search/ProductIndexPayloadMapper.java` — criar

### ADRs relacionados
- [ADR-002: ProductSnapshot substitui ProductIndexPayload](../adrs/adr-002.md)

## Entregáveis
- Família DTO ProductSnapshot
- Builder + mapper payload
- Handler de índice retrocompatível em search-service
- Testes unitários 80%+ em DTOs e mapper **(OBRIGATÓRIO)**

## Testes
- Testes unitários:
  - [ ] Builder snapshot mapea SKU, nome, store code
  - [ ] Payload schemaVersion 2 quando construído de snapshot
  - [ ] Payload v1 ainda deserializa
- Testes de integração:
  - [ ] Producer de índice posta payload v2 em slice de teste search-service
- Meta de cobertura: >=80%
- Todos os testes devem passar

## Critérios de sucesso
- Todos os testes passando
- Milestone SNP-ready
- Caminho de evolução AD-009 implementado
