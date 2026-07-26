package com.salesmanager.shop.strangler.content;

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

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.content.page.ReadableContentPage;
import com.salesmanager.shop.strangler.support.ServiceUnavailableException;
import com.salesmanager.shop.strangler.support.StranglerRestClient;
import com.salesmanager.shop.utils.ImageFilePath;

@ExtendWith(MockitoExtension.class)
class ContentFacadeHttpAdapterTest {

	private static final String BASE_URL = "http://content-test:8083";

	private MockRestServiceServer server;
	private ContentFacadeHttpAdapter adapter;

	@Mock
	private ImageFilePath imageUtils;

	@Mock
	private StaticContentProxy staticContentProxy;

	private MerchantStore store;
	private Language language;

	@BeforeEach
	void setUp() {
		RestTemplate restTemplate = new RestTemplate();
		server = MockRestServiceServer.createServer(restTemplate);
		adapter = new ContentFacadeHttpAdapter(restTemplate, BASE_URL, imageUtils, staticContentProxy);

		store = new MerchantStore();
		store.setCode("DEFAULT");
		language = new Language("en");

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(StranglerRestClient.CORRELATION_HEADER, "corr-content-1");
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer test-jwt");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
	}

	@AfterEach
	void tearDown() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	void privateEndpoint_forwardsJwtAndCorrelation() {
		server.expect(requestTo("http://content-test:8083/api/v1/private/content/page?store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header(StranglerRestClient.CORRELATION_HEADER, "corr-content-1"))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-jwt"))
				.andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

		com.salesmanager.shop.model.content.page.PersistableContentPage page =
				new com.salesmanager.shop.model.content.page.PersistableContentPage();
		page.setCode("home");
		Long id = adapter.saveContentPage(page, store, language);

		assertThat(id).isEqualTo(1L);
		server.verify();
	}

	@Test
	void getContentPage_forwardsCorrelationOnPublicPath() {
		server.expect(requestTo("http://content-test:8083/api/v1/content/pages/home?store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.GET))
				.andExpect(header(StranglerRestClient.CORRELATION_HEADER, "corr-content-1"))
				.andRespond(withSuccess("{\"code\":\"home\",\"path\":\"/home\"}", MediaType.APPLICATION_JSON));

		ReadableContentPage page = adapter.getContentPage("home", store, language);

		assertThat(page.getCode()).isEqualTo("home");
		server.verify();
	}

	@Test
	void connectFailure_mapsTo503() {
		server.expect(requestTo("http://content-test:8083/api/v1/content/pages/home?store=DEFAULT&lang=en"))
				.andRespond(withException(new ConnectException("Connection refused")));

		assertThatThrownBy(() -> adapter.getContentPage("home", store, language))
				.isInstanceOf(ServiceUnavailableException.class);
	}

	@Test
	void connectFailureOnPrivateEndpoint_mapsTo503() {
		server.expect(requestTo("http://content-test:8083/api/v1/private/content/page?store=DEFAULT&lang=en"))
				.andRespond(withException(new ConnectException("Connection refused")));

		com.salesmanager.shop.model.content.page.PersistableContentPage page =
				new com.salesmanager.shop.model.content.page.PersistableContentPage();

		assertThatThrownBy(() -> adapter.saveContentPage(page, store, language))
				.isInstanceOf(ServiceUnavailableException.class);
	}

	@Test
	void absolutePath_usesImageUtilsLocally() {
		when(imageUtils.getContextPath()).thenReturn("/shop");
		when(imageUtils.buildStaticImageUtils(store, "logo.png")).thenReturn("/static/files/DEFAULT/IMAGE/logo.png");

		assertThat(adapter.absolutePath(store, "logo.png")).isEqualTo("/shop/static/files/DEFAULT/IMAGE/logo.png");
	}
}
