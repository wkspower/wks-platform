package com.wks.caseengine.dto.cpp;

import java.util.UUID;

import lombok.Data;

@Data
public class AssetPrioriryDTO {

   private UUID assetId;
    private String assetName; // optional, ignored in DB

    private Integer april;
    private Integer may;
    private Integer june;
    private Integer july;
    private Integer aug;
    private Integer sep;
    private Integer oct;
    private Integer nov;
    private Integer dec;

    private Integer jan;
    private Integer feb;
    private Integer mar;
    private String remarks;
}