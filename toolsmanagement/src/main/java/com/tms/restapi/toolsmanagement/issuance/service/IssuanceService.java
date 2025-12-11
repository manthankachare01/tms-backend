package com.tms.restapi.toolsmanagement.issuance.service;

import com.tms.restapi.toolsmanagement.issuance.dto.ReturnItemDto;
import com.tms.restapi.toolsmanagement.issuance.dto.ReturnRequestDto;
import com.tms.restapi.toolsmanagement.issuance.model.Issuance;
import com.tms.restapi.toolsmanagement.issuance.model.ReturnItem;
import com.tms.restapi.toolsmanagement.issuance.model.ReturnRecord;
import com.tms.restapi.toolsmanagement.issuance.repository.IssuanceRepository;
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

import java.time.LocalDate;
import java.util.List;

@Service
public class IssuanceService {

    @Autowired
    private IssuanceRepository issuanceRepository;

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

        issuance.setStatus("ISSUED");
        if (issuance.getIssuanceDate() == null) {
            issuance.setIssuanceDate(LocalDate.now());
        }

        // update quantities and trainer stats (may throw BadRequestException or ResourceNotFoundException)
        quantityService.reduceQuantities(issuance.getToolIds(), issuance.getKitIds(), issuance.getTrainerName());

        Trainer trainer = trainerRepository.findById(issuance.getTrainerId()).orElse(null);
        if (trainer != null) {
            int issuedCount =
                    (issuance.getToolIds() != null ? issuance.getToolIds().size() : 0)
                            + (issuance.getKitIds() != null ? issuance.getKitIds().size() : 0);
            trainer.setToolsIssued(trainer.getToolsIssued() + issuedCount);
            trainer.setActiveIssuance(trainer.getActiveIssuance() + 1);
            trainerRepository.save(trainer);
        }

        return issuanceRepository.save(issuance);
    }

    public List<Issuance> getRequestsByTrainer(Long trainerId) {
        return issuanceRepository.findByTrainerId(trainerId);
    }

    public List<Issuance> getRequestsByLocation(String location) {
        return issuanceRepository.findByLocation(location);
    }

    public Issuance processReturn(ReturnRequestDto body) {
        if (body.getIssuanceId() == null) {
            throw new BadRequestException("issuanceId is required");
        }
        if (body.getActualReturnDate() == null) {
            throw new BadRequestException("actualReturnDate is required");
        }

        return issuanceRepository.findById(body.getIssuanceId()).map(req -> {
            LocalDate actualReturnDate = body.getActualReturnDate();
            LocalDate plannedReturnDate = req.getReturnDate();

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
                quantityService.increaseQuantities(req.getToolIds(), req.getKitIds());
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
            return issuanceRepository.save(req);
        }).orElse(null);
    }

    public List<Issuance> getAllRequests() {
        return issuanceRepository.findAll();
    }

    public List<Issuance> getCurrentIssuedItems() {
        return issuanceRepository.findByStatus("ISSUED");
    }
}
