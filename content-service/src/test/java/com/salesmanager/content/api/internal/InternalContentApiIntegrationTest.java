package com.salesmanager.content.api.internal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.contracts.client.ReferenceServiceClient;
import com.salesmanager.content.api.internal.InternalLogoController.LogoUploadRequest;
import com.salesmanager.content.security.JWTTokenUtil;
import com.salesmanager.content.support.TestDataFactory;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDataFactory.class)
class InternalContentApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private JWTTokenUtil jwtTokenUtil;
	@Autowired
	private TestDataFactory testDataFactory;

	@MockBean
	private ReferenceServiceClient referenceServiceClient;

	@BeforeEach
	void setUp() {
		testDataFactory.ensureDefaultAdmin();
		jwtTokenUtil.generateToken("admin");
	}

	@Test
	void staticGet_returnsBytesAndContentType() throws Exception {
		byte[] png = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
		MockMultipartFile file = new MockMultipartFile("file", "static.png", "image/png", png);
		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/private/file")
						.file(file)
						.header("Authorization", "Bearer " + jwtTokenUtil.generateToken("admin"))
						.param("store", "DEFAULT")
						.param("lang", "en"))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/internal/v1/static/files/DEFAULT/IMAGE/static.png"))
				.andExpect(status().isOk())
				.andExpect(header().string("Content-Type", MediaType.IMAGE_PNG_VALUE));
	}

	@Test
	void logoPostAndDelete_work() throws Exception {
		LogoUploadRequest request = new LogoUploadRequest();
		request.setStoreCode("DEFAULT");
		request.setFileName("logo.png");
		request.setContentType("image/png");
		request.setContent(new byte[] { 1, 2, 3, 4 });

		mockMvc.perform(post("/internal/v1/content/logo")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNoContent());

		mockMvc.perform(delete("/internal/v1/content/logo")
						.param("storeCode", "DEFAULT")
						.param("fileName", "logo.png"))
				.andExpect(status().isNoContent());
	}
}
