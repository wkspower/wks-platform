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
public class PlantTeamDTO {
	
	private String id;
    private String functions;
    private String jobRole;
    private String name;
    private Integer age;
    private Integer teamSize;
    private String plantId;
    private String aopYear;
    private String remark;
    private String saveStatus;
    private String errDescription;
}
