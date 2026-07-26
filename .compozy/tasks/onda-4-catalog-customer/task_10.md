---
status: pending
title: Checkpoint de integração cross-track
type: backend
complexity: medium
---

# Checkpoint de integração cross-track

## Visão geral
Consolida convergência após CAT-ready (task_03) e CUS-ready (task_05). Verifica ambos os serviços saudáveis juntos com dependências Onda 1–2 antes de migração search e tasks de merge de carrinho prosseguirem em paralelo.

<requirements>
1. MUST verificar catalog-service + customer-service iniciam contra Testcontainers MySQL compartilhado.
2. MUST smoke: catalog GET product + customer GET snapshot APIs internas.
3. MUST verificar reference-service + merchant-service alcançáveis a partir de catalog-service.
4. MUST documentar gaps de paridade encontrados vs baseline monólito.
5. MUST desbloquear execução paralela de task_06 e task_07 após checkpoint passar.
</requirements>

## Entregáveis
- `Wave4ServicesCheckpointTest` ou equivalente **(OBRIGATÓRIO)**
- Relatório curto de checkpoint na memória da task (se usar memória Compozy)

## Testes
- `./mvnw test -pl catalog-service,customer-service -Dtest=*Checkpoint*Test`

## Critérios de sucesso
- Ambos os serviços UP com deps
- CAT-ready + CUS-ready confirmados
- Sem defeitos bloqueantes para tasks downstream
