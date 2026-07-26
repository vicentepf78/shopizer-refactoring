---
status: pending
title: Docker Compose wave4, gate, STATE
type: infra
complexity: medium
---

# Docker Compose wave4, gate, STATE

## Visão geral
Consolida TLC T35–T38. Empacota docker-compose-wave4.yml, suite de integração Wave4, gate reator `./mvnw clean install`, atualiza STATE.md e rastreabilidade de requisitos para Verified.

<requirements>
1. MUST criar `docker-compose-wave4.yml` estendendo Onda 2 com catalog:8086 + customer:8087 — T35.
2. MUST consolidar suite `*Wave4*Integration*Test` — T36.
3. MUST passar `./mvnw clean install` reator completo — T37.
4. MUST atualizar STATE.md, rastreabilidade spec 30/30 Verified, status design — T38.
5. MUST validar `docker compose -f docker-compose-wave4.yml config`.
6. MUST mapear todos os 30 IDs de requisito (CAT/CUS/STR) sem gaps.
</requirements>

## Subtarefas
- [ ] 15.1 docker-compose-wave4.yml (T35)
- [ ] 15.2 Suite de integração (T36)
- [ ] 15.3 Gate reator (T37)
- [ ] 15.4 STATE + rastreabilidade (T38)

## ADRs relacionados
- [ADR-001](adrs/adr-001.md)

## Entregáveis
- docker-compose-wave4.yml
- Suite de integração + evidência install **(OBRIGATÓRIO)**
- STATE.md atualizado

## Testes
- `docker compose -f docker-compose-wave4.yml config`
- `./mvnw test -pl sm-shop -Dtest=*Wave4*Integration*Test`
- `./mvnw clean install`
- `./mvnw test -pl sm-shop -Dtest=Wave4ConsumerPactTest -DfailIfNoTests=false`

## Critérios de sucesso
- Topologia Wave 4 reproduzível
- Gate reator verde
- 30/30 requisitos Verified em spec.md
- Onda 4 pronta para declarar Execute completo no STATE
