package com.salesmanager.content.api.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.salesmanager.contracts.client.ReferenceServiceClient;
import com.salesmanager.content.security.JWTTokenUtil;
import com.salesmanager.content.support.TestDataFactory;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDataFactory.class)
class ContentAdminIntegrationTest {

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
	void deprecatedDownloadStub_returnsNullBody() throws Exception {
		MvcResult result = mockMvc.perform(get("/api/v1/content/images/download")
						.param("store", "DEFAULT")
						.param("lang", "en")
						.param("path", "/files/DEFAULT/IMAGE/test.png"))
				.andExpect(status().isOk())
				.andReturn();
		assertThat(result.getResponse().getContentAsString()).isBlank();
	}

	@Test
	void adminRenameAndRemove_roundTrip() throws Exception {
		MockMultipartFile file = new MockMultipartFile("qqfile", "admin.png", "image/png",
				new byte[] { 1, 2, 3 });
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.multipart("/api/v1/private/content/images/add")
						.file(file)
						.param("qqfilename", "admin.png")
						.param("qquuid", "uuid-1")
						.header("Authorization", bearer)
						.param("store", "DEFAULT")
						.param("lang", "en"))
				.andExpect(status().isCreated());

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.post("/api/v1/private/content/images/rename")
						.header("Authorization", bearer)
						.param("store", "DEFAULT")
						.param("lang", "en")
						.param("path", "/files/DEFAULT/IMAGE/admin.png")
						.param("newName", "admin-renamed.png"))
				.andExpect(status().isOk());

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.delete("/api/v1/private/content/images/remove")
						.header("Authorization", bearer)
						.param("store", "DEFAULT")
						.param("lang", "en")
						.param("path", "/files/DEFAULT/IMAGE/admin-renamed.png"))
				.andExpect(status().isOk());
	}

	@Test
	void contentSummaryStub_returnsNullJson() throws Exception {
		MvcResult result = mockMvc.perform(get("/api/v1/content/summary")
						.param("store", "DEFAULT")
						.param("lang", "en"))
				.andExpect(status().isOk())
				.andReturn();
		assertThat(result.getResponse().getContentAsString()).isIn("null", "");
	}
}
