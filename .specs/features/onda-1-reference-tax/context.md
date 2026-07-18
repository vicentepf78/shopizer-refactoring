# OQ Resolutions — Onda 1 Design (2026-07-04)

Decisões tomadas com as recomendações da Specify. Detalhes em `design.md`.

| ID | Decisão | Evidência |
|----|---------|-----------|
| OQ-01 | **Excluir GeoZone** | Sem service layer; AD-007 |
| OQ-02 | **HTTP 200 + `[]`** para país inexistente em `/zones` | `ZoneFacadeImpl:33` — 404 comentado |
| OQ-03 | **HTTP 409** ao deletar TaxClass com Products FK | Novo guard — comportamento atual não verifica |
| OQ-04 | **`shopizer-api-contracts` = DTOs only**; mappers/populators nos serviços | DTOs JPA-free; mappers acoplados a entidades |
| OQ-05 | **`PersistableTaxRateMapper` → tax-service** | Resolve refs via `ReferenceServiceClient` |
| OQ-06 | **Config URL** (`wave1.*.base-url`) | Sem service discovery na Onda 1 |

**Decisões adicionais (Design):**

| ID | Decisão |
|----|---------|
| AD-005 | HTTP client = `RestTemplate` |
| AD-006 | tax-service replica JWT chain completa (shared DB) |
| AD-007 | GeoZone fora de escopo Onda 1 (confirma OQ-01) |
