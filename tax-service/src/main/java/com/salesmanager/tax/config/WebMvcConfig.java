package com.salesmanager.tax.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.salesmanager.tax.web.LanguageArgumentResolver;
import com.salesmanager.tax.web.MerchantStoreArgumentResolver;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	private final MerchantStoreArgumentResolver merchantStoreArgumentResolver;
	private final LanguageArgumentResolver languageArgumentResolver;

	public WebMvcConfig(
			MerchantStoreArgumentResolver merchantStoreArgumentResolver,
			LanguageArgumentResolver languageArgumentResolver) {
		this.merchantStoreArgumentResolver = merchantStoreArgumentResolver;
		this.languageArgumentResolver = languageArgumentResolver;
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(merchantStoreArgumentResolver);
		resolvers.add(languageArgumentResolver);
	}
}
