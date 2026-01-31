package com.wks.caseengine.cpp.dto;

import lombok.Data;

@Data
public class CPPSRMappingImportDTO {

    private String id;

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
    private String aopYear;

    // tracking fields
    private String saveStatus;
    private String errDescription;
}
