package com.salesmanager.shop.strangler.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.ConnectException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.salesmanager.core.model.content.FileContentType;
import com.salesmanager.shop.strangler.support.ServiceUnavailableException;

class StaticContentProxyTest {

	private MockRestServiceServer server;
	private StaticContentProxy proxy;

	@BeforeEach
	void setUp() {
		RestTemplate restTemplate = new RestTemplate();
		server = MockRestServiceServer.createServer(restTemplate);
		proxy = new StaticContentProxy(restTemplate, "http://content-test:8083");
	}

	@Test
	void getStaticFile_fetchesFromInternalApi() {
		server.expect(requestTo("http://content-test:8083/internal/v1/static/files/DEFAULT/IMAGE/logo.png"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(new byte[] { 1, 2, 3 }, MediaType.APPLICATION_OCTET_STREAM));

		byte[] bytes = proxy.getStaticFile("DEFAULT", FileContentType.IMAGE, "logo.png");

		assertThat(bytes).containsExactly(1, 2, 3);
		server.verify();
	}

	@Test
	void connectFailure_mapsTo503() {
		server.expect(requestTo("http://content-test:8083/internal/v1/static/files/DEFAULT/STATIC_FILE/app.css"))
				.andRespond(withException(new ConnectException("Connection refused")));

		assertThatThrownBy(() -> proxy.getStaticFile("DEFAULT", FileContentType.STATIC_FILE, "app.css"))
				.isInstanceOf(ServiceUnavailableException.class);
	}
}
