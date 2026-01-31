package com.wks.caseengine.cpp.service;

import java.util.List;
import java.io.OutputStream;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.cpp.dto.CPPSRMappingDTO;
import com.wks.caseengine.cpp.dto.CPPSRMappingImportDTO;
import com.wks.caseengine.cpp.entity.CPPSRMapping;

public interface CPPSRMappingService {

    CPPSRMapping saveMapping(CPPSRMapping entity);

    List<CPPSRMapping> getMappingsByFilters(
            String aopYear,
            UUID plantFkId
    );
    List<CPPSRMappingDTO> saveMappings(List<CPPSRMappingDTO> dtoList);

    void exportToExcel(OutputStream outputStream, String aopYear, UUID plantFkId) throws Exception;

    List<CPPSRMappingImportDTO> importFromExcel(MultipartFile file) throws Exception;
}
