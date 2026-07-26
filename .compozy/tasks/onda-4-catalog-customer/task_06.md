---
status: pending
title: ProductSnapshot builder + migração search v2
type: backend
complexity: high
---

# ProductSnapshot builder + migração search v2

## Visão geral
Consolida TLC T21–T23. Substitui `ProductIndexPayloadBuilder` por `ProductSnapshotBuilder` no monólito; estende search-service para aceitar ProductSnapshot v2; atualiza SearchIndexProducerHttp.

<requirements>
1. MUST implementar ProductSnapshotBuilder produzindo v2 a partir do modelo de leitura de catálogo — T21.
2. MUST estender serviço de índice search-service para deserialização v2 — T22.
3. MUST atualizar SearchIndexProducerHttp para enviar v2 — T23.
4. MUST manter aceitação temporária v1 no search durante migração.
5. MUST NOT exigir catalog-service UP para indexação no lado write do monólito (builder local).
</requirements>

## Subtarefas
- [ ] 6.1 ProductSnapshotBuilder + testes unitários (T21)
- [ ] 6.2 Intake v2 search-service (T22)
- [ ] 6.3 Migração producer (T23)

## ADRs relacionados
- [ADR-003](adrs/adr-003.md)

## Entregáveis
- Builder + migração producer
- Testes round-trip índice v2 **(OBRIGATÓRIO)**

## Testes
- `./mvnw test -pl sm-core -Dtest=*ProductSnapshotBuilder*Test`
- `./mvnw test -pl search-service -Dtest=*Index*Test`
- `./mvnw test -pl sm-shop -Dtest=*SearchIndexProducer*Test`

## Critérios de sucesso
- Evento de índice produz snapshot v2
- search-service indexa documento v2
