package com.wks.caseengine.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportPowerHoursDto {
    
    private UUID sourceId;
    private String sourceName;
    private String materialCode;
    private String sapCode;
    private String utilityName;
    private String plantName;
    private Double april;
    private Double may;
    private Double june;
    private Double july;
    private Double august;
    private Double september;
    private Double october;
    private Double november;
    private Double december;
    private Double january;
    private Double february;
    private Double march;
    private String remarks;
    private boolean isEditable;
}
