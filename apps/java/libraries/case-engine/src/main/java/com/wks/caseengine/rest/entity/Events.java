package com.wks.caseengine.rest.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "events")
public class Events {

    @Id
    @Column(name = "Event_PK_ID", nullable = false)
    private UUID eventPkId;

    @Column(name = "Event_Name", columnDefinition = "nvarchar(250)")
    private String eventName;

    @Column(name = "Parent_PK_ID", nullable = false)
    private UUID parentPkId;

    @Column(name = "Operation_PK_ID", nullable = false)
    private UUID operationPkId;

    @Column(name = "Events_Clustered_Id", nullable = false)
    private int eventsClusteredId;

    @Column(name = "Equipment_Type_PK_ID", nullable = false)
    private UUID equipmentTypePkId;

    @Column(name = "Event_Handler_Type", nullable = false)
    private int eventHandlerType;

	public UUID getEventPkId() {
		return eventPkId;
	}

	public void setEventPkId(UUID eventPkId) {
		this.eventPkId = eventPkId;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public UUID getParentPkId() {
		return parentPkId;
	}

	public void setParentPkId(UUID parentPkId) {
		this.parentPkId = parentPkId;
	}

	public UUID getOperationPkId() {
		return operationPkId;
	}

	public void setOperationPkId(UUID operationPkId) {
		this.operationPkId = operationPkId;
	}

	public int getEventsClusteredId() {
		return eventsClusteredId;
	}

	public void setEventsClusteredId(int eventsClusteredId) {
		this.eventsClusteredId = eventsClusteredId;
	}

	public UUID getEquipmentTypePkId() {
		return equipmentTypePkId;
	}

	public void setEquipmentTypePkId(UUID equipmentTypePkId) {
		this.equipmentTypePkId = equipmentTypePkId;
	}

	public int getEventHandlerType() {
		return eventHandlerType;
	}

	public void setEventHandlerType(int eventHandlerType) {
		this.eventHandlerType = eventHandlerType;
	}
}
