# Wave 2 — suite de integração Strangler

Testes de integração Wave 2 distribuídos por módulo. OpenSearch real **não** é exigido nos testes Maven — search-service usa `@MockBean SearchModule`; o monólito com `strangler-wave2` desliga auto-config OpenSearch (`search.noindex=true`).

## Gate Strangler (sm-shop)

```bash
./mvnw -pl sm-shop -am test \
  -Dtest=Wave2*Test,ContentFacadeHttpAdapterTest,StaticContentProxyTest,SearchFacadeHttpAdapterTest,MerchantFacadeHttpAdapterTest,SearchIndexProducerHttpTest,SearchBulkIndexOrchestratorTest,MerchantStoreResolverIntegrationTest \
  -DfailIfNoTests=false
```

Classes `Wave2*Test` em sm-shop:

| Classe | Escopo |
|--------|--------|
| `Wave2ConsumerPactTest` | Contratos consumer → content/search/merchant |
| `Wave2ClientConfigTest` | Properties + beans RestTemplate Wave 2 |
| `Wave2StranglerConditionalBeanTest` | Facades HTTP quando `wave2.strangler.enabled=true` |
| `Wave2OpenSearchDisabledTest` | Monólito sem OpenSearch quando strangler Wave 2 ativo |

Adapters HTTP (MockRestServiceServer): `ContentFacadeHttpAdapterTest`, `StaticContentProxyTest`, `SearchFacadeHttpAdapterTest`, `MerchantFacadeHttpAdapterTest`, `SearchIndexProducerHttpTest`, `SearchBulkIndexOrchestratorTest`, `MerchantStoreResolverIntegrationTest`.

## Gate serviços Wave 2

```bash
./mvnw -pl content-service,search-service,merchant-service -am test -DfailIfNoTests=false
```

Integração por serviço: `*IntegrationTest` em content/merchant; `SearchServiceIntegrationTest` em search (OpenSearch mockado).

## Gate Pact Wave 2

Ver [PACT-WAVE2.md](./PACT-WAVE2.md).

## Gate reactor completo

```bash
./mvnw clean install
```

Inclui Pact consumer/provider e JaCoCo nos módulos com gate de cobertura.

## `@Ignore` — Elasticsearch legado (opcional)

`sm-shop/.../integration/search/SearchApiIntegrationTest` permanece `@Ignore` — exige cluster ES/OpenSearch externo e produto indexado end-to-end. Não faz parte da suite Wave 2 Strangler; cobertura de search delegada está em `SearchFacadeHttpAdapterTest`, `SearchServiceIntegrationTest` e Pact.

Para smoke manual com compose: subir `docker-compose-wave2.yml`, popular índice via `POST /internal/v1/index` ou reindex BFF, então chamar `POST /api/v1/search` no BFF.

## Docker Compose

```bash
docker compose -f docker-compose-wave2.yml config
./mvnw -pl reference-service,content-service,search-service,merchant-service,sm-shop -am package -DskipTests
docker compose -f docker-compose-wave2.yml up --build
```

`tax-service` não entra na topologia Wave 2 (escopo task T51); URLs Wave 1 tax permanecem configuradas no BFF para coexistência com Wave 1 quando tax-service estiver disponível separadamente.
