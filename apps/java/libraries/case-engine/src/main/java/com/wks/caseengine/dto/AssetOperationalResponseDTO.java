package com.wks.caseengine.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetOperationalResponseDTO {


    public AssetOperationalResponseDTO(AssetOperationalResponseDTO dto) {
        this.index = dto.getIndex();
        this.assetName = dto.getAssetName();
        this.assetId = dto.getAssetId();
        this.assetType = dto.getAssetType();
        this.april = dto.getApril();
        this.may = dto.getMay();
        this.june = dto.getJune();
        this.july = dto.getJuly();
        this.aug = dto.getAug();
        this.sep = dto.getSep();
        this.oct = dto.getOct();
        this.nov = dto.getNov();
        this.dec = dto.getDec();
        this.jan = dto.getJan();
        this.feb = dto.getFeb();
        this.march = dto.getMarch();
        this.remarks = dto.getRemarks();
        this.isEditable = dto.isEditable();
    }
    private Integer index;
    private String assetName;   // Name of the PowerGenerationAsset
    private UUID assetId;       // Id of the PowerGenerationAsset
    private UUID utilityPlantAssetId; // Id of the UtilityPlantAsset
    private String utilityPlantAsset; // Name of the UtilityPlantAsset
    private String assetType;
   // private Map<String, MonthlyHoursDTO> months;
    private AssetUtilityDTO utilityDistributed;
    private AssetUtilityDTO utilityGenerated;
    private MonthlyHoursDTO april;
    private MonthlyHoursDTO may; 
    private MonthlyHoursDTO june;
    private MonthlyHoursDTO july;
    private MonthlyHoursDTO aug;
    private MonthlyHoursDTO sep;
    private MonthlyHoursDTO oct;
    private MonthlyHoursDTO nov;
    private MonthlyHoursDTO dec;
    private MonthlyHoursDTO jan;
    private MonthlyHoursDTO feb;
    private MonthlyHoursDTO march;
    private String remarks;
    private boolean isEditable;

    // public AssetOperationalResponseDTO(String assetName,
    //                                    Map<String, MonthlyHoursDTO> months) {
    //     this.assetName = assetName;
    //     this.months = months;
    // }

    public String getAssetName() {
        return assetName;
    }

    // public Map<String, MonthlyHoursDTO> getMonths() {
    //     return months;
    // }

    public boolean isEditable() {
        return isEditable;
    }

    public void setIsEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }
}
