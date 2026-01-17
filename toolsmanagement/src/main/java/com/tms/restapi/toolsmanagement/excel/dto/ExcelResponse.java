package com.tms.restapi.toolsmanagement.excel.dto;

public class ExcelResponse {

    private int totalRecords;
    private int successRecords;
    private int failedRecords;
    private String message;

    public ExcelResponse(int total, int success, int failed, String msg) {
        this.totalRecords = total;
        this.successRecords = success;
        this.failedRecords = failed;
        this.message = msg;
    }

    // getters setters
    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getSuccessRecords() {
        return successRecords;
    }

    public void setSuccessRecords(int successRecords) {
        this.successRecords = successRecords;
    }

    public int getFailedRecords() {
        return failedRecords;
    }

    public void setFailedRecords(int failedRecords) {
        this.failedRecords = failedRecords;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}