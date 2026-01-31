package com.wks.caseengine.cpp.service;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.dto.ImportPowerCapacityDto;

public interface ImportPowerCapacityService {

    /**
     * Get import power capacity for a financial year
     */
    List<ImportPowerCapacityDto> getImportPowerCapacity(UUID cppPlantId, String financialYear);

    /**
     * Upsert import power capacity
     */
    void upsertImportPowerCapacity(List<ImportPowerCapacityDto> dtoList, String financialYear);
}
