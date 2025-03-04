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
    private Float april;
    private Float may;
    private Float june;
    private Float july;
    private Float august;
    private Float september;
    private Float october;
    private Float november;
    private Float december;
    private Float january;
    private Float february;
    private Float march;
    private String plantFKId;
    private String year;
    private String normParametersFKId;
    private String bDNormParametersFKId;
    private Integer displayOrder;

}
