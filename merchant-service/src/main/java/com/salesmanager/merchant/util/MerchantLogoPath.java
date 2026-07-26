package com.salesmanager.merchant.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.salesmanager.core.model.content.FileContentType;
import com.salesmanager.core.model.merchant.MerchantStore;

@Component("img")
public class MerchantLogoPath {

	private static final String FILES_URI = "/files";
	private static final String SLASH = "/";

	@Value("${config.cms.contentUrl:}")
	private String contentUrl;

	public String buildStoreLogoFilePath(MerchantStore store) {
		if (store == null || StringUtils.isBlank(store.getStoreLogo())) {
			return null;
		}
		return buildStaticImageUtils(store, FileContentType.LOGO.name(), store.getStoreLogo());
	}

	private String buildStaticImageUtils(MerchantStore store, String type, String imageName) {
		StringBuilder imgName = new StringBuilder()
				.append(StringUtils.defaultString(contentUrl))
				.append(FILES_URI)
				.append(SLASH)
				.append(store.getCode())
				.append(SLASH)
				.append(type)
				.append(SLASH);
		if (StringUtils.isNotBlank(imageName)) {
			imgName.append(imageName);
		}
		return imgName.toString();
	}
}
