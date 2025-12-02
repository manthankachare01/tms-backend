package com.tms.restapi.toolsmanagement.kit.model;

import com.tms.restapi.toolsmanagement.tools.model.Tool;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "kits")
public class Kit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String qualificationType;

    private int quantity = 1; // Default quantity = 1 (since each kit is unique but availability needs tracking)

    @ManyToMany
    @JoinTable(
        name = "kit_tools",
        joinColumns = @JoinColumn(name = "kit_id"),
        inverseJoinColumns = @JoinColumn(name = "tool_id")
    )
    private List<Tool> tools;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getQualificationType() { return qualificationType; }
    public void setQualificationType(String qualificationType) { this.qualificationType = qualificationType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public List<Tool> getTools() { return tools; }
    public void setTools(List<Tool> tools) { this.tools = tools; }
}