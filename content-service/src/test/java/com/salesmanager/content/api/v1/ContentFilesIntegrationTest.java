package com.salesmanager.content.api.v1;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.salesmanager.contracts.client.ReferenceServiceClient;
import com.salesmanager.content.security.JWTTokenUtil;
import com.salesmanager.content.support.TestDataFactory;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDataFactory.class)
class ContentFilesIntegrationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private JWTTokenUtil jwtTokenUtil;
	@Autowired
	private TestDataFactory testDataFactory;

	@MockBean
	private ReferenceServiceClient referenceServiceClient;

	private String bearer;

	@BeforeEach
	void setUp() {
		testDataFactory.ensureDefaultAdmin();
		bearer = "Bearer " + jwtTokenUtil.generateToken("admin");
	}

	@Test
	void uploadImage_persistsAndListsInFolder() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png",
				new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47 });

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/private/file")
						.file(file)
						.header("Authorization", bearer)
						.param("store", "DEFAULT")
						.param("lang", "en"))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/v1/content/images")
						.param("store", "DEFAULT")
						.param("lang", "en"))
				.andExpect(status().isOk());
	}
}
