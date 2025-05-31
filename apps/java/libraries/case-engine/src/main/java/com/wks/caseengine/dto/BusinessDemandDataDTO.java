package com.wks.caseengine.dto;

import java.util.UUID;

import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@JsonInclude(Include.ALWAYS)
public class BusinessDemandDataDTO {

    private String id;
    private String remark;
    private String normParameterId;
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
    private String year;
    private String plantId;
    private Double avgTph;
    private String productName;
    private Integer displayOrder;
    private String normParameterTypeId;
    private String normParameterTypeDisplayName;
    private String normParameterTypeName;
    private String siteFKId;
    private String verticalFKId;
    private Boolean isEditable;
    private Boolean isVisible;
    private String UOM;

}
