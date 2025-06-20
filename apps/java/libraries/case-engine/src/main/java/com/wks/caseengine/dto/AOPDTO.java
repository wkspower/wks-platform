package com.wks.caseengine.dto;

import java.util.UUID;

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
public class AOPDTO {
	
	private String id;
    private String aopCaseId;
    private String aopStatus;
    private String aopRemarks;
    private String normItem;
    private String aopType;
    private Double jan;
    private Double feb;
    private Double march;
    private Double april;
    private Double may;
    private Double june;
    private Double july;
    private Double aug;
    private Double sep;
    private Double oct;
    private Double nov;
    private Double dec;
    private String aopYear;
    private String plantFKId;
    private Double avgTPH;
    private String materialFKId;
    private String bDNormParametersFKId;
    private Integer displayOrder;
    private String siteFKId;
    private String verticalFKId;
    private String normParameterName;
    private String normParameterDisplayName;
    private String normParameterTypeId;
    private String displayName;
    private String remark;
    private Boolean isEditable;
    private Boolean isVisible;
}
