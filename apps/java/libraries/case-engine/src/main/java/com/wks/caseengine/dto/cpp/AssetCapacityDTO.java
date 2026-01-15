package com.wks.caseengine.dto.cpp;

import java.util.UUID;

import com.wks.caseengine.dto.AssetUtilityDTO;
import com.wks.caseengine.dto.MonthCapacityDto;

import lombok.Data;

@Data
public class AssetCapacityDTO {
    
   // private UUID assetId;
    private String assetId;
    private String assetName;
    private String plantCode;
    private AssetUtilityDTO utilityDistributed;
    private AssetUtilityDTO utilityGenerated;
    // private String utilityDistributedName;
    // private String utilityDistributedSAP;
    // private String utilityGeneratedName;
    // private String utilityGeneratedSAP;
    private String uom;
    private String remarks;
    private Double fixedMin;
    private Double fixedMax;
    private MonthCapacityDto april;
    private MonthCapacityDto may;
    private MonthCapacityDto june;
    private MonthCapacityDto july;  
    private MonthCapacityDto aug;
    private MonthCapacityDto sep;
    private MonthCapacityDto oct;
    private MonthCapacityDto nov;
    private MonthCapacityDto dec;
    private MonthCapacityDto jan;
    private MonthCapacityDto feb;
    private MonthCapacityDto march;
 
    // Fields for import/export tracking
    private String saveStatus;
    private String errDescription;
}