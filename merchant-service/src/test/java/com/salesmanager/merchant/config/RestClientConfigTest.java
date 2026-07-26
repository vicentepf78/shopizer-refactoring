package com.salesmanager.merchant.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.salesmanager.merchant.web.CorrelationIdFilter;

class RestClientConfigTest {

	@AfterEach
	void tearDown() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	void correlationInterceptorCopiesInboundHeader() {
		RestTemplate restTemplate = new RestClientConfig()
				.contentRestTemplate(new RestTemplateBuilder(), 1000);
		MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);

		MockHttpServletRequest servletRequest = new MockHttpServletRequest();
		servletRequest.addHeader(CorrelationIdFilter.HEADER, "corr-merchant-out");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));

		server.expect(requestTo("http://example/health"))
				.andExpect(header(CorrelationIdFilter.HEADER, "corr-merchant-out"))
				.andRespond(withSuccess("ok", MediaType.TEXT_PLAIN));

		String body = restTemplate.getForObject("http://example/health", String.class);

		assertThat(body).isEqualTo("ok");
		server.verify();
	}
}
