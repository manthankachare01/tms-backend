package com.tms.restapi.toolsmanagement.issuance.controller;

import com.tms.restapi.toolsmanagement.issuance.dto.ReturnRequestDto;
import com.tms.restapi.toolsmanagement.issuance.model.ReturnRecord;
import com.tms.restapi.toolsmanagement.issuance.model.Issuance;
import com.tms.restapi.toolsmanagement.issuance.service.IssuanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issuance")
public class IssuanceController {

	/*
	 * Issuance API endpoints
	 * ---------------------
	 * POST   /api/issuance/request                -> create a new issuance request
	 * GET    /api/issuance/trainer/{trainerId}    -> get issuance requests for a trainer
	 * GET    /api/issuance/admin?location={loc}   -> get issuance requests filtered by location
	 * GET    /api/issuance/admin/all              -> get issuance requests for all locations (no filter)
	 * GET    /api/issuance/issued-items           -> get currently issued items (status=ISSUED)
	 * PUT    /api/issuance/process-return         -> process a return for an issuance
	 * GET    /api/issuance/returns                -> get return records (optional query: location, trainerId)
	 */

	@Autowired
	private IssuanceService issuanceService;

	@PostMapping("/request")
	// POST /api/issuance/request
	public ResponseEntity<Issuance> createRequest(@RequestBody Issuance issuance) {
		Issuance created = issuanceService.createIssuanceRequest(issuance);
		return ResponseEntity.ok(created);
	}

	@GetMapping("/trainer/{trainerId}")
	// GET /api/issuance/trainer/{trainerId}
	public ResponseEntity<List<Issuance>> getRequestsByTrainer(@PathVariable Long trainerId) {
		return ResponseEntity.ok(issuanceService.getRequestsByTrainer(trainerId));
	}

	@GetMapping("/admin")
	// GET /api/issuance/admin?location={location}
	public ResponseEntity<List<Issuance>> getRequestsByLocation(@RequestParam String location) {
		return ResponseEntity.ok(issuanceService.getRequestsByLocation(location));
	}

	@GetMapping("/admin/all")
	// GET /api/issuance/admin/all
	public ResponseEntity<List<Issuance>> getRequestsForAllLocations() {
		return ResponseEntity.ok(issuanceService.getAllRequests());
	}

	@PutMapping("/process-return")
	public ResponseEntity<Issuance> processReturn(@RequestBody ReturnRequestDto body) {
		Issuance updated = issuanceService.processReturn(body);
		if (updated == null) return ResponseEntity.notFound().build();
		return ResponseEntity.ok(updated);
	}

	@GetMapping("/issued-items")
	// GET /api/issuance/issued-items
	public ResponseEntity<List<Issuance>> getCurrentIssuedItems() {
		return ResponseEntity.ok(issuanceService.getCurrentIssuedItems());
	}

	    // Return records endpoints
	    // GET /api/issuance/returns?location={location}&trainerId={trainerId}
	    // - if both provided -> filtered by both
	    // - if only location -> filtered by location
	    // - if only trainerId -> filtered by trainer
	    // - if none -> all return records
	    @GetMapping("/returns")
	public ResponseEntity<List<ReturnRecord>> getReturnRecords(
			@RequestParam(required = false) String location,
			@RequestParam(required = false) Long trainerId
	) {
		if (location != null && trainerId != null) {
			return ResponseEntity.ok(issuanceService.getReturnRecordsByLocationAndTrainer(location, trainerId));
		}
		if (location != null) {
			return ResponseEntity.ok(issuanceService.getReturnRecordsByLocation(location));
		}
		if (trainerId != null) {
			return ResponseEntity.ok(issuanceService.getReturnRecordsByTrainer(trainerId));
		}
		return ResponseEntity.ok(issuanceService.getAllReturnRecords());
	}
}