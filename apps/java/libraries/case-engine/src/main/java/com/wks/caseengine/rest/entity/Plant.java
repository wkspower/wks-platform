package com.wks.caseengine.rest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name ="Plant")
public class Plant {
	
	@Id
    @Column(name = "plant_id", nullable = false, unique = true)
    private String plantId;
	
    @Column(name = "plant_name")
    private String plantName;

    @Column(name = "site_id")
    private String siteId;

	public String getPlantId() {
		return plantId;
	}

	public void setPlantId(String plantId) {
		this.plantId = plantId;
	}

	public String getPlantName() {
		return plantName;
	}

	public void setPlantName(String plantName) {
		this.plantName = plantName;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}
}
