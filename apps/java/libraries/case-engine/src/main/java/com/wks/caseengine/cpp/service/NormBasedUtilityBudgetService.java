package com.wks.caseengine.cpp.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.cpp.dto.norm.NormBasedUtilityBudgetResponseDTO;
import com.wks.caseengine.cpp.dto.norm.NormsMonthUpdateRequestDTO;
import com.wks.caseengine.cpp.entity.NormsMonthDetail;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface NormBasedUtilityBudgetService {

    AOPMessageVM getNormBasedUtilityBudget(UUID cppPlantId, String financialYear);

    AOPMessageVM saveOrUpdate(NormsMonthUpdateRequestDTO dto, String financialYear, List<Object[]> remarkUpdates, List<NormsMonthDetail> allNormsMonthDetailsToUpdate);

    AOPMessageVM saveOrUpdateBulk(List<NormsMonthUpdateRequestDTO> dtoList, String financialYear);

    
    byte[] exportNormBasedUtilityBudget(UUID cppPlantId, String financialYear, boolean isAfterSave, List<NormBasedUtilityBudgetResponseDTO> dtoList);

    AOPMessageVM importExcel(UUID cppPlantId, String financialYear, MultipartFile file);
}


