package com.wks.caseengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AssetImportMappingDTO {

    private String id;                 
    private String assetId;            
    private String financialMonthId;   
    private Double value;
    private String uom;
              
}
