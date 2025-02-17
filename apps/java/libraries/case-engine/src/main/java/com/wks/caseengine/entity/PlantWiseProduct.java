package com.wks.caseengine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
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
}
