package com.wks.caseengine.rest.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "EventCategories")
public class EventCategory {

    @Id
    @Column(name = "Event_Category_PK_ID", nullable = false)
    private UUID eventCategoryPKId;

    @Column(name = "Name")
    private String name;

    @Column(name = "Description")
    private String description;

    @Column(name = "Event_Category_Id")
    private Integer eventCategoryID;

    @Column(name = "Event_Type_PK_ID")
    private UUID eventTypeId;

    @Column(name = "Event_Category_Clustered_ID")
    private Long eventCategoryClusteredId;

	public UUID getEventCategoryPKId() {
		return eventCategoryPKId;
	}

	public void setEventCategoryPKId(UUID eventCategoryPKId) {
		this.eventCategoryPKId = eventCategoryPKId;
	}

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
	
	public Integer getEventCategoryID() {
		return eventCategoryID;
	}

	public void setEventCategoryID(Integer eventCategoryID) {
		this.eventCategoryID = eventCategoryID;
	}

	public UUID getEventTypeId() {
		return eventTypeId;
	}

	public void setEventTypeId(UUID eventTypeId) {
		this.eventTypeId = eventTypeId;
	}

	public Long getEventCategoryClusteredId() {
		return eventCategoryClusteredId;
	}

	public void setEventCategoryClusteredId(Long eventCategoryClusteredId) {
		this.eventCategoryClusteredId = eventCategoryClusteredId;
	}
}
