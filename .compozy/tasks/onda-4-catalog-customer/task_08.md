---
status: pending
title: Product images via content-service (P2)
type: backend
complexity: medium
---

# Product images via content-service (P2)

## Overview
Consolidates TLC T26. Completes Onda 2 OQ-02 deferral: product/variant/option image uploads use ContentServiceClient; extend content-service if needed for product file types.

<requirements>
1. MUST wire ProductOptionFacadeImpl / ProductVariantGroupFacadeImpl to ContentServiceClient — T26.
2. MUST support FileContentType PRODUCT/VARIANT/PROPERTY uploads.
3. MAY extend content-service internal APIs for product blobs if Wave 2 insufficient.
4. MUST extend StaticContentProxy for `/static/products/**` if required by parity tests.
5. SHOULD NOT store blobs in catalog-service.
</requirements>

## Related ADRs
- [ADR-007](adrs/adr-007.md)

## Deliverables
- Monolith facade HTTP blob calls
- Integration test option image upload **(REQUIRED)**

## Tests
- `./mvnw test -pl content-service,sm-shop -Dtest=*ProductImage*Test -DfailIfNoTests=false`

## Success Criteria
- Admin option image upload hits content-service
- Catalog read DTOs return consistent image URLs
