package com.salesmanager.core.business.services.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

class ContentServiceThinCoreWiringTest {

	@Test
	void contentServiceImplComesFromSmContentCoreJar() {
		assertThat(ContentServiceImpl.class.getProtectionDomain().getCodeSource().getLocation().toString())
				.contains("sm-content-core");
	}

	@Test
	void contentServiceImplIsComponentScannedFromThinCore() {
		assertTrue(ContentServiceImpl.class.isAnnotationPresent(Service.class));
		assertThat(ContentServiceImpl.class.getPackageName())
				.isEqualTo("com.salesmanager.core.business.services.content");
	}
}
