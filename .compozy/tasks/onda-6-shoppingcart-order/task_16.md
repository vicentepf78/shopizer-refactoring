---
status: pending
title: Docker Compose wave6, E2E, gate STATE/ROADMAP
type: infra
complexity: medium
---

# Docker Compose wave6, E2E, gate STATE/ROADMAP

## Visão geral
TLC T43–T45. `docker-compose-wave6.yml` completo; Wave6E2EIntegrationTest (cart → totals → place order → read order); atualizar STATE.md e ROADMAP.md marcando Onda 6 completa.

<requirements>
1. MUST adicionar docker-compose-wave6.yml com todos os serviços dependentes — T43.
2. MUST passar `docker compose -f docker-compose-wave6.yml config` — T43.
3. MUST implementar Wave6E2EIntegrationTest — T44, CHK-01.
4. MUST atualizar STATE.md (AD-020+) e ROADMAP.md — T45.
5. SHOULD executar `./mvnw clean install` quando reator completo incluir todas as ondas.
</requirements>

## Entregáveis
- docker-compose-wave6.yml + Dockerfiles
- Teste E2E
- Atualizações STATE.md + ROADMAP.md

## Testes
- `./mvnw test -pl sm-shop -Dtest=Wave6E2EIntegrationTest`
- `docker compose -f docker-compose-wave6.yml config`

## Critérios de sucesso
- E2E verde contra topologia compose
- Onda 6 marcada completa na documentação do projeto
