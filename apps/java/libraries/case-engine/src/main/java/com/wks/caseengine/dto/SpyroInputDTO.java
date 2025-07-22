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
public class SpyroInputDTO {

    private String verticalFKId;
    private String plantFKId;
    private String normParameterFKID;
    private String particulars;
    private String normParameterTypeName;
    private String normParameterTypeFKID;
    private String type;
    private String uom;
    private String auditYear;
    private String remarks;

    private Double jan;
    private Double feb;
    private Double mar;
    private Double apr;
    private Double may;
    private Double jun;
    private Double jul;
    private Double aug;
    private Double sep;
    private Double oct;
    private Double nov;
    private Double dec;

    private String saveStatus;
    private String errDescription;

}
