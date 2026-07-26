package com.salesmanager.shop.strangler.merchant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.ConnectException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.salesmanager.contracts.client.MerchantServiceClient;
import com.salesmanager.contracts.merchant.MerchantStoreSnapshot;
import com.salesmanager.core.model.content.InputContentFile;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.merchant.MerchantStoreCriteria;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.store.ReadableMerchantStore;
import com.salesmanager.shop.model.system.Configs;
import com.salesmanager.shop.store.controller.system.MerchantConfigurationFacade;
import com.salesmanager.shop.strangler.support.ServiceUnavailableException;
import com.salesmanager.shop.strangler.support.StranglerRestClient;

@ExtendWith(MockitoExtension.class)
class MerchantFacadeHttpAdapterTest {

	private static final String BASE_URL = "http://merchant-test:8085";

	private MockRestServiceServer server;
	private StoreFacadeHttpAdapter storeAdapter;
	private MerchantConfigurationFacadeHttpAdapter configAdapter;

	@Mock
	private MerchantServiceClient merchantServiceClient;

	@Mock
	private MerchantStoreEntityHydrator hydrator;

	private MerchantStore store;
	private Language language;

	@BeforeEach
	void setUp() {
		RestTemplate restTemplate = new RestTemplate();
		server = MockRestServiceServer.createServer(restTemplate);
		storeAdapter = new StoreFacadeHttpAdapter(restTemplate, BASE_URL, merchantServiceClient, hydrator);
		configAdapter = new MerchantConfigurationFacadeHttpAdapter(restTemplate, BASE_URL);

		store = new MerchantStore();
		store.setCode("DEFAULT");
		language = new Language("en");

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(StranglerRestClient.CORRELATION_HEADER, "corr-merchant-1");
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer test-jwt");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
	}

	@AfterEach
	void tearDown() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	void get_usesSnapshotHydrator() {
		MerchantStoreSnapshot snapshot = new MerchantStoreSnapshot();
		snapshot.setCode("DEFAULT");
		snapshot.setId(1);
		MerchantStore hydrated = new MerchantStore();
		hydrated.setCode("DEFAULT");
		when(merchantServiceClient.getStoreSnapshot("DEFAULT")).thenReturn(snapshot);
		when(hydrator.hydrate(snapshot)).thenReturn(hydrated);

		assertThat(storeAdapter.get("DEFAULT").getCode()).isEqualTo("DEFAULT");
	}

	@Test
	void getByCode_forwardsJwtOnPrivateStorePath() {
		server.expect(requestTo("http://merchant-test:8085/api/v1/private/store/DEFAULT?lang=en"))
				.andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-jwt"))
				.andRespond(withSuccess("{\"code\":\"DEFAULT\",\"name\":\"Default\"}", MediaType.APPLICATION_JSON));

		ReadableMerchantStore readable = storeAdapter.getFullByCode("DEFAULT", language);

		assertThat(readable.getCode()).isEqualTo("DEFAULT");
		server.verify();
	}

	@Test
	void getMerchantConfig_fetchesPublicConfig() {
		server.expect(requestTo("http://merchant-test:8085/api/v1/config?store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"displaySearchBox\":true}", MediaType.APPLICATION_JSON));

		Configs configs = configAdapter.getMerchantConfig(store, language);

		assertThat(configs.isDisplaySearchBox()).isTrue();
		server.verify();
	}

	@Test
	void connectFailure_mapsTo503() {
		server.expect(requestTo("http://merchant-test:8085/api/v1/config?store=DEFAULT&lang=en"))
				.andRespond(withException(new ConnectException("Connection refused")));

		assertThatThrownBy(() -> configAdapter.getMerchantConfig(store, language))
				.isInstanceOf(ServiceUnavailableException.class);
	}

	@Test
	void getByCode_throwsWhenSnapshotMissing() {
		when(merchantServiceClient.getStoreSnapshot("MISSING")).thenReturn(null);
		when(hydrator.hydrate(null)).thenReturn(null);

		assertThatThrownBy(() -> storeAdapter.getByCode("MISSING"))
				.isInstanceOf(com.salesmanager.shop.store.api.exception.ResourceNotFoundException.class);
	}

