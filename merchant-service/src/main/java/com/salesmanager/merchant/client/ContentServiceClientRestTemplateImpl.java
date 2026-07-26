package com.salesmanager.merchant.client;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.salesmanager.contracts.client.ContentServiceClient;
import com.salesmanager.merchant.support.ContentUnavailableException;

@Component
public class ContentServiceClientRestTemplateImpl implements ContentServiceClient {

	private final RestTemplate contentRestTemplate;
	private final String baseUrl;

	public ContentServiceClientRestTemplateImpl(
			@Qualifier("contentRestTemplate") RestTemplate contentRestTemplate,
			@Value("${wave2.content-service.base-url}") String baseUrl) {
		this.contentRestTemplate = contentRestTemplate;
		this.baseUrl = StringUtils.removeEnd(baseUrl, "/");
	}

	@Override
	public byte[] getStaticFile(String storeCode, String imageType, String fileName) {
		String url = baseUrl + "/internal/v1/static/files/"
				+ encodePathSegment(storeCode) + "/"
				+ encodePathSegment(imageType) + "/"
				+ encodePathSegment(fileName);
		try {
			ResponseEntity<byte[]> response = contentRestTemplate.exchange(
					url, HttpMethod.GET, null, byte[].class);
			return response.getBody() != null ? response.getBody() : new byte[0];
		} catch (RestClientException e) {
			throw new ContentUnavailableException("Content service call failed: " + url, e);
		}
	}

	@Override
	public void uploadLogo(String storeCode, String fileName, byte[] content, String contentType) {
		String url = baseUrl + "/internal/v1/content/logo";
		LogoUploadRequest body = new LogoUploadRequest(storeCode, fileName, contentType, content);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		try {
			contentRestTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), Void.class);
		} catch (RestClientException e) {
			throw new ContentUnavailableException("Content service call failed: " + url, e);
		}
	}

	@Override
	public void deleteLogo(String storeCode, String fileName) {
		String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/internal/v1/content/logo")
				.queryParam("storeCode", storeCode)
				.queryParam("fileName", fileName)
				.toUriString();
		try {
			contentRestTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
		} catch (RestClientException e) {
			throw new ContentUnavailableException("Content service call failed: " + url, e);
		}
	}

	private static String encodePathSegment(String value) {
		return UriComponentsBuilder.fromPath("/").pathSegment(value).build().getPath().substring(1);
	}

	static final class LogoUploadRequest {
		private final String storeCode;
		private final String fileName;
		private final String contentType;
		private final byte[] content;

		LogoUploadRequest(String storeCode, String fileName, String contentType, byte[] content) {
			this.storeCode = storeCode;
			this.fileName = fileName;
			this.contentType = contentType;
			this.content = content;
		}

		public String getStoreCode() {
			return storeCode;
		}

		public String getFileName() {
			return fileName;
		}

		public String getContentType() {
			return contentType;
		}

		public byte[] getContent() {
			return content;
		}
	}
}
