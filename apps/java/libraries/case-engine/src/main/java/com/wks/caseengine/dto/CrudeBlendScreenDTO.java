package com.wks.caseengine.dto;

import lombok.Data;

@Data
public class CrudeBlendScreenDTO {
    
    private String table;
    private MasterCrudeBlendDTO<?> data;
}
