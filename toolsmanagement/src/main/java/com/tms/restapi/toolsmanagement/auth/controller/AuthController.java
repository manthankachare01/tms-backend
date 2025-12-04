package com.tms.restapi.toolsmanagement.auth.controller;

import com.tms.restapi.toolsmanagement.trainer.model.Trainer;
import com.tms.restapi.toolsmanagement.trainer.repository.TrainerRepository;
import com.tms.restapi.toolsmanagement.superadmin.service.SuperAdminService;
import com.tms.restapi.toolsmanagement.admin.model.Admin;
import com.tms.restapi.toolsmanagement.admin.repository.AdminRepository;
import com.tms.restapi.toolsmanagement.superadmin.model.SuperAdmin;
import com.tms.restapi.toolsmanagement.security.model.Security;
import com.tms.restapi.toolsmanagement.security.repository.SecurityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private SecurityRepository securityRepository;

    @Autowired
    private SuperAdminService superAdminService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String role = loginData.get("role");
        String email = loginData.get("email");
        String password = loginData.get("password");

        if (role == null || email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing login fields"));
        }

        Map<String, Object> response = new HashMap<>();

        // Admin login
        if (role.equalsIgnoreCase("admin")) {
            Admin admin = adminRepository.findByEmail(email);

            if (admin == null || !passwordEncoder.matches(password, admin.getPassword())) {
                return ResponseEntity.status(401).body(Map.of("message", "Invalid admin credentials"));
            }

            admin.setPassword(null);
            response.put("message", "Admin login successful");
            response.put("user", admin);
            return ResponseEntity.ok(response);
        }

        // Trainer login
        if (role.equalsIgnoreCase("trainer")) {
            Trainer trainer = trainerRepository.findByEmail(email);

            if (trainer == null || !passwordEncoder.matches(password, trainer.getPassword())) {
                return ResponseEntity.status(401).body(Map.of("message", "Invalid trainer credentials"));
            }

            trainer.setPassword(null);
            response.put("message", "Trainer login successful");
            response.put("user", trainer);
            return ResponseEntity.ok(response);
        }

        // Security login
        if (role.equalsIgnoreCase("security")) {
            Security security = securityRepository.findByEmail(email);

            if (security == null || !passwordEncoder.matches(password, security.getPassword())) {
                return ResponseEntity.status(401).body(Map.of("message", "Invalid security credentials"));
            }

            security.setPassword(null);
            response.put("message", "Security login successful");
            response.put("user", security);
            return ResponseEntity.ok(response);
        }

        // Superadmin login
        if (role.equalsIgnoreCase("superadmin")) {
            SuperAdmin internal = superAdminService.findInternal(email);

            if (internal == null || !passwordEncoder.matches(password, internal.getPassword())) {
                return ResponseEntity.status(401).body(Map.of("message", "Invalid superadmin credentials"));
            }

            internal.setPassword(null);
            response.put("message", "Superadmin login successful");
            response.put("user", internal);
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Invalid role"));
    }
}
