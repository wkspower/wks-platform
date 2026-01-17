package com.wks.caseengine.cpp.dto;

import lombok.Data;
import java.util.List;

@Data
public class AssetImportMappingPivotDTO {

    private String financialYear;

    private List<AssetImportMappingPivotRecordDTO> records;
}


