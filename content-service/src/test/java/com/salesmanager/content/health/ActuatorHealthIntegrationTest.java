package com.salesmanager.content.health;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.salesmanager.contracts.client.ReferenceServiceClient;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ActuatorHealthIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ReferenceServiceClient referenceServiceClient;

	@MockBean(name = "referenceRestTemplate")
	private RestTemplate referenceRestTemplate;

	@Test
	void healthListsContentHealthComponents() throws Exception {
		org.mockito.Mockito.when(referenceRestTemplate.getForEntity(
				org.mockito.ArgumentMatchers.anyString(),
				org.mockito.ArgumentMatchers.eq(String.class)))
				.thenReturn(new ResponseEntity<>("{\"status\":\"UP\"}", HttpStatus.OK));

		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.components.content.status").value("UP"))
				.andExpect(jsonPath("$.components.content.details.cms").exists())
				.andExpect(jsonPath("$.components.content.details.referenceService").exists());
	}
}
