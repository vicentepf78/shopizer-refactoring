---
status: pending
title: Facades Strangler pagamento/frete
type: backend
complexity: high
---

# Facades Strangler pagamento/frete

## Visão geral
Consolida TLC T25–T26. Implementa `PaymentFacadeHttpAdapter` e `ShippingFacadeHttpAdapter` com `@ConditionalOnProperty(wave5.strangler.enabled)`; mapeia 503 em falha remota sem fallback in-process.

<critical>
- SEMPRE LER o PRD e a TechSpec antes de iniciar
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST implementar adaptadores HTTP para `PaymentConfigurationFacade` — T25, STR-01.
2. MUST implementar adaptadores HTTP para `ShippingFacade` / configuração de frete — T26.
3. MUST propagar `X-Correlation-Id` — STR-05.
4. MUST retornar 503 em erros de conexão/timeout — STR-02.
5. MUST usar stub `IntegrationServiceClientRestTemplateImpl` até task_11 completar client completo.
</requirements>

## Subtarefas
- [ ] 8.1 PaymentConfigurationFacadeHttpAdapter (T25)
- [ ] 8.2 ShippingFacadeHttpAdapter (T26)
- [ ] 8.3 Testes unitários de adapter com MockRestServiceServer
- [ ] 8.4 Wiring de profile em Wave5ClientConfig

## ADRs relacionados
- [ADR-001](adrs/adr-001.md)
- [ADR-006](adrs/adr-006.md)

## Entregáveis
- Adaptadores de facade Strangler em sm-shop
- Testes de adapter **(REQUIRED)**

## Critérios de sucesso
- `wave5.strangler.enabled=true` roteia para client HTTP
- `matchIfMissing=false` preserva default in-process
