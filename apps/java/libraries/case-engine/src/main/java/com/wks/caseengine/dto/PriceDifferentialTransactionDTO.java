package com.wks.caseengine.dto;

import java.util.Date;

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
public class PriceDifferentialTransactionDTO {
	
	private String id;
    private String displayName;
    private Double percentage;
    private String plantId;
    private String aopYear;
    private String remark;
    private String updatedBy;
    private Date modifiedOn;
    private String saveStatus;
    private String errDescription;
    private String normParameterTypeName;
    private String materialId;
}
