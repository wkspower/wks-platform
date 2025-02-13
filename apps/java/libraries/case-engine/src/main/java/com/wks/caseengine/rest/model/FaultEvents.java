package com.wks.caseengine.rest.model;

public class FaultEvents {
	private EventEnrichmentModel eventEnrichment;
	private EventsModel events;
	private EventCategoryModel eventCategory;
	private String AssetDisplayName;
	private String assetName;
	public EventEnrichmentModel getEventEnrichment() {
		return eventEnrichment;
	}
	public void setEventEnrichment(EventEnrichmentModel eventEnrichment) {
		this.eventEnrichment = eventEnrichment;
	}
	public EventsModel getEvents() {
		return events;
	}
	public void setEvents(EventsModel events) {
		this.events = events;
	}
	public EventCategoryModel getEventCategory() {
		return eventCategory;
	}
	public void setEventCategory(EventCategoryModel eventCategory) {
		this.eventCategory = eventCategory;
	}
	public String getAssetDisplayName() {
		return AssetDisplayName;
	}
	public void setAssetDisplayName(String assetDisplayName) {
		AssetDisplayName = assetDisplayName;
	}
	public String getAssetName() {
		return assetName;
	}
	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}
}
