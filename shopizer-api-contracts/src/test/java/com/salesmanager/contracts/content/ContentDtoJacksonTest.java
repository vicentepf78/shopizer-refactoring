package com.salesmanager.contracts.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.contracts.content.box.ReadableContentBox;
import com.salesmanager.contracts.content.common.ContentDescription;
import com.salesmanager.contracts.content.page.ReadableContentPage;
import com.salesmanager.contracts.content.page.ReadableContentPageFull;

class ContentDtoJacksonTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@ParameterizedTest
	@MethodSource("contentDtoTypes")
	void contentDtosSerializeAndDeserialize(Class<?> type) throws Exception {
		Object instance = type.getDeclaredConstructor().newInstance();
		String json = mapper.writeValueAsString(instance);
		assertNotNull(mapper.readValue(json, type));
	}

	static Stream<Class<?>> contentDtoTypes() throws IOException {
		List<Class<?>> types = new ArrayList<>();
		Path root = Paths.get("src/main/java/com/salesmanager/contracts/content");
		try (Stream<Path> paths = Files.walk(root)) {
			paths.filter(p -> p.toString().endsWith(".java")).forEach(p -> {
				String className = "com.salesmanager.contracts.content"
						+ p.toString().substring(root.toString().length()).replace('/', '.').replace(".java", "");
				try {
					Class<?> type = Class.forName(className);
					if (!type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
						types.add(type);
					}
				} catch (ClassNotFoundException ignored) {
					// skip
				}
			});
		}
		return types.stream();
	}

	@Test
	void readableContentPageRoundTripWithDescription() throws Exception {
		ContentDescription description = new ContentDescription();
		description.setName("About");
		description.setDescription("About us");
		description.setLanguage("en");

		ReadableContentPage page = new ReadableContentPage();
		page.setCode("about");
		page.setDescription(description);

		String json = mapper.writeValueAsString(page);
		JsonNode tree = mapper.readTree(json);

		assertEquals("about", tree.get("code").asText());
		assertEquals("About", tree.get("description").get("name").asText());
		assertFalse(tree.has("auditSection"));

		ReadableContentPage roundTrip = mapper.readValue(json, ReadableContentPage.class);
		assertEquals("about", roundTrip.getCode());
		assertEquals("About", roundTrip.getDescription().getName());
	}

	@Test
	void readableContentBoxAndPageFullRoundTrip() throws Exception {
		ReadableContentBox box = new ReadableContentBox();
		box.setCode("footer");
		box.setDescription(new ContentDescription());

		ReadableContentPageFull pageFull = new ReadableContentPageFull();
		pageFull.setCode("home");
		pageFull.setDescriptions(Collections.singletonList(new ContentDescription()));

		mapper.readValue(mapper.writeValueAsString(box), ReadableContentBox.class);
		mapper.readValue(mapper.writeValueAsString(pageFull), ReadableContentPageFull.class);
	}

}
