package com.salesmanager.contracts.merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.contracts.content.ReadableImage;
import com.salesmanager.contracts.reference.ReadableAddress;
import com.salesmanager.contracts.reference.ReadableLanguage;

class MerchantDtoJacksonTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@ParameterizedTest
	@MethodSource("merchantDtoTypes")
	void merchantDtosSerializeAndDeserialize(Class<?> type) throws Exception {
		Object instance = type.getDeclaredConstructor().newInstance();
		String json = mapper.writeValueAsString(instance);
		assertNotNull(mapper.readValue(json, type));
	}

	static Stream<Class<?>> merchantDtoTypes() throws IOException {
		List<Class<?>> types = new ArrayList<>();
		Path root = Paths.get("src/main/java/com/salesmanager/contracts/merchant");
		try (Stream<Path> paths = Files.walk(root)) {
			paths.filter(p -> p.toString().endsWith(".java")).forEach(p -> {
				String className = "com.salesmanager.contracts.merchant"
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
	void merchantStoreSnapshotRoundTripWithNestedFields() throws Exception {
		MerchantStoreSnapshot snapshot = new MerchantStoreSnapshot();
		snapshot.setId(1);
		snapshot.setCode("default");
		snapshot.setName("Default Store");
		snapshot.setDefaultLanguage("en");
		snapshot.setCurrency("USD");
		snapshot.setEmail("store@example.com");
		snapshot.setParentCode("parent");

		ReadableAddress address = new ReadableAddress();
		address.setCity("Montreal");
		address.setCountry("CA");
		snapshot.setAddress(address);

		ReadableImage logo = new ReadableImage();
		logo.setName("logo.png");
		snapshot.setLogo(logo);

		ReadableLanguage language = new ReadableLanguage();
		language.setCode("en");
		snapshot.getSupportedLanguages().add(language);

		String json = mapper.writeValueAsString(snapshot);
		JsonNode tree = mapper.readTree(json);

		assertEquals("default", tree.get("code").asText());
		assertEquals("Montreal", tree.get("address").get("city").asText());
		assertEquals("logo.png", tree.get("logo").get("name").asText());
		assertEquals(1, tree.get("supportedLanguages").size());

		MerchantStoreSnapshot roundTrip = mapper.readValue(json, MerchantStoreSnapshot.class);
		assertEquals("default", roundTrip.getCode());
		assertEquals("Montreal", roundTrip.getAddress().getCity());
	}

	@Test
	void configsRoundTripPublicFlags() throws Exception {
		Configs configs = new Configs();
		configs.setFacebook("fb");
		configs.setDisplaySearchBox(true);
		configs.setAllowOnlinePurchase(true);

		String json = mapper.writeValueAsString(configs);
		Configs roundTrip = mapper.readValue(json, Configs.class);
		assertEquals("fb", roundTrip.getFacebook());
		assertEquals(true, roundTrip.isDisplaySearchBox());
	}

}
