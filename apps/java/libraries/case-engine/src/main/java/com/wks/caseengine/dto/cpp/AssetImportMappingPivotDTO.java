package com.wks.caseengine.dto.cpp;

import lombok.Data;
import java.util.List;

@Data
public class AssetImportMappingPivotDTO {

    private String financialYear;

    private List<AssetImportMappingPivotRecordDTO> records;
}