	@Test
	void supportedLanguages_fetchesFromMerchantService() {
		server.expect(requestTo("http://merchant-test:8085/api/v1/store/languages?store=DEFAULT"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("[{\"code\":\"en\"}]", MediaType.APPLICATION_JSON));

		assertThat(storeAdapter.supportedLanguages(store)).hasSize(1);
		server.verify();
	}

	@Test
	void getByCodePublic_usesStorefrontPath() {
		server.expect(requestTo("http://merchant-test:8085/api/v1/store/DEFAULT?lang=en"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"code\":\"DEFAULT\"}", MediaType.APPLICATION_JSON));

		assertThat(storeAdapter.getByCode("DEFAULT", "en").getCode()).isEqualTo("DEFAULT");
		server.verify();
	}

	@Test
	void findAll_privateListEndpoint() {
		server.expect(requestTo("http://merchant-test:8085/api/v1/private/stores?page=0&count=5&lang=en"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"data\":[]}", MediaType.APPLICATION_JSON));

		assertThat(storeAdapter.findAll(null, language, 0, 5).getData()).isEmpty();
		server.verify();
	}

	@Test
	void create_update_delete_store() {
		server.expect(requestTo("http://merchant-test:8085/api/v1/private/store"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess());
		server.expect(requestTo("http://merchant-test:8085/api/v1/private/store/NEW"))
				.andExpect(method(HttpMethod.PUT))
				.andRespond(withSuccess());
		server.expect(requestTo("http://merchant-test:8085/api/v1/private/store/NEW"))
				.andExpect(method(HttpMethod.DELETE))
				.andRespond(withSuccess());

		com.salesmanager.shop.model.store.PersistableMerchantStore persistable =
				new com.salesmanager.shop.model.store.PersistableMerchantStore();
		persistable.setCode("NEW");
		storeAdapter.create(persistable);
		storeAdapter.update(persistable);
		storeAdapter.delete("NEW");
		server.verify();
	}

	@Test
	void existByCode_checksUniqueEndpoint() {
		server.expect(requestTo("http://merchant-test:8085/api/v1/store/unique?code=NEW"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"exists\":true}", MediaType.APPLICATION_JSON));

		assertThat(storeAdapter.existByCode("NEW")).isTrue();
		server.verify();
	}

	@Test
	void getChildStores_usesMerchantHierarchyPath() {
		server.expect(requestTo("http://merchant-test:8085/api/v1/private/merchant/PARENT/stores?page=0&count=10&lang=en"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"data\":[]}", MediaType.APPLICATION_JSON));

		assertThat(storeAdapter.getChildStores(language, "PARENT", 0, 10).getData()).isEmpty();
		server.verify();
	}

	@Test
	void deleteLogo_callsMarketingEndpoint() {
		server.expect(requestTo("http://merchant-test:8085/api/v1/private/store/DEFAULT/marketing/logo"))
				.andExpect(method(HttpMethod.DELETE))
				.andRespond(withSuccess());

		storeAdapter.deleteLogo("DEFAULT");
		server.verify();
	}

	@Test
	void getByCode_withLanguageObject() {
		server.expect(requestTo("http://merchant-test:8085/api/v1/store/DEFAULT?lang=en"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"code\":\"DEFAULT\"}", MediaType.APPLICATION_JSON));

		assertThat(storeAdapter.getByCode("DEFAULT", language).getCode()).isEqualTo("DEFAULT");
		server.verify();
	}

	@Test
	void getBrand_readsMarketingEndpoint() {
		server.expect(requestTo("http://merchant-test:8085/api/v1/private/store/DEFAULT/marketing"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"code\":\"DEFAULT\"}", MediaType.APPLICATION_JSON));

		assertThat(storeAdapter.getBrand("DEFAULT")).isNotNull();
		server.verify();
	}

	@Test
	void getMerchantStoreNames_withCriteria() {
		server.expect(requestTo("http://merchant-test:8085/api/v1/private/stores/names?store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("[{\"code\":\"DEFAULT\"}]", MediaType.APPLICATION_JSON));

		MerchantStoreCriteria criteria = new MerchantStoreCriteria();
		criteria.setStoreCode("DEFAULT");
		criteria.setLanguage("en");
		assertThat(storeAdapter.getMerchantStoreNames(criteria)).hasSize(1);
		server.verify();
	}

	@Test
	void addStoreLogo_multipartUpload() throws Exception {
		server.expect(requestTo("http://merchant-test:8085/api/v1/private/store/DEFAULT/marketing/logo"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess());

		InputContentFile logo = new InputContentFile();
		logo.setFileName("logo.png");
		logo.setFile(new java.io.ByteArrayInputStream("png".getBytes()));
		storeAdapter.addStoreLogo("DEFAULT", logo);
		server.verify();
	}
}
