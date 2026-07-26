package com.salesmanager.contracts.content;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class ContentMerchantDtoClasspathTest {

	@Test
	void contentAndMerchantSourcesCompileWithoutCoreModelImports() throws IOException {
		Path contractsRoot = Paths.get("src/main/java/com/salesmanager/contracts");
		try (Stream<Path> paths = Files.walk(contractsRoot)) {
			paths.filter(p -> p.toString().endsWith(".java"))
					.filter(p -> {
						String s = p.toString();
						return s.contains("/content/") || s.contains("/merchant/") || s.contains("/search/");
					})
					.forEach(p -> {
						try {
							String source = Files.readString(p);
							assertFalse(source.contains("com.salesmanager.core.model"),
									p + " must not import core.model");
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});
		}
	}

	@Test
	void representativeContentAndMerchantTypesAreLoadable() throws Exception {
		assertNotNull(Class.forName("com.salesmanager.contracts.content.page.ReadableContentPage"));
		assertNotNull(Class.forName("com.salesmanager.contracts.merchant.ReadableMerchantStore"));
		assertNotNull(Class.forName("com.salesmanager.contracts.merchant.MerchantStoreSnapshot"));
		assertNotNull(Class.forName("com.salesmanager.contracts.merchant.Configs"));
	}

}
