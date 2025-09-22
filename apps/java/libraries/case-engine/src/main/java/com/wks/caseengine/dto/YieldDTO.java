package com.wks.caseengine.dto;

import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class YieldDTO {
	
	
	private String saveStatus;
	private String errDescription;
	private String particulars;
	private Double fiveFC2C3;
	private Double fiveFPropane;
	private Double fiveFEthane;
	private Double fourFC2C3;
	private Double fourFPropane;
	private Double fourFEthane;
	private Double fourFDC2C3;
	private Double fourFDPropane;
	private Double fourFDEthane;
	
	

}
