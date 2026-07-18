# Codebase Concerns

**Analysis Date:** 2026-07-04
**Scope:** Foco em áreas que impactam Onda 1 (Reference + Tax). Fonte: MIGRATION-MASTER-PLAN.md + exploração de código.

## Tech Debt

**MODEL coupling em APIs (~94% facades):**

- Issue: Entidades JPA expostas ou passadas como parâmetros em facades e APIs
- Files: `ReferencesApi.java` (retorna `Language`, `Currency`); 20+ interfaces em `sm-shop-model/.../facade/`
- Why: Padrão histórico Populator + entidade direta
- Impact: Impossível extrair serviços sem anti-corruption layer
- Fix approach: DTOs na fronteira (Onda 1 Reference/Tax); `LanguageCode` sistêmico (Onda 3)

**Mapper vs Populator duplicado:**

- Issue: ~54 populators + ~42 mappers para mesma transformação
- Files: `ReadableProductPopulator` (~730L) + `ReadableProductMapper` (~692L)
- Impact: Custo de manutenção em cada extração
- Fix approach: Fase 1 consolidação — padrão canônico Mapper

**AbstractConfigurationFacadeImpl vazio:**

- Files: `sm-shop/.../AbstractConfigurationFacadeImpl.java`
- Impact: Stubs em shipping/payment configuration facades
- Fix approach: Quick win S5 do backlog mestre

## Known Bugs

**ReferencesApi entity leak:**

- Symptoms: `GET /languages` e `GET /currency` serializam entidades JPA
- Files: `sm-shop/.../api/v1/references/ReferencesApi.java`
- Workaround: Nenhum
- Root cause: Facades `LanguageFacadeImpl`/`CurrencyFacadeImpl` retornam entidades; sem `ReadableCurrency`
- Blocked by: Onda 1 REF-04, REF-05

**existsTaxRate lança exceção quando ausente:**

- Symptoms: `existsTaxRate` delega a `taxRateByCode` que throw `ResourceNotFoundException`
- Files: `TaxFacadeImpl.java`
- Workaround: Clientes não devem usar endpoint `/unique` para rates como exists check confiável
- Root cause: Implementação incorreta de semântica exists
- Blocked by: TAX-09 (fix durante extração)

**TaxRateRepository query ignora taxClass:**

- Symptoms: Parâmetro `taxClass` na assinatura não aparece no JPQL
- Files: `TaxRateRepository.java`
- Impact: Comportamento incorreto em lookup por tax class + geo
- Fix approach: Corrigir query ou documentar comportamento intencional em Design

**stateProvince → postalCode em address populator:**

- Files: `PersistableCustomerShippingAddressPopulator.java`
- Impact: Dados de endereço incorretos (quick win Fase 1)

## Security Considerations

**Tax admin APIs privadas:**

- Risk: CRUD de tax classes/rates é sensível (impacto fiscal)
- Files: `TaxClassApi.java`, `TaxRatesApi.java` — `/api/v1/private/tax/**`
- Current mitigation: JWT Bearer + role `AUTH`; store scoping via `MerchantStoreArgumentResolver`
- Recommendations: Manter mesma auth no tax-service; validar store ownership em cada mutação (já em `TaxFacadeImpl`)

**Reference APIs públicas:**

- Risk: Baixo — dados de referência são read-mostly
- Files: `ReferencesApi.java` — `/api/v1/country`, `/zones`, `/languages`, `/currency`
- Recommendations: Rate limiting em deploy distribuído; não expor campos internos JPA (AuditSection, etc.)

## Fragile Areas

**InitializationDatabaseImpl:**

- Files: `sm-core/.../reference/init/InitializationDatabaseImpl.java`
- Why fragile: Orquestra reference + merchant + catalog + tax + user + system em uma sequência
- Common failures: Ordem de seed incorreta quebra FK constraints
- Safe modification: Não mover sem split de bootstrap planejado
- Test coverage: Limitado

**Country/Zone localized queries:**

- Files: `CountryServiceImpl`, `ZoneServiceImpl`
- Why fragile: Assumem `descriptions.get(0)` existe; cache manual com chaves string
- Safe modification: Preservar contrato de localização; testes de contrato com lang codes
- Test coverage: Parcial

**LanguageService.toLocale(Language, MerchantStore):**

- Files: `LanguageServiceImpl.java` linhas 54–65
- Why fragile: Acopla reference a merchant geography
- Safe modification: Aceitar `countryCode` string em novo contrato (REF-08)

## Scaling Limits

**LanguageService afferent coupling:**

- Problem: 31 arquivos sm-shop + múltiplos sm-core injetam `LanguageService`
- Impact: Strangler parcial na Onda 1 — maioria dos callers permanece in-process
- Improvement path: Onda 3 — `LanguageCode` + client adapter universal

**Cache manual em reference services:**

- Problem: `CacheUtils` com chaves `COUNTRIES_{lang}`, `ZONES_{country}_{lang}`
- Impact: Invalidação inconsistente em deploy multi-instância sem cache distribuído
- Improvement path: Redis/Infinispan ou cache HTTP no reference-service

## Extraction Risks (Onda 1 specific)

| Risk | Severity | Mitigation |
|------|----------|------------|
| `Product.TAX_CLASS_ID` FK | High | Schema compartilhado AD-003 |
| `TaxRate` → Country/Zone FK | Medium | reference-service + tax-service co-deploy |
| 60+ LanguageService callers | High | Escopo limitado; full migration Onda 3 |
| Bootstrap entanglement | High | AD-004 — init stays in monolith |
| GeoZone orphan | Low | Defer scope decision |
