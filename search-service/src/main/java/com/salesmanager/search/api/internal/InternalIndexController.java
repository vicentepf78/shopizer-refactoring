package com.salesmanager.search.api.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.contracts.search.ProductIndexBulkPayload;
import com.salesmanager.contracts.search.ProductIndexPayload;
import com.salesmanager.search.services.SearchIndexService;
import com.salesmanager.search.support.UnsupportedSchemaVersionException;

@RestController
@RequestMapping("/internal/v1")
public class InternalIndexController {

	private static final int SUPPORTED_SCHEMA_VERSION_MIN = 1;
	private static final int SUPPORTED_SCHEMA_VERSION_MAX = 2;

	private final SearchIndexService searchIndexService;

	public InternalIndexController(SearchIndexService searchIndexService) {
		this.searchIndexService = searchIndexService;
	}

	@PostMapping("/index")
	public ResponseEntity<Void> index(@RequestBody ProductIndexPayload payload) {
		validateSchemaVersion(payload);
		searchIndexService.index(payload);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PostMapping("/index/bulk")
	public ResponseEntity<Void> indexBulk(@Valid @RequestBody ProductIndexBulkPayload bulk) {
		for (ProductIndexPayload payload : bulk.getPayloads()) {
			validateSchemaVersion(payload);
		}
		searchIndexService.indexBulk(bulk.getPayloads());
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@DeleteMapping("/index/{productId}")
	public ResponseEntity<Void> delete(
			@PathVariable Long productId,
			@RequestParam String store,
			@RequestParam(required = false) String languages) {
		searchIndexService.delete(productId, store, parseLanguages(languages));
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	static void validateSchemaVersion(ProductIndexPayload payload) {
		if (payload == null) {
			throw new UnsupportedSchemaVersionException(-1);
		}
		int version = payload.getSchemaVersion();
		if (version < SUPPORTED_SCHEMA_VERSION_MIN || version > SUPPORTED_SCHEMA_VERSION_MAX) {
			throw new UnsupportedSchemaVersionException(version);
		}
	}

	static List<String> parseLanguages(String languages) {
		if (StringUtils.isBlank(languages)) {
			return Collections.emptyList();
		}
		return Arrays.stream(languages.split(","))
				.map(String::trim)
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.toList());
	}
}
