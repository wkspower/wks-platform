package com.wks.caseengine.crude.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NormBasisDTO {
    
    private UUID id;
    private String name;
    private String displayName;
    private String uom;
    private String attributeValue;
    private String config;
    private String remarks;
    private String type;
    private String normParameterType;
    private String displayOrder;
}
