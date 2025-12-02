package com.tms.restapi.toolsmanagement.tools.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "tools")
public class Tool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Location location;

    private int slNo;
    private String toolNo;
    private String description;
    private String toolLocation;
    private int quantity;

    @Enumerated(EnumType.STRING)
    private Status status = Status.Good;

    private boolean calibrationRequired;
    private String calibrationDate;
    private String remarks;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    private String qrCodePath;

    @PrePersist
    protected void onCreate() {
        createdAt = new Timestamp(System.currentTimeMillis());
        updatedAt = new Timestamp(System.currentTimeMillis());
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Timestamp(System.currentTimeMillis());
    }

    // Getters and Setters
    public Long getId() { return id; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public int getSlNo() { return slNo; }
    public void setSlNo(int slNo) { this.slNo = slNo; }
    public String getToolNo() { return toolNo; }
    public void setToolNo(String toolNo) { this.toolNo = toolNo; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getToolLocation() { return toolLocation; }
    public void setToolLocation(String toolLocation) { this.toolLocation = toolLocation; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public boolean isCalibrationRequired() { return calibrationRequired; }
    public void setCalibrationRequired(boolean calibrationRequired) { this.calibrationRequired = calibrationRequired; }
    public String getCalibrationDate() { return calibrationDate; }
    public void setCalibrationDate(String calibrationDate) { this.calibrationDate = calibrationDate; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public String getQrCodePath() { return qrCodePath; }
    public void setQrCodePath(String qrCodePath) { this.qrCodePath = qrCodePath; }

    public enum Location {
        Pune, Bangalore, NCR
    }

    public enum Status {
        Good, Damaged, Missing, Obsolete
    }
}