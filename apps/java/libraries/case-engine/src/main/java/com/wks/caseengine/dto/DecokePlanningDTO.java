package com.wks.caseengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import org.springframework.context.annotation.Configuration;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Configuration
public class DecokePlanningDTO {

	private UUID id;
    private String monthName;
    private Double ibr;
    private Double mnt;
    private Double shutdown;
    private Double sad;
    private Double totalSAD;
    private Double bbu;
    private Double demoHSS;
    private Double demoBBU;
    private Double demoSAD;
    private Double fourFD;
    private Double fourF;
    private Double fiveF;
    private Double total;
    private Double fourFHours;
    private String aopYear;
    private UUID plantId;
    private String remarks;
    private Double slowdown;
    private Double bbd;
    private Double demoSD;
    private Double numberOfDays;
    private Double coilReplacement;	   
    private String saveStatus;
    private String errDescription;
}
