package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.dto.AssetImportMappingDTO;
import com.wks.caseengine.dto.AssetImportMappingPivotDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface AssetImportMappingService {

    AOPMessageVM getPivotData(String financialYear);

    AOPMessageVM savePivotData(AssetImportMappingPivotDTO payload);

}
