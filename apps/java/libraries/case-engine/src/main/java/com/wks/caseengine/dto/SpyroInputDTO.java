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
public class SpyroInputDTO {
	
	private String VerticalFKId;
    private String PlantFKId;
    private String NormParameterFKID;
    private String Particulars;
    private String NormParameterTypeName;
    private String NormParameterTypeFKID;
    private String Type;
    private String UOM;
    private String AuditYear;
    private String Remarks;

    private Double Jan;
    private Double Feb;
    private Double Mar;
    private Double Apr;
    private Double May;
    private Double Jun;
    private Double Jul;
    private Double Aug;
    private Double Sep;
    private Double Oct;
    private Double Nov;
    private Double Dec;

}
