package com.salesmanager.shop.strangler.content;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.salesmanager.core.model.content.FileContentType;
import com.salesmanager.core.model.content.InputContentFile;
import com.salesmanager.shop.strangler.support.StranglerRestClient;

@Component
@ConditionalOnProperty(name = "wave2.strangler.enabled", havingValue = "true")
public class ContentBlobClient {

	private final StranglerRestClient restClient;
	private final String baseUrl;

	public ContentBlobClient(
			RestTemplate wave2RestTemplate,
			@Value("${wave2.content-service.base-url}") String baseUrl) {
		this.restClient = new StranglerRestClient(wave2RestTemplate);
		this.baseUrl = StringUtils.removeEnd(baseUrl, "/");
	}

	public void addOptionImage(String storeCode, InputContentFile cmsContentImage) {
		postBlob("/internal/v1/content/blobs/option-image", storeCode, cmsContentImage, FileContentType.PROPERTY.name());
	}

	public void addContentFile(String storeCode, InputContentFile cmsContentImage) {
		String type = cmsContentImage.getFileContentType() != null
				? cmsContentImage.getFileContentType().name()
				: FileContentType.VARIANT.name();
		postBlob("/internal/v1/content/blobs/content-file", storeCode, cmsContentImage, type);
	}

	public void removeFile(String storeCode, FileContentType fileType, String fileName) {
		removeBlob(storeCode, fileType.name(), fileName);
	}

	private void removeBlob(String storeCode, String fileType, String fileName) {
		String url = baseUrl + "/internal/v1/content/blobs"
				+ "?storeCode=" + encode(storeCode)
				+ "&fileType=" + encode(fileType)
				+ "&fileName=" + encode(fileName);
		restClient.exchangeVoid(url, HttpMethod.DELETE, null, false);
	}

	private void postBlob(String path, String storeCode, InputContentFile file, String fileContentType) {
		CatalogBlobRequest request = new CatalogBlobRequest();
		request.setStoreCode(storeCode);
		request.setFileName(file.getFileName());
		request.setContentType(file.getMimeType());
		request.setPath(file.getPath());
		request.setFileContentType(fileContentType);
		request.setContent(readBytes(file));
		restClient.exchangeVoid(baseUrl + path, HttpMethod.POST, request, false);
	}

	private static byte[] readBytes(InputContentFile file) {
		try (InputStream in = file.getFile()) {
			return in != null ? IOUtils.toByteArray(in) : new byte[0];
		} catch (IOException e) {
			throw new IllegalStateException("Cannot read blob content", e);
		}
	}

	private static String encode(String value) {
		return org.springframework.web.util.UriUtils.encodeQueryParam(value, java.nio.charset.StandardCharsets.UTF_8);
	}

	static final class CatalogBlobRequest {
		private String storeCode;
		private String fileName;
		private String contentType;
		private byte[] content;
		private String path;
		private String fileContentType;

		public String getStoreCode() {
			return storeCode;
		}

		public void setStoreCode(String storeCode) {
			this.storeCode = storeCode;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getContentType() {
			return contentType;
		}

		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

		public byte[] getContent() {
			return content;
		}

		public void setContent(byte[] content) {
			this.content = content;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getFileContentType() {
			return fileContentType;
		}

		public void setFileContentType(String fileContentType) {
			this.fileContentType = fileContentType;
		}
	}
}
