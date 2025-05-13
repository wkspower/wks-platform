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
public class ConfigurationAccessMatrixDTO {

	private String id;
	private String verticalId;
	private String siteId;
	private String plantId;
	private String configurationTabs;

}
