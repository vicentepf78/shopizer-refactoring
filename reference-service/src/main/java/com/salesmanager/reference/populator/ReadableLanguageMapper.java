package com.salesmanager.reference.populator;

import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.core.model.reference.language.Language;

public final class ReadableLanguageMapper {

	private ReadableLanguageMapper() {
	}

	public static ReadableLanguage toDto(Language source) {
		ReadableLanguage target = new ReadableLanguage();
		if (source.getId() != null) {
			target.setId(source.getId());
		}
		target.setCode(source.getCode());
		if (source.getSortOrder() != null) {
			target.setSortOrder(source.getSortOrder());
		}
		return target;
	}
}
