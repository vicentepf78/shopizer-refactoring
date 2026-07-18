package com.salesmanager.shop.strangler.tax;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.ConnectException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.entity.Entity;
import com.salesmanager.shop.model.entity.ReadableEntityList;
import com.salesmanager.shop.model.tax.PersistableTaxClass;
import com.salesmanager.shop.model.tax.PersistableTaxRate;
import com.salesmanager.shop.model.tax.ReadableTaxClass;
import com.salesmanager.shop.model.tax.ReadableTaxRate;
import com.salesmanager.shop.strangler.support.DownstreamHttpException;
import com.salesmanager.shop.strangler.support.ServiceUnavailableException;
import com.salesmanager.shop.strangler.support.StranglerRestClient;

class TaxFacadeHttpAdapterTest {

	private MockRestServiceServer server;
	private TaxFacadeHttpAdapter adapter;
	private MerchantStore store;
	private Language language;

	@BeforeEach
	void setUp() {
		RestTemplate restTemplate = new RestTemplate();
		server = MockRestServiceServer.createServer(restTemplate);
		adapter = new TaxFacadeHttpAdapter(new StranglerRestClient(restTemplate), "http://tax-test:8082");

		store = new MerchantStore();
		store.setCode("DEFAULT");
		language = new Language("en");

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer jwt-token");
		request.addHeader(StranglerRestClient.CORRELATION_HEADER, "corr-tax-1");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
	}

	@AfterEach
	void tearDown() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	void createTaxClass_forwardsAuthorizationAndCorrelationId() {
		server.expect(requestTo("http://tax-test:8082/api/v1/private/tax/class?store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer jwt-token"))
				.andExpect(header(StranglerRestClient.CORRELATION_HEADER, "corr-tax-1"))
				.andRespond(withSuccess("{\"id\":42}", MediaType.APPLICATION_JSON));

		PersistableTaxClass taxClass = new PersistableTaxClass();
		taxClass.setCode("TAX");
		Entity created = adapter.createTaxClass(taxClass, store, language);

		assertThat(created.getId()).isEqualTo(42L);
		server.verify();
	}

	@Test
	void taxClasses_delegatesListToTaxService() {
		server.expect(requestTo("http://tax-test:8082/api/v1/private/tax/class?store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer jwt-token"))
				.andRespond(withSuccess(
						"{\"items\":[{\"id\":1,\"code\":\"TAX\",\"name\":\"Tax\"}],\"number\":1,\"totalPages\":1,\"recordsTotal\":1}",
						MediaType.APPLICATION_JSON));

		ReadableEntityList<ReadableTaxClass> list = adapter.taxClasses(store, language);

		assertThat(list.getItems()).hasSize(1);
		assertThat(list.getItems().get(0).getCode()).isEqualTo("TAX");
		server.verify();
	}

	@Test
	void createTaxRate_delegatesToTaxService() {
		server.expect(requestTo("http://tax-test:8082/api/v1/private/tax/rate?store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer jwt-token"))
				.andRespond(withSuccess("{\"id\":7}", MediaType.APPLICATION_JSON));

		PersistableTaxRate rate = new PersistableTaxRate();
		rate.setCode("RATE1");
		Entity created = adapter.createTaxRate(rate, store, language);

		assertThat(created.getId()).isEqualTo(7L);
		server.verify();
	}

	@Test
	void taxRate_getById() {
		server.expect(requestTo("http://tax-test:8082/api/v1/private/tax/rate/7?store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(
						"{\"id\":7,\"code\":\"RATE1\",\"name\":\"Rate\"}",
						MediaType.APPLICATION_JSON));

		ReadableTaxRate rate = adapter.taxRate(7L, store, language);

		assertThat(rate.getCode()).isEqualTo("RATE1");
		server.verify();
	}

	@Test
	void existsTaxClass_parsesEntityExists() {
		server.expect(requestTo("http://tax-test:8082/api/v1/private/tax/class/unique?code=TAX&store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"exists\":true}", MediaType.APPLICATION_JSON));

		assertThat(adapter.existsTaxClass("TAX", store, language)).isTrue();
		server.verify();
	}

	@Test
	void existsTaxRate_missingCodeReturnsFalse() {
		server.expect(requestTo("http://tax-test:8082/api/v1/private/tax/rate/unique?code=MISSING&store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"exists\":false}", MediaType.APPLICATION_JSON));

		assertThat(adapter.existsTaxRate("MISSING", store, language)).isFalse();
		server.verify();
	}

	@Test
	void connectFailure_mapsTo503() {
		server.expect(requestTo("http://tax-test:8082/api/v1/private/tax/class?store=DEFAULT&lang=en"))
				.andRespond(withException(new ConnectException("Connection refused")));

		assertThatThrownBy(() -> adapter.taxClasses(store, language))
				.isInstanceOf(ServiceUnavailableException.class);
	}

	@Test
	void notFound_propagatesDownstreamStatus() {
		server.expect(requestTo("http://tax-test:8082/api/v1/private/tax/class/MISSING?store=DEFAULT&lang=en"))
				.andRespond(withStatus(HttpStatus.NOT_FOUND));

		assertThatThrownBy(() -> adapter.taxClass("MISSING", store, language))
				.isInstanceOf(DownstreamHttpException.class)
				.extracting(ex -> ((DownstreamHttpException) ex).getStatus())
				.isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void updateAndDeleteTaxClass_delegate() {
		server.expect(requestTo("http://tax-test:8082/api/v1/private/tax/class/9?store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.PUT))
				.andRespond(withSuccess());
		server.expect(requestTo("http://tax-test:8082/api/v1/private/tax/class/9?store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.DELETE))
				.andRespond(withSuccess());

		PersistableTaxClass taxClass = new PersistableTaxClass();
		taxClass.setCode("TAX");
		adapter.updateTaxClass(9L, taxClass, store, language);
		adapter.deleteTaxClass(9L, store, language);
		server.verify();
	}

	@Test
	void updateDeleteAndListTaxRates_delegate() {
		server.expect(requestTo("http://tax-test:8082/api/v1/private/tax/rate/3?store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.PUT))
				.andRespond(withSuccess());
		server.expect(requestTo("http://tax-test:8082/api/v1/private/tax/rate/3?store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.DELETE))
				.andRespond(withSuccess());
		server.expect(requestTo("http://tax-test:8082/api/v1/private/tax/rates?store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(
						"{\"items\":[{\"id\":3,\"code\":\"RATE1\"}],\"number\":1,\"totalPages\":1,\"recordsTotal\":1}",
						MediaType.APPLICATION_JSON));

		PersistableTaxRate rate = new PersistableTaxRate();
		rate.setCode("RATE1");
		adapter.updateTaxRate(3L, rate, store, language);
		adapter.deleteTaxRate(3L, store, language);
		assertThat(adapter.taxRates(store, language).getItems()).hasSize(1);
		server.verify();
	}
}
