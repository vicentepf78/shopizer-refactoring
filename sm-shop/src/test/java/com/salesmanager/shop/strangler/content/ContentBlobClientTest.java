package com.salesmanager.shop.strangler.content;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.ByteArrayInputStream;
import java.net.ConnectException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.salesmanager.core.model.content.FileContentType;
import com.salesmanager.core.model.content.InputContentFile;
import com.salesmanager.shop.strangler.support.ServiceUnavailableException;

class ContentBlobClientTest {

	private MockRestServiceServer server;
	private ContentBlobClient client;

	@BeforeEach
	void setUp() {
		RestTemplate restTemplate = new RestTemplate();
		server = MockRestServiceServer.createServer(restTemplate);
		client = new ContentBlobClient(restTemplate, "http://content-test:8083");
	}

	@Test
	void addOptionImage_postsToInternalCatalogBlobApi() {
		server.expect(requestTo("http://content-test:8083/internal/v1/content/blobs/option-image"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("", MediaType.APPLICATION_JSON));

		InputContentFile file = new InputContentFile();
		file.setFileName("color-red.png");
		file.setMimeType("image/png");
		file.setFile(new ByteArrayInputStream(new byte[] { 9 }));

		client.addOptionImage("DEFAULT", file);
		server.verify();
	}

	@Test
	void removeFile_deletesViaInternalCatalogBlobApi() {
		server.expect(requestTo(
				"http://content-test:8083/internal/v1/content/blobs?storeCode=DEFAULT&fileType=PROPERTY&fileName=red.png"))
				.andExpect(method(HttpMethod.DELETE))
				.andRespond(withSuccess());

		client.removeFile("DEFAULT", FileContentType.PROPERTY, "red.png");
		server.verify();
	}

	@Test
	void connectFailure_mapsTo503() {
		server.expect(requestTo("http://content-test:8083/internal/v1/content/blobs/option-image"))
				.andRespond(withException(new ConnectException("Connection refused")));

		InputContentFile file = new InputContentFile();
		file.setFileName("x.png");
		file.setFile(new ByteArrayInputStream(new byte[0]));

		assertThatThrownBy(() -> client.addOptionImage("DEFAULT", file))
				.isInstanceOf(ServiceUnavailableException.class);
	}
}
