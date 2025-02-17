package com.wks.caseengine.rest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name ="PlantWiseProduct")
public class PlantWiseProduct {

	@Id
    @Column(name = "ID", nullable = false, unique = true)
    private String id;
	
    @Column(name = "Name")
    private String name;
    
    @Column(name = "Site")
    private String site;
	
    @Column(name = "Plant")
    private String plant;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getPlant() {
		return plant;
	}

	public void setPlant(String plant) {
		this.plant = plant;
	}
}
