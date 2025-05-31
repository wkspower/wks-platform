package com.wks.caseengine.dto;

import lombok.*;

import java.util.UUID;

import org.springframework.context.annotation.Configuration;

import com.wks.caseengine.entity.MaintenanceCalculatedData;
//@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MaintenanceCalculatedDataDTO {
    private String id;
    private Integer runningHoursInMonth;
    private Double shoutdownHrs;
    private Integer nonShoutdownHrs;
    private Double eoeAvgSlowdownLoadPVT;
    private Double eoAvgSlowdownLoadPVT; 
    private Double eoeSlowdownLoadReduction;
    private Double eoSlowdownLoadReduction;
    private Integer eoeEffectiveOperatingHrs;
    private Integer eoEffectiveOperatingHrs;
    private Integer monthNo;
    private String aopYear;
    private String plantFKId;
    public MaintenanceCalculatedDataDTO(MaintenanceCalculatedData entity) {
        this.id = entity.getId()!= null ? entity.getId().toString() : null;
        this.runningHoursInMonth = entity.getRunningHoursInMonth();
        this.shoutdownHrs = entity.getShoutdownHrs();
        this.nonShoutdownHrs = entity.getNonShoutdownHrs();
        this.eoeAvgSlowdownLoadPVT = entity.getEoeAvgSlowdownLoadPVT();
        this.eoAvgSlowdownLoadPVT = entity.getEoAvgSlowdownLoadPVT(); 
        this.eoeSlowdownLoadReduction = entity.getEoeSlowdownLoadReduction();
        this.eoSlowdownLoadReduction = entity.getEoSlowdownLoadReduction();
        this.eoeEffectiveOperatingHrs = entity.getEoeEffectiveOperatingHrs();
        this.eoEffectiveOperatingHrs = entity.getEoEffectiveOperatingHrs();
        this.monthNo = entity.getMonthNo();
        this.aopYear = entity.getAopYear();
        this.plantFKId = entity.getPlantFKId() != null ? entity.getPlantFKId().toString() : null;
    }
}
