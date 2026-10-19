package com.wks.caseengine.naphthasplitter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NormsRationalizationFactorDTO {

    private String normParameterFKId;
    private String displayName;
    private String value;
    private String remarks;
    private String uom;
    private String normParameterTypeDisplayName;
    private String type;
    private String saveStatus;
    private String errDescription;
}
