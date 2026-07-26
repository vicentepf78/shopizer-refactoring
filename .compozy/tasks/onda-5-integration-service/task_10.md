---
status: pending
title: Correlation ID + health indicators Wave5
type: infra
complexity: medium
---

# Correlation ID + health indicators Wave5

## Visão geral
Consolida TLC T29. Adiciona filtro de correlation em integration-service; health indicators para DB, registry de módulos, reference-service, catalog-service; verificação de interceptor RestTemplate em sm-shop.

<critical>
- SEMPRE LER o PRD e a TechSpec antes de iniciar
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST adicionar `CorrelationIdFilter` em integration-service — STR-08.
2. MUST adicionar health indicators: datasource, bean paymentModules, ping reference, ping catalog.
3. MUST verificar RestTemplate sm-shop propagando `X-Correlation-Id` para integration-service — STR-05.
4. MUST expor indicators em `/actuator/health` — STR-08.
</requirements>

## Subtarefas
- [ ] 10.1 CorrelationIdFilter + testes
- [ ] 10.2 HealthIndicators customizados
- [ ] 10.3 Teste de integração propagação de correlation

## Entregáveis
- Infraestrutura health + correlation
- `IntegrationServiceHealthTest` **(REQUIRED)**

## Critérios de sucesso
- Actuator health mostra UP/DOWN por dependência
- Correlation id aparece nos logs de integration-service quando chamado pelo BFF
