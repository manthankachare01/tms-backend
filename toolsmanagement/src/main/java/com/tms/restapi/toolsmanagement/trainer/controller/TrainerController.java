package com.tms.restapi.toolsmanagement.trainer.controller;

import com.tms.restapi.toolsmanagement.trainer.model.Trainer;
import com.tms.restapi.toolsmanagement.trainer.service.TrainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Example: /api/trainers?adminLocation=Pune
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

    @Autowired
    private TrainerService trainerService;

    // Admin creates trainer (restricted by admin location)
    @PostMapping("/create")
    public Trainer createTrainer(@RequestParam String adminLocation, @RequestBody Trainer trainer) {
        return trainerService.createTrainer(trainer, adminLocation);
    }

    // Fetch all trainers for admin’s location
    @GetMapping("/all")
    public List<Trainer> getAllTrainers(@RequestParam String adminLocation) {
        return trainerService.getAllTrainersByLocation(adminLocation);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trainer> getTrainerById(@PathVariable Long id) {
        return trainerService.getTrainerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Trainer> updateTrainer(@PathVariable Long id, @RequestBody Trainer trainerDetails) {
        Trainer updated = trainerService.updateTrainer(id, trainerDetails);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Trainer>> searchTrainers(@RequestParam String keyword) {
        List<Trainer> trainers = trainerService.searchTrainers(keyword);
        return ResponseEntity.ok(trainers);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTrainer(@PathVariable Long id) {
        return ResponseEntity.ok(trainerService.deleteTrainer(id));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginTrainer(@RequestBody Trainer loginRequest) {
        Trainer trainer = trainerService.findByEmail(loginRequest.getEmail());
        if (trainer == null) {
            return ResponseEntity.status(404).body("{\"message\": \"Trainer not found\"}");
        }

        boolean passwordMatch = trainerService.verifyPassword(loginRequest.getPassword(), trainer.getPassword());
        if (!passwordMatch) {
            return ResponseEntity.status(401).body("{\"message\": \"Invalid password\"}");
        }

        // Create clean response without password
        trainer.setPassword(null);

        // Build a structured response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("user", trainer);

        return ResponseEntity.ok(response);
    }


}