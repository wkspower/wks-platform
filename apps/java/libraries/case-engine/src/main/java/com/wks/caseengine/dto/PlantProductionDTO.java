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
public class PlantProductionDTO {
	
	private String id;
	private int sno;
    private String item;
    private Double budget1;
    private Double actual1;
    private Double budget2;
    private Double actual2;
    private Double budget3;
    private Double actual3;
    private Double budget4;
    private String remark;

}
