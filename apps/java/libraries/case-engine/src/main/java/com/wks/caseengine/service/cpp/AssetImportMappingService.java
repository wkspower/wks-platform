package com.wks.caseengine.service.cpp;

import java.util.List;

import com.wks.caseengine.dto.cpp.AssetImportMappingDTO;
import com.wks.caseengine.dto.cpp.AssetImportMappingPivotDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface AssetImportMappingService {

    AOPMessageVM getPivotData(String financialYear);

    AOPMessageVM savePivotData(AssetImportMappingPivotDTO payload);

}
