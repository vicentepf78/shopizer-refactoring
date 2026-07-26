---
status: pending
title: Outbox processOrder, gate e atualização STATE
type: infra
complexity: medium
---

# Outbox processOrder, gate e atualização STATE

## Visão geral
Consolida TLC T44–T48. Adiciona tabela CHECKOUT_OUTBOX, processOrder em estágios com feature flag, gate reactor, verificação Pact e atualização STATE.md.

<critical>
- SEMPRE LER o PRD e a TechSpec antes de começar
- REFERENCIAR A TECHSPEC para detalhes de implementação — não duplicar aqui
- FOCAR NO "O QUÊ" — descrever o que precisa ser feito, não como
- MINIMIZAR CÓDIGO — mostrar código apenas para ilustrar estrutura atual ou áreas problemáticas
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST adicionar migração schema `CHECKOUT_OUTBOX` — TLC T44.
2. MUST implementar processOrder em estágios em CheckoutApplicationService com escritas outbox — TLC T45–T46.
3. MUST adicionar flag `checkout.outbox.enabled` (default false) e dispatcher in-process — TLC T47.
4. MUST executar gate reactor completo `./mvnw clean install` — TLC T48, GAT-01.
5. MUST atualizar `.specs/project/STATE.md` — Onda 3 completa, status B-001/B-002, referências ADR.
6. MUST verificar suites Pact Onda 1+2 ainda verdes após migração SearchItem.
</requirements>

## Subtarefas
- [ ] 10.1 Tabela outbox + repository (T44)
- [ ] 10.2 Checkout em estágios + eventos outbox (T45–T46)
- [ ] 10.3 Feature flag + dispatcher (T47)
- [ ] 10.4 Gate reactor completo + atualização STATE (T48)

## Detalhes de implementação
Ver TechSpec: **Database**, ADR-005. Estágios: PAYMENT_REQUESTED, PAYMENT_CONFIRMED, ORDER_PERSISTED, INVENTORY_DECREMENTED.

### Arquivos relevantes
- `sm-core/.../checkout/CheckoutApplicationServiceImpl.java`
- `sm-core/.../order/OrderServiceImpl.java` — delegação processOrder
- `.specs/project/STATE.md`

### Arquivos dependentes
- `sm-core/.../checkout/outbox/CheckoutOutboxEvent.java` — criar
- `sm-core/.../checkout/outbox/CheckoutOutboxRepository.java` — criar
- Script migração DB — criar

### ADRs relacionados
- [ADR-005: Outbox transacional local](../adrs/adr-005.md)

## Entregáveis
- Tabela outbox + repository + dispatcher
- processOrder em estágios atrás de flag
- `./mvnw clean install` verde
- STATE.md atualizado
- Testes integração flag on/off **(OBRIGATÓRIO)**

## Testes
- Testes unitários:
  - [ ] Append outbox idempotente por aggregate+event type
- Testes de integração:
  - [ ] Linhas outbox criadas quando flag habilitada
  - [ ] Caminho legacy quando flag desabilitada
  - [ ] Build reactor completo
- Meta de cobertura: >=80%
- Todos os testes devem passar

## Critérios de sucesso
- Todos os testes passando
- Gate Onda 3 verde
- STATE.md reflete conclusão
- Pré-requisitos Ondas 4–6 atendidos no monólito
