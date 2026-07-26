package com.salesmanager.core.business;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class NoContentDuplicateInSmCoreSourceTest {

	private static final String[] FORBIDDEN_MARKERS = {
			"ContentServiceImpl",
			"ContentRepositoryImpl",
			"ContentRepositoryCustom",
			"id=\"contentFileManager\"",
			"StaticContentCacheManagerImpl",
			"id=\"defaultContentAssetsManager\"",
			"id=\"infinispanStaticAssetsManager\""
	};

	@Test
	void smCoreSourcesExcludeDuplicatedContentDomain() throws IOException {
		Path moduleRoot = moduleRoot();
		assertFalse(containsForbiddenMarker(moduleRoot.resolve("src/main/java")));
		assertFalse(containsForbiddenMarker(moduleRoot.resolve("src/main/resources/spring/shopizer-core-cms.xml")));
	}

	private static Path moduleRoot() {
		Path root = Paths.get("").toAbsolutePath();
		if (!Files.exists(root.resolve("pom.xml"))) {
			root = root.getParent();
		}
		return root;
	}

	private static boolean containsForbiddenMarker(Path root) throws IOException {
		if (root.toString().endsWith(".xml")) {
			if (!Files.isRegularFile(root)) {
				return false;
			}
			String text = Files.readString(root);
			for (String marker : FORBIDDEN_MARKERS) {
				if (text.contains(marker)) {
					return true;
				}
			}
			return false;
		}
		if (!Files.isDirectory(root)) {
			return false;
		}
		try (Stream<Path> paths = Files.walk(root)) {
			return paths.filter(Files::isRegularFile)
					.filter(p -> {
						String name = p.getFileName().toString();
						return name.endsWith(".java") || name.endsWith(".xml");
					})
					.anyMatch(p -> {
						try {
							String text = Files.readString(p);
							for (String marker : FORBIDDEN_MARKERS) {
								if (text.contains(marker)) {
									return true;
								}
							}
							return false;
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});
		}
	}
}
