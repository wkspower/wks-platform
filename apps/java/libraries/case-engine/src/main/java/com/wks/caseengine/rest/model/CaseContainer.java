package com.wks.caseengine.rest.model;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CaseContainer {
    
    private String caseNo; // from JSON: "caseNo"
    private String caseTitle; // from JSON: "caseTitle"
    private String caseAssignTo; // from JSON: "caseAssignTo"
    private String faultCategory; // from JSON: "faultCategory"
    private String caseDescription; // from JSON: "caseDescription"
    
    @JsonProperty("dataGrid")
    private List<FaultDetail> dataGrid; // assuming FaultDetail is another class

    private LocalDateTime createdOn; // from JSON: "createdOn"
    private LocalDateTime dueDate; // from JSON: "dueDate"
    private LocalDateTime endDate; // from JSON: "endDate"
    
    private List<String> analysisTeam; // from JSON: "analysisTeam"
    private Integer caseStatus; // from JSON: "caseStatus"
    private String RecommendationsRadio; // from JSON: "RecommendationsRadio"
    private String valueRealizationCategory; // from JSON: "valueRealizationCategory"
    private String valueRealizationConclusion; // from JSON: "valueRealizationConclusion"
    private Double totalValueCaptured; // from JSON: "totalValueCaptured"
    private Double productionLoss; // from JSON: "productionLoss"
    private Double manHoursCost; // from JSON: "manHoursCost"
    private Double spareCost;
	public String getCaseNo() {
		return caseNo;
	}
	public void setCaseNo(String caseNo) {
		this.caseNo = caseNo;
	}
	public String getCaseTitle() {
		return caseTitle;
	}
	public void setCaseTitle(String caseTitle) {
		this.caseTitle = caseTitle;
	}
	public String getCaseAssignTo() {
		return caseAssignTo;
	}
	public void setCaseAssignTo(String caseAssignTo) {
		this.caseAssignTo = caseAssignTo;
	}
	public String getFaultCategory() {
		return faultCategory;
	}
	public void setFaultCategory(String faultCategory) {
		this.faultCategory = faultCategory;
	}
	public String getCaseDescription() {
		return caseDescription;
	}
	public void setCaseDescription(String caseDescription) {
		this.caseDescription = caseDescription;
	}
	public List<FaultDetail> getDataGrid() {
		return dataGrid;
	}
	public void setDataGrid(List<FaultDetail> dataGrid) {
		this.dataGrid = dataGrid;
	}
	public LocalDateTime getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(LocalDateTime createdOn) {
		this.createdOn = createdOn;
	}
	public LocalDateTime getDueDate() {
		return dueDate;
	}
	public void setDueDate(LocalDateTime dueDate) {
		this.dueDate = dueDate;
	}
	public LocalDateTime getEndDate() {
		return endDate;
	}
	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}
	public List<String> getAnalysisTeam() {
		return analysisTeam;
	}
	public void setAnalysisTeam(List<String> analysisTeam) {
		this.analysisTeam = analysisTeam;
	}
	public Integer getCaseStatus() {
		return caseStatus;
	}
	public void setCaseStatus(Integer caseStatus) {
		this.caseStatus = caseStatus;
	}
	public String getRecommendationsRadio() {
		return RecommendationsRadio;
	}
	public void setRecommendationsRadio(String recommendationsRadio) {
		RecommendationsRadio = recommendationsRadio;
	}
	public String getValueRealizationCategory() {
		return valueRealizationCategory;
	}
	public void setValueRealizationCategory(String valueRealizationCategory) {
		this.valueRealizationCategory = valueRealizationCategory;
	}
	public String getValueRealizationConclusion() {
		return valueRealizationConclusion;
	}
	public void setValueRealizationConclusion(String valueRealizationConclusion) {
		this.valueRealizationConclusion = valueRealizationConclusion;
	}
	public Double getTotalValueCaptured() {
		return totalValueCaptured;
	}
	public void setTotalValueCaptured(Double totalValueCaptured) {
		this.totalValueCaptured = totalValueCaptured;
	}
	public Double getProductionLoss() {
		return productionLoss;
	}
	public void setProductionLoss(Double productionLoss) {
		this.productionLoss = productionLoss;
	}
	public Double getManHoursCost() {
		return manHoursCost;
	}
	public void setManHoursCost(Double manHoursCost) {
		this.manHoursCost = manHoursCost;
	}
	public Double getSpareCost() {
		return spareCost;
	}
	public void setSpareCost(Double spareCost) {
		this.spareCost = spareCost;
	} 
}
