package com.wks.caseengine.cpp.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class CPPSRMappingDTO {

    private UUID id;

    private String receiverUtility;
    private String receiverUtilityId;

    private String receiverCostCenter;
    private String receiverCostCenterId;

    private String receiverPlant;
    private String receiverPlantId;

    private String senderCostCenter;
    private String senderCostCenterId;

    private String senderPlant;
    private String senderPlantId;

    private String utility;
    private String utilityId;

    private String remarks;

    // New filter fields
    private String aopYear;
    private UUID verticalFkId;
    private UUID siteFkId;
    private UUID plantFkId;

    // Fields for import/export tracking (like your AssetCapacityDTO)
    private String saveStatus;
    private String errDescription;
}
