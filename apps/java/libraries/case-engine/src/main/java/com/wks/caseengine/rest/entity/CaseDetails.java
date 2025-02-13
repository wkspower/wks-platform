package com.wks.caseengine.rest.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "CaseDetails")
public class CaseDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;  // [ID]

    private String board; // [board]
    private String createdBy; // [createdBy]
    private String assignedTo; // [assignedTo]
    private Integer caseId; // [caseId]
    private String caseNbr; // [caseNbr]
    private String swimlane; // [swimlane]
    private String title; // [title]
    private String description; // [description]
    private String status; // [Status]

    @Temporal(TemporalType.TIMESTAMP)
    private Date startedAt; // [startedAt]

    @Temporal(TemporalType.TIMESTAMP)
    private Date closedAt; // [closedAt]

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // [createdAt]

    @Temporal(TemporalType.TIMESTAMP)
    private Date dueAt; // [dueAt]

    private String caseCategory; // [caseCategory]
    private String caseImpact; // [caseImpact]
    private String caseLikelihood; // [caseLikelihood]

    private Double impactExpectedSavings; // [impactExpectedSavings]
    private Double impactImplementationCost; // [impactImplementationCost]
    private Double impactProduction; // [impactProduction]
    private Double impactEfforts; // [impactEfforts]

    private String trackingSystem; // [trackingSystem]
    private String trackingNbr; // [trackingNbr]
    private String possibleCause; // [possibleCause]
    private String recommendedAction; // [recommendedAction]
    private String justification; // [justification]

    private Integer uasCaseId; // [uasCaseId]

    @Temporal(TemporalType.TIMESTAMP)
    private Date expectedValueOn; // [expectedValueOn]

    private String assetIds; // [assetIds]
    private String mainAssetIds; // [mainAssetIds]
    private String eventDisplayTexts; // [eventDisplayTexts]

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Integer getCaseId() {
        return caseId;
    }

    public void setCaseId(Integer caseId) {
        this.caseId = caseId;
    }

    public String getCaseNbr() {
        return caseNbr;
    }

    public void setCaseNbr(String caseNbr) {
        this.caseNbr = caseNbr;
    }

    public String getSwimlane() {
        return swimlane;
    }

    public void setSwimlane(String swimlane) {
        this.swimlane = swimlane;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Date closedAt) {
        this.closedAt = closedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getDueAt() {
        return dueAt;
    }

    public void setDueAt(Date dueAt) {
        this.dueAt = dueAt;
    }

    public String getCaseCategory() {
        return caseCategory;
    }

    public void setCaseCategory(String caseCategory) {
        this.caseCategory = caseCategory;
    }

    public String getCaseImpact() {
        return caseImpact;
    }

    public void setCaseImpact(String caseImpact) {
        this.caseImpact = caseImpact;
    }

    public String getCaseLikelihood() {
        return caseLikelihood;
    }

    public void setCaseLikelihood(String caseLikelihood) {
        this.caseLikelihood = caseLikelihood;
    }

    public Double getImpactExpectedSavings() {
        return impactExpectedSavings;
    }

    public void setImpactExpectedSavings(Double impactExpectedSavings) {
        this.impactExpectedSavings = impactExpectedSavings;
    }

    public Double getImpactImplementationCost() {
        return impactImplementationCost;
    }

    public void setImpactImplementationCost(Double impactImplementationCost) {
        this.impactImplementationCost = impactImplementationCost;
    }

    public Double getImpactProduction() {
        return impactProduction;
    }

    public void setImpactProduction(Double impactProduction) {
        this.impactProduction = impactProduction;
    }

    public Double getImpactEfforts() {
        return impactEfforts;
    }

    public void setImpactEfforts(Double impactEfforts) {
        this.impactEfforts = impactEfforts;
    }

    public String getTrackingSystem() {
        return trackingSystem;
    }

    public void setTrackingSystem(String trackingSystem) {
        this.trackingSystem = trackingSystem;
    }

    public String getTrackingNbr() {
        return trackingNbr;
    }

    public void setTrackingNbr(String trackingNbr) {
        this.trackingNbr = trackingNbr;
    }

    public String getPossibleCause() {
        return possibleCause;
    }

    public void setPossibleCause(String possibleCause) {
        this.possibleCause = possibleCause;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public Integer getUasCaseId() {
        return uasCaseId;
    }

    public void setUasCaseId(Integer uasCaseId) {
        this.uasCaseId = uasCaseId;
    }

    public Date getExpectedValueOn() {
        return expectedValueOn;
    }

    public void setExpectedValueOn(Date expectedValueOn) {
        this.expectedValueOn = expectedValueOn;
    }

    public String getAssetIds() {
        return assetIds;
    }

    public void setAssetIds(String assetIds) {
        this.assetIds = assetIds;
    }

    public String getMainAssetIds() {
        return mainAssetIds;
    }

    public void setMainAssetIds(String mainAssetIds) {
        this.mainAssetIds = mainAssetIds;
    }

    public String getEventDisplayTexts() {
        return eventDisplayTexts;
    }

    public void setEventDisplayTexts(String eventDisplayTexts) {
        this.eventDisplayTexts = eventDisplayTexts;
    }
}
