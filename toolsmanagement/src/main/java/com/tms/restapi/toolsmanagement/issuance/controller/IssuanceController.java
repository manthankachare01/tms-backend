package com.tms.restapi.toolsmanagement.issuance.controller;

import com.tms.restapi.toolsmanagement.issuance.dto.ReturnRequestDto;
import com.tms.restapi.toolsmanagement.issuance.model.Issuance;
import com.tms.restapi.toolsmanagement.issuance.service.IssuanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issuance")
public class IssuanceController {

	@Autowired
	private IssuanceService issuanceService;

	@PostMapping("/request")
	public ResponseEntity<Issuance> createRequest(@RequestBody Issuance issuance) {
		Issuance created = issuanceService.createIssuanceRequest(issuance);
		return ResponseEntity.ok(created);
	}

	@GetMapping("/trainer/{trainerId}")
	public ResponseEntity<List<Issuance>> getRequestsByTrainer(@PathVariable Long trainerId) {
		return ResponseEntity.ok(issuanceService.getRequestsByTrainer(trainerId));
	}

	@GetMapping("/admin")
	public ResponseEntity<List<Issuance>> getRequestsByLocation(@RequestParam String location) {
		return ResponseEntity.ok(issuanceService.getRequestsByLocation(location));
	}

	@PutMapping("/process-return")
	public ResponseEntity<Issuance> processReturn(@RequestBody ReturnRequestDto body) {
		Issuance updated = issuanceService.processReturn(body);
		if (updated == null) return ResponseEntity.notFound().build();
		return ResponseEntity.ok(updated);
	}

	@GetMapping("/issued-items")
	public ResponseEntity<List<Issuance>> getCurrentIssuedItems() {
		return ResponseEntity.ok(issuanceService.getCurrentIssuedItems());
	}
}