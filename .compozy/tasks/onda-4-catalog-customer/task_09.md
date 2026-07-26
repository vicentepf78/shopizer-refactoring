---
status: pending
title: Adaptadores HTTP Strangler catalog + customer
type: backend
complexity: high
---

# Adaptadores HTTP Strangler catalog + customer

## Visão geral
Consolida TLC T27–T28. Implementa CatalogFacadeHttpAdapter (somente leitura) e CustomerFacadeHttpAdapter (profile/address/optin). Métodos de auth permanecem in-process.

<requirements>
1. MUST delegar métodos de **leitura** de ProductFacade/ProductCommonFacade/CategoryFacade para catalog-service — T27.
2. MUST delegar profile/address/optin do CustomerFacade para customer-service — T28.
3. MUST retornar 503 em falha remota sem fallback in-process quando strangler on.
4. MUST encaminhar JWT + X-Correlation-Id em rotas privadas.
5. MUST NOT delegar POST/PUT/DELETE admin privado de produto — AD-006.
</requirements>

## ADRs relacionados
- [ADR-002](adrs/adr-002.md)
- [ADR-006](adrs/adr-006.md)

## Entregáveis
- CatalogFacadeHttpAdapter + CustomerFacadeHttpAdapter
- Testes de integração por adaptador **(OBRIGATÓRIO)**

## Testes
- `./mvnw test -pl sm-shop -Dtest=*CatalogFacadeHttp*Test,*CustomerFacadeHttp*Test`

## Critérios de sucesso
- Strangler on: GET product remoto; POST product local
- Strangler off: comportamento in-process preservado
