package com.wks.caseengine.cpp.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight dropdown DTO for the Plants table.
 * Used by the plants dropdown API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CPPPlantDTO {

    /** Maps to Plants.Id */
    private UUID   plantId;

    /** Maps to Plants.DisplayName */
    private String plantName;

    /** Maps to Plants.PlantCode */
    private String plantCode;


    /** Maps to Plants.SourceName */
    private String sourceName;

    /** Maps to Plants.Site_FK_Id */
    private UUID siteId;

    /** Maps to Sites.Name */
    private String siteName;

    /** Maps to Sites.DisplayName */
    private String siteDisplayName;

    /** Maps to Plants.Vertical_FK_Id */
    private UUID verticalId;

    /** Maps to Verticals.Name */
    private String verticalName;

    /** Maps to Verticals.DisplayName */
    private String verticalDisplayName;

    /** Source of this plant entry: "sourceName" (from Plants.SourceName) or "processMapping" (from CPPProcessPlantsMapping). */
    private String plantSource;
}
