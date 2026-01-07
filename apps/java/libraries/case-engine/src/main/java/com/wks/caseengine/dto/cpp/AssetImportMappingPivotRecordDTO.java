package com.wks.caseengine.dto.cpp;

import lombok.Data;
import java.util.Map;

@Data
public class AssetImportMappingPivotRecordDTO {

    private String assetName;

    private String uom;

    private String remarks;

    private Map<String, Double> monthValues;
}
