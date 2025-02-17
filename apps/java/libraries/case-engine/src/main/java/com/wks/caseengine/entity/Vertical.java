package com.wks.caseengine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name ="Vertical")
public class Vertical {

	@Id
    @Column(name = "vertical_id", nullable = false, unique = true)
    private String verticalId;
	
    @Column(name = "vertical")
    private String vertical;

	public String getVerticalId() {
		return verticalId;
	}

	public void setVerticalId(String verticalId) {
		this.verticalId = verticalId;
	}

	public String getVertical() {
		return vertical;
	}

	public void setVertical(String vertical) {
		this.vertical = vertical;
	}
    
    

}
