package com.salesmanager.shop.strangler.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;

class StranglerRestClientTest {

	@AfterEach
	void tearDown() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	void generatesCorrelationIdWhenAbsent() {
		RestTemplate restTemplate = new RestTemplate();
		MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
		StranglerRestClient client = new StranglerRestClient(restTemplate);

		server.expect(requestTo("http://example/api"))
				.andExpect(request -> assertThat(request.getHeaders().getFirst(StranglerRestClient.CORRELATION_HEADER))
						.isNotBlank())
				.andRespond(withSuccess("ok", MediaType.TEXT_PLAIN));

		String body = client.exchange("http://example/api", HttpMethod.GET, null, String.class, false);

		assertThat(body).isEqualTo("ok");
		server.verify();
	}
}
