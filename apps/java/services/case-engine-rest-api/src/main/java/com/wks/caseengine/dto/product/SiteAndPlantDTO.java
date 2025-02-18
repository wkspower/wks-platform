package com.wks.caseengine.dto.product;

import java.util.UUID;

public class SiteAndPlantDTO {
	
	private UUID siteId;
	private String siteName;
	private String displayName;
	private UUID plantId;
	private String plantName;
	private String plantDisplayName;
	public UUID getSiteId() {
		return siteId;
	}
	public void setSiteId(UUID siteId) {
		this.siteId = siteId;
	}
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public UUID getPlantId() {
		return plantId;
	}
	public void setPlantId(UUID plantId) {
		this.plantId = plantId;
	}
	public String getPlantName() {
		return plantName;
	}
	public void setPlantName(String plantName) {
		this.plantName = plantName;
	}
	public String getPlantDisplayName() {
		return plantDisplayName;
	}
	public void setPlantDisplayName(String plantDisplayName) {
		this.plantDisplayName = plantDisplayName;
	}
	
	
	

}
