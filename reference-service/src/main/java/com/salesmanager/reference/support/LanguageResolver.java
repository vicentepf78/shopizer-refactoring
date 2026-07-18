package com.salesmanager.reference.support;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.model.reference.language.Language;

/**
 * Resolves request language from {@code lang}. {@code store} is accepted by controllers for
 * path parity with the monolito; store-default language requires MerchantStore (outside this
 * thin service). Strangler BFF resolves language before calling reference-service.
 */
@Component
public class LanguageResolver {

	private final LanguageService languageService;

	public LanguageResolver(LanguageService languageService) {
		this.languageService = languageService;
	}

	public Language resolve(String lang) {
		if (StringUtils.isNotBlank(lang) && !"_all".equals(lang)) {
			try {
				Language language = languageService.getByCode(lang);
				if (language != null) {
					return language;
				}
			} catch (ServiceException e) {
				throw new ServiceRuntimeException(e);
			}
		}
		return languageService.defaultLanguage();
	}
}
