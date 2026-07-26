package com.salesmanager.merchant.web;

import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.merchant.support.ServiceRuntimeException;

@Component
public class LanguageArgumentResolver implements HandlerMethodArgumentResolver {

	public static final String REQUEST_PARAMETER_LANG = "lang";

	private final LanguageService languageService;

	public LanguageArgumentResolver(LanguageService languageService) {
		this.languageService = languageService;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType().equals(Language.class);
	}

	@Override
	public Object resolveArgument(
			MethodParameter parameter,
			ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory) {
		String lang = Optional.ofNullable(webRequest.getParameter(REQUEST_PARAMETER_LANG))
				.filter(StringUtils::isNotBlank)
				.orElse(null);
		try {
			if (lang != null) {
				Language language = languageService.getByCode(lang);
				if (language != null) {
					return language;
				}
			}
			return languageService.toLanguage(Locale.ENGLISH);
		} catch (ServiceException e) {
			throw new ServiceRuntimeException("Cannot resolve language", e);
		}
	}
}
