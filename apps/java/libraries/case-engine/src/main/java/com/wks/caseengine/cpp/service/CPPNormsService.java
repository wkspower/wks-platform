package com.wks.caseengine.cpp.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.cpp.dto.norm.CPPNormsRequestDTO;
import com.wks.caseengine.cpp.dto.norm.CPPNormsResponseDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface CPPNormsService {

    AOPMessageVM getCPPNorms(UUID cppPlantId, String financialYear, String fromDate, String toDate);

    AOPMessageVM saveOrUpdateCPPNorms(List<CPPNormsRequestDTO> dtoList, String financialYear, String modifiedBy);

    byte[] exportCPPNorms(UUID cppPlantId, String financialYear, boolean isAfterSave, List<CPPNormsResponseDTO> dtoList);

    AOPMessageVM importExcel(UUID cppPlantId, String financialYear, MultipartFile file, String modifiedBy);
}
