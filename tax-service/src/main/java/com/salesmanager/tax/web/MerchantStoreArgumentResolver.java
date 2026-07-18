package com.salesmanager.tax.web;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.tax.security.MerchantStoreRepository;
import com.salesmanager.tax.security.StoreAuthorizationService;
import com.salesmanager.tax.support.ResourceNotFoundException;

@Component
public class MerchantStoreArgumentResolver implements HandlerMethodArgumentResolver {

	public static final String REQUEST_PARAMETER_STORE = "store";
	public static final String DEFAULT_STORE = "DEFAULT";

	private final MerchantStoreRepository merchantStoreRepository;
	private final StoreAuthorizationService storeAuthorizationService;

	public MerchantStoreArgumentResolver(
			MerchantStoreRepository merchantStoreRepository,
			StoreAuthorizationService storeAuthorizationService) {
		this.merchantStoreRepository = merchantStoreRepository;
		this.storeAuthorizationService = storeAuthorizationService;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType().equals(MerchantStore.class);
	}

	@Override
	public Object resolveArgument(
			MethodParameter parameter,
			ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory) {
		String storeValue = Optional.ofNullable(webRequest.getParameter(REQUEST_PARAMETER_STORE))
				.filter(StringUtils::isNotBlank)
				.orElse(DEFAULT_STORE);

		MerchantStore storeModel = merchantStoreRepository.findByCode(storeValue);
		if (storeModel == null) {
			throw new ResourceNotFoundException("MerchantStore [" + storeValue + "] not found");
		}

		HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
		String uri = httpServletRequest != null ? httpServletRequest.getRequestURI() : "";
		storeAuthorizationService.authorize(storeModel, uri);
		return storeModel;
	}
}
