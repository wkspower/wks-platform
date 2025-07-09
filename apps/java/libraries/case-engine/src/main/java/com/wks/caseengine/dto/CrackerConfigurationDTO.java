package com.wks.caseengine.dto;

import java.util.Date;
import java.util.UUID;

import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CrackerConfigurationDTO {
	
	private UUID id;
    private String name;
    private String displayName;
   
    private Integer postCrDays;
    private Integer preCrDays;
    private Boolean isCr;
    private UUID plantFkId;
    private String aopYear;
    private String remarks;
    private Integer displaySeq;
    

    private Date ibrStartDate;


    private Date ibrEndDate;


    private Date taStartDate;


    private Date taEndDate;


    private Date shutDownStartDate;


    private Date shutDownEndDate;



}
