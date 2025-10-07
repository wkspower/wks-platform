package com.wks.caseengine.dto;

import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Configuration
public class DecokingActivitiesDTO {
	
	private String id;
	private String days;
	private String MonthNameDropdown;
	private String remarks;
	private String normParameterId;
	private Boolean isEditable;
	private Integer aopMonth;

}
