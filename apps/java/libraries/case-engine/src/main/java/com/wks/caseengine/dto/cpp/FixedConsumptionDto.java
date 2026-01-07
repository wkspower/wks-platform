package com.wks.caseengine.dto.cpp;

import java.util.UUID;

import lombok.Data;

@Data
public class FixedConsumptionDto {

    private String plant;  //plant
    private String plantId;  //plantId
    private String costCenter;  //costCenter
    private String costCenterId;  //costCenterId
    private String cppUtility;    //NormParameter.DisplayName
    private String cppUtilityId;    // cppUtilityId
    private String cppPlant;  //cppPlant
    private String cppPlantId;  //cppPlantId
    private String uom;  //uom
    private String normParameterId;  //normParameterId
    private Double april;   //april
    private Double may;   //may
    private Double june;   //june
    private Double july;    // july
    private Double aug;    // aug
    private Double sep;     // sep
    private Double oct;     // oct
    private Double nov;     // nov
    private Double dec;     // dec
    private Double jan;     // jan
    private Double feb;     // feb
    private Double mar;     // mar
    private Double grandTotal;     // grandTotal
    private String remarks;
    private UUID remarkId;
    private UUID costCenter_FK_Id;
    private UUID normParameter_FK_Id;

}

