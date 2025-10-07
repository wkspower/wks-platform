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
public class NextYearEntryDTO {
	
	private String date;
    private String hTenProposed;
    private String hElevenProposed;
    private String hTwelveProposed;
    private String hThirteenProposed;
    private String hFourteenProposed;
    private String demo;
    private String aopYear;
    private String plantId;
    private String month;
   
    


}
