package com.salesmanager.shop.strangler.content;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.salesmanager.core.model.content.FileContentType;
import com.salesmanager.core.model.content.InputContentFile;

class ContentBlobClientTest {

	private static final String BASE_URL = "http://content-test:8083";

	private MockRestServiceServer server;
	private ContentBlobClient client;

	@BeforeEach
	void setUp() {
		RestTemplate restTemplate = new RestTemplate();
		server = MockRestServiceServer.createServer(restTemplate);
		client = new ContentBlobClient(restTemplate, BASE_URL);
	}

	@Test
	void addOptionImage_postsToInternalBlobEndpoint() {
		server.expect(requestTo(BASE_URL + "/internal/v1/content/blobs/option-image"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess());

		client.addOptionImage("DEFAULT", inputFile("opt.png", "image/png", "abc".getBytes()));

		server.verify();
	}

	@Test
	void addContentFile_usesVariantWhenTypeMissing() {
		server.expect(requestTo(BASE_URL + "/internal/v1/content/blobs/content-file"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess());

		InputContentFile file = inputFile("file.txt", "text/plain", "data".getBytes());
		file.setFileContentType(null);
		client.addContentFile("DEFAULT", file);

		server.verify();
	}

	@Test
	void removeFile_deletesViaQueryParams() {
		server.expect(requestTo(
				BASE_URL + "/internal/v1/content/blobs?storeCode=DEFAULT&fileType=IMAGE&fileName=logo.png"))
				.andExpect(method(HttpMethod.DELETE))
				.andRespond(withSuccess());

		client.removeFile("DEFAULT", FileContentType.IMAGE, "logo.png");

		server.verify();
	}

	private static InputContentFile inputFile(String name, String mime, byte[] bytes) {
		InputContentFile file = new InputContentFile();
		file.setFileName(name);
		file.setMimeType(mime);
		file.setPath("/files");
		file.setFile(new ByteArrayInputStream(bytes));
		return file;
	}
}
