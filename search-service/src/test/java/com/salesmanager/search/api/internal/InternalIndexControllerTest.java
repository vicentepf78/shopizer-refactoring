package com.salesmanager.search.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.salesmanager.contracts.search.ProductIndexPayload;
import com.salesmanager.search.support.UnsupportedSchemaVersionException;

class InternalIndexControllerTest {

	@Test
	void validateSchemaVersionRejectsUnsupportedVersions() {
		ProductIndexPayload payload = new ProductIndexPayload();
		payload.setSchemaVersion(2);
		assertThatThrownBy(() -> InternalIndexController.validateSchemaVersion(payload))
				.isInstanceOf(UnsupportedSchemaVersionException.class);
	}

	@Test
	void parseLanguagesSplitsCommaSeparatedValues() {
		assertThat(InternalIndexController.parseLanguages("en, fr"))
				.containsExactly("en", "fr");
		assertThat(InternalIndexController.parseLanguages(null)).isEmpty();
	}
}
