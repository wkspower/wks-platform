package com.wks.caseengine.rest.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "FaultHistory")
public class FaultHistory {

	@Id
    @Column(name = "fault_history_pk_id", nullable = false)
    private UUID faultHistoryPkId;

    @Column(name = "accepted_user_pk_id")
    private UUID acceptedUserPkId;

    @Column(name = "autoreset", nullable = false)
    private boolean autoreset;

    @Column(name = "causes", columnDefinition = "nvarchar(MAX)")
    private String causes;

    @Column(name = "close_out_pk_id")
    private Long closeOutPkId;

    @Column(name = "close_time")
    private String closeTime;

    @Column(name = "consequences", columnDefinition = "nvarchar(MAX)")
    private String consequences;

    @Column(name = "created_user_pk_id")
    private UUID createdUserPkId;

    @Column(name = "description", columnDefinition = "nvarchar(MAX)")
    private String description;

    @Column(name = "destination_type", nullable = false)
    private int destinationType;

    @Column(name = "end_time")
    private String endTime;

    @Column(name = "equipment_pk_id")
    private UUID equipmentPkId;

    @Column(name = "event_category_pk_id", nullable = false)
    private UUID eventCategoryPkId;

    @Column(name = "event_enrichment_pk_id")
    private Long eventEnrichmentPkId;

    @Column(name = "event_status", nullable = false)
    private int eventStatus;

    @Column(name = "expression", columnDefinition = "nvarchar(MAX)")
    private String expression;

    @Column(name = "fault_display_name", columnDefinition = "nvarchar(500)")
    private String faultDisplayName;

    @Column(name = "fault_history_clustered_id")
    private int faultHistoryClusteredId;

    @Column(name = "fault_mode")
    private int faultMode;

    @Column(name = "fault_name", columnDefinition = "nvarchar(200)")
    private String faultName;

    @Column(name = "fault_severity")
    private float faultSeverity;

    @Column(name = "fault_state")
    private int faultState;

    @Column(name = "fault_visualisation_data", columnDefinition = "nvarchar(MAX)")
    private String faultVisualisationData;

    @Column(name = "input_data", columnDefinition = "nvarchar(MAX)")
    private String inputData;

    @Column(name = "lock_expiry_time")
    private String lockExpiryTime;

    @Column(name = "message_type")
    private int messageType;

    @Column(name = "mode_time")
    private String modeTime;

    @Column(name = "recommendation", columnDefinition = "nvarchar(MAX)")
    private String recommendation;

    @Column(name = "rejection_reason_pk_id")
    private UUID rejectionReasonPkId;

    @Column(name = "start_time")
    private String startTime;

    @Column(name = "trend_detail_json", columnDefinition = "nvarchar(MAX)")
    private String trendDetailJson;

	public UUID getFaultHistoryPkId() {
		return faultHistoryPkId;
	}

	public void setFaultHistoryPkId(UUID faultHistoryPkId) {
		this.faultHistoryPkId = faultHistoryPkId;
	}

	public UUID getAcceptedUserPkId() {
		return acceptedUserPkId;
	}

	public void setAcceptedUserPkId(UUID acceptedUserPkId) {
		this.acceptedUserPkId = acceptedUserPkId;
	}

	public boolean isAutoreset() {
		return autoreset;
	}

	public void setAutoreset(boolean autoreset) {
		this.autoreset = autoreset;
	}

	public String getCauses() {
		return causes;
	}

	public void setCauses(String causes) {
		this.causes = causes;
	}

	public Long getCloseOutPkId() {
		return closeOutPkId;
	}

	public void setCloseOutPkId(Long closeOutPkId) {
		this.closeOutPkId = closeOutPkId;
	}

	public String getCloseTime() {
		return closeTime;
	}

	public void setCloseTime(String closeTime) {
		this.closeTime = closeTime;
	}

	public String getConsequences() {
		return consequences;
	}

	public void setConsequences(String consequences) {
		this.consequences = consequences;
	}

	public UUID getCreatedUserPkId() {
		return createdUserPkId;
	}

	public void setCreatedUserPkId(UUID createdUserPkId) {
		this.createdUserPkId = createdUserPkId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getDestinationType() {
		return destinationType;
	}

	public void setDestinationType(int destinationType) {
		this.destinationType = destinationType;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public UUID getEquipmentPkId() {
		return equipmentPkId;
	}

	public void setEquipmentPkId(UUID equipmentPkId) {
		this.equipmentPkId = equipmentPkId;
	}

	public UUID getEventCategoryPkId() {
		return eventCategoryPkId;
	}

	public void setEventCategoryPkId(UUID eventCategoryPkId) {
		this.eventCategoryPkId = eventCategoryPkId;
	}

	public Long getEventEnrichmentPkId() {
		return eventEnrichmentPkId;
	}

	public void setEventEnrichmentPkId(Long eventEnrichmentPkId) {
		this.eventEnrichmentPkId = eventEnrichmentPkId;
	}

	public int getEventStatus() {
		return eventStatus;
	}

	public void setEventStatus(int eventStatus) {
		this.eventStatus = eventStatus;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getFaultDisplayName() {
		return faultDisplayName;
	}

	public void setFaultDisplayName(String faultDisplayName) {
		this.faultDisplayName = faultDisplayName;
	}

	public int getFaultHistoryClusteredId() {
		return faultHistoryClusteredId;
	}

	public void setFaultHistoryClusteredId(int faultHistoryClusteredId) {
		this.faultHistoryClusteredId = faultHistoryClusteredId;
	}

	public int getFaultMode() {
		return faultMode;
	}

	public void setFaultMode(int faultMode) {
		this.faultMode = faultMode;
	}

	public String getFaultName() {
		return faultName;
	}

	public void setFaultName(String faultName) {
		this.faultName = faultName;
	}

	public float getFaultSeverity() {
		return faultSeverity;
	}

	public void setFaultSeverity(float faultSeverity) {
		this.faultSeverity = faultSeverity;
	}

	public int getFaultState() {
		return faultState;
	}

	public void setFaultState(int faultState) {
		this.faultState = faultState;
	}

	public String getFaultVisualisationData() {
		return faultVisualisationData;
	}

	public void setFaultVisualisationData(String faultVisualisationData) {
		this.faultVisualisationData = faultVisualisationData;
	}

	public String getInputData() {
		return inputData;
	}

	public void setInputData(String inputData) {
		this.inputData = inputData;
	}

	public String getLockExpiryTime() {
		return lockExpiryTime;
	}

	public void setLockExpiryTime(String lockExpiryTime) {
		this.lockExpiryTime = lockExpiryTime;
	}

	public int getMessageType() {
		return messageType;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	public String getModeTime() {
		return modeTime;
	}

	public void setModeTime(String modeTime) {
		this.modeTime = modeTime;
	}

	public String getRecommendation() {
		return recommendation;
	}

	public void setRecommendation(String recommendation) {
		this.recommendation = recommendation;
	}

	public UUID getRejectionReasonPkId() {
		return rejectionReasonPkId;
	}

	public void setRejectionReasonPkId(UUID rejectionReasonPkId) {
		this.rejectionReasonPkId = rejectionReasonPkId;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getTrendDetailJson() {
		return trendDetailJson;
	}

	public void setTrendDetailJson(String trendDetailJson) {
		this.trendDetailJson = trendDetailJson;
	}
    
    
}

