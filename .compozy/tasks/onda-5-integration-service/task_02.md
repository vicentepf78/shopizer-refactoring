---
status: pending
title: Plugins pagamento + extração PaymentOrchestrator
type: backend
complexity: high
---

# Plugins pagamento + extração PaymentOrchestrator

## Visão geral
Consolida TLC T6–T8. Move implementações de plugins de pagamento para `sm-integration-core`, adiciona ponte adaptadora V2 e extrai configuração/resolução de módulos de `PaymentServiceImpl` para `PaymentOrchestratorImpl` (sem métodos de mutação de pedido).

<critical>
- SEMPRE LER o PRD e a TechSpec antes de iniciar
- CONSULTAR a TechSpec para detalhes de implementação — não duplicar aqui
- FOCAR NO "O QUÊ" — descrever o que precisa ser feito, não como
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST relocar `sm-core/.../modules/integration/payment/impl/*` para `sm-integration-core` — T6.
2. MUST implementar `PaymentModuleV2Adapter` fazendo ponte com plugins legados — T7.
3. MUST extrair CRUD de config, `getPaymentMethods`, validação de cartão de crédito para orquestrador — T8.
4. MUST NOT incluir `OrderService` no pacote de pagamento de sm-integration-core.
5. MUST preservar comportamento de criptografia para credenciais armazenadas.
6. MUST passar testes unitários de plugins relocados.
</requirements>

## Subtarefas
- [ ] 2.1 Mover classes de plugins de pagamento + atualizar `ModulesConfiguration` (T6)
- [ ] 2.2 `PaymentModuleV2Adapter` + utilitários de mapper (T7)
- [ ] 2.3 `PaymentOrchestratorImpl` caminhos de config (T8)
- [ ] 2.4 Testes unitários save/load de config

## Detalhes de implementação
TechSpec **Ordem de construção** passos 3–4. Fonte: `PaymentServiceImpl.java`, `ModulesConfiguration.java`.

### Arquivos relevantes
- `sm-core/.../modules/integration/payment/impl/StripePayment.java` (e similares)
- `sm-core/.../services/payments/PaymentServiceImpl.java`
- `sm-core/.../configuration/ModulesConfiguration.java`

### ADRs relacionados
- [ADR-004](adrs/adr-004.md) — contratos V2
- [ADR-005](adrs/adr-005.md) — registry in-process

## Entregáveis
- Plugins de pagamento em sm-integration-core
- Interface + impl `PaymentOrchestrator` (porção config)
- Testes unitários >=80% nos caminhos de config do orquestrador **(REQUIRED)**

## Testes
- [ ] Roundtrip de configuração MoneyOrder ou módulo mock
- [ ] Adapter invoca módulo legado com contexto mapeado para DTO
- [ ] `./mvnw test -pl sm-integration-core -Dtest=*Payment*`

## Critérios de sucesso
- Sem import `OrderService` no pacote do orquestrador de pagamento
- Todos os testes de plugins de pagamento verdes no novo módulo
