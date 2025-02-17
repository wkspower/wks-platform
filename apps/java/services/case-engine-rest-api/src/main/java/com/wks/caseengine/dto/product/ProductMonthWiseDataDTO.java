package com.wks.caseengine.dto.product;

public class ProductMonthWiseDataDTO {
	
	private Long id;
	private Long productId;
	private Long plantId;
	private String type;
	private String month;
	private Long year;
	private Long monthValue;
	
	
	
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



	public Long getPlantId() {
		return plantId;
	}



	public void setPlantId(Long plantId) {
		this.plantId = plantId;
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



	public ProductMonthWiseDataDTO() {
	    // Default constructor
	}

}
