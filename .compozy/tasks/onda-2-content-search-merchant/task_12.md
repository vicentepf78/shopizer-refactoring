---
status: completed
title: Correlation ID e health indicators Wave2
type: infra
complexity: medium
---

# Correlation ID e health indicators Wave2

## Visão geral
Consolida TLC T47–T48. Propaga `X-Correlation-Id` nos apps Wave2 + sm-shop (filter + interceptor RestTemplate) e adiciona health indicators customizados (content: db/cms/reference; search: openSearch; merchant: db/reference/content). Pode avançar em paralelo ao Strangler após os Boot apps existirem.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST adicionar `CorrelationIdFilter` em content-service, search-service, merchant-service e sm-shop — TLC T47.
2. MUST propagar o header via interceptor RestTemplate nos clients Wave2/Wave1 usados pelos serviços — TLC T47.
3. MUST expor `/actuator/health` com indicadores: content (`db`, `cms`, `referenceService`); search (`openSearch`); merchant (`db`, `referenceService`, `contentService`) — TLC T48 / TechSpec Monitoramento.
4. MUST gerar correlation id se ausente no request inbound.
5. MUST incluir correlationId no corpo de erro 503 strangler quando aplicável.
6. SHOULD reutilizar padrão da Onda 1 se já existir filter equivalente.
</requirements>

## Subtarefas
- [x] 12.1 CorrelationIdFilter nos 3 serviços + sm-shop (T47)
- [x] 12.2 Interceptor RestTemplate de propagação (T47)
- [x] 12.3 Health indicators content (T48)
- [x] 12.4 Health indicators search (T48)
- [x] 12.5 Health indicators merchant (T48)

## Detalhes de implementação
Ver TechSpec: **Monitoramento e observabilidade**, **Ordem de construção** passo 28. Requisito STR-05.

### Arquivos relevantes
- `content-service/`, `search-service/`, `merchant-service/`, `sm-shop/` — apps alvo
- `sm-shop/.../strangler/config/Wave2ClientConfig.java` — RestTemplate a interceptar
- Padrão Onda 1 em reference-service/tax-service (se existir) — reutilizar

### Arquivos dependentes
- `*/.../web/CorrelationIdFilter.java` — filters
- `{content,search,merchant}-service/.../health/*HealthIndicator.java` — indicators
- Configs RestTemplate dos três serviços + sm-shop

### ADRs relacionados
- [ADR-001: workflow único](adrs/adr-001.md) — observabilidade compartilhada Wave2

## Entregáveis
- Correlation ID end-to-end nos hops Wave2
- Health components nos 3 serviços
- Unit tests filters/indicators 80%+ **(REQUIRED)**
- Integration smoke `/actuator/health` **(REQUIRED)**

## Testes
- Unit tests:
  - [x] Filter gera UUID quando header ausente
  - [x] Filter preserva header inbound existente
  - [x] Interceptor copia correlation id para request outbound
  - [x] Health indicator openSearch DOWN quando client falha
  - [x] Health indicator cms DOWN quando backend blob inacessível
- Integration tests:
  - [x] `./mvnw test -pl content-service,search-service,merchant-service,sm-shop -Dtest=CorrelationId*Test`
  - [x] `./mvnw test -pl content-service,search-service,merchant-service -Dtest=*HealthIndicatorTest`
  - [x] `/actuator/health` lista componentes esperados por serviço
- Test coverage target: >=80%
- All tests must pass

## Critérios de sucesso
- All tests passing
- Test coverage >=80%
- Correlation id visível em falhas 503
- Health ready para ops Wave2
