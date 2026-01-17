package com.wks.caseengine.cpp.dto;

import java.util.UUID;

public interface PowerGenerationSteamResposeProject {  

//     private String assetName;
//     private UUID assetId;
//     private String assetType;
//    // private Map<String, MonthlyHoursDTO> months;
//     private AssetUtilityDTO utilityDistributed;
//     private AssetUtilityDTO utilityGenerated;
//     private MonthlyHoursDTO april;
//     private MonthlyHoursDTO may; 
//     private MonthlyHoursDTO june;
//     private MonthlyHoursDTO july;
//     private MonthlyHoursDTO aug;
//     private MonthlyHoursDTO sep;
//     private MonthlyHoursDTO oct;
//     private MonthlyHoursDTO nov;
//     private MonthlyHoursDTO dec;
//     private MonthlyHoursDTO jan;
//     private MonthlyHoursDTO feb;
//     private MonthlyHoursDTO march;
//     private String remarks;

// create table UtilityPlantAssets (
//     Id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
//     PlantAsset varchar(100),
//     UtilityGenerated varchar(100),
//     UtilityDistributed varchar(100),
//     PowerGenerationAsset_FK_Id UNIQUEIDENTIFIER,
//     Apr decimal(8,2),
//     May decimal(8,2),
//     Jun decimal(8,2),
//     Jul decimal(8,2),
//     Aug decimal(8,2),
//     Sep decimal(8,2),
//     Oct decimal(8,2),
//     Nov decimal(8,2),
//     Dec decimal(8,2),
//     Jan decimal(8,2),
//     Feb decimal(8,2),
//     Mar decimal(8,2),
//     FinancialYear varchar(20),
//     Remarks varchar(8000),
//     Type varchar(100)
 
//  )
    UUID getAssetId();      // Id of the PowerGenerationAsset
    String getAssetName();    // Name of the PowerGenerationAsset
    UUID getUtilityPlantAssetId();
    String getUtilityPlantAsset();
    String getUtilityDistributed();
    String getUtilityDistributedSAPCode();
    String getUtilityGenerated();
    String getUtilityGeneratedSAPCode();
    Double getApr();
    Double getMay();
    Double getJun();
    Double getJul();
    Double getAug();
    Double getSep();
    Double getOct();
    Double getNov();
    Double getDec();
    Double getJan();
    Double getFeb();
    Double getMar();
    String getRemarks();
    String getType();

}
