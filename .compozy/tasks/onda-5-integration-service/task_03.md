---
status: pending
title: Ops pagamento stateless + testes P-ready
type: backend
complexity: high
---

# Ops pagamento stateless + testes P-ready

## Visão geral
Consolida TLC T9–T11. Implementa `process`, `capture`, `refund`, `init` em `PaymentOrchestrator` retornando `TransactionResult`; garante ausência de dependência `OrderService`; atinge cobertura de testes P-ready.

<critical>
- SEMPRE LER o PRD e a TechSpec antes de iniciar
- CONSULTAR a TechSpec para detalhes de implementação — não duplicar aqui
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST implementar operações de pagamento usando `PaymentModuleV2` — T10.
2. MUST persistir apenas `Transaction` — nunca atualizar Order — T9, T10.
3. MUST retornar DTO `TransactionResult` para todos os desfechos incluindo falha de gateway — PAY-11.
4. MUST atingir JaCoCo ≥70% no pacote do orquestrador de pagamento — T11.
5. MUST documentar GAP-INT-05 se regex de cartão de crédito movida as-is.
</requirements>

## Subtarefas
- [ ] 3.1 Remover qualquer wiring OrderService do core de pagamento (T9)
- [ ] 3.2 Implementar process/capture/refund/init (T10)
- [ ] 3.3 Teste de integração com gateway mock (T10)
- [ ] 3.4 Gate de cobertura P-ready (T11)

## ADRs relacionados
- [ADR-002](adrs/adr-002.md) — pagamento stateless

## Entregáveis
- Operações completas do orquestrador de pagamento
- `PaymentOrchestratorIntegrationTest` **(REQUIRED)**
- ArchUnit ou teste estático banindo OrderService **(REQUIRED)**

## Critérios de sucesso
- Marco P-ready atingido
- Transaction salva; tabela Order intocada nos testes
