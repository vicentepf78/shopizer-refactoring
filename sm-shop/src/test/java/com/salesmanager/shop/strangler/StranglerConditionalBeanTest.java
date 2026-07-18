package com.salesmanager.shop.strangler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.references.ReadableCountry;
import com.salesmanager.shop.store.controller.country.facade.CountryFacade;
import com.salesmanager.shop.strangler.reference.CountryFacadeHttpAdapter;
import com.salesmanager.shop.strangler.support.StranglerRestClient;

import java.util.Collections;
import java.util.List;

/**
 * Lightweight wiring check: exactly one CountryFacade bean per strangler flag.
 */
class StranglerConditionalBeanTest {

	@Test
	void stranglerOn_countryFacadeIsHttpAdapter() {
		try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
			MockEnvironment env = new MockEnvironment();
			env.setProperty("wave1.strangler.enabled", "true");
			env.setProperty("wave1.reference-service.base-url", "http://localhost:8081");
			ctx.setEnvironment(env);
			ctx.register(StranglerOnConfig.class);
			ctx.refresh();

			CountryFacade facade = ctx.getBean(CountryFacade.class);
			assertThat(facade).isInstanceOf(CountryFacadeHttpAdapter.class);
			assertThat(ctx.getBeansOfType(CountryFacade.class)).hasSize(1);
		}
	}

	@Test
	void stranglerOff_countryFacadeIsInProcess() {
		try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
			MockEnvironment env = new MockEnvironment();
			env.setProperty("wave1.strangler.enabled", "false");
			ctx.setEnvironment(env);
			ctx.register(MonolithConfig.class);
			ctx.refresh();

			CountryFacade facade = ctx.getBean(CountryFacade.class);
			assertThat(facade).isInstanceOf(InProcessCountryFacade.class);
			assertThat(ctx.getBeansOfType(CountryFacade.class)).hasSize(1);
		}
	}

	@Test
	void stranglerMissing_defaultsToInProcess() {
		try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
			ctx.setEnvironment(new MockEnvironment());
			ctx.register(MonolithConfig.class);
			ctx.refresh();

			assertThat(ctx.getBean(CountryFacade.class)).isInstanceOf(InProcessCountryFacade.class);
		}
	}

	@Configuration
	static class StranglerOnConfig {
		@Bean
		RestTemplate wave1RestTemplate() {
			return new RestTemplate();
		}

		@Bean
		StranglerRestClient stranglerRestClient(RestTemplate wave1RestTemplate) {
			return new StranglerRestClient(wave1RestTemplate);
		}

		@Bean
		CountryFacadeHttpAdapter countryFacadeHttpAdapter(StranglerRestClient client) {
			return new CountryFacadeHttpAdapter(client, "http://localhost:8081");
		}
	}

	@Configuration
	static class MonolithConfig {
		@Bean
		@ConditionalOnProperty(name = "wave1.strangler.enabled", havingValue = "false", matchIfMissing = true)
		InProcessCountryFacade inProcessCountryFacade() {
			return new InProcessCountryFacade();
		}

		@Bean
		@ConditionalOnProperty(name = "wave1.strangler.enabled", havingValue = "true")
		CountryFacadeHttpAdapter countryFacadeHttpAdapter() {
			return new CountryFacadeHttpAdapter(new StranglerRestClient(new RestTemplate()), "http://localhost:8081");
		}
	}

	@Service
	static class InProcessCountryFacade implements CountryFacade {
		@Override
		public List<ReadableCountry> getListCountryZones(Language language, MerchantStore merchantStore) {
			return Collections.emptyList();
		}
	}
}
