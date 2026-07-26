---
status: pending
title: Pact provider/consumer + IntegrationServiceClient
type: test
complexity: medium
---

# Pact provider/consumer + IntegrationServiceClient

## Visão geral
Consolida TLC T30–T32. Implementa `IntegrationServiceClientRestTemplateImpl` completo; adiciona `IntegrationProviderPactTest` em integration-service e `Wave5ConsumerPactTest` em sm-shop.

<critical>
- SEMPRE LER o PRD e a TechSpec antes de iniciar
- Seguir padrões Pact Ondas 1/2
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST implementar `IntegrationServiceClientRestTemplateImpl` — T32.
2. MUST adicionar pacts provider para config pagamento + cotação frete P1 — T30, STR-07.
3. MUST adicionar consumer `Wave5ConsumerPactTest` em sm-shop — T31.
4. MUST preservar `X-Correlation-Id` no client — STR-05.
5. MUST executar: `./mvnw -pl sm-shop,integration-service -am test -Dtest=Wave5ConsumerPactTest,IntegrationProviderPactTest -DfailIfNoTests=false`
</requirements>

## Subtarefas
- [ ] 11.1 IntegrationServiceClientRestTemplateImpl (T32)
- [ ] 11.2 Testes pact provider (T30)
- [ ] 11.3 Testes pact consumer (T31)
- [ ] 11.4 Substituir stub client da task_08

## Entregáveis
- Implementação completa do client HTTP
- Testes Pact provider + consumer **(REQUIRED)**

## Critérios de sucesso
- Verificação Pact verde
- Contratos consumer publicados em target/pacts
