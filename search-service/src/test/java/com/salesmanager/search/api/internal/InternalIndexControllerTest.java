package com.salesmanager.search.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.salesmanager.contracts.search.ProductIndexPayload;
import com.salesmanager.search.support.UnsupportedSchemaVersionException;

class InternalIndexControllerTest {

	@Test
	void validateSchemaVersionAcceptsVersionOneAndTwo() {
		ProductIndexPayload v1 = new ProductIndexPayload();
		v1.setSchemaVersion(1);
		ProductIndexPayload v2 = new ProductIndexPayload();
		v2.setSchemaVersion(2);

		assertThatCode(() -> InternalIndexController.validateSchemaVersion(v1)).doesNotThrowAnyException();
		assertThatCode(() -> InternalIndexController.validateSchemaVersion(v2)).doesNotThrowAnyException();
	}

	@Test
	void validateSchemaVersionRejectsUnsupportedVersions() {
		ProductIndexPayload payload = new ProductIndexPayload();
		payload.setSchemaVersion(3);
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
