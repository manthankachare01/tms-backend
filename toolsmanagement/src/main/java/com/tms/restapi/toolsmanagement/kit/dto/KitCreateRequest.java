package com.tms.restapi.toolsmanagement.kit.dto;

import java.util.List;

public class KitCreateRequest {

    private String kitName;
    private String qualificationLevel;
    private String trainingName;
    private String location;
    private List<String> toolNos;
    private List<KitAggregateRequest> aggregates;
    private String remark;
    private String condition;

    public String getKitName() {
        return kitName;
    }

    public void setKitName(String kitName) {
        this.kitName = kitName;
    }

    public String getQualificationLevel() {
        return qualificationLevel;
    }

    public void setQualificationLevel(String qualificationLevel) {
        this.qualificationLevel = qualificationLevel;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    public List<String> getToolNos() {
        return toolNos;
    }

    public void setToolNos(List<String> toolNos) {
        this.toolNos = toolNos;
    }

    public List<KitAggregateRequest> getAggregates() {
        return aggregates;
    }

    public void setAggregates(List<KitAggregateRequest> aggregates) {
        this.aggregates = aggregates;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
