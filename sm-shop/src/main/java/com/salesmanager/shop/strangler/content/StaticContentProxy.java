package com.salesmanager.shop.strangler.content;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.salesmanager.core.model.content.FileContentType;
import com.salesmanager.shop.strangler.support.ServiceUnavailableException;
import com.salesmanager.shop.strangler.support.StranglerRestClient;

@Component
@ConditionalOnProperty(name = "wave2.strangler.enabled", havingValue = "true")
public class StaticContentProxy {

	private final StranglerRestClient restClient;
	private final String baseUrl;

	public StaticContentProxy(
			RestTemplate wave2RestTemplate,
			@Value("${wave2.content-service.base-url}") String baseUrl) {
		this.restClient = new StranglerRestClient(wave2RestTemplate);
		this.baseUrl = StringUtils.removeEnd(baseUrl, "/");
	}

	public byte[] getStaticFile(String storeCode, FileContentType fileType, String fileName) {
		String url = baseUrl + "/internal/v1/static/files/"
				+ encode(storeCode) + "/"
				+ encode(fileType.name()) + "/"
				+ encode(fileName);
		byte[] bytes = restClient.exchange(url, HttpMethod.GET, null, byte[].class, false);
		if (bytes == null || bytes.length == 0) {
			throw new ServiceUnavailableException("Static file not found: " + url, null);
		}
		return bytes;
	}

	private static String encode(String value) {
		return org.springframework.web.util.UriUtils.encodePathSegment(value, java.nio.charset.StandardCharsets.UTF_8);
	}
}
