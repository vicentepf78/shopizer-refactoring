package com.salesmanager.search.api.v1;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SearchReindexController {

	@PostMapping("/private/system/search/index")
	public ResponseEntity<Void> reindexAll() {
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
	}
}
