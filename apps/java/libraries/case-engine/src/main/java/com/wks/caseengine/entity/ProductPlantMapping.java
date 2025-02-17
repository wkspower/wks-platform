package com.wks.caseengine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name ="Product_Plant_Mapping")
public class ProductPlantMapping {
	
	@Id
    @Column(name = "product_id", nullable = false, unique = true)
    private String productId;
	
    @Column(name = "plant_id")
    private String plantId;

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getPlantId() {
		return plantId;
	}

	public void setPlantId(String plantId) {
		this.plantId = plantId;
	}
    
    


}
