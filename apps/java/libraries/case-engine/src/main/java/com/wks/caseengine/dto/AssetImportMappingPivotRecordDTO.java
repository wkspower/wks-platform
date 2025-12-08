package com.wks.caseengine.dto;

import lombok.Data;
import java.util.Map;

@Data
public class AssetImportMappingPivotRecordDTO {

    private String assetName;

    private String uom;

    private Map<String, Double> monthValues;
}
