package com.tms.restapi.toolsmanagement.trainer.service;

import com.tms.restapi.toolsmanagement.trainer.model.Trainer;
import com.tms.restapi.toolsmanagement.trainer.repository.TrainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerService {

    @Autowired
    private TrainerRepository trainerRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Trainer createTrainer(Trainer trainer, String adminLocation) {
        // Force trainer’s location to match admin’s location
        trainer.setLocation(adminLocation);
        trainer.setPassword(passwordEncoder.encode(trainer.getPassword()));
        return trainerRepository.save(trainer);
    }

    public List<Trainer> getAllTrainersByLocation(String location) {
        return trainerRepository.findByLocation(location);
    }

    public Optional<Trainer> getTrainerById(Long id) {
        return trainerRepository.findById(id);
    }

    public Trainer updateTrainer(Long id, Trainer trainerDetails) {
        return trainerRepository.findById(id).map(trainer -> {
            trainer.setName(trainerDetails.getName());
            trainer.setEmail(trainerDetails.getEmail());
            trainer.setContact(trainerDetails.getContact());
            trainer.setRole(trainerDetails.getRole());
            trainer.setShift(trainerDetails.getShift());
            trainer.setStatus(trainerDetails.getStatus());
            trainer.setDob(trainerDetails.getDob());
            trainer.setDoj(trainerDetails.getDoj());
            trainer.setToolsIssued(trainerDetails.getToolsIssued());
            trainer.setToolsReturned(trainerDetails.getToolsReturned());
            return trainerRepository.save(trainer);
        }).orElse(null);
    }

    public List<Trainer> searchTrainers(String keyword) {
        return trainerRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
    }

    public Trainer findByEmail(String email) {
        return trainerRepository.findByEmail(email);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(rawPassword, encodedPassword);
    }



    public String deleteTrainer(Long id) {
        if (trainerRepository.existsById(id)) {
            trainerRepository.deleteById(id);
            return "Trainer deleted successfully.";
        } else {
            return "Trainer not found.";
        }
    }

    public void resetPassword(String email, String newPassword) {
        Trainer trainer = trainerRepository.findByEmail(email);
        if (trainer == null) {
            throw new RuntimeException("Trainer not found with this email");
        }

        trainer.setPassword(passwordEncoder.encode(newPassword)); // your BCryptPasswordEncoder
        trainerRepository.save(trainer);
    }
}