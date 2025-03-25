package com.wks.caseengine.dto;

import java.util.UUID;

import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@JsonInclude(Include.ALWAYS)
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
    private String siteFKId;
    private String verticalFKId;

}
