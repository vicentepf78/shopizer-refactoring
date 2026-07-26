package com.salesmanager.core.modules.integration.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class IntegrationDtoNoJpaTest {

	private static final Path DTO_ROOT = Paths.get("src/main/java/com/salesmanager/core/modules/integration");

	@Test
	void integrationDtosDoNotImportJpaEntities() throws IOException {
		try (Stream<Path> paths = Files.walk(DTO_ROOT)) {
			paths.filter(path -> path.toString().endsWith(".java"))
					.filter(path -> path.toString().contains("/dto/"))
					.forEach(this::assertNoForbiddenReferences);
		}
	}

	private void assertNoForbiddenReferences(Path source) {
		try {
			String content = Files.readString(source);
			assertFalse(content.contains("com.salesmanager.core.model.order.Order"),
					source + " must not reference Order");
			assertFalse(content.contains("com.salesmanager.core.model.customer.Customer"),
					source + " must not reference Customer");
			assertFalse(content.contains("com.salesmanager.core.model.shoppingcart.ShoppingCartItem"),
					source + " must not reference ShoppingCartItem");
			assertFalse(content.contains("javax.persistence."),
					source + " must not import JPA");
		} catch (IOException e) {
			throw new AssertionError("failed to read " + source, e);
		}
	}

}
