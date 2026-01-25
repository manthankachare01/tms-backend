package com.tms.restapi.toolsmanagement.kit.service;

import com.tms.restapi.toolsmanagement.kit.dto.*;
import com.tms.restapi.toolsmanagement.kit.model.Kit;
import com.tms.restapi.toolsmanagement.kit.model.KitAggregate;
import com.tms.restapi.toolsmanagement.kit.repository.KitRepository;
import com.tms.restapi.toolsmanagement.tools.model.Tool;
import com.tms.restapi.toolsmanagement.tools.repository.ToolRepository;
import org.springframework.stereotype.Service;
import com.tms.restapi.toolsmanagement.exception.BadRequestException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KitService {

    private final KitRepository kitRepository;
    private final ToolRepository toolRepository;

    public KitService(KitRepository kitRepository, ToolRepository toolRepository) {
        this.kitRepository = kitRepository;
        this.toolRepository = toolRepository;
    }

    // simple example: generate KIT-001, KIT-002 ...
    private String generateKitId() {
        long count = kitRepository.count() + 1;
        return String.format("KIT-%03d", count);
    }

    // CREATE: one kit per request (single location only)
    public KitResponse createKit(KitCreateRequest request, String createdBy) {

        // Require a non-empty location string for kit creation
        if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
            throw new BadRequestException("Kit creation requires a non-empty 'location' field.");
        }

        List<Tool> tools = new ArrayList<>();
        String loc = request.getLocation().trim();

        // Use new toolItems approach if provided (SI_NO based)
        if (request.getToolItems() != null && !request.getToolItems().isEmpty()) {
            for (KitCreateRequest.ToolItem item : request.getToolItems()) {
                Tool tool = toolRepository.findBySiNoAndLocationIgnoreCaseAndTrim(
                        item.getSiNo(), 
                        item.getLocation()
                );
                if (tool == null) {
                    throw new BadRequestException(
                            "Tool not found with SI_NO: " + item.getSiNo() + 
                            " at location: " + item.getLocation()
                    );
                }
                tools.add(tool);
            }
        } 
        // Fall back to old toolNos approach if provided
        else if (request.getToolNos() != null && !request.getToolNos().isEmpty()) {
            tools = toolRepository.findByToolNoIn(request.getToolNos());
        }

        Kit kit = new Kit();
        kit.setKitId(generateKitId());
        kit.setKitName(request.getKitName());
        kit.setQualificationLevel(request.getQualificationLevel());
        kit.setTrainingName(request.getTrainingName());
        kit.setLocation(loc);
        kit.setAvailability(1); // default availability to 1
        kit.setRemark(request.getRemark());
        kit.setCondition(request.getCondition());
        kit.setTools(tools);
        
        // Set creator info
        kit.setCreatedBy(createdBy);
        kit.setCreatedAt(LocalDateTime.now());

        List<KitAggregate> aggregates = new ArrayList<>();
        if (request.getAggregates() != null) {
            for (KitAggregateRequest ar : request.getAggregates()) {
                KitAggregate agg = new KitAggregate();
                agg.setKit(kit);
                agg.setName(ar.getName());
                agg.setRemark(ar.getRemark());
                aggregates.add(agg);
            }
        }
        kit.setAggregates(aggregates);

        Kit saved = kitRepository.save(kit);
        return mapToResponse(saved);
    }

    public List<KitResponse> getAllKits() {
        return kitRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<KitResponse> getKitsByLocation(String location) {
        return kitRepository.findByLocationIgnoreCase(location)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Optional<KitResponse> getKitById(Long id) {
        return kitRepository.findById(id)
                .map(this::mapToResponse);
    }

    public List<KitResponse> searchKitsByLocationAndKeyword(String location, String keyword) {
        List<Kit> kits = kitRepository.searchByLocationAndKeyword(location, keyword);
        return kits.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // UPDATE: update a single kit (by id)
    public Optional<KitResponse> updateKit(Long id, KitCreateRequest request) {
        return kitRepository.findById(id).map(existing -> {

            existing.setKitName(request.getKitName());
            existing.setQualificationLevel(request.getQualificationLevel());
            existing.setTrainingName(request.getTrainingName());

            // IMPORTANT CHANGE: location now comes from single location field
            if (request.getLocation() != null && !request.getLocation().trim().isEmpty()) {
                existing.setLocation(request.getLocation().trim());
            }
            // else keep existing location

            // Update tools using new approach or fallback
            List<Tool> tools = new ArrayList<>();
            
            // Use new toolItems approach if provided (SI_NO based)
            if (request.getToolItems() != null && !request.getToolItems().isEmpty()) {
                for (KitCreateRequest.ToolItem item : request.getToolItems()) {
                    Tool tool = toolRepository.findBySiNoAndLocationIgnoreCaseAndTrim(
                            item.getSiNo(), 
                            item.getLocation()
                    );
                    if (tool == null) {
                        throw new BadRequestException(
                                "Tool not found with SI_NO: " + item.getSiNo() + 
                                " at location: " + item.getLocation()
                        );
                    }
                    tools.add(tool);
                }
            } 
            // Fall back to old toolNos approach if provided
            else if (request.getToolNos() != null && !request.getToolNos().isEmpty()) {
                tools = toolRepository.findByToolNoIn(request.getToolNos());
            }

            existing.getTools().clear();
            existing.getTools().addAll(tools);

            // update aggregates
            existing.getAggregates().clear();
            if (request.getAggregates() != null) {
                for (KitAggregateRequest aggReq : request.getAggregates()) {
                    KitAggregate agg = new KitAggregate();
                    agg.setKit(existing);
                    agg.setName(aggReq.getName());
                    agg.setRemark(aggReq.getRemark());
                    existing.getAggregates().add(agg);
                }
            }

            Kit updated = kitRepository.save(existing);
            return mapToResponse(updated);
        });
    }

    public boolean deleteKit(Long id) {
        if (kitRepository.existsById(id)) {
            kitRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private KitResponse mapToResponse(Kit kit) {
        KitResponse response = new KitResponse();
        response.setId(kit.getId());
        response.setKitId(kit.getKitId());
        response.setKitName(kit.getKitName());
        response.setQualificationLevel(kit.getQualificationLevel());
        response.setTrainingName(kit.getTrainingName());
        response.setLocation(kit.getLocation());
        response.setAvailability(kit.getAvailability());
        response.setLastBorrowedBy(kit.getLastBorrowedBy());
        response.setRemark(kit.getRemark());
        response.setCondition(kit.getCondition());
        response.setTools(kit.getTools());

        List<KitAggregateResponse> aggResponses = kit.getAggregates()
                .stream()
                .map(a -> {
                    KitAggregateResponse r = new KitAggregateResponse();
                    r.setId(a.getId());
                    r.setName(a.getName());
                    r.setRemark(a.getRemark());
                    return r;
                })
                .collect(Collectors.toList());

        response.setAggregates(aggResponses);

        return response;
    }
}
