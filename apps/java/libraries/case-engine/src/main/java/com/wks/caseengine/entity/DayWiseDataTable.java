package com.wks.caseengine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name ="DayWiseDataTbl")
public class DayWiseDataTable {
	
	@Id
    @Column(name = "data_id", nullable = false, unique = true)
    private String dataId;
	
    @Column(name = "product_id")
    private String productId;

    @Column(name = "day")
    private String day;

    @Column(name = "value")
    private String value;

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
    
    

}
