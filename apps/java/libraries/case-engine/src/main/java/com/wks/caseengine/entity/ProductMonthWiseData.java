package com.wks.caseengine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "product_month_plant_wise_data", schema = "dbo")
public class ProductMonthWiseData {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="product_id")
    private Long productId;
	
	@Column(name="type")
	private String type;
	
	@Column(name="month")
	private String month;
	
	@Column(name="year")
	private Long year;
	
	@Column(name="month_value")
	private Long monthValue;
	
	@Column(name="plant_id")
	private Long plantId;
	
	
	
	public Long getId() {
		return id;
	}



	public void setId(Long id) {
		this.id = id;
	}



	public Long getProductId() {
		return productId;
	}



	public void setProductId(Long productId) {
		this.productId = productId;
	}



	public String getType() {
		return type;
	}



	public void setType(String type) {
		this.type = type;
	}



	public String getMonth() {
		return month;
	}



	public void setMonth(String month) {
		this.month = month;
	}



	public Long getYear() {
		return year;
	}



	public void setYear(Long year) {
		this.year = year;
	}



	public Long getMonthValue() {
		return monthValue;
	}



	public void setMonthValue(Long monthValue) {
		this.monthValue = monthValue;
	}



	public Long getPlantId() {
		return plantId;
	}



	public void setPlantId(Long plantId) {
		this.plantId = plantId;
	}



	public ProductMonthWiseData() {
	    // Default constructor
	}
}
