---
status: pending
title: Trim sm-core + fronteira stateless monólito
type: backend
complexity: medium
---

# Trim sm-core + fronteira stateless monólito

## Visão geral
Consolida TLC T23–T24. Faz trim ou delega `PaymentServiceImpl`/`ShippingServiceImpl` em sm-core; remove `orderService.saveOrUpdate` do caminho de pagamento quando `wave5.strangler.enabled`; garante que saga de checkout é dona das atualizações de pedido.

<critical>
- SEMPRE LER o PRD e a TechSpec antes de iniciar
- Requer saga de checkout da Onda 3 funcional
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST delegar ou deprecar serviços in-process de pagamento/frete quando extraídos — T23.
2. MUST condicionar writes de pedido a `!wave5.strangler.enabled` para rollback — T24, ADR-017.
3. MUST manter monólito compilando e testes non-strangler verdes — T23.
4. MUST adicionar teste provando que pagamento strangler não chama save de pedido em PaymentServiceImpl — T24.
</requirements>

## Subtarefas
- [ ] 7.1 Trim serviços sm-core + atualizar wiring (T23)
- [ ] 7.2 Flag de fronteira stateless + teste (T24)
- [ ] 7.3 Atualizar spring XML / component scan se necessário

## ADRs relacionados
- [ADR-002](adrs/adr-002.md)
- [ADR-017](adrs/adr-017.md) em design.md

## Entregáveis
- Stubs de delegação ou beans condicionais em sm-core
- `StatelessPaymentBoundaryTest` em sm-shop **(REQUIRED)**

## Critérios de sucesso
- `./mvnw test -pl sm-core,sm-shop -DfailIfNoTests=false` passa
- Teste profile strangler mostra sem update de Order a partir de PaymentServiceImpl
