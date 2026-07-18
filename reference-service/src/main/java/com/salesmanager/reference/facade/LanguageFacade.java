package com.salesmanager.reference.facade;

import java.util.List;

import com.salesmanager.contracts.reference.ReadableLanguage;

public interface LanguageFacade {

	List<ReadableLanguage> getLanguages();
}
