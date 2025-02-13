package com.wks.caseengine.rest.model;

public class EventCategoryModel {
	private String name;
    private String description;
    private String eventCategoryId;
    private String eventCategoryPkId;
    private String eventTypePkId;
    private String eventCategoryClusteredId;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getEventCategoryId() {
		return eventCategoryId;
	}
	public void setEventCategoryId(String eventCategoryId) {
		this.eventCategoryId = eventCategoryId;
	}
	public String getEventCategoryPkId() {
		return eventCategoryPkId;
	}
	public void setEventCategoryPkId(String eventCategoryPkId) {
		this.eventCategoryPkId = eventCategoryPkId;
	}
	public String getEventTypePkId() {
		return eventTypePkId;
	}
	public void setEventTypePkId(String eventTypePkId) {
		this.eventTypePkId = eventTypePkId;
	}
	public String getEventCategoryClusteredId() {
		return eventCategoryClusteredId;
	}
	public void setEventCategoryClusteredId(String eventCategoryClusteredId) {
		this.eventCategoryClusteredId = eventCategoryClusteredId;
	}
}
