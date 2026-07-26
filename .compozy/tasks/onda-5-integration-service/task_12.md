---
status: pending
title: Docker Compose, gate integração, STATE
type: infra
complexity: medium
---

# Docker Compose, gate integração, STATE

## Visão geral
Consolida TLC T33–T38. Remove beans duplicados de plugins quando strangler habilitado; adiciona `docker-compose-wave5.yml` e `Dockerfile.wave5`; executa gate de integração cross-service; adiciona limites JaCoCo verify; atualiza STATE.md e ROADMAP.

<critical>
- SEMPRE LER o PRD e a TechSpec antes de iniciar
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST corrigir beans duplicados `paymentModules` no profile strangler — T33.
2. MUST adicionar `docker-compose-wave5.yml` com integration-service :8086 — T34, STR-10.
3. MUST adicionar `Dockerfile.wave5` esperando JAR pré-compilado — T35.
4. MUST executar script/teste de gate health cross-service — T36.
5. MUST adicionar limites JaCoCo em integration-service + sm-integration-core — T37.
6. MUST atualizar `.specs/project/STATE.md` e ROADMAP com evidência Onda 5 — T38.
7. MUST executar `./mvnw clean install` antes de marcar completo.
</requirements>

## Subtarefas
- [ ] 12.1 Deduplicação de beans profile strangler (T33)
- [ ] 12.2 docker-compose-wave5.yml + Dockerfile (T34–T35)
- [ ] 12.3 Teste/script gate integração (T36)
- [ ] 12.4 Configuração JaCoCo pom (T37)
- [ ] 12.5 Atualização STATE.md + ROADMAP (T38)

## Entregáveis
- Topologia Docker para Onda 5 local
- Gates JaCoCo verify
- STATE.md atualizado
- `docker compose -f docker-compose-wave5.yml config` passa **(REQUIRED)**

## Testes
- [ ] Cross-service: reference + catalog (parcial) + integration + shop health
- [ ] `./mvnw clean install` verde

## Critérios de sucesso
- Gate Onda 5 documentado com evidência
- GAP-INT-01..05 listados em STATE
- Todos os testes passando; gates de cobertura atendidos
