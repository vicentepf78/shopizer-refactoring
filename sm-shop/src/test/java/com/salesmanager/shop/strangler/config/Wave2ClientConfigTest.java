package com.salesmanager.shop.strangler.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import com.salesmanager.contracts.client.SearchIndexClient;

@SpringBootTest(classes = Wave2ClientConfigTest.TestConfig.class)
@TestPropertySource(properties = {
		"wave2.strangler.enabled=true",
		"wave2.content-service.base-url=http://content-test:8083",
		"wave2.search-service.base-url=http://search-test:8084",
		"wave2.search-service.internal-token=test-token",
		"wave2.merchant-service.base-url=http://merchant-test:8085",
		"wave2.http.client.timeout-ms=3500",
		"wave2.search.index.reindex-delay-ms=100",
		"wave2.merchant-service.cache.ttl-seconds=120",
		"wave1.strangler.enabled=true",
		"wave1.reference-service.base-url=http://reference-test:8081",
		"wave1.tax-service.base-url=http://tax-test:8082"
})
class Wave2ClientConfigTest {

	@Configuration
	@EnableConfigurationProperties({ Wave2Properties.class, Wave1Properties.class })
	static class TestConfig extends Wave2ClientConfig {

		@Bean
		RestTemplateBuilder restTemplateBuilder() {
			return new RestTemplateBuilder();
		}
	}

	@Autowired
	private Wave2Properties wave2Properties;

	@Autowired
	private Wave1Properties wave1Properties;

	@Autowired
	private RestTemplate wave2RestTemplate;

	@Autowired
	private SearchIndexClient searchIndexClient;

	@Test
	void bindsWave2Properties() {
		assertThat(wave2Properties.getStrangler().isEnabled()).isTrue();
		assertThat(wave2Properties.getContentService().getBaseUrl()).isEqualTo("http://content-test:8083");
		assertThat(wave2Properties.getSearchService().getBaseUrl()).isEqualTo("http://search-test:8084");
		assertThat(wave2Properties.getSearchService().getInternalToken()).isEqualTo("test-token");
		assertThat(wave2Properties.getMerchantService().getBaseUrl()).isEqualTo("http://merchant-test:8085");
		assertThat(wave2Properties.getHttp().getClient().getTimeoutMs()).isEqualTo(3500L);
		assertThat(wave2Properties.getSearch().getIndex().getReindexDelayMs()).isEqualTo(100L);
		assertThat(wave2Properties.getMerchantService().getCache().getTtlSeconds()).isEqualTo(120L);
	}

	@Test
	void wave2CoexistsWithWave1Properties() {
		assertThat(wave1Properties.getStrangler().isEnabled()).isTrue();
		assertThat(wave1Properties.getReferenceService().getBaseUrl()).isEqualTo("http://reference-test:8081");
		assertThat(wave1Properties.getTaxService().getBaseUrl()).isEqualTo("http://tax-test:8082");
	}

	@Test
	void registersWave2RestTemplateAndSearchIndexClient() {
		assertThat(wave2RestTemplate).isNotNull();
		assertThat(searchIndexClient).isNotNull();
	}
}
