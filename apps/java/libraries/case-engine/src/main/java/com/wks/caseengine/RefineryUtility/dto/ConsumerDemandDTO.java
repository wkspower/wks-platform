package com.wks.caseengine.RefineryUtility.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsumerDemandDTO {

    private String normParameterFKId;
    private String name;
    private String displayName;
    private String uom;
    private String normTypeName;
    private Double previousFYAvg;
    private Double apr;
    private Double may;
    private Double jun;
    private Double jul;
    private Double aug;
    private Double sep;
    private Double oct;
    private Double nov;
    private Double dec;
    private Double jan;
    private Double feb;
    private Double mar;
    private String auditYear;
    private String remarks;
    private Integer displayOrder;
    private Boolean isEditable;
    private String type;
    private String saveStatus;
    private String errDescription;
}
