package com.wks.caseengine.dto.cpp;

import lombok.Data;
import java.util.Map;

@Data
public class AssetImportMappingPivotResponseDTO {

    private String assetName;
    private String uom;

    private Map<String, Double> monthValues;
}
