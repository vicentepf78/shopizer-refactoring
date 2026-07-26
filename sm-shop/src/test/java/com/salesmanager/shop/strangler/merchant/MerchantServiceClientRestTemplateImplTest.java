package com.salesmanager.shop.strangler.merchant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.salesmanager.contracts.merchant.MerchantStoreSnapshot;
import com.salesmanager.shop.strangler.config.Wave2Properties;

class MerchantServiceClientRestTemplateImplTest {

	private MockRestServiceServer server;
	private MerchantServiceClientRestTemplateImpl client;

	@BeforeEach
	void setUp() {
		RestTemplate restTemplate = new RestTemplate();
		server = MockRestServiceServer.createServer(restTemplate);
		Wave2Properties properties = new Wave2Properties();
		properties.getMerchantService().setBaseUrl("http://merchant-test:8085");
		client = new MerchantServiceClientRestTemplateImpl(restTemplate, properties);
	}

	@Test
	void getStoreSnapshot_callsInternalSnapshotEndpoint() {
		server.expect(requestTo("http://merchant-test:8085/internal/v1/store/DEFAULT?lang=en"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"code\":\"DEFAULT\",\"id\":1}", MediaType.APPLICATION_JSON));

		MerchantStoreSnapshot snapshot = client.getStoreSnapshot("DEFAULT");

		assertThat(snapshot.getCode()).isEqualTo("DEFAULT");
		assertThat(snapshot.getId()).isEqualTo(1);
		server.verify();
	}
}
