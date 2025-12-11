package com.tms.restapi.toolsmanagement.tools.repository;

import com.tms.restapi.toolsmanagement.tools.model.Tool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToolRepository extends JpaRepository<Tool, Long> {

    List<Tool> findByLocation(String location);

    List<Tool> findByDescriptionContainingIgnoreCaseOrToolNoContainingIgnoreCase(
            String description,
            String toolNo
    );

    List<Tool> findByToolNoIn(List<String> toolNos);
}
