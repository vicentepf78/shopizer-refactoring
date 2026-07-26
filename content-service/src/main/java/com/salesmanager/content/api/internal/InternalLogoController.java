package com.salesmanager.content.api.internal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.content.facade.content.ContentFacade;

@RestController
@RequestMapping("/internal/v1/content")
public class InternalLogoController {

	private final ContentFacade contentFacade;

	public InternalLogoController(ContentFacade contentFacade) {
		this.contentFacade = contentFacade;
	}

	@PostMapping("/logo")
	public ResponseEntity<Void> uploadLogo(@RequestBody LogoUploadRequest request) {
		contentFacade.uploadLogo(request.getStoreCode(), request.getFileName(), request.getContent(),
				request.getContentType());
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@DeleteMapping("/logo")
	public ResponseEntity<Void> deleteLogo(
			@RequestParam String storeCode,
			@RequestParam String fileName) {
		contentFacade.deleteLogo(storeCode, fileName);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	public static class LogoUploadRequest {
		private String storeCode;
		private String fileName;
		private String contentType;
		private byte[] content;

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
	}
}
