package com.salesmanager.test.shop.strangler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.client.RestTemplate;

import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.catalog.SearchProductRequest;
import com.salesmanager.shop.model.entity.ValueList;
import com.salesmanager.shop.store.controller.search.facade.SearchFacade;
import com.salesmanager.shop.strangler.search.SearchBulkIndexOrchestrator;
import com.salesmanager.shop.strangler.search.SearchFacadeHttpAdapter;
import com.salesmanager.shop.tenant.TenantEntityBridge;

class SearchStranglerConditionalBeanTest {

	@Test
	void wave2StranglerOn_searchFacadeIsHttpAdapter() {
		try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
			MockEnvironment env = new MockEnvironment();
			env.setProperty("wave2.strangler.enabled", "true");
			env.setProperty("wave2.search-service.base-url", "http://localhost:8084");
			ctx.setEnvironment(env);
			ctx.register(Wave2OnConfig.class);
			ctx.refresh();

			SearchFacade facade = ctx.getBean(SearchFacade.class);
			assertThat(facade).isInstanceOf(SearchFacadeHttpAdapter.class);
			assertThat(ctx.getBeansOfType(SearchFacade.class)).hasSize(1);
		}
	}

	@Test
	void wave2StranglerOff_searchFacadeIsInProcess() {
		try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
			MockEnvironment env = new MockEnvironment();
			env.setProperty("wave2.strangler.enabled", "false");
			ctx.setEnvironment(env);
			ctx.register(MonolithConfig.class);
			ctx.refresh();

			SearchFacade facade = ctx.getBean(SearchFacade.class);
			assertThat(facade).isInstanceOf(InProcessSearchFacade.class);
			assertThat(ctx.getBeansOfType(SearchFacade.class)).hasSize(1);
		}
	}

	@Test
	void wave2StranglerMissing_defaultsToInProcess() {
		try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
			ctx.setEnvironment(new MockEnvironment());
			ctx.register(MonolithConfig.class);
			ctx.refresh();

			assertThat(ctx.getBean(SearchFacade.class)).isInstanceOf(InProcessSearchFacade.class);
		}
	}

	@Configuration
	static class Wave2OnConfig {
		@Bean
		RestTemplate wave2RestTemplate() {
			return new RestTemplate();
		}

		@Bean
		SearchBulkIndexOrchestrator searchBulkIndexOrchestrator() {
			return org.mockito.Mockito.mock(SearchBulkIndexOrchestrator.class);
		}

		@Bean
		TenantEntityBridge tenantEntityBridge() {
			return org.mockito.Mockito.mock(TenantEntityBridge.class);
		}

		@Bean
		SearchFacadeHttpAdapter searchFacadeHttpAdapter(
				RestTemplate wave2RestTemplate,
				SearchBulkIndexOrchestrator orchestrator,
				TenantEntityBridge tenantEntityBridge) {
			return new SearchFacadeHttpAdapter(wave2RestTemplate, "http://localhost:8084", orchestrator,
					tenantEntityBridge);
		}
	}

	@Configuration
	static class MonolithConfig {
		@Bean
		@ConditionalOnProperty(name = "wave2.strangler.enabled", havingValue = "false", matchIfMissing = true)
		InProcessSearchFacade inProcessSearchFacade() {
			return new InProcessSearchFacade();
		}

		@Bean
		TenantEntityBridge tenantEntityBridge() {
			return org.mockito.Mockito.mock(TenantEntityBridge.class);
		}

		@Bean
		@ConditionalOnProperty(name = "wave2.strangler.enabled", havingValue = "true")
		SearchFacadeHttpAdapter searchFacadeHttpAdapter(TenantEntityBridge tenantEntityBridge) {
			return new SearchFacadeHttpAdapter(
					new RestTemplate(),
					"http://localhost:8084",
					org.mockito.Mockito.mock(SearchBulkIndexOrchestrator.class),
					tenantEntityBridge);
		}
	}

	static class InProcessSearchFacade implements SearchFacade {
		@Override
		public void indexAllData(MerchantStoreId storeId) {
		}

		@Override
		public List<modules.commons.search.request.SearchItem> search(
				MerchantStoreId storeId, LanguageCode language, SearchProductRequest searchRequest) {
			return Collections.emptyList();
		}

		@Override
		public ValueList autocompleteRequest(String query, MerchantStoreId storeId, LanguageCode language) {
			return new ValueList();
		}
	}
}
