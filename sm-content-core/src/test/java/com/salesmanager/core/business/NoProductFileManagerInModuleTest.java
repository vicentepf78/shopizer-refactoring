package com.salesmanager.core.business;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class NoProductFileManagerInModuleTest {

	@Test
	void moduleSourcesAndResourcesExcludeProductFileManager() throws IOException {
		Path moduleRoot = Paths.get("").toAbsolutePath();
		if (!Files.exists(moduleRoot.resolve("pom.xml"))) {
			moduleRoot = moduleRoot.getParent();
		}
		assertFalse(containsProductFileManager(moduleRoot.resolve("src/main/java")));
		assertFalse(containsProductFileManager(moduleRoot.resolve("src/main/resources")));
	}

	private static boolean containsProductFileManager(Path root) throws IOException {
		if (!Files.isDirectory(root)) {
			return false;
		}
		try (Stream<Path> paths = Files.walk(root)) {
			return paths.filter(Files::isRegularFile)
					.filter(p -> {
						String name = p.getFileName().toString();
						return name.endsWith(".java") || name.endsWith(".xml") || name.endsWith(".properties");
					})
					.anyMatch(p -> {
						try {
							String text = Files.readString(p);
							return text.contains("id=\"productFileManager\"")
									|| text.contains("ProductFileManager");
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});
		}
	}
}
