package com.wks.caseengine.rest.entity;

import java.math.BigInteger;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "EventEnrichments")
public class EventEnrichment {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Event_Enrichment_PK_ID", nullable = false)
    private BigInteger eventEnrichmentPkId;

    @Column(name = "Event_PK_ID", nullable = false)
    private UUID eventPkId;

    @Column(name = "Enrichment_Key", columnDefinition = "nvarchar(MAX)")
    private String enrichmentKey;

    @Column(name = "Creation_Date", nullable = false)
    private String creationDate;

    @Column(name = "Modified_Date")
    private String modifiedDate;

    @Column(name = "Display_Name_Template", length = 500, nullable = false)
    private String displayNameTemplate;

    @Column(name = "Description_Template", columnDefinition = "nvarchar(MAX)")
    private String descriptionTemplate;

    @Column(name = "Expression", columnDefinition = "nvarchar(MAX)")
    private String expression;

    @Column(name = "Fault_Severity", nullable = false)
    private int faultSeverity;

    @Column(name = "Message_Type", nullable = false)
    private int messageType;

    @Column(name = "Auto_Reset", nullable = false)
    private boolean autoReset;

    @Column(name = "On_Timer_Interval_Minutes")
    private Integer onTimerIntervalMinutes;

    @Column(name = "Off_Timer_Interval_Minutes")
    private Integer offTimerIntervalMinutes;

    @Column(name = "Destination_Type", nullable = false)
    private int destinationType;

    @Column(name = "Destination_Json", columnDefinition = "nvarchar(MAX)")
    private String destinationJson;

    @Column(name = "Trend_Detail_Json_Template", columnDefinition = "nvarchar(MAX)")
    private String trendDetailJsonTemplate;

    @Column(name = "Modified_User_PK_ID")
    private UUID modifiedUserPkId;

    @Column(name = "Event_Cause_PK_ID")
    private BigInteger eventCausePkId;

    @Column(name = "Event_Category_PK_ID", nullable = false)
    private UUID eventCategoryPkId;
    
    @Transient
    @Column(name = "Display_Name")
    private String DisplayName;

	public BigInteger getEventEnrichmentPkId() {
		return eventEnrichmentPkId;
	}

	public void setEventEnrichmentPkId(BigInteger eventEnrichmentPkId) {
		this.eventEnrichmentPkId = eventEnrichmentPkId;
	}

	public UUID getEventPkId() {
		return eventPkId;
	}

	public void setEventPkId(UUID eventPkId) {
		this.eventPkId = eventPkId;
	}

	public String getEnrichmentKey() {
		return enrichmentKey;
	}

	public void setEnrichmentKey(String enrichmentKey) {
		this.enrichmentKey = enrichmentKey;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public String getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(String modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getDisplayNameTemplate() {
		return displayNameTemplate;
	}

	public void setDisplayNameTemplate(String displayNameTemplate) {
		this.displayNameTemplate = displayNameTemplate;
	}

	public String getDescriptionTemplate() {
		return descriptionTemplate;
	}

	public void setDescriptionTemplate(String descriptionTemplate) {
		this.descriptionTemplate = descriptionTemplate;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public int getFaultSeverity() {
		return faultSeverity;
	}

	public void setFaultSeverity(int faultSeverity) {
		this.faultSeverity = faultSeverity;
	}

	public int getMessageType() {
		return messageType;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	public boolean isAutoReset() {
		return autoReset;
	}

	public void setAutoReset(boolean autoReset) {
		this.autoReset = autoReset;
	}

	public Integer getOnTimerIntervalMinutes() {
		return onTimerIntervalMinutes;
	}

	public void setOnTimerIntervalMinutes(Integer onTimerIntervalMinutes) {
		this.onTimerIntervalMinutes = onTimerIntervalMinutes;
	}

	public Integer getOffTimerIntervalMinutes() {
		return offTimerIntervalMinutes;
	}

	public void setOffTimerIntervalMinutes(Integer offTimerIntervalMinutes) {
		this.offTimerIntervalMinutes = offTimerIntervalMinutes;
	}

	public int getDestinationType() {
		return destinationType;
	}

	public void setDestinationType(int destinationType) {
		this.destinationType = destinationType;
	}

	public String getDestinationJson() {
		return destinationJson;
	}

	public void setDestinationJson(String destinationJson) {
		this.destinationJson = destinationJson;
	}

	public String getTrendDetailJsonTemplate() {
		return trendDetailJsonTemplate;
	}

	public void setTrendDetailJsonTemplate(String trendDetailJsonTemplate) {
		this.trendDetailJsonTemplate = trendDetailJsonTemplate;
	}

	public UUID getModifiedUserPkId() {
		return modifiedUserPkId;
	}

	public void setModifiedUserPkId(UUID modifiedUserPkId) {
		this.modifiedUserPkId = modifiedUserPkId;
	}

	public BigInteger getEventCausePkId() {
		return eventCausePkId;
	}

	public void setEventCausePkId(BigInteger eventCausePkId) {
		this.eventCausePkId = eventCausePkId;
	}

	public UUID getEventCategoryPkId() {
		return eventCategoryPkId;
	}

	public void setEventCategoryPkId(UUID eventCategoryPkId) {
		this.eventCategoryPkId = eventCategoryPkId;
	}

	public String getDisplayName() {
		return DisplayName;
	}

	public void setDisplayName(String displayName) {
		DisplayName = displayName;
	}

	@Override
	public String toString() {
		return "EventEnrichment [eventEnrichmentPkId=" + eventEnrichmentPkId + ", eventPkId=" + eventPkId
				+ ", enrichmentKey=" + enrichmentKey + ", creationDate=" + creationDate + ", modifiedDate="
				+ modifiedDate + ", displayNameTemplate=" + displayNameTemplate + ", descriptionTemplate="
				+ descriptionTemplate + ", expression=" + expression + ", faultSeverity=" + faultSeverity
				+ ", messageType=" + messageType + ", autoReset=" + autoReset + ", onTimerIntervalMinutes="
				+ onTimerIntervalMinutes + ", offTimerIntervalMinutes=" + offTimerIntervalMinutes + ", destinationType="
				+ destinationType + ", destinationJson=" + destinationJson + ", trendDetailJsonTemplate="
				+ trendDetailJsonTemplate + ", modifiedUserPkId=" + modifiedUserPkId + ", eventCausePkId="
				+ eventCausePkId + ", eventCategoryPkId=" + eventCategoryPkId + ", DisplayName=" + DisplayName + "]";
	}
	
}
