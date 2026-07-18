package com.salesmanager.reference.facade;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.reference.populator.ReadableLanguageMapper;
import com.salesmanager.reference.support.ResourceNotFoundException;
import com.salesmanager.reference.support.ServiceRuntimeException;

@Service
public class LanguageFacadeImpl implements LanguageFacade {

	private final LanguageService languageService;

	public LanguageFacadeImpl(LanguageService languageService) {
		this.languageService = languageService;
	}

	@Override
	public List<ReadableLanguage> getLanguages() {
		try {
			List<Language> languages = languageService.getLanguages();
			if (languages == null || languages.isEmpty()) {
				throw new ResourceNotFoundException("No languages found");
			}
			return languages.stream().map(ReadableLanguageMapper::toDto).collect(Collectors.toList());
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e);
		}
	}
}
