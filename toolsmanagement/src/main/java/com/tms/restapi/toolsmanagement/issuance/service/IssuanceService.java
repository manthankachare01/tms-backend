package com.tms.restapi.toolsmanagement.issuance.service;

import com.tms.restapi.toolsmanagement.issuance.dto.ReturnItemDto;
import com.tms.restapi.toolsmanagement.issuance.dto.ReturnRequestDto;
import com.tms.restapi.toolsmanagement.issuance.model.Issuance;
import com.tms.restapi.toolsmanagement.issuance.model.IssuanceRequest;
import com.tms.restapi.toolsmanagement.issuance.model.ReturnItem;
import com.tms.restapi.toolsmanagement.issuance.model.ReturnRecord;
import com.tms.restapi.toolsmanagement.issuance.repository.IssuanceRepository;
import com.tms.restapi.toolsmanagement.issuance.repository.IssuanceRequestRepository;
import com.tms.restapi.toolsmanagement.issuance.repository.ReturnRepository;
import com.tms.restapi.toolsmanagement.kit.model.Kit;
import com.tms.restapi.toolsmanagement.kit.repository.KitRepository;
import com.tms.restapi.toolsmanagement.tools.model.Tool;
import com.tms.restapi.toolsmanagement.tools.repository.ToolRepository;
import com.tms.restapi.toolsmanagement.trainer.model.Trainer;
import com.tms.restapi.toolsmanagement.trainer.repository.TrainerRepository;
import com.tms.restapi.toolsmanagement.exception.BadRequestException;
import com.tms.restapi.toolsmanagement.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IssuanceService {

    @Autowired
    private IssuanceRepository issuanceRepository;

    @Autowired
    private IssuanceRequestRepository issuanceRequestRepository;

    @Autowired
    private QuantityUpdateService quantityService;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private KitRepository kitRepository;

    @Autowired
    private ReturnRepository returnRecordRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private com.tms.restapi.toolsmanagement.auth.service.EmailService emailService;

    @Autowired
    private com.tms.restapi.toolsmanagement.admin.repository.AdminRepository adminRepository;

    /**
     * Update overdue status for issuances where expected return date has been exceeded
     * and issuance status is not yet RETURNED or OVERDUE
     */
    public void updateOverdueStatuses() {
        LocalDateTime now = LocalDateTime.now();
        List<Issuance> issuances = issuanceRepository.findAll();
        
        for (Issuance i : issuances) {
            // Check if issuance is still ISSUED and expected return date has passed
            if ("ISSUED".equalsIgnoreCase(i.getStatus()) && 
                i.getReturnDate() != null && 
                i.getReturnDate().isBefore(now)) {
                
                // Mark as overdue
                i.setStatus("OVERDUE");
                issuanceRepository.save(i);
                
                // Send notifications
                try {
                    Trainer trainer = trainerRepository.findById(i.getTrainerId()).orElse(null);
                    if (trainer != null && trainer.getEmail() != null) {
                        emailService.sendOverdueEmailToTrainer(i, trainer.getEmail(), trainer.getName());
                    }
                } catch (Exception e) {
                    // ignore trainer email failure
                }
                
                // Notify admins
                if (i.getLocation() != null) {
                    try {
                        List<com.tms.restapi.toolsmanagement.admin.model.Admin> admins =
                                adminRepository.findByLocation(i.getLocation());
                        if (admins != null && !admins.isEmpty()) {
                            for (com.tms.restapi.toolsmanagement.admin.model.Admin admin : admins) {
                                try {
                                    emailService.sendOverdueEmailToAdmin(i, admin.getEmail(), admin.getName());
                                } catch (Exception e) {
                                    // ignore individual admin email failure
                                }
                            }
                        }
                    } catch (Exception e) {
                        // ignore admin repository access failure
                    }
                }
            }
        }
    }

    public Issuance createIssuanceRequest(Issuance issuance) {
        // basic validation
        if (issuance.getTrainerId() == null) {
            throw new BadRequestException("trainerId is required");
        }
        if (issuance.getTrainerName() == null || issuance.getTrainerName().isEmpty()) {
            throw new BadRequestException("trainerName is required");
        }
        if ((issuance.getToolIds() == null || issuance.getToolIds().isEmpty())
                && (issuance.getKitIds() == null || issuance.getKitIds().isEmpty())) {
            throw new BadRequestException("At least one toolId or kitId is required");
        }

        // Create an actual Issuance record with PENDING status
        Issuance pendingIssuance = new Issuance();
        pendingIssuance.setTrainerId(issuance.getTrainerId());
        pendingIssuance.setTrainerName(issuance.getTrainerName());
        pendingIssuance.setTrainingName(issuance.getTrainingName());
        pendingIssuance.setToolIds(issuance.getToolIds());
        pendingIssuance.setKitIds(issuance.getKitIds());
        pendingIssuance.setReturnDate(issuance.getReturnDate());
        pendingIssuance.setLocation(issuance.getLocation());
        pendingIssuance.setComment(issuance.getComment());
        pendingIssuance.setIssuanceType(issuance.getIssuanceType());
        pendingIssuance.setRemarks(issuance.getRemarks());
        pendingIssuance.setStatus("PENDING");
        pendingIssuance.setIssuanceDate(LocalDateTime.now());

        Issuance savedIssuance = issuanceRepository.save(pendingIssuance);

        // Create an IssuanceRequest record for admin tracking
        IssuanceRequest request = new IssuanceRequest();
        request.setTrainerId(issuance.getTrainerId());
        request.setTrainerName(issuance.getTrainerName());
        request.setTrainingName(issuance.getTrainingName());
        request.setToolIds(issuance.getToolIds());
        request.setKitIds(issuance.getKitIds());
        request.setReturnDate(issuance.getReturnDate());
        request.setLocation(issuance.getLocation());
        request.setComment(issuance.getComment());
        request.setIssuanceType(issuance.getIssuanceType());
        request.setRemarks(issuance.getRemarks());
        request.setStatus("PENDING");
        request.setRequestDate(LocalDateTime.now());
        request.setIssuanceId(savedIssuance.getId());  // Link to the Issuance record

        IssuanceRequest savedRequest = issuanceRequestRepository.save(request);

        // Send notification to admins of the location to approve request
        if (issuance.getLocation() != null) {
            try {
                List<com.tms.restapi.toolsmanagement.admin.model.Admin> admins =
                        adminRepository.findByLocation(issuance.getLocation());
                if (admins != null && !admins.isEmpty()) {
                    for (com.tms.restapi.toolsmanagement.admin.model.Admin admin : admins) {
                        try {
                            emailService.sendIssuanceRequestNotification(savedRequest, admin.getEmail(), admin.getName());
                        } catch (Exception e) {
                            // ignore email failure
                        }
                    }
                }
            } catch (Exception e) {
                // ignore admin notification failure
            }
        }

        // Return the saved Issuance with PENDING status
        return savedIssuance;
    }

    /**
     * Approve an issuance request by an admin
     * This updates the Issuance status from PENDING to ISSUED and deducts availability
     */
    public Issuance approveIssuanceRequest(Long requestId, String approvedBy, String approvalRemark) {
        IssuanceRequest request = issuanceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Issuance request not found: id=" + requestId));

        if (!request.getStatus().equals("PENDING")) {
            throw new BadRequestException("Issuance request is not in PENDING status. Current status: " + request.getStatus());
        }

        // Deduct availability from tools and kits
        if (request.getToolIds() != null) {
            for (Long toolId : request.getToolIds()) {
                Tool tool = toolRepository.findById(toolId)
                        .orElseThrow(() -> new ResourceNotFoundException("Tool not found: id=" + toolId));
                if (tool.getAvailability() <= 0) {
                    throw new BadRequestException("Tool not available: " + tool.getDescription());
                }
                tool.setAvailability(tool.getAvailability() - 1);
                toolRepository.save(tool);
            }
        }

        if (request.getKitIds() != null) {
            for (Long kitId : request.getKitIds()) {
                Kit kit = kitRepository.findById(kitId)
                        .orElseThrow(() -> new ResourceNotFoundException("Kit not found: id=" + kitId));
                if (kit.getAvailability() <= 0) {
                    throw new BadRequestException("Kit not available: " + kit.getKitName());
                }
                kit.setAvailability(kit.getAvailability() - 1);
                kitRepository.save(kit);
            }
        }

        // Update existing PENDING Issuance to ISSUED status
        Issuance existingIssuance = issuanceRepository.findById(request.getIssuanceId())
                .orElseThrow(() -> new ResourceNotFoundException("Issuance not found: id=" + request.getIssuanceId()));

        existingIssuance.setStatus("ISSUED");
        existingIssuance.setApprovedBy(approvedBy);
        existingIssuance.setApprovalDate(LocalDateTime.now());
        existingIssuance.setApprovalRemark(approvalRemark);

        Issuance savedIssuance = issuanceRepository.save(existingIssuance);

        // Update trainer stats
        Trainer trainer = trainerRepository.findById(request.getTrainerId()).orElse(null);
        if (trainer != null) {
            int issuedCount =
                    (request.getToolIds() != null ? request.getToolIds().size() : 0)
                            + (request.getKitIds() != null ? request.getKitIds().size() : 0);
            trainer.setToolsIssued(trainer.getToolsIssued() + issuedCount);
            trainer.setActiveIssuance(trainer.getActiveIssuance() + 1);
            trainerRepository.save(trainer);
        }

        // Update the request status
        request.setStatus("APPROVED");
        request.setApprovedBy(approvedBy);
        request.setApprovalDate(LocalDateTime.now());
        request.setApprovalRemark(approvalRemark);
        issuanceRequestRepository.save(request);

        // Send approval email to trainer (best-effort)
        try {
            Trainer t = trainerRepository.findById(savedIssuance.getTrainerId()).orElse(null);
            if (t != null && t.getEmail() != null) {
                emailService.sendIssuanceApprovalEmail(savedIssuance, t.getEmail(), t.getName());
            }
        } catch (Exception e) {
            // do not fail if email sending fails
        }

        return savedIssuance;
    }

    /**
     * Reject an issuance request by an admin
     * Marks both the IssuanceRequest and Issuance as REJECTED
     */
    public void rejectIssuanceRequest(Long requestId, String rejectedBy, String rejectionReason) {
        IssuanceRequest request = issuanceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Issuance request not found: id=" + requestId));

        if (!request.getStatus().equals("PENDING")) {
            throw new BadRequestException("Issuance request is not in PENDING status. Current status: " + request.getStatus());
        }

        // Mark the associated Issuance as REJECTED
        if (request.getIssuanceId() != null) {
            Issuance issuance = issuanceRepository.findById(request.getIssuanceId()).orElse(null);
            if (issuance != null) {
                issuance.setStatus("REJECTED");
                issuance.setApprovedBy(rejectedBy);
                issuance.setApprovalDate(LocalDateTime.now());
                issuance.setApprovalRemark(rejectionReason);
                issuanceRepository.save(issuance);
            }
        }

        // Mark the request as REJECTED
        request.setStatus("REJECTED");
        request.setApprovedBy(rejectedBy);
        request.setApprovalDate(LocalDateTime.now());
        request.setApprovalRemark(rejectionReason);
        issuanceRequestRepository.save(request);

        // Send rejection email to trainer (best-effort)
        try {
            Trainer t = trainerRepository.findById(request.getTrainerId()).orElse(null);
            if (t != null && t.getEmail() != null) {
                emailService.sendIssuanceRejectionEmail(request, t.getEmail(), t.getName());
            }
        } catch (Exception e) {
            // do not fail if email sending fails
        }
    }

    /**
     * Get all pending issuance requests for a location
     */
    public List<IssuanceRequest> getPendingRequestsByLocation(String location) {
        return issuanceRequestRepository.findByLocationAndStatus(location, "PENDING");
    }

    /**
     * Get all issuance requests for a location
     */
    public List<IssuanceRequest> getAllRequestsByLocation(String location) {
        return issuanceRequestRepository.findByLocation(location);
    }

    public List<Issuance> getRequestsByTrainer(Long trainerId) {
        return issuanceRepository.findByTrainerId(trainerId);
    }

    public List<IssuanceRequest> getIssuanceRequestsByTrainer(Long trainerId) {
        return issuanceRequestRepository.findByTrainerId(trainerId);
    }

    public List<Issuance> getRequestsByLocation(String location) {
        return issuanceRepository.findByLocation(location);
    }

    public List<IssuanceRequest> getAllIssuanceRequests() {
        return issuanceRequestRepository.findAll();
    }

    public Issuance processReturn(ReturnRequestDto body) {
        if (body.getIssuanceId() == null) {
            throw new BadRequestException("issuanceId is required");
        }

        return issuanceRepository.findById(body.getIssuanceId()).map(req -> {
            // Use provided return timestamp or set current timestamp
            LocalDateTime actualReturnDate = body.getActualReturnDate() != null 
                ? body.getActualReturnDate() 
                : LocalDateTime.now();
            
            LocalDateTime plannedReturnDate = req.getReturnDate();

            // set status with null-safe check on plannedReturnDate
            if (plannedReturnDate != null && actualReturnDate.isAfter(plannedReturnDate)) {
                req.setStatus("OVERDUE");
            } else {
                req.setStatus("RETURNED");
            }

            // Persist a ReturnRecord
            ReturnRecord rr = new ReturnRecord();
            rr.setIssuance(req);
            rr.setActualReturnDate(actualReturnDate);
            rr.setProcessedBy(body.getProcessedBy());
            rr.setRemarks(body.getRemarks());

            boolean hasItems = body.getItems() != null && !body.getItems().isEmpty();

            if (hasItems) {
                // handle per-item returns
                for (ReturnItemDto it : body.getItems()) {
                    ReturnItem ri = new ReturnItem();
                    ri.setReturnRecord(rr);
                    ri.setToolId(it.getToolId());
                    ri.setKitId(it.getKitId());
                    ri.setQuantityReturned(it.getQuantityReturned() == null ? 1 : it.getQuantityReturned());
                    ri.setCondition(it.getCondition());
                    ri.setRemark(it.getRemark());
                    rr.getItems().add(ri);

                    // Return for an individual tool (toolId present) -> update that tool fully
                    if (it.getToolId() != null) {
                        Tool t = toolRepository.findById(it.getToolId())
                                .orElseThrow(() -> new ResourceNotFoundException("Tool not found: id=" + it.getToolId()));
                        t.setAvailability(t.getAvailability() + ri.getQuantityReturned());
                        if (ri.getCondition() != null) {
                            t.setCondition(ri.getCondition());
                        }
                        if (ri.getRemark() != null) {
                            t.setRemark(ri.getRemark());
                        }
                        toolRepository.save(t);
                    }

                    // Return for a kit (kitId present) -> update kit availability and ALL tools inside the kit.
                    // When a kit is returned we update kit-level condition/remark (if provided) but do NOT update
                    // per-tool condition/remark. We only increment availability for each tool inside the kit.
                    if (it.getKitId() != null) {
                        Kit k = kitRepository.findById(it.getKitId())
                                .orElseThrow(() -> new ResourceNotFoundException("Kit not found: id=" + it.getKitId()));

                        // increase kit availability
                        k.setAvailability(k.getAvailability() + ri.getQuantityReturned());

                        // update kit-level condition/remark if provided on the return item.
                        // (Assumes Kit has setCondition / setRemark methods; adjust if your Kit model differs.)
                        if (ri.getCondition() != null) {
                            try {
                                k.getClass().getMethod("setCondition", String.class).invoke(k, ri.getCondition());
                            } catch (NoSuchMethodException ignore) {
                                // Kit doesn't have setCondition; skip
                            } catch (Exception ex) {
                                // ignore reflective error - kit condition update is optional
                            }
                        }
                        if (ri.getRemark() != null) {
                            try {
                                k.getClass().getMethod("setRemark", String.class).invoke(k, ri.getRemark());
                            } catch (NoSuchMethodException ignore) {
                                // Kit doesn't have setRemark; skip
                            } catch (Exception ex) {
                                // ignore reflective error - kit remark update is optional
                            }
                        }

                        kitRepository.save(k);

                        // increment availability for every tool inside the kit by fetching each tool
                        // from the tool repository (ensures we operate on managed entities).
                        if (k.getTools() != null) {
                            for (Tool toolRef : k.getTools()) {
                                if (toolRef == null) continue;
                                Long toolId = null;
                                try {
                                    // assume Tool has getId(); fall back if not present will cause exception
                                    toolId = (Long) toolRef.getClass().getMethod("getId").invoke(toolRef);
                                } catch (Exception e) {
                                    // if reflection fails, try to use toolRef directly (it might be managed)
                                }

                                if (toolId != null) {
                                    toolRepository.findById(toolId).ifPresent(managedTool -> {
                                        managedTool.setAvailability(managedTool.getAvailability() + ri.getQuantityReturned());
                                        // IMPORTANT: do NOT change managedTool condition/remark for kit returns
                                        toolRepository.save(managedTool);
                                    });
                                } else {
                                    // fallback: try to increment availability on the toolRef object if it has availability
                                    try {
                                        Integer avail = (Integer) toolRef.getClass().getMethod("getAvailability").invoke(toolRef);
                                        toolRef.getClass().getMethod("setAvailability", Integer.class).invoke(toolRef, avail + ri.getQuantityReturned());
                                        // attempt to save via repository by id is not possible here; skip if not found
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        }
                    }
                }
                // IMPORTANT: do NOT call quantityService.increaseQuantities here,
                // because we already incremented per item above.
            } else {
                // no per-item details provided: treat as full return of all issued items
                // Add availability back for all tools and kits
                if (req.getToolIds() != null) {
                    for (Long toolId : req.getToolIds()) {
                        Tool tool = toolRepository.findById(toolId).orElse(null);
                        if (tool != null) {
                            tool.setAvailability(tool.getAvailability() + 1);
                            toolRepository.save(tool);
                        }
                    }
                }
                if (req.getKitIds() != null) {
                    for (Long kitId : req.getKitIds()) {
                        Kit kit = kitRepository.findById(kitId).orElse(null);
                        if (kit != null) {
                            kit.setAvailability(kit.getAvailability() + 1);
                            kitRepository.save(kit);
                        }
                    }
                }
            }

            // save ReturnRecord
            returnRecordRepository.save(rr);

            // update trainer stats
            Trainer trainer = trainerRepository.findById(req.getTrainerId()).orElse(null);
            if (trainer != null) {
                int returnCount =
                        (req.getToolIds() != null ? req.getToolIds().size() : 0)
                                + (req.getKitIds() != null ? req.getKitIds().size() : 0);
                trainer.setToolsReturned(trainer.getToolsReturned() + returnCount);
                trainer.setActiveIssuance(Math.max(0, trainer.getActiveIssuance() - 1));

                if (plannedReturnDate != null && actualReturnDate.isAfter(plannedReturnDate)) {
                    trainer.setOverdueIssuance(trainer.getOverdueIssuance() + 1);
                }

                trainerRepository.save(trainer);
            }

            // store actual return date on issuance
            req.setReturnDate(actualReturnDate);
            Issuance savedReq = issuanceRepository.save(req);

            // send return email to trainer (best-effort)
            try {
                Trainer tr = trainerRepository.findById(savedReq.getTrainerId()).orElse(null);
                if (tr != null && tr.getEmail() != null) {
                    ReturnRecord savedRr = rr; // rr already saved above
                    emailService.sendReturnEmail(savedRr, tr.getEmail());
                }
            } catch (Exception e) {
                // ignore email failures
            }

            // Check if any items were returned in damaged/missing/obsolete condition
            // If so, notify the admin(s) of that location
            List<ReturnItem> problematicItems = new java.util.ArrayList<>();
            if (rr.getItems() != null) {
                for (ReturnItem ri : rr.getItems()) {
                    String condition = ri.getCondition();
                    if (condition != null && (
                            condition.equalsIgnoreCase("damaged") ||
                            condition.equalsIgnoreCase("missing") ||
                            condition.equalsIgnoreCase("obsolete"))) {
                        problematicItems.add(ri);
                    }
                }
            }

            // If problematic items exist, send notification to admins of the location
            if (!problematicItems.isEmpty() && savedReq.getLocation() != null) {
                try {
                    List<com.tms.restapi.toolsmanagement.admin.model.Admin> admins =
                            adminRepository.findByLocation(savedReq.getLocation());
                    if (admins != null && !admins.isEmpty()) {
                        for (com.tms.restapi.toolsmanagement.admin.model.Admin admin : admins) {
                            try {
                                emailService.sendDamagedItemNotification(problematicItems, savedReq, admin.getEmail(), admin.getName());
                            } catch (Exception e) {
                                // ignore individual admin email failures
                            }
                        }
                    }
                } catch (Exception e) {
                    // ignore if admin repository access fails
                }
            }

            // If issuance is overdue, send notification to trainer and admins
            if ("OVERDUE".equals(savedReq.getStatus())) {
                try {
                    Trainer tr = trainerRepository.findById(savedReq.getTrainerId()).orElse(null);
                    if (tr != null && tr.getEmail() != null) {
                        emailService.sendOverdueEmailToTrainer(savedReq, tr.getEmail(), tr.getName());
                    }
                } catch (Exception e) {
                    // ignore trainer email failure
                }

                // Notify admins of the location about overdue
                if (savedReq.getLocation() != null) {
                    try {
                        List<com.tms.restapi.toolsmanagement.admin.model.Admin> admins =
                                adminRepository.findByLocation(savedReq.getLocation());
                        if (admins != null && !admins.isEmpty()) {
                            for (com.tms.restapi.toolsmanagement.admin.model.Admin admin : admins) {
                                try {
                                    emailService.sendOverdueEmailToAdmin(savedReq, admin.getEmail(), admin.getName());
                                } catch (Exception e) {
                                    // ignore individual admin email failure
                                }
                            }
                        }
                    } catch (Exception e) {
                        // ignore admin repository access failure
                    }
                }
            }

            return savedReq;
        }).orElse(null);
    }

    public List<Issuance> getAllRequests() {
        return issuanceRepository.findAll();
    }

    public List<Issuance> getCurrentIssuedItems() {
        return issuanceRepository.findByStatus("ISSUED");
    }

    // Return record retrieval helpers
    public List<ReturnRecord> getAllReturnRecords() {
        return returnRecordRepository.findAll();
    }

    public List<ReturnRecord> getReturnRecordsByLocation(String location) {
        return returnRecordRepository.findByIssuance_Location(location);
    }

    public List<ReturnRecord> getReturnRecordsByTrainer(Long trainerId) {
        return returnRecordRepository.findByIssuance_TrainerId(trainerId);
    }

    public List<ReturnRecord> getReturnRecordsByLocationAndTrainer(String location, Long trainerId) {
        return returnRecordRepository.findByIssuance_LocationAndIssuance_TrainerId(location, trainerId);
    }
}
