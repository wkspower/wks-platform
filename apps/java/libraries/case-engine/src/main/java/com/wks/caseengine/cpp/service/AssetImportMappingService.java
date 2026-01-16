package com.wks.caseengine.cpp.service;

import java.util.List;

import com.wks.caseengine.cpp.dto.AssetImportMappingDTO;
import com.wks.caseengine.cpp.dto.AssetImportMappingPivotDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface AssetImportMappingService {

    AOPMessageVM getPivotData(String financialYear);

    AOPMessageVM savePivotData(AssetImportMappingPivotDTO payload);

}


