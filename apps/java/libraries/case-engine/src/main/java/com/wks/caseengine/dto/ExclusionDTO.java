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
public class ExclusionDTO {
	
	private String id;
    private Date startDate;
    private Date endDate;
    private String plantId;
    private String aopYear;
    private String remark;
    private String saveStatus;
    private String errDescription;
}
