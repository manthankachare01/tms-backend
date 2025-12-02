package com.tms.restapi.toolsmanagement.admin.controller;

import com.tms.restapi.toolsmanagement.admin.model.Admin;
import com.tms.restapi.toolsmanagement.admin.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admins")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // Create admin
    // POST /api/admins/create
    @PostMapping("/create")
    public ResponseEntity<Admin> createAdmin(@RequestBody Admin admin) {
        Admin created = adminService.createAdmin(admin);
        return ResponseEntity.status(201).body(created);
    }

    // Get all admins
    // GET /api/admins/all
    @GetMapping("/all")
    public ResponseEntity<List<Admin>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    // Get single admin by adminId
    // GET /api/admins/{adminId}
    @GetMapping("/{adminId}")
    public ResponseEntity<Admin> getAdminById(@PathVariable String adminId) {
        return adminService.getAdminById(adminId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update admin (name, adminId, role are NOT updatable)
    // PUT /api/admins/update/{adminId}
    @PutMapping("/update/{adminId}")
    public ResponseEntity<Admin> updateAdmin(@PathVariable String adminId, @RequestBody Admin adminDetails) {
        Admin updated = adminService.updateAdmin(adminId, adminDetails);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // Search admins by name or email
    // GET /api/admins/search?keyword=...
    @GetMapping("/search")
    public ResponseEntity<List<Admin>> searchAdmins(@RequestParam String keyword) {
        return ResponseEntity.ok(adminService.searchAdmins(keyword));
    }

    // Delete admin by adminId
    // DELETE /api/admins/delete/{adminId}
    @DeleteMapping("/delete/{adminId}")
    public ResponseEntity<String> deleteAdmin(@PathVariable String adminId) {
        return ResponseEntity.ok(adminService.deleteAdmin(adminId));
    }
}
