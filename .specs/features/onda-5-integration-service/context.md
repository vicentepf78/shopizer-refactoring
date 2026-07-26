# Resoluções OQ — Design Onda 5 (2026-07-26)

Decisões confirmadas para extração de integration-service. Detalhes em `design.md`.

| ID | Decisão | Escolha |
|----|----------|--------|
| OQ-01 | Fronteira de mutação de pedido no pagamento | **integration-service stateless** — retorna `TransactionResult`; monólito/saga checkout persiste status do pedido (Opção A) |
| OQ-02 | Fonte de dados de produto para frete | **Snapshots de leitura de catálogo** via HTTP de catalog-service (Onda 4 parcial) + DTO `ShippingProductSnapshot` (Opção A) |
| OQ-03 | Hospedagem de módulos plugin | **Registry in-process** dentro de integration-service — beans `Map<String, PaymentModule>` / `ShippingQuoteModule` (Opção A) |
| OQ-04 | Persistência de configuração | **MySQL compartilhado** — `MERCHANT_CONFIGURATION`, metadata `INTEGRATION_MODULE`; AD-003 herdado (Opção A) |
| OQ-05 | APIs admin config pagamento/frete | **Migrar para integration-service** — preservar caminhos REST congelados via Strangler (Opção A) |
| OQ-06 | OrderPaymentApi / OrderShippingApi | **Permanecem no BFF** — delegam orquestração ao application service de checkout + clients HTTP integration (Opção B) |

**Decisões de design adicionais:**

| ID | Decisão |
|----|----------|
| AD-015 | Porta `integration-service` **8086**; profile `strangler-wave5` |
| AD-016 | `PaymentModuleV2` / `ShippingQuoteModuleV2` em `sm-core-modules` — fronteiras somente DTO (entregue na Onda 3) |
| AD-017 | Adaptadores `PaymentModule` legados envolvem V2 até todos os plugins migrarem |
| AD-018 | Serviço de criptografia para credenciais de módulo permanece em integration-service |
| AD-019 | Empacotamento de frete (`DefaultPackagingImpl`) co-localizado com orquestração de frete |
| AD-020 | Sem capture/refund sem referência `orderId` — caller fornece apenas id `OrderSnapshot` |

**Pré-requisitos (gates rígidos):**

| Gate | Fonte | Bloqueia |
|------|--------|--------|
| Execute Onda 3 completo | `onda-3-contracts-checkout` | Contratos DTO, application service checkout, fundação saga/outbox |
| Onda 4 parcial — leitura catálogo | `onda-4-catalog-customer` | Leituras `ProductSnapshot` / peso-dimensão para cotações de frete |
| Padrões Ondas 1–2 | reference, strangler, Pact | Clients HTTP, JWT, correlation |

**Status:** Pronto para Tasks
