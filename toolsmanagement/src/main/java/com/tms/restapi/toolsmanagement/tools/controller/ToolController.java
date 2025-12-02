package com.tms.restapi.toolsmanagement.tools.controller;

import com.google.zxing.WriterException;
import com.tms.restapi.toolsmanagement.tools.model.Tool;
import com.tms.restapi.toolsmanagement.tools.service.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/tools")
public class ToolController {

    @Autowired
    private ToolService toolService;

    // Admin creates tool (restricted by admin location)
    @PostMapping("/create")
    public Tool createTool(@RequestParam String adminLocation, @RequestBody Tool tool)
            throws IOException, WriterException {
        return toolService.createTool(tool, adminLocation);
    }

    // Get all tools (optionally filtered by location)
    @GetMapping("/all")
    public List<Tool> getAllTools(@RequestParam(required = false) String location) {
        if (location != null && !location.isEmpty()) {
            return toolService.getToolsByLocation(location);
        }
        return toolService.getAllTools();
    }

    @GetMapping("/{id}")
    public Optional<Tool> getToolById(@PathVariable Long id) {
        return toolService.getToolById(id);
    }

    @GetMapping("/search")
    public List<Tool> searchTools(@RequestParam String keyword) {
        return toolService.searchByToolNoOrDescription(keyword);
    }

    @PutMapping("/update/{id}")
    public Tool updateTool(@PathVariable Long id, @RequestBody Tool toolDetails) {
        return toolService.updateTool(id, toolDetails);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteTool(@PathVariable Long id) {
        return toolService.deleteTool(id);
    }
}