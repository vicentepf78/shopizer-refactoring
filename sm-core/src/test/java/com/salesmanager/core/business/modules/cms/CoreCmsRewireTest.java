package com.salesmanager.core.business.modules.cms;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class CoreCmsRewireTest {

	@Test
	void coreContextImportsContentCmsBeforeProductCms() throws IOException {
		Path contextXml = moduleRoot().resolve("src/main/resources/spring/shopizer-core-context.xml");
		String context = Files.readString(contextXml);
		assertTrue(context.contains("shopizer-content-cms.xml"));
		assertTrue(context.contains("shopizer-core-cms.xml"));
		assertTrue(context.indexOf("shopizer-content-cms") < context.indexOf("shopizer-core-cms"));
	}

	@Test
	void coreCmsXmlKeepsProductBeansOnly() throws IOException {
		Path cmsXml = moduleRoot().resolve("src/main/resources/spring/shopizer-core-cms.xml");
		String cms = Files.readString(cmsXml);
		assertTrue(cms.contains("id=\"productFileManager\""));
		assertTrue(cms.contains("id=\"productDownloadsFileManager\""));
		assertFalse(cms.contains("id=\"contentFileManager\""));
		assertFalse(cms.contains("id=\"defaultContentAssetsManager\""));
		assertFalse(cms.contains("id=\"infinispanStaticAssetsManager\""));
	}

	private static Path moduleRoot() {
		Path root = Paths.get("").toAbsolutePath();
		if (!Files.exists(root.resolve("pom.xml"))) {
			root = root.getParent();
		}
		return root;
	}
}
