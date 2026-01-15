package com.wks.caseengine.tcs.dto;

import java.util.List;

import lombok.Data;

@Data
public class MasterFurnaceDTO {
    
    private List<FurnaceDTO> furnaceData;
    private GCalPerHrDTO gCalPerHrData;
}


