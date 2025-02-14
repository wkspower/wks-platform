package com.wks.caseengine.dto.product;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ProductMonthWiseDataDTO {
	
	private Long id;
	private Long productId;
	private Long plantId;
	private String type;
	private String month;
	private Long year;
	private Long monthValue;
	
	public ProductMonthWiseDataDTO() {
	    // Default constructor
	}

}
