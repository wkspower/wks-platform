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
public class PerformanceHighlightDTO {
	
	private String id;
    private String summary;
    private String siteId;
    private String aopYear;
    private String updatedBy;
    private Date updatedDate;
    private String saveStatus;
    private String errDescription;
}
