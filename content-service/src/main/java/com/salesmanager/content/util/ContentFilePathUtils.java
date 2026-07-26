package com.salesmanager.content.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ContentFilePathUtils {

	private static final String FILES_URI = "/files";
	private static final String SLASH = "/";

	public String buildStaticFilePath(String storeCode, String fileName) {
		String path = FILES_URI + SLASH + storeCode + SLASH;
		if (StringUtils.isNotBlank(fileName)) {
			path = path + fileName;
		}
		return path;
	}
}
