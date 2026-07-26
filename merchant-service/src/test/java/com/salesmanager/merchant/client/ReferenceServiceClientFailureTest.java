package com.salesmanager.merchant.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.salesmanager.merchant.support.ReferenceUnavailableException;

class ReferenceServiceClientFailureTest {

	private RestTemplate restTemplate;
	private MockRestServiceServer server;
	private ReferenceServiceClientRestTemplateImpl client;

	@BeforeEach
	void setUp() {
		restTemplate = new RestTemplate();
		server = MockRestServiceServer.createServer(restTemplate);
		client = new ReferenceServiceClientRestTemplateImpl(restTemplate, "http://reference-test:8081");
	}

	@Test
	void getLanguageByCode_whenReferenceDown_throwsReferenceUnavailable() {
		server.expect(org.springframework.test.web.client.match.MockRestRequestMatchers
						.requestTo("http://reference-test:8081/api/v1/languages"))
				.andRespond(org.springframework.test.web.client.response.MockRestResponseCreators
						.withServerError());

		assertThatThrownBy(() -> client.getLanguageByCode("en"))
				.isInstanceOf(ReferenceUnavailableException.class);
	}
}
