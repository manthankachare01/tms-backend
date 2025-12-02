package com.tms.restapi.toolsmanagement.trainer.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "trainers")
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String contact;
    private String location;
    private String role;
    private String shift;
    private String status;
    private LocalDate dob;
    private LocalDate doj;
    private String password;
    private int toolsIssued;
    private int toolsReturned;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public LocalDate getDoj() {
        return doj;
    }

    public void setDoj(LocalDate doj) {
        this.doj = doj;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getToolsIssued() {
        return toolsIssued;
    }

    public void setToolsIssued(int toolsIssued) {
        this.toolsIssued = toolsIssued;
    }

    public int getToolsReturned() {
        return toolsReturned;
    }

    public void setToolsReturned(int toolsReturned) {
        this.toolsReturned = toolsReturned;
    }
}