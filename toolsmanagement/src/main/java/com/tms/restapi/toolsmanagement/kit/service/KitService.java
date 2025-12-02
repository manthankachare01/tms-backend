package com.tms.restapi.toolsmanagement.kit.service;

import com.tms.restapi.toolsmanagement.kit.model.Kit;
import com.tms.restapi.toolsmanagement.kit.repository.KitRepository;
import com.tms.restapi.toolsmanagement.tools.model.Tool;
import com.tms.restapi.toolsmanagement.tools.repository.ToolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KitService {

    @Autowired
    private KitRepository kitRepository;

    @Autowired
    private ToolRepository toolRepository;

    public Kit createKit(Kit kit, List<Long> toolIds) {
        List<Tool> tools = toolRepository.findAllById(toolIds);
        kit.setTools(tools);
        return kitRepository.save(kit);
    }

    public List<Kit> getAllKits() {
        return kitRepository.findAll();
    }

    public Optional<Kit> getKitById(Long id) {
        return kitRepository.findById(id);
    }

    public Kit updateKit(Long id, Kit kitDetails, List<Long> toolIds) {
        return kitRepository.findById(id).map(kit -> {
            kit.setName(kitDetails.getName());
            kit.setQualificationType(kitDetails.getQualificationType());
            List<Tool> tools = toolRepository.findAllById(toolIds);
            kit.setTools(tools);
            return kitRepository.save(kit);
        }).orElse(null);
    }

    public String deleteKit(Long id) {
        if (kitRepository.existsById(id)) {
            kitRepository.deleteById(id);
            return "Kit deleted successfully.";
        } else {
            return "Kit not found.";
        }
    }
}