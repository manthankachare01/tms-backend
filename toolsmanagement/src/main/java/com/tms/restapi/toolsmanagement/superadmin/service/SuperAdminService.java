package com.tms.restapi.toolsmanagement.superadmin.service;

import com.tms.restapi.toolsmanagement.superadmin.model.SuperAdmin;
import com.tms.restapi.toolsmanagement.superadmin.repository.SuperAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SuperAdminService {

    @Autowired
    private SuperAdminRepository repository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public SuperAdmin createSuperAdmin(SuperAdmin admin) {
        admin.setPassword(encoder.encode(admin.getPassword()));
        admin.setRole("SUPERADMIN");
        return repository.save(admin);
    }

    public SuperAdmin updateSuperAdmin(Long id, SuperAdmin data) {
        SuperAdmin saved = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Superadmin not found"));

        if (data.getEmail() != null) saved.setEmail(data.getEmail());
        if (data.getName() != null) saved.setName(data.getName());
        if (data.getPassword() != null) {
            saved.setPassword(encoder.encode(data.getPassword()));
        }

        return repository.save(saved);
    }

    public SuperAdmin findInternal(String email) {
        return repository.findByEmail(email).orElse(null);
    }

    public void resetPassword(String email, String newPassword) {
        SuperAdmin admin = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Superadmin not found"));

        admin.setPassword(encoder.encode(newPassword));
        repository.save(admin);
    }
}
