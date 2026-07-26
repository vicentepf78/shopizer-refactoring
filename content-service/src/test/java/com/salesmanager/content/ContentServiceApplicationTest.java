package com.salesmanager.content;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.salesmanager.contracts.client.ReferenceServiceClient;

@SpringBootTest
@ActiveProfiles("test")
class ContentServiceApplicationTest {

	@MockBean
	private ReferenceServiceClient referenceServiceClient;

	@Test
	void contextLoads() {
	}
}
