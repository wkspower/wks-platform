package com.wks.caseengine.dto.tcs;

import java.util.List;

import lombok.Data;

@Data
public class MasterFurnaceDTO {
    
    private List<FurnaceDTO> furnaceData;
    private GCalPerHrDTO gCalPerHrData;
}
