package com.wks.caseengine.dto.tcs;

import com.wks.caseengine.dto.MasterCrudeBlendDTO;

import lombok.Data;

@Data
public class CrudeBlendScreenDTO {
    
    private String table;
    private MasterCrudeBlendDTO<?> data;
}
