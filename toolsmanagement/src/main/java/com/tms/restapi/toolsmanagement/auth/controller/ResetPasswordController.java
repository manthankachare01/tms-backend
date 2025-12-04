package com.tms.restapi.toolsmanagement.auth.controller;

import com.tms.restapi.toolsmanagement.trainer.service.TrainerService;
import com.tms.restapi.toolsmanagement.superadmin.service.SuperAdminService;
import com.tms.restapi.toolsmanagement.admin.service.AdminService;
import com.tms.restapi.toolsmanagement.security.service.SecurityService;
import com.tms.restapi.toolsmanagement.auth.dto.ResetPasswordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class ResetPasswordController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private SuperAdminService superAdminService;

    @Autowired
    private SecurityService securityService;

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {

        if (request.getNewPassword() == null || request.getConfirmPassword() == null
                || !request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("New password and confirm password do not match");
        }

        if (request.getRole() == null || request.getEmail() == null) {
            return ResponseEntity.badRequest().body("Role and email are required");
        }

        String role = request.getRole().trim().toUpperCase();

        try {
            switch (role) {
                case "ADMIN":
                    adminService.resetPassword(request.getEmail(), request.getNewPassword());
                    break;

                case "TRAINER":
                    trainerService.resetPassword(request.getEmail(), request.getNewPassword());
                    break;

                case "SECURITY":
                    securityService.resetPassword(request.getEmail(), request.getNewPassword());
                    break;

                case "SUPERADMIN":
                    superAdminService.resetPassword(request.getEmail(), request.getNewPassword());
                    break;

                default:
                    return ResponseEntity.badRequest().body("Invalid role. Use ADMIN, TRAINER, SECURITY or SUPERADMIN");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.ok("Password reset successful");
    }
}
