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
public class ConfigurationVersionDTO {
	
	private String attributeValue;
	private String normParameterId;
	private String year; 
	private String attributeValueVersion;

}
