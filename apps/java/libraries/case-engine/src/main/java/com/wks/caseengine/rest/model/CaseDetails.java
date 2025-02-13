package com.wks.caseengine.rest.model;

import java.time.LocalDateTime;
import java.util.List;
import com.wks.caseengine.rest.model.FaultDetail;

public class CaseDetails {
	private String caseNo;
    private String caseTitle;
    private String caseDescription;
    private List<FaultDetail> faultDetails;
    private LocalDateTime createdOn;
    private LocalDateTime dueDate;
    private LocalDateTime endDate;
    private List<String> analysisTeam;
    private int caseStatus;
    private String recommendationsRadio;
    private String valueRealizationCategory;
    private String valueRealizationConclusion;
    private double totalValueCaptured;
    private double productionLoss;
    private double manHoursCost;
    private double spareCost;
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
	public String getCaseDescription() {
		return caseDescription;
	}
	public void setCaseDescription(String caseDescription) {
		this.caseDescription = caseDescription;
	}
	public List<FaultDetail> getFaultDetails() {
		return faultDetails;
	}
	public void setFaultDetails(List<FaultDetail> faultDetails) {
		this.faultDetails = faultDetails;
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
	public int getCaseStatus() {
		return caseStatus;
	}
	public void setCaseStatus(int caseStatus) {
		this.caseStatus = caseStatus;
	}
	public String getRecommendationsRadio() {
		return recommendationsRadio;
	}
	public void setRecommendationsRadio(String recommendationsRadio) {
		this.recommendationsRadio = recommendationsRadio;
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
	public double getTotalValueCaptured() {
		return totalValueCaptured;
	}
	public void setTotalValueCaptured(double totalValueCaptured) {
		this.totalValueCaptured = totalValueCaptured;
	}
	public double getProductionLoss() {
		return productionLoss;
	}
	public void setProductionLoss(double productionLoss) {
		this.productionLoss = productionLoss;
	}
	public double getManHoursCost() {
		return manHoursCost;
	}
	public void setManHoursCost(double manHoursCost) {
		this.manHoursCost = manHoursCost;
	}
	public double getSpareCost() {
		return spareCost;
	}
	public void setSpareCost(double spareCost) {
		this.spareCost = spareCost;
	}
    
    
}
