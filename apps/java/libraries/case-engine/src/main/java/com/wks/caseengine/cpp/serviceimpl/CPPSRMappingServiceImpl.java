package com.wks.caseengine.cpp.serviceimpl;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.cpp.entity.CPPSRMapping;
import com.wks.caseengine.cpp.repository.CPPSRMappingRepository;
import com.wks.caseengine.cpp.service.CPPSRMappingService;
import com.wks.caseengine.cpp.dto.CPPSRMappingDTO;
import com.wks.caseengine.cpp.dto.CPPSRMappingImportDTO;

@Service
public class CPPSRMappingServiceImpl implements CPPSRMappingService {

    private final CPPSRMappingRepository repository;

    public CPPSRMappingServiceImpl(CPPSRMappingRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public List<CPPSRMappingDTO> saveMappings(List<CPPSRMappingDTO> dtoList) {

        List<CPPSRMappingDTO> responseList = new ArrayList<>();

        for (CPPSRMappingDTO dto : dtoList) {
            try {
                CPPSRMapping entity = new CPPSRMapping();

                // Set ID
                entity.setId(dto.getId() != null ? dto.getId() : UUID.randomUUID());

                entity.setReceiverUtility(dto.getReceiverUtility());
                entity.setReceiverUtilityId(dto.getReceiverUtilityId());
                entity.setReceiverCostCenter(dto.getReceiverCostCenter());
                entity.setReceiverCostCenterId(dto.getReceiverCostCenterId());
                entity.setReceiverPlant(dto.getReceiverPlant());
                entity.setReceiverPlantId(dto.getReceiverPlantId());
                entity.setSenderCostCenter(dto.getSenderCostCenter());
                entity.setSenderCostCenterId(dto.getSenderCostCenterId());
                entity.setSenderPlant(dto.getSenderPlant());
                entity.setSenderPlantId(dto.getSenderPlantId());
                entity.setUtility(dto.getUtility());
                entity.setUtilityId(dto.getUtilityId());
                entity.setRemarks(dto.getRemarks());

                entity.setAopYear(dto.getAopYear());
                entity.setVerticalFkId(dto.getVerticalFkId());
                entity.setSiteFkId(dto.getSiteFkId());
                entity.setPlantFkId(dto.getPlantFkId());

                repository.save(entity);

                dto.setSaveStatus("SUCCESS");
                dto.setErrDescription(null);

            } catch (Exception e) {

                dto.setSaveStatus("FAILED");
                dto.setErrDescription(e.getMessage());

            }

            responseList.add(dto);
        }

        return responseList;
    }
    public List<CPPSRMappingImportDTO> importFromExcel(MultipartFile file) throws Exception {

    List<CPPSRMappingImportDTO> responseList = new ArrayList<>();

    Workbook workbook = new XSSFWorkbook(file.getInputStream());
    Sheet sheet = workbook.getSheetAt(0);

    for (int i = 1; i <= sheet.getLastRowNum(); i++) {

        Row row = sheet.getRow(i);
        CPPSRMappingImportDTO dto = new CPPSRMappingImportDTO();

        try {
            String idStr = row.getCell(0).getStringCellValue(); // hidden ID

            dto.setReceiverUtility(row.getCell(1).getStringCellValue());
            dto.setReceiverUtilityId(row.getCell(2).getStringCellValue());
            dto.setReceiverCostCenter(row.getCell(3).getStringCellValue());
            dto.setReceiverCostCenterId(row.getCell(4).getStringCellValue());
            dto.setReceiverPlant(row.getCell(5).getStringCellValue());
            dto.setReceiverPlantId(row.getCell(6).getStringCellValue());
            dto.setSenderCostCenter(row.getCell(7).getStringCellValue());
            dto.setSenderCostCenterId(row.getCell(8).getStringCellValue());
            dto.setSenderPlant(row.getCell(9).getStringCellValue());
            dto.setSenderPlantId(row.getCell(10).getStringCellValue());
            dto.setUtility(row.getCell(11).getStringCellValue());
            dto.setUtilityId(row.getCell(12).getStringCellValue());
            dto.setRemarks(row.getCell(13).getStringCellValue());
            dto.setAopYear(row.getCell(14).getStringCellValue());

            CPPSRMapping entity;

            if (idStr != null && !idStr.isEmpty()) {
                UUID id = UUID.fromString(idStr);
                entity = repository.findById(id)
                        .orElse(new CPPSRMapping());
                entity.setId(id);
            } else {
                entity = new CPPSRMapping();
                entity.setId(UUID.randomUUID());
            }

            entity.setReceiverUtility(dto.getReceiverUtility());
            entity.setReceiverUtilityId(dto.getReceiverUtilityId());
            entity.setReceiverCostCenter(dto.getReceiverCostCenter());
            entity.setReceiverCostCenterId(dto.getReceiverCostCenterId());
            entity.setReceiverPlant(dto.getReceiverPlant());
            entity.setReceiverPlantId(dto.getReceiverPlantId());
            entity.setSenderCostCenter(dto.getSenderCostCenter());
            entity.setSenderCostCenterId(dto.getSenderCostCenterId());
            entity.setSenderPlant(dto.getSenderPlant());
            entity.setSenderPlantId(dto.getSenderPlantId());
            entity.setUtility(dto.getUtility());
            entity.setUtilityId(dto.getUtilityId());
            entity.setRemarks(dto.getRemarks());
            entity.setAopYear(dto.getAopYear());

            repository.save(entity);

            dto.setSaveStatus("SUCCESS");

        } catch (Exception e) {
            dto.setSaveStatus("FAILED");
            dto.setErrDescription(e.getMessage());
        }

        responseList.add(dto);
    }

    workbook.close();
    return responseList;
}
public void exportToExcel(OutputStream outputStream) throws Exception {

    List<CPPSRMapping> entities = repository.findAll();

    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("CPP_SRMapping");

    String[] headers = {
        "Id",
        "Receiver Utility", "Receiver Utility ID",
        "Receiver Cost Center", "Receiver Cost Center ID",
        "Receiver Plant", "Receiver Plant ID",
        "Sender Cost Center", "Sender Cost Center ID",
        "Sender Plant", "Sender Plant ID",
        "Utility", "Utility ID",
        "Remarks", "AOPYear"
    };

    Row headerRow = sheet.createRow(0);
    for (int i = 0; i < headers.length; i++) {
        headerRow.createCell(i).setCellValue(headers[i]);
    }

    // 🔒 Hide ID column (index 0)
    sheet.setColumnHidden(0, true);

    int rowNum = 1;
    for (CPPSRMapping e : entities) {
        Row row = sheet.createRow(rowNum++);

        row.createCell(0).setCellValue(e.getId().toString()); // hidden

        row.createCell(1).setCellValue(e.getReceiverUtility());
        row.createCell(2).setCellValue(e.getReceiverUtilityId());
        row.createCell(3).setCellValue(e.getReceiverCostCenter());
        row.createCell(4).setCellValue(e.getReceiverCostCenterId());
        row.createCell(5).setCellValue(e.getReceiverPlant());
        row.createCell(6).setCellValue(e.getReceiverPlantId());
        row.createCell(7).setCellValue(e.getSenderCostCenter());
        row.createCell(8).setCellValue(e.getSenderCostCenterId());
        row.createCell(9).setCellValue(e.getSenderPlant());
        row.createCell(10).setCellValue(e.getSenderPlantId());
        row.createCell(11).setCellValue(e.getUtility());
        row.createCell(12).setCellValue(e.getUtilityId());
        row.createCell(13).setCellValue(e.getRemarks());
        row.createCell(14).setCellValue(e.getAopYear());
    }

    workbook.write(outputStream);
    workbook.close();
}
}