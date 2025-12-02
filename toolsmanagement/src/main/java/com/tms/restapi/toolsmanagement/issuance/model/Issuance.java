package com.tms.restapi.toolsmanagement.issuance.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "issuance_requests")
public class Issuance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long trainerId;
    private String trainerName;
    private String trainingName;
    private LocalDate issuanceDate;
    private LocalDate returnDate;
    private String status; // Pending, Approved, Rejected, Returned
    private String location;
    private String comment;
    @ElementCollection
    private List<Long> toolIds;

    @ElementCollection
    private List<Long> kitIds;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }

    public String getTrainerName() { return trainerName; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }

    public String getTrainingName() { return trainingName; }
    public void setTrainingName(String trainingName) { this.trainingName = trainingName; }

    public LocalDate getIssuanceDate() { return issuanceDate; }
    public void setIssuanceDate(LocalDate issuanceDate) { this.issuanceDate = issuanceDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public List<Long> getToolIds() { return toolIds; }
    public void setToolIds(List<Long> toolIds) { this.toolIds = toolIds; }

    public List<Long> getKitIds() { return kitIds; }
    public void setKitIds(List<Long> kitIds) { this.kitIds = kitIds; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

}