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
public class AOPMCCalculatedDataDTO {
	 	private String id;
	    private String site;
	    private String plant;
	    private String material;
	    private Double april;
	    private Double may;
	    private Double june;
	    private Double july;
	    private Double august;
	    private Double september;
	    private Double october;
	    private Double november;
	    private Double december;
	    private Double january;
	    private Double february;
	    private Double march;

}
