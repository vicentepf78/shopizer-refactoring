package com.salesmanager.content.api.internal;

import java.io.ByteArrayInputStream;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.core.business.services.content.ContentService;
import com.salesmanager.core.model.content.FileContentType;
import com.salesmanager.core.model.content.InputContentFile;

@RestController
@RequestMapping("/internal/v1/content/blobs")
public class InternalCatalogBlobController {

	private final ContentService contentService;

	public InternalCatalogBlobController(ContentService contentService) {
		this.contentService = contentService;
	}

	@PostMapping("/option-image")
	public ResponseEntity<Void> addOptionImage(@RequestBody CatalogBlobRequest request) throws Exception {
		InputContentFile file = toInputFile(request, FileContentType.PROPERTY);
		contentService.addOptionImage(request.getStoreCode(), file);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PostMapping("/content-file")
	public ResponseEntity<Void> addContentFile(@RequestBody CatalogBlobRequest request) throws Exception {
		InputContentFile file = toInputFile(request, FileContentType.valueOf(request.getFileContentType()));
		contentService.addContentFile(request.getStoreCode(), file);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@DeleteMapping
	public ResponseEntity<Void> removeFile(
			@RequestParam String storeCode,
			@RequestParam String fileType,
			@RequestParam String fileName) throws Exception {
		contentService.removeFile(storeCode, FileContentType.valueOf(fileType), fileName);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	private InputContentFile toInputFile(CatalogBlobRequest request, FileContentType type) {
		InputContentFile file = new InputContentFile();
		file.setFileName(request.getFileName());
		file.setMimeType(request.getContentType());
		file.setFileContentType(type);
		if (request.getPath() != null) {
			file.setPath(request.getPath());
		}
		byte[] content = request.getContent() != null ? request.getContent() : new byte[0];
		file.setFile(new ByteArrayInputStream(content));
		return file;
	}

	public static class CatalogBlobRequest {
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
