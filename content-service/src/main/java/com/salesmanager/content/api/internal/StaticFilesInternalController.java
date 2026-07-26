package com.salesmanager.content.api.internal;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.content.facade.content.ContentFacade;
import com.salesmanager.core.model.content.FileContentType;

@RestController
@RequestMapping("/internal/v1/static/files")
public class StaticFilesInternalController {

	private final ContentFacade contentFacade;

	public StaticFilesInternalController(ContentFacade contentFacade) {
		this.contentFacade = contentFacade;
	}

	@GetMapping("/{storeCode}/{imageType}/{fileName:.+}")
	public ResponseEntity<byte[]> getStaticFile(
			@PathVariable String storeCode,
			@PathVariable String imageType,
			@PathVariable String fileName) {
		FileContentType fileType = FileContentType.valueOf(imageType.toUpperCase());
		byte[] bytes = contentFacade.getStaticFile(storeCode, fileType, fileName);
		MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
		if (fileName.toLowerCase().endsWith(".png")) {
			mediaType = MediaType.IMAGE_PNG;
		} else if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
			mediaType = MediaType.IMAGE_JPEG;
		}
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_TYPE, mediaType.toString())
				.body(bytes);
	}
}
