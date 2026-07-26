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

import com.salesmanager.core.model.content.ContentType;
import com.salesmanager.core.model.content.FileContentType;
import com.salesmanager.core.model.content.OutputContentFile;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.shop.model.content.page.ReadableContentPage;
import com.salesmanager.shop.model.content.box.ReadableContentBox;
import com.salesmanager.shop.model.entity.ReadableEntityList;
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

	@Test
	void getContentBox_forwardsPublicPath() {
		server.expect(requestTo("http://content-test:8083/api/v1/content/boxes/footer?store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"code\":\"footer\"}", MediaType.APPLICATION_JSON));

		assertThat(adapter.getContentBox("footer", store, language).getCode()).isEqualTo("footer");
		server.verify();
	}

	@Test
	void getContentPages_listsPaginated() {
		server.expect(requestTo(
				"http://content-test:8083/api/v1/content/pages?page=0&count=10&store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"items\":[],\"recordsTotal\":0}", MediaType.APPLICATION_JSON));

		assertThat(adapter.getContentPages(store, language, 0, 10).getRecordsTotal()).isZero();
		server.verify();
	}

	@Test
	void codeExist_checksPagePath() {
		server.expect(requestTo("http://content-test:8083/api/v1/private/content/page/home/exists?store=DEFAULT"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"exists\":true}", MediaType.APPLICATION_JSON));

		assertThat(adapter.codeExist("home", "PAGE", store)).isTrue();
		server.verify();
	}

	@Test
	void deleteContentById_callsPrivateEndpoint() {
		server.expect(requestTo("http://content-test:8083/api/v1/private/content/99?store=DEFAULT"))
				.andExpect(method(HttpMethod.DELETE))
				.andRespond(withSuccess());

		adapter.delete(store, 99L);
		server.verify();
	}

	@Test
	void saveContentBox_postsToPrivateEndpoint() {
		server.expect(requestTo("http://content-test:8083/api/v1/private/content/box?store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("{\"id\":7}", MediaType.APPLICATION_JSON));

		com.salesmanager.shop.model.content.box.PersistableContentBox box =
				new com.salesmanager.shop.model.content.box.PersistableContentBox();
		box.setCode("footer");
		assertThat(adapter.saveContentBox(box, store, language)).isEqualTo(7L);
		server.verify();
	}

	@Test
	void getContentPageByName_usesNamePath() {
		server.expect(requestTo("http://content-test:8083/api/v1/content/pages/name/home?store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"code\":\"home\"}", MediaType.APPLICATION_JSON));

		assertThat(adapter.getContentPageByName("home", store, language).getCode()).isEqualTo("home");
		server.verify();
	}

	@Test
	void getContentFolder_listsImages() throws Exception {
		server.expect(requestTo("http://content-test:8083/api/v1/content/images?path=icons&store=DEFAULT"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"path\":\"icons\"}", MediaType.APPLICATION_JSON));

		assertThat(adapter.getContentFolder("icons", store).getPath()).isEqualTo("icons");
		server.verify();
	}

	@Test
	void codeExist_checksBoxPath() {
		server.expect(requestTo("http://content-test:8083/api/v1/private/content/box/footer/exists?store=DEFAULT"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"exists\":false}", MediaType.APPLICATION_JSON));

		assertThat(adapter.codeExist("footer", "BOX", store)).isFalse();
		server.verify();
	}

	@Test
	void updateContentPage_putsToPrivateEndpoint() {
		server.expect(requestTo("http://content-test:8083/api/v1/private/content/page/5?store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.PUT))
				.andRespond(withSuccess());

		com.salesmanager.shop.model.content.page.PersistableContentPage page =
				new com.salesmanager.shop.model.content.page.PersistableContentPage();
		page.setCode("home");
		adapter.updateContentPage(5L, page, store, language);
		server.verify();
	}

	@Test
	void downloadImage_delegatesToStaticProxy() {
		when(staticContentProxy.getStaticFile("DEFAULT", FileContentType.IMAGE, "logo.png"))
				.thenReturn(new byte[] { 9, 8 });

		OutputContentFile file = adapter.download(store, FileContentType.IMAGE, "logo.png");

		assertThat(file.getFileName()).isEqualTo("logo.png");
	}

	@Test
	void renameFile_postsRenameEndpoint() {
		server.expect(requestTo(
				"http://content-test:8083/api/v1/private/content/images/rename?path=/files/DEFAULT/IMAGE/old.png&newName=new.png&store=DEFAULT"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess());

		adapter.renameFile(store, FileContentType.IMAGE, "old.png", "new.png");
		server.verify();
	}

	@Test
	void getContentBoxesWithPrefix_filtersInMemory() {
		server.expect(requestTo("http://content-test:8083/api/v1/content/boxes?page=0&count=20&store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(
						"{\"items\":[{\"code\":\"promo-a\"},{\"code\":\"other\"}],\"totalPages\":1}",
						MediaType.APPLICATION_JSON));

		ReadableEntityList<ReadableContentBox> boxes =
				adapter.getContentBoxes(ContentType.BOX, "promo", store, language, 0, 20);

		assertThat(boxes.getItems()).extracting(ReadableContentBox::getCode).containsExactly("promo-a");
		server.verify();
	}
}
