---
status: pending
title: Imagens de produto via content-service (P2)
type: backend
complexity: medium
---

# Imagens de produto via content-service (P2)

## Visão geral
Consolida TLC T26. Completa adiamento Onda 2 OQ-02: uploads de imagem produto/variante/opção usam ContentServiceClient; estende content-service se necessário para tipos de arquivo de produto.

<requirements>
1. MUST wire ProductOptionFacadeImpl / ProductVariantGroupFacadeImpl para ContentServiceClient — T26.
2. MUST suportar uploads FileContentType PRODUCT/VARIANT/PROPERTY.
3. MAY estender APIs internas content-service para blobs de produto se Onda 2 insuficiente.
4. MUST estender StaticContentProxy para `/static/products/**` se exigido por testes de paridade.
5. SHOULD NOT armazenar blobs no catalog-service.
</requirements>

## ADRs relacionados
- [ADR-007](adrs/adr-007.md)

## Entregáveis
- Facades monólito com chamadas HTTP blob
- Teste de integração upload imagem de opção **(OBRIGATÓRIO)**

## Testes
- `./mvnw test -pl content-service,sm-shop -Dtest=*ProductImage*Test -DfailIfNoTests=false`

## Critérios de sucesso
- Upload admin de imagem de opção atinge content-service
- DTOs read de catálogo retornam URLs de imagem consistentes
