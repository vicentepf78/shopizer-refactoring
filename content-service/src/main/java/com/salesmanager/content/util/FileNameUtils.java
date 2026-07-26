package com.salesmanager.content.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class FileNameUtils {

	public boolean validFileName(String fileName) {
		return StringUtils.isNotEmpty(FilenameUtils.getExtension(fileName))
				&& StringUtils.isNotEmpty(FilenameUtils.getBaseName(fileName));
	}
}
