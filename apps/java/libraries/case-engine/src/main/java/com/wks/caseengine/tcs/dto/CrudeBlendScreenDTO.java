package com.wks.caseengine.tcs.dto;

import com.wks.caseengine.dto.MasterCrudeBlendDTO;

import lombok.Data;

@Data
public class CrudeBlendScreenDTO {
    
    private String table;
    private MasterCrudeBlendDTO<?> data;
}


