package com.tms.restapi.toolsmanagement.reports.dto;

public class LocationStatisticsDTO {
    private String location;
    private Long totalTools;
    private Long availableTools;
    private Long issuedTools;
    private Double availabilityPercentage;

    public LocationStatisticsDTO() {
    }

    public LocationStatisticsDTO(String location, Long totalTools, Long availableTools,
                               Long issuedTools, Double availabilityPercentage) {
        this.location = location;
        this.totalTools = totalTools;
        this.availableTools = availableTools;
        this.issuedTools = issuedTools;
        this.availabilityPercentage = availabilityPercentage;
    }

    // Getters and Setters
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getTotalTools() {
        return totalTools;
    }

    public void setTotalTools(Long totalTools) {
        this.totalTools = totalTools;
    }

    public Long getAvailableTools() {
        return availableTools;
    }

    public void setAvailableTools(Long availableTools) {
        this.availableTools = availableTools;
    }

    public Long getIssuedTools() {
        return issuedTools;
    }

    public void setIssuedTools(Long issuedTools) {
        this.issuedTools = issuedTools;
    }

    public Double getAvailabilityPercentage() {
        return availabilityPercentage;
    }

    public void setAvailabilityPercentage(Double availabilityPercentage) {
        this.availabilityPercentage = availabilityPercentage;
    }
}
