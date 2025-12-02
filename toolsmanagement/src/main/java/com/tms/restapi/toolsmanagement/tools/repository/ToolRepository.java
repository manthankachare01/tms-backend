package com.tms.restapi.toolsmanagement.tools.repository;

import com.tms.restapi.toolsmanagement.tools.model.Tool;
import com.tms.restapi.toolsmanagement.tools.model.Tool.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ToolRepository extends JpaRepository<Tool, Long> {
    List<Tool> findByDescriptionContainingIgnoreCase(String description);
    List<Tool> findByToolNoContainingIgnoreCase(String toolNo);
    List<Tool> findByLocation(Location location);
}