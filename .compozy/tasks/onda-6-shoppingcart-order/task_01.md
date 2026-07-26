---
status: pending
title: Gate Ondas 3–5 + contratos Wave6 e config Strangler
type: backend
complexity: medium
---

# Gate Ondas 3–5 + contratos Wave6 e config Strangler

## Visão geral
Consolida TLC T1–T5, T46–T51. Verifica gate Ondas 3–5; entrega DTOs cart/order/checkout e clients HTTP em `shopizer-api-contracts`; profile Strangler Wave6 e stubs RestTemplate no sm-shop.

<critical>
- SEMPRE LER o PRD e a TechSpec antes de iniciar
- CONSULTAR a TechSpec para detalhes de implementação — não duplicar aqui
- FOCAR NO "O QUÊ" — descrever o que precisa ser feito, não como
- MINIMIZAR CÓDIGO — mostrar código só para ilustrar estrutura atual ou áreas problemáticas
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST adicionar `scripts/wave6-gate.sh` verificando artefatos Ondas 3–5 e testes verdes — TLC T1.
2. MUST adicionar DTOs cart/order/checkout (`CartLineSnapshot`, `OrderSnapshot`, `CartTotalsRequest/Response`, `CheckoutCommitRequest/Response`) — TLC T2, T46, T47.
3. MUST adicionar interfaces client `ShoppingCartServiceClient`, `OrderServiceClient`, `CartTotalsClient`, `CheckoutCommitClient` — TLC T3.
4. MUST adicionar profile `strangler-wave6`, properties `wave6.*`, três flags strangler + `wave6.totals.http.enabled` — TLC T5.
5. MUST implementar stubs RestTemplate para todos os clients Wave6 — TLC T48–T51.
6. MUST NOT importar `com.salesmanager.core.model` nos contracts.
7. MUST NOT iniciar sem gate Ondas 3–5 passando.
</requirements>

## Subtarefas
- [ ] 1.1 Script de gate + nota de pré-requisito em STATE.md (T1)
- [ ] 1.2 Pacotes DTO em shopizer-api-contracts (T2, T46, T47)
- [ ] 1.3 Interfaces client (T3)
- [ ] 1.4 DTOs saga checkout (T4)
- [ ] 1.5 Wave6ClientConfig + properties (T5)
- [ ] 1.6 Stubs client RestTemplate (T48–T51)

## Detalhes de implementação
Ver TechSpec: **Interfaces principais**, **Configuração**. Reutilizar padrão `WaveNClientConfig`. Estender tipos snapshot da Onda 3 onde já existirem.

### Arquivos relevantes
- `shopizer-api-contracts/` — novos pacotes cart/order/checkout
- `sm-shop/.../strangler/config/Wave6ClientConfig.java` — a criar
- `sm-shop/src/main/resources/application-strangler-wave6.properties` — a criar
- `scripts/wave6-gate.sh` — a criar

### ADRs relacionados
- [ADR-001: Um workflow](adrs/adr-001.md)
- [ADR-008: Feature flags](adrs/adr-008.md)

## Entregáveis
- Script de gate + contracts + config Wave6
- Testes unitários: serialização DTO, Wave6ClientConfig **(REQUIRED)**

## Testes
- Unit: `Wave6ContractsSerializationTest`, `Wave6ClientConfigTest`
- Gate: `./scripts/wave6-gate.sh && ./mvnw test -pl shopizer-api-contracts,sm-shop -Dtest=Wave6*Test`

## Critérios de sucesso
- Script de gate sai com 0
- Contracts compilam isolados
- Todos os testes Wave6 passando
