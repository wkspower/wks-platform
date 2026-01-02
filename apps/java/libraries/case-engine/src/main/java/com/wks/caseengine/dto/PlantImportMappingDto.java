package com.wks.caseengine.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class PlantImportMappingDto {
    
    private String plant;
    private UUID assetId;
    private String uom;
    private double april;
    private double may;
    private double june;    
    private double july;
    private double aug;
    private double sept;
    private double oct; 
    private double nov;
    private double dec;
    private double jan;
    private double feb;
    private double mar;
    private String remarks;
}