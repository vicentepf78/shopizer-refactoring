# Pact Wave 2 — execução local

Contratos entre `sm-shop` (consumer `sm-shop-wave2`) e os serviços Wave 2 ficam em `pacts/sm-shop-wave2-*.json`.

## Gerar / atualizar contratos (consumer)

```bash
./mvnw -pl sm-shop -am test -Dtest=Wave2ConsumerPactTest -DfailIfNoTests=false
```

## Verificar providers

```bash
./mvnw test -pl content-service,search-service,merchant-service -Dtest=*ProviderPactTest
```

## Gate completo Wave 2 Pact

```bash
./mvnw -pl sm-shop test -Dtest=Wave2ConsumerPactTest
./mvnw test -pl content-service,search-service,merchant-service -Dtest=*ProviderPactTest
```

Ordem recomendada: consumer primeiro (regenera JSON em `pacts/`), depois providers.

Upload multipart (`POST /api/v1/private/file`) fica fora do Pact — ver `ContentFilesIntegrationTest` no content-service; Pact JVM + MockMvc standalone não reproduz multipart de forma estável.

Breaking change em DTO ou path P1 falha na verificação do provider ou na asserção do consumer.
