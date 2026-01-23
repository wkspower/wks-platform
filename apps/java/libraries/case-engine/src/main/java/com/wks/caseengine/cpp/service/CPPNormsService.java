package com.wks.caseengine.cpp.service;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.cpp.dto.norm.CPPNormsRequestDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface CPPNormsService {

    AOPMessageVM getCPPNorms(UUID cppPlantId, String financialYear);

    AOPMessageVM saveOrUpdateCPPNorms(List<CPPNormsRequestDTO> dtoList, String financialYear, String modifiedBy);
}
