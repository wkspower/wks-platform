package com.wks.caseengine.dto;

import java.util.UUID;

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
public class BusinessDemandDataDTO {
	
	private String id;
    private String remark;
    private String normParameterId;
    private Float jan;
    private Float feb;
    private Float march;
    private Float april;
    private Float may;
    private Float june;
    private Float july;
    private Float aug;
    private Float sep;
    private Float oct;
    private Float nov;
    private Float dec;
    private String year;
    private String plantId;
    private Float avgTph;
    private String productName;
    private Integer displayOrder;
    private String normParameterTypeId;
    private String normParameterTypeDisplayName;
    private String normParameterTypeName;

}
