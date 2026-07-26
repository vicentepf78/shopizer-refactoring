package com.salesmanager.content.api.v1;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.UUID;

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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.contracts.client.ReferenceServiceClient;
import com.salesmanager.contracts.content.common.ContentDescription;
import com.salesmanager.contracts.content.page.PersistableContentPage;
import com.salesmanager.content.security.JWTTokenUtil;
import com.salesmanager.content.support.TestDataFactory;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDataFactory.class)
class ContentPagesIntegrationTest {

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

	private String bearer;

	@BeforeEach
	void setUp() {
		testDataFactory.ensureDefaultAdmin();
		testDataFactory.ensureLanguage("fr");
		bearer = "Bearer " + jwtTokenUtil.generateToken("admin");
	}

	@Test
	void pageCrud_withTwoLanguages_returnsReadableDtos() throws Exception {
		String code = "pg" + UUID.randomUUID().toString().substring(0, 6);

		ContentDescription en = description("en", "About EN", "about-en");
		ContentDescription fr = description("fr", "About FR", "about-fr");

		PersistableContentPage page = new PersistableContentPage();
		page.setCode(code);
		page.setVisible(true);
		page.setLinkToMenu(true);
		page.setContentType("PAGE");
		page.setDescriptions(Arrays.asList(en, fr));

		mockMvc.perform(post("/api/v1/private/content/page")
						.header("Authorization", bearer)
						.param("store", "DEFAULT")
						.param("lang", "en")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(page)))
				.andExpect(status().isCreated());

		MvcResult read = mockMvc.perform(get("/api/v1/content/pages/" + code)
						.param("store", "DEFAULT")
						.param("lang", "en"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(code))
				.andExpect(jsonPath("$.description.name").value("About EN"))
				.andReturn();

		long id = objectMapper.readTree(read.getResponse().getContentAsString()).get("id").asLong();

		PersistableContentPage update = new PersistableContentPage();
		update.setCode(code);
		update.setVisible(true);
		update.setLinkToMenu(true);
		update.setContentType("PAGE");
		ContentDescription enUpdated = description("en", "About EN updated", "about-en");
		update.setDescriptions(Arrays.asList(enUpdated, fr));

		mockMvc.perform(put("/api/v1/private/content/page/" + id)
						.header("Authorization", bearer)
						.param("store", "DEFAULT")
						.param("lang", "en")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(update)))
				.andExpect(status().isOk());

		mockMvc.perform(delete("/api/v1/private/content/page/" + id)
						.header("Authorization", bearer)
						.param("store", "DEFAULT")
						.param("lang", "en"))
				.andExpect(status().isOk());
	}

	private static ContentDescription description(String lang, String name, String url) {
		ContentDescription d = new ContentDescription();
		d.setLanguage(lang);
		d.setName(name);
		d.setTitle(name);
		d.setFriendlyUrl(url);
		d.setDescription("Body " + name);
		return d;
	}
}
