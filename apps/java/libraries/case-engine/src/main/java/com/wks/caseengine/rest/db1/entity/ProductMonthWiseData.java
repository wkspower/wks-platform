package com.wks.caseengine.rest.db1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
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
	
	public ProductMonthWiseData() {
	    // Default constructor
	}
}
