package com.wks.caseengine.dto;

import java.util.List;

import lombok.Data;

@Data
public class MasterFurnaceDTO {
    
    private List<FurnaceDTO> furnaceData;
    private GCalPerHrDTO gCalPerHrData;
}
