package com.tms.restapi.toolsmanagement.issuance.service;

import com.tms.restapi.toolsmanagement.kit.model.Kit;
import com.tms.restapi.toolsmanagement.kit.repository.KitRepository;
import com.tms.restapi.toolsmanagement.tools.model.Tool;
import com.tms.restapi.toolsmanagement.tools.repository.ToolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuantityUpdateService {

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private KitRepository kitRepository;

    public void reduceQuantities(List<Long> toolIds, List<Long> kitIds) {
        if (toolIds != null) {
            for (Long toolId : toolIds) {
                Tool tool = toolRepository.findById(toolId)
                        .orElseThrow(() -> new RuntimeException("Tool not found"));
                if (tool.getQuantity() > 0) {
                    tool.setQuantity(tool.getQuantity() - 1);
                    toolRepository.save(tool);
                } else {
                    throw new RuntimeException("Tool unavailable: " + tool.getToolNo());
                }
            }
        }

        if (kitIds != null) {
            for (Long kitId : kitIds) {
                Kit kit = kitRepository.findById(kitId)
                        .orElseThrow(() -> new RuntimeException("Kit not found"));
                if (kit.getQuantity() > 0) {
                    kit.setQuantity(kit.getQuantity() - 1);
                    kitRepository.save(kit);
                } else {
                    throw new RuntimeException("Kit unavailable: " + kit.getName());
                }
            }
        }
    }

    public void increaseQuantities(List<Long> toolIds, List<Long> kitIds) {
        if (toolIds != null) {
            for (Long toolId : toolIds) {
                toolRepository.findById(toolId).ifPresent(tool -> {
                    tool.setQuantity(tool.getQuantity() + 1);
                    toolRepository.save(tool);
                });
            }
        }

        if (kitIds != null) {
            for (Long kitId : kitIds) {
                kitRepository.findById(kitId).ifPresent(kit -> {
                    kit.setQuantity(kit.getQuantity() + 1);
                    kitRepository.save(kit);
                });
            }
        }
    }
}