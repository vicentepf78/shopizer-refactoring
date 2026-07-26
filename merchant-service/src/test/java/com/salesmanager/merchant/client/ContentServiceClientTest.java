package com.salesmanager.merchant.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class ContentServiceClientTest {

	private RestTemplate restTemplate;
	private MockRestServiceServer server;
	private ContentServiceClientRestTemplateImpl client;

	@BeforeEach
	void setUp() {
		restTemplate = new RestTemplate();
		server = MockRestServiceServer.createServer(restTemplate);
		client = new ContentServiceClientRestTemplateImpl(restTemplate, "http://content-test:8083");
	}

	@Test
	void uploadLogo_postsToInternalLogoPath() {
		server.expect(requestTo("http://content-test:8083/internal/v1/content/logo"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(withNoContent());

		client.uploadLogo("DEFAULT", "logo.png", new byte[] { 1, 2, 3 }, "image/png");

		server.verify();
	}

	@Test
	void deleteLogo_callsInternalLogoDeleteWithQueryParams() {
		server.expect(requestTo(
						"http://content-test:8083/internal/v1/content/logo?storeCode=DEFAULT&fileName=logo.png"))
				.andExpect(method(HttpMethod.DELETE))
				.andRespond(withNoContent());

		client.deleteLogo("DEFAULT", "logo.png");

		server.verify();
	}

	@Test
	void getStaticFile_callsInternalStaticPath() {
		server.expect(requestTo("http://content-test:8083/internal/v1/static/files/DEFAULT/LOGO/logo.png"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(new byte[] { 9, 8, 7 }, MediaType.IMAGE_PNG));

		byte[] bytes = client.getStaticFile("DEFAULT", "LOGO", "logo.png");

		assertThat(bytes).containsExactly(9, 8, 7);
		server.verify();
	}
}
