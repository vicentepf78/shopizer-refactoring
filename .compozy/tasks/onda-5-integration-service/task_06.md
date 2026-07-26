---
status: pending
title: APIs REST públicas + internas (I-ready)
type: backend
complexity: high
---

# APIs REST públicas + internas (I-ready)

## Visão geral
Consolida TLC T20–T22. Adiciona endpoint público de métodos de pagamento, APIs internas de pagamento e frete com filtro `X-Internal-Token`. Atinge marco **I-ready**.

<critical>
- SEMPRE LER o PRD e a TechSpec antes de iniciar
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST expor métodos de pagamento aceitos publicamente — T20, PAY-04.
2. MUST expor `/internal/v1/payments/*` com auth por token — T21, PAY-07..12.
3. MUST expor `/internal/v1/shipping/quote` e `/summary` — T22, SHP-01..07.
4. MUST NOT registrar repositório JPA Order em integration-service.
5. MUST mapear erros de gateway para `TransactionResult` ou 502 conforme TechSpec.
6. MUST passar teste de integração: processar pagamento sem UPDATE em Order — I-ready.
</requirements>

## Subtarefas
- [ ] 6.1 Controller público métodos de pagamento (T20)
- [ ] 6.2 Controller interno pagamento + filtro de token (T21)
- [ ] 6.3 Controller interno frete (T22)
- [ ] 6.4 Testes de integração I-ready

## ADRs relacionados
- [ADR-002](adrs/adr-002.md)
- [ADR-006](adrs/adr-006.md)

## Entregáveis
- Controllers API internos + públicos
- `InternalPaymentIntegrationTest` assertando sem writes em Order **(REQUIRED)**
- Testes de filtro de token **(REQUIRED)**

## Critérios de sucesso
- Marco I-ready atingido
- Endpoint health UP em :8086
