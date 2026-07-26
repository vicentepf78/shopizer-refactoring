package com.salesmanager.shop.strangler.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.salesmanager.core.business.configuration.ApplicationSearchConfiguration;
import com.salesmanager.core.business.services.search.SearchServiceImpl;
import com.salesmanager.shop.store.controller.search.facade.SearchFacadeImpl;

class Wave2OpenSearchDisabledTest {

	@Test
	void wave2ProfilePropertiesExcludeOpenSearchAutoConfiguration() throws IOException {
		Path props = Paths.get("src/main/resources/application-strangler-wave2.properties");
		String content = Files.readString(props, StandardCharsets.UTF_8);
		assertThat(content).contains("spring.autoconfigure.exclude=com.shopizer.search.autoconfigure.SearchAutoConfiguration");
		assertThat(content).contains("search.noindex=true");
		assertThat(content).contains("wave2.strangler.enabled=true");
	}

	@Test
	void legacySearchBeansActiveOnlyWhenWave2StranglerDisabled() {
		assertConditionalOnWave2Disabled(SearchServiceImpl.class);
		assertConditionalOnWave2Disabled(ApplicationSearchConfiguration.class);
		assertConditionalOnWave2Disabled(SearchFacadeImpl.class);
	}

	private static void assertConditionalOnWave2Disabled(Class<?> type) {
		ConditionalOnProperty annotation = type.getAnnotation(ConditionalOnProperty.class);
		assertThat(annotation).isNotNull();
		assertThat(annotation.name()).containsExactly("wave2.strangler.enabled");
		assertThat(annotation.havingValue()).isEqualTo("false");
		assertThat(annotation.matchIfMissing()).isTrue();
	}
}
