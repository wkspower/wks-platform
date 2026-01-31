package com.wks.caseengine.cpp.service;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.dto.ImportPowerHoursDto;

public interface ImportPowerHoursService {

    /**
     * Get import power operational hours for a financial year
     */
    List<ImportPowerHoursDto> getImportPowerOperationalHours(UUID cppPlantId, String financialYear);

    /**
     * Upsert import power operational hours
     */
    void upsertImportPowerOperationalHours(List<ImportPowerHoursDto> dtoList, String financialYear);
}
