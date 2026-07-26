---
status: pending
title: Correlation ID + health indicators Wave4
type: infra
complexity: medium
---

# Correlation ID + health indicators Wave4

## Visão geral
Consolida TLC T31. Adiciona CorrelationIdFilter em catalog-service e customer-service; health indicators para db, reference, merchant (catalog).

<requirements>
1. MUST propagar X-Correlation-Id nos serviços Wave 4 e interceptor RestTemplate — T31.
2. MUST adicionar actuator health: catalog → db, referenceService, merchantService.
3. MUST adicionar actuator health: customer → db, referenceService.
4. MUST corresponder aos padrões de health das Ondas 1–2.
</requirements>

## Entregáveis
- Filters + health indicators
- Testes de integração health **(OBRIGATÓRIO)**

## Testes
- `./mvnw test -pl catalog-service,customer-service -Dtest=*Health*Test,*Correlation*Test`

## Critérios de sucesso
- /actuator/health mostra componentes de dependência
- Correlation id presente em logs cross-service (smoke manual OK)
