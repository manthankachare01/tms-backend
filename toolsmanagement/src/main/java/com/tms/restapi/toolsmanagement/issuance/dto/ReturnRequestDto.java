package com.tms.restapi.toolsmanagement.issuance.dto;

import java.time.LocalDate;
import java.util.List;

public class ReturnRequestDto {
    private Long issuanceId;
    private LocalDate actualReturnDate;
    private String processedBy;
    private String remarks;
    private List<ReturnItemDto> items;

    public Long getIssuanceId() { return issuanceId; }
    public void setIssuanceId(Long issuanceId) { this.issuanceId = issuanceId; }

    public LocalDate getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(LocalDate actualReturnDate) { this.actualReturnDate = actualReturnDate; }

    public String getProcessedBy() { return processedBy; }
    public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public List<ReturnItemDto> getItems() { return items; }
    public void setItems(List<ReturnItemDto> items) { this.items = items; }
}
