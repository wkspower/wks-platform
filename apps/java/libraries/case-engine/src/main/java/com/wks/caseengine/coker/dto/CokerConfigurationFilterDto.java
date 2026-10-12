package com.wks.caseengine.coker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CokerConfigurationFilterDto {

    private String normParameterFKId;
    private String displayName;
    private String value;
    private String remarks;
    private String UOM;
    private String normParameterTypeDisplayName;
    private String type;
    private String auditYear;
    private String saveStatus;
    private String errDescription;
}
