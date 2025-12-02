package com.tms.restapi.toolsmanagement.issuance.service;

import com.tms.restapi.toolsmanagement.issuance.model.Issuance;
import com.tms.restapi.toolsmanagement.issuance.repository.IssuanceRepository;
import com.tms.restapi.toolsmanagement.trainer.model.Trainer;
import com.tms.restapi.toolsmanagement.trainer.repository.TrainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IssuanceService {

    @Autowired
    private IssuanceRepository issuanceRepository;

    @Autowired
    private QuantityUpdateService quantityService;

    // @Autowired
    // private EmailService emailService;

    @Autowired
    private TrainerRepository trainerRepository;

    public Issuance createIssuanceRequest(Issuance issuance) {
        issuance.setStatus("Pending");
        return issuanceRepository.save(issuance);
    }

    public List<Issuance> getRequestsByTrainer(Long trainerId) {
        return issuanceRepository.findByTrainerId(trainerId);
    }

    public List<Issuance> getRequestsByLocation(String location) {
        return issuanceRepository.findByLocation(location);
    }

    // Updated method to include comment
    public Issuance updateStatus(Long id, String status, String trainerEmail, String comment) {
        return issuanceRepository.findById(id).map(req -> {

            req.setStatus(status);

            // Save comment if provided
            if (comment != null && !comment.isEmpty()) {
                req.setComment(comment);
            }

            Issuance updated = issuanceRepository.save(req);

            Trainer trainer = trainerRepository.findById(req.getTrainerId()).orElse(null);

            if (status.equalsIgnoreCase("Approved")) {
                quantityService.reduceQuantities(req.getToolIds(), req.getKitIds());

                if (trainer != null) {
                    int issuedCount = (req.getToolIds() != null ? req.getToolIds().size() : 0)
                                    + (req.getKitIds() != null ? req.getKitIds().size() : 0);
                    trainer.setToolsIssued(trainer.getToolsIssued() + issuedCount);
                    trainerRepository.save(trainer);
                }

                // emailService.sendEmail(trainerEmail, "Issuance Approved",
                //         "Your issuance request has been approved.");

            } else if (status.equalsIgnoreCase("Rejected")) {
                // emailService.sendEmail(trainerEmail, "Issuance Rejected",
                //         "Your issuance request has been rejected.");

            } else if (status.equalsIgnoreCase("Returned")) {
                quantityService.increaseQuantities(req.getToolIds(), req.getKitIds());

                if (trainer != null) {
                    int returnCount = (req.getToolIds() != null ? req.getToolIds().size() : 0)
                                    + (req.getKitIds() != null ? req.getKitIds().size() : 0);
                    trainer.setToolsReturned(trainer.getToolsReturned() + returnCount);
                    trainerRepository.save(trainer);
                }

                // emailService.sendEmail(trainerEmail, "Items Returned",
                //         "Items have been marked as returned and inventory updated.");
            }

            return updated;
        }).orElse(null);
    }

    public List<Issuance> getAllRequests() {
        return issuanceRepository.findAll();
    }

    public List<Issuance> getCurrentIssuedItems() {
        return issuanceRepository.findByStatus("Approved");
    }
}
