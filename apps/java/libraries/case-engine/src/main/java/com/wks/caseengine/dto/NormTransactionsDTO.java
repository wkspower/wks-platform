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
public class NormTransactionsDTO {
	
	private String id;
	private String plantId;
	private String aopYear;
	private String normParameterDisplayName;
	private Integer aopMonth;
	private String attributeValue;
	private String remark;
	private Integer version;
	private String createdBy;
	private String updatedBy;
	private String createdDateTime;
	private String updatedDateTime;
	private String mcuNormValueFKId;
	private String screen;
	private String aopMonthName;
	
	
}
