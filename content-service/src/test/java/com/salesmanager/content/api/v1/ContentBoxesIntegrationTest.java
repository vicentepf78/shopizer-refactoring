package com.salesmanager.content.api.v1;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.contracts.client.ReferenceServiceClient;
import com.salesmanager.contracts.content.box.PersistableContentBox;
import com.salesmanager.contracts.content.common.ContentDescription;
import com.salesmanager.content.security.JWTTokenUtil;
import com.salesmanager.content.support.TestDataFactory;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDataFactory.class)
class ContentBoxesIntegrationTest {

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
		bearer = "Bearer " + jwtTokenUtil.generateToken("admin");
	}

	@Test
	void boxCrud_localizedByLang() throws Exception {
		String code = "bx" + UUID.randomUUID().toString().substring(0, 6);
		ContentDescription en = new ContentDescription();
		en.setLanguage("en");
		en.setName("Footer EN");
		en.setDescription("Footer body EN");

		PersistableContentBox box = new PersistableContentBox();
		box.setCode(code);
		box.setVisible(true);
		box.setContentType("BOX");
		box.setDescriptions(Collections.singletonList(en));

		mockMvc.perform(post("/api/v1/private/content/box")
						.header("Authorization", bearer)
						.param("store", "DEFAULT")
						.param("lang", "en")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(box)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/v1/content/boxes/" + code)
						.param("store", "DEFAULT")
						.param("lang", "en"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.description.name").value("Footer EN"));
	}
}
