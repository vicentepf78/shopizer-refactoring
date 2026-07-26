package com.salesmanager.content.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.salesmanager.core.model.content.FileContentType;
import com.salesmanager.core.model.merchant.MerchantStore;

@Component("img")
public class ContentImagePath {

	private static final String FILES_URI = "/files";
	private static final String SLASH = "/";

	@Value("${config.cms.contentUrl:}")
	private String contentUrl;

	public String getContextPath() {
		return StringUtils.defaultString(contentUrl);
	}

	public String buildStaticImageUtils(MerchantStore store, String imageName) {
		return buildStaticImageUtils(store, FileContentType.IMAGE.name(), imageName);
	}

	public String buildStaticImageUtils(MerchantStore store, String type, String imageName) {
		StringBuilder imgName = new StringBuilder()
				.append(getContextPath())
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
