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
public class NextYearConfigurationDTO {
	
	private String startDate;
    private String hTen;
    private String hEleven;
    private String hTwelve;
    private String hThirteen;
    private String hFourteen;
    private String demo;
    private String aopYear;
    private String plantId;
}
