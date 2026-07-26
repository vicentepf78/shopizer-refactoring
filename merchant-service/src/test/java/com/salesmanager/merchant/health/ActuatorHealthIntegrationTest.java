package com.salesmanager.merchant.health;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import com.salesmanager.contracts.client.ContentServiceClient;
import com.salesmanager.contracts.client.ReferenceServiceClient;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ActuatorHealthIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ReferenceServiceClient referenceServiceClient;

	@MockBean
	private ContentServiceClient contentServiceClient;

	@MockBean(name = "referenceRestTemplate")
	private RestTemplate referenceRestTemplate;

	@MockBean(name = "contentRestTemplate")
	private RestTemplate contentRestTemplate;

	@BeforeEach
	void stubRemoteHealth() {
		when(referenceRestTemplate.getForEntity(anyString(), eq(String.class)))
				.thenReturn(new ResponseEntity<>("{\"status\":\"UP\"}", HttpStatus.OK));
		when(contentRestTemplate.getForEntity(anyString(), eq(String.class)))
				.thenReturn(new ResponseEntity<>("{\"status\":\"UP\"}", HttpStatus.OK));
	}

	@Test
	void healthListsMerchantHealthComponents() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.components.merchant.status").value("UP"))
				.andExpect(jsonPath("$.components.merchant.details.referenceService").exists())
				.andExpect(jsonPath("$.components.merchant.details.contentService").exists());
	}
}
