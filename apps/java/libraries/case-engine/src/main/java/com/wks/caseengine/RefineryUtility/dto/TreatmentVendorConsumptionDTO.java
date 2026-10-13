package com.wks.caseengine.RefineryUtility.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TreatmentVendorConsumptionDTO {

    private String normParameterFKId;
    private String name;
    private String displayName;
    private String uom;
    private String normTypeName;
    private Double april;
    private String auditYear;
    private String remarks;
    private Integer displayOrder;
    private Boolean isEditable;
    private String saveStatus;
    private String errorMessage;
}
