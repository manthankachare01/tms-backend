package com.tms.restapi.toolsmanagement.tools.service;

import com.google.zxing.WriterException;
import com.tms.restapi.toolsmanagement.tools.model.Tool;
import com.tms.restapi.toolsmanagement.tools.model.Tool.Location;
import com.tms.restapi.toolsmanagement.tools.repository.ToolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ToolService {

    @Autowired
    private ToolRepository toolRepository;

    public Tool createTool(Tool tool, String adminLocation) throws IOException, WriterException {
        // Force tool’s location to match admin’s location
        try {
            tool.setLocation(Location.valueOf(adminLocation));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid location. Allowed values: Pune, Bangalore, NCR");
        }

        // Save to get ID
        Tool savedTool = toolRepository.save(tool);

        // Prepare QR content
        // String qrContent = "Tool ID: " + savedTool.getId() +
        //         "\nTool No: " + savedTool.getToolNo() +
        //         "\nDescription: " + savedTool.getDescription() +
        //         "\nLocation: " + savedTool.getLocation() +
        //         "\nStatus: " + savedTool.getStatus() +
        //         "\nQuantity: " + savedTool.getQuantity() +
        //         "\nCalibration Date: " + savedTool.getCalibrationDate();

        // // Generate QR Code
        // String qrFileName = "tool_" + savedTool.getId();
        // String qrPath = QRCodeGenerator.generateQRCode(qrContent, qrFileName);

        // savedTool.setQrCodePath(qrPath);
        return toolRepository.save(savedTool);
    }

    public List<Tool> getAllTools() {
        return toolRepository.findAll();
    }

    public List<Tool> getToolsByLocation(String location) {
        try {
            Location loc = Location.valueOf(location);
            return toolRepository.findByLocation(loc);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid location. Allowed values: Pune, Bangalore, NCR");
        }
    }

    public Optional<Tool> getToolById(Long id) {
        return toolRepository.findById(id);
    }

    public List<Tool> searchByToolNoOrDescription(String keyword) {
        List<Tool> byToolNo = toolRepository.findByToolNoContainingIgnoreCase(keyword);
        List<Tool> byDesc = toolRepository.findByDescriptionContainingIgnoreCase(keyword);

        // Merge results without duplicates
        List<Tool> merged = new ArrayList<>(byToolNo);
        for (Tool t : byDesc) {
            if (!merged.contains(t)) {
                merged.add(t);
            }
        }
        return merged;
    }

    public Tool updateTool(Long id, Tool toolDetails) {
        return toolRepository.findById(id).map(tool -> {
            tool.setToolNo(toolDetails.getToolNo());
            tool.setDescription(toolDetails.getDescription());
            tool.setToolLocation(toolDetails.getToolLocation());
            tool.setQuantity(toolDetails.getQuantity());
            tool.setStatus(toolDetails.getStatus());
            tool.setCalibrationRequired(toolDetails.isCalibrationRequired());
            tool.setCalibrationDate(toolDetails.getCalibrationDate());
            tool.setRemarks(toolDetails.getRemarks());
            return toolRepository.save(tool);
        }).orElse(null);
    }

    public String deleteTool(Long id) {
        if (toolRepository.existsById(id)) {
            toolRepository.deleteById(id);
            return "Tool deleted successfully.";
        } else {
            return "Tool not found.";
        }
    }
}