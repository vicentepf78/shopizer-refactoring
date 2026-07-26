---
status: pending
title: Gates JaCoCo verify módulos Wave4
type: test
complexity: low
---

# Gates JaCoCo verify módulos Wave4

## Visão geral
Consolida TLC T32. Adiciona thresholds JaCoCo verify em catalog-service, customer-service, sm-catalog-core, sm-customer-core seguindo padrão das Ondas 1–2.

<requirements>
1. MUST configurar JaCoCo em pom.xml dos 4 módulos Wave 4 — T32.
2. MUST passar `./mvnw verify` nos módulos Wave 4.
3. SHOULD alinhar threshold com gates existentes reference-service / merchant-service.
</requirements>

## Entregáveis
- Config JaCoCo em 4 poms
- Gate verify verde **(OBRIGATÓRIO)**

## Testes
- `./mvnw verify -pl catalog-service,customer-service,sm-catalog-core,sm-customer-core`

## Critérios de sucesso
- Fase verify passa nos módulos Wave 4
