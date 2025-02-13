package com.wks.caseengine.rest.model;

public class EventsModel {
	private String eventName;
    private String eventPkId;
    private String parentPkId;
    private String operationPkId;
    private String eventsClusteredId;
    private String equipmentTypePkId;
    private String eventHandlerType;
    
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public String getEventPkId() {
		return eventPkId;
	}
	public void setEventPkId(String eventPkId) {
		this.eventPkId = eventPkId;
	}
	public String getParentPkId() {
		return parentPkId;
	}
	public void setParentPkId(String parentPkId) {
		this.parentPkId = parentPkId;
	}
	public String getOperationPkId() {
		return operationPkId;
	}
	public void setOperationPkId(String operationPkId) {
		this.operationPkId = operationPkId;
	}
	public String getEventsClusteredId() {
		return eventsClusteredId;
	}
	public void setEventsClusteredId(String eventsClusteredId) {
		this.eventsClusteredId = eventsClusteredId;
	}
	public String getEquipmentTypePkId() {
		return equipmentTypePkId;
	}
	public void setEquipmentTypePkId(String equipmentTypePkId) {
		this.equipmentTypePkId = equipmentTypePkId;
	}
	public String getEventHandlerType() {
		return eventHandlerType;
	}
	public void setEventHandlerType(String eventHandlerType) {
		this.eventHandlerType = eventHandlerType;
	}
    
    
}
