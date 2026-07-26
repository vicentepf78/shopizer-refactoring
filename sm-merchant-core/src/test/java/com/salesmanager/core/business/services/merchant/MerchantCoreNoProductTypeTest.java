package com.salesmanager.core.business.services.merchant;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class MerchantCoreNoProductTypeTest {

	@Test
	void moduleSources_excludeProductType() throws IOException {
		Path moduleRoot = Paths.get("src/main/java");
		assertFalse(Files.notExists(moduleRoot), "expected sm-merchant-core main sources");

		try (Stream<Path> paths = Files.walk(moduleRoot)) {
			boolean referencesProductType = paths
					.filter(Files::isRegularFile)
					.filter(path -> path.toString().endsWith(".java"))
					.anyMatch(this::containsProductTypeReference);

			assertFalse(referencesProductType, "sm-merchant-core must not reference ProductType");
		}
	}

	private boolean containsProductTypeReference(Path file) {
		try {
			String source = Files.readString(file);
			return source.contains("ProductType");
		} catch (IOException e) {
			throw new AssertionError("failed reading " + file, e);
		}
	}
}
