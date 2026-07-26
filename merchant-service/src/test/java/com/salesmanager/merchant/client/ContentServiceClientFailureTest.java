package com.salesmanager.merchant.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.salesmanager.merchant.support.ContentUnavailableException;

class ContentServiceClientFailureTest {

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
	void uploadLogo_whenContentDown_throwsContentUnavailable() {
		server.expect(org.springframework.test.web.client.match.MockRestRequestMatchers
						.requestTo("http://content-test:8083/internal/v1/content/logo"))
				.andRespond(org.springframework.test.web.client.response.MockRestResponseCreators
						.withServerError());

		assertThatThrownBy(() -> client.uploadLogo("DEFAULT", "logo.png", new byte[0], "image/png"))
				.isInstanceOf(ContentUnavailableException.class);
	}

	@Test
	void deleteLogo_whenContentDown_throwsContentUnavailable() {
		server.expect(org.springframework.test.web.client.match.MockRestRequestMatchers
						.requestTo(
								"http://content-test:8083/internal/v1/content/logo?storeCode=DEFAULT&fileName=logo.png"))
				.andRespond(org.springframework.test.web.client.response.MockRestResponseCreators
						.withServerError());

		assertThatThrownBy(() -> client.deleteLogo("DEFAULT", "logo.png"))
				.isInstanceOf(ContentUnavailableException.class);
	}
}
