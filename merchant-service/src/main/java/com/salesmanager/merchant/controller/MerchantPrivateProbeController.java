package com.salesmanager.merchant.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/private/merchant")
public class MerchantPrivateProbeController {

	@GetMapping("/probe")
	public ResponseEntity<String> probe() {
		return ResponseEntity.ok("ok");
	}
}
