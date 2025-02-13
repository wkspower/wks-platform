package com.wks.caseengine.rest.db1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "EventEnrichments", schema = "dbo")
public class EventEnrichment {
	@Id
    @Column(name = "EventEnrichment_PK_ID")
    private Long eventEnrichmentPkId;

    @Column(name = "Event_PK_ID")
    private String eventPkId;

    @Column(name = "EnrichmentKey")
    private String enrichmentKey;

    @Column(name = "CreationDate")
    private String creationDate;

    @Column(name = "ModifiedDate")
    private String modifiedDate;

    @Column(name = "DisplayNameTemplate")
    private String displayNameTemplate;

    @Column(name = "DescriptionTemplate")
    private String descriptionTemplate;

    @Column(name = "Expression")
    private String expression;

    @Column(name = "FaultSeverity")
    private String faultSeverity;

    @Column(name = "MessageType")
    private String messageType;

    @Column(name = "AutoReset")
    private Boolean autoReset;

    @Column(name = "OnTimerIntervalMinutes")
    private Integer onTimerIntervalMinutes;

    @Column(name = "OffTimerIntervalMinutes")
    private Integer offTimerIntervalMinutes;

    @Column(name = "DestinationType")
    private String destinationType;

    @Column(name = "DestinationJSon")
    private String destinationJson;

    @Column(name = "TrendDetailJSonTemplate")
    private String trendDetailJsonTemplate;

    @Column(name = "ModifiedUser_PK_ID")
    private String modifiedUserPkId;

    @Column(name = "EventCause_PK_ID")
    private String eventCausePkId;

    @Column(name = "EventCategory_PK_ID")
    private String eventCategoryPkId;

	public Long getEventEnrichmentPkId() {
		return eventEnrichmentPkId;
	}

	public void setEventEnrichmentPkId(Long eventEnrichmentPkId) {
		this.eventEnrichmentPkId = eventEnrichmentPkId;
	}

	public String getEventPkId() {
		return eventPkId;
	}

	public void setEventPkId(String eventPkId) {
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

	public String getFaultSeverity() {
		return faultSeverity;
	}

	public void setFaultSeverity(String faultSeverity) {
		this.faultSeverity = faultSeverity;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public Boolean getAutoReset() {
		return autoReset;
	}

	public void setAutoReset(Boolean autoReset) {
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

	public String getDestinationType() {
		return destinationType;
	}

	public void setDestinationType(String destinationType) {
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

	public String getModifiedUserPkId() {
		return modifiedUserPkId;
	}

	public void setModifiedUserPkId(String modifiedUserPkId) {
		this.modifiedUserPkId = modifiedUserPkId;
	}

	public String getEventCausePkId() {
		return eventCausePkId;
	}

	public void setEventCausePkId(String eventCausePkId) {
		this.eventCausePkId = eventCausePkId;
	}

	public String getEventCategoryPkId() {
		return eventCategoryPkId;
	}

	public void setEventCategoryPkId(String eventCategoryPkId) {
		this.eventCategoryPkId = eventCategoryPkId;
	}
}
