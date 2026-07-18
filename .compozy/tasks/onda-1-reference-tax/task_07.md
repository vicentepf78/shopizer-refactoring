---
status: pending
title: tax-service Boot, JWT, client Reference e REST
type: backend
complexity: high
---

# tax-service Boot, JWT, client Reference e REST

## Overview
Consolida TLC T17–T20. Entrega `tax-service` (porta 8082) com cadeia JWT, implementação HTTP de `ReferenceServiceClient`, facade/mappers de tax e REST privado sob `/api/v1/private/tax/*` — TechSpec **Ordem de build** passo 7. Depende dos endpoints de reference (task_06) e do core tax (task_05).

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST criar Boot app `tax-service` com `server.port=8082`, DB compartilhado e security JWT espelhando APIs privadas (TAX-01, TAX-05, ADR-008).
2. MUST rejeitar requests sem/invalid token com 401 em `/api/v1/private/**`.
3. MUST implementar `ReferenceServiceClient` via RestTemplate + `wave1.reference-service.base-url` (TAX-06, ADR-004).
4. MUST portar `TaxFacade` + mappers; `PersistableTaxRateMapper` resolve country/zone via client HTTP (não CountryService in-process).
5. MUST espelhar paths de `TaxClassApi` / `TaxRatesApi` (TechSpec **Endpoints da API** tax-service).
6. MUST propagar 409 em delete de tax class em uso e `{exists:false}` em unique checks (OQ-03, TAX-09).
7. MUST NOT mover `TaxService.calculateTax` para este serviço (ADR-003).
</requirements>

## Subtasks
- [ ] 7.1 Scaffold Boot + JWT security chain + IT 401
- [ ] 7.2 Implementar `ReferenceServiceClient` HTTP + testes MockRestServiceServer/WireMock
- [ ] 7.3 Portar TaxFacadeImpl + 4 mappers (rate usa client)
- [ ] 7.4 Expor controllers REST private tax (class + rate)
- [ ] 7.5 IT CRUD autenticado + validação country/zone inválido → 400

## Implementation Details
Ver TechSpec: **Endpoints da API** (tax-service), **Pontos de integração** (tax→reference, JWT), **Configuração**, **Ordem de build** passo 7, **Convenções de erro**. Login permanece só no monolito.

### Relevant Files
- `sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/tax/TaxClassApi.java` — paths class
- `sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/tax/TaxRatesApi.java` — paths rate
- `sm-shop/src/main/java/com/salesmanager/shop/store/facade/tax/TaxFacadeImpl.java` — fonte facade
- `sm-shop/src/main/java/com/salesmanager/shop/mapper/tax/PersistableTaxRateMapper.java` — mapper a adaptar
- `sm-shop/src/main/java/com/salesmanager/shop/mapper/tax/PersistableTaxClassMapper.java` — mapper
- `sm-tax-core/` — CRUD services (task_05)
- `shopizer-api-contracts/.../client/ReferenceServiceClient.java` — contrato
- `reference-service/` — upstream HTTP (task_06)

### Dependent Files
- `tax-service/` — novo módulo (a criar)
- Config JWT/`jwt.secret` compartilhado com monolito
- Root `pom.xml` — registrar módulo

### Related ADRs
- [ADR-003: Tax admin only; calculation stays in monolith](adrs/adr-003.md) — só admin
- [ADR-004: RestTemplate Strangler clients](adrs/adr-004.md) — client HTTP
- [ADR-008: JWT replication for tax-service](adrs/adr-008.md) — cadeia JWT completa

## Deliverables
- JAR `tax-service` na 8082 com JWT
- Client Reference + facade/mappers + REST private
- Unit + security/API ITs com 80%+ coverage **(REQUIRED)**
- Paridade de paths com monolito para tax privado **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] `ReferenceServiceClient` resolve country/zone por code (MockRestServiceServer)
  - [ ] PersistableTaxRateMapper falha com validação quando reference retorna country inválido
  - [ ] Facade delete class em uso propaga conflito (409)
- Integration tests:
  - [ ] Request sem JWT em `/api/v1/private/tax/class` → 401
  - [ ] CRUD tax class com JWT válido → 2xx e escopo de store
  - [ ] CRUD tax rate com descriptions i18n + codes válidos
  - [ ] GET unique tax-rate code ausente → 200 `{exists:false}`
  - [ ] Store mismatch → 403 (paridade)
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- `./mvnw test -pl tax-service -Dtest=TaxSecurityIntegrationTest,TaxApiIntegrationTest` verdes
- Nenhum import in-process de CountryService no mapper de rate
