---
status: pending
title: Boot integration-service + REST admin
type: backend
complexity: high
---

# Boot integration-service + REST admin

## Visão geral
Consolida TLC T17–T19. Cria app Spring Boot `integration-service` na porta 8086 com segurança JWT, actuator e controllers REST admin espelhando APIs de configuração de pagamento/frete.

<critical>
- SEMPRE LER o PRD e a TechSpec antes de iniciar
- Requer P-ready (task_03) e S-ready (task_04)
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST criar módulo Boot `integration-service` escaneando `sm-integration-core` — T17.
2. MUST expor endpoints admin de módulo de pagamento com caminhos congelados — T18, PAY-01..06.
3. MUST expor endpoints admin de configuração de frete — T19, SHP-04.
4. MUST replicar padrão de segurança JWT `/private/**` das Ondas 1–2.
5. MUST restringir entity scan JPA apenas a pacotes relacionados a integration — ADR-003.
6. MUST passar context load `IntegrationServiceApplicationTest`.
</requirements>

## Subtarefas
- [ ] 5.1 Scaffold Boot + security + actuator (T17)
- [ ] 5.2 Controllers admin pagamento (T18)
- [ ] 5.3 Controllers admin frete (T19)
- [ ] 5.4 Testes MockMvc CRUD de config

## ADRs relacionados
- [ADR-003](adrs/adr-003.md) — DB compartilhado
- [ADR-005](adrs/adr-005.md) — registry de plugins no serviço

## Entregáveis
- `integration-service` executável em :8086
- REST admin + testes MockMvc **(REQUIRED)**

## Critérios de sucesso
- Context da aplicação carrega com orquestradores wired
- Roundtrip de config admin para pelo menos um módulo de pagamento + um de frete
