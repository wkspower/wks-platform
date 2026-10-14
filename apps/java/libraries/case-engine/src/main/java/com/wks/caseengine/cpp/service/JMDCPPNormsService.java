package com.wks.caseengine.cpp.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.cpp.dto.norm.CPPNormsRequestDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface JMDCPPNormsService {

    AOPMessageVM getCPPNormsForPlants(List<UUID> plantIds, String financialYear, String fromDate, String toDate);

    AOPMessageVM saveOrUpdateCPPNorms(List<CPPNormsRequestDTO> dtoList, String financialYear, String modifiedBy);

    byte[] exportCPPNorms(List<UUID> plantIds, String financialYear, String startDate, String endDate) throws IOException;

    AOPMessageVM importCPPNorms(List<UUID> plantIds, String financialYear, MultipartFile file, String modifiedBy) throws IOException;

    AOPMessageVM calculateCPPNorms(List<UUID> plantIds, String financialYear, String fromDate, String toDate);

}
