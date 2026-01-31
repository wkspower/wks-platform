package com.wks.caseengine.cpp.serviceimpl;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.cpp.dto.CPPSRMappingDTO;
import com.wks.caseengine.cpp.dto.CPPSRMappingImportDTO;
import com.wks.caseengine.cpp.entity.CPPSRMapping;
import com.wks.caseengine.cpp.repository.CPPSRMappingRepository;
import com.wks.caseengine.cpp.service.CPPSRMappingService;
import com.wks.caseengine.utility.Utility;

/**
 * Service implementation for managing CPP SR Mapping.
 */
@Service
public class CPPSRMappingServiceImpl implements CPPSRMappingService {

    private final CPPSRMappingRepository repository;

    public CPPSRMappingServiceImpl(CPPSRMappingRepository repository) {
        this.repository = repository;
    }

    @Override
    public CPPSRMapping saveMapping(CPPSRMapping entity) {
        return repository.save(entity);
    }

    @Override
    public List<CPPSRMapping> getMappingsByFilters(String aopYear, UUID plantFkId) {
        return repository.findByAopYearAndPlantFkId(aopYear, plantFkId);
    }

    private String getCellString(DataFormatter formatter, Row row, int cellIdx) {
        if (row == null) {
            return "";
        }
        Cell cell = row.getCell(cellIdx);
        if (cell == null) {
            return "";
        }
        String value = formatter.formatCellValue(cell);
        return value != null ? value.trim() : "";
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

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                CPPSRMappingImportDTO dto = new CPPSRMappingImportDTO();

                try {
                    String idStr = getCellString(formatter, row, 0); // hidden ID

                    dto.setId(idStr);
                    dto.setReceiverUtility(getCellString(formatter, row, 1));
                    dto.setReceiverUtilityId(getCellString(formatter, row, 2));
                    dto.setReceiverCostCenter(getCellString(formatter, row, 3));
                    dto.setReceiverCostCenterId(getCellString(formatter, row, 4));
                    dto.setReceiverPlant(getCellString(formatter, row, 5));
                    dto.setReceiverPlantId(getCellString(formatter, row, 6));
                    dto.setSenderCostCenter(getCellString(formatter, row, 7));
                    dto.setSenderCostCenterId(getCellString(formatter, row, 8));
                    dto.setSenderPlant(getCellString(formatter, row, 9));
                    dto.setSenderPlantId(getCellString(formatter, row, 10));
                    dto.setUtility(getCellString(formatter, row, 11));
                    dto.setUtilityId(getCellString(formatter, row, 12));
                    dto.setRemarks(getCellString(formatter, row, 13));
                    dto.setAopYear(getCellString(formatter, row, 14));

                    CPPSRMapping entity;

                    if (idStr != null && !idStr.isEmpty()) {
                        UUID id = UUID.fromString(idStr);
                        entity = repository.findById(id).orElse(new CPPSRMapping());
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
                    dto.setErrDescription(null);

                } catch (Exception e) {
                    dto.setSaveStatus("FAILED");
                    dto.setErrDescription(e.getMessage());
                }

                responseList.add(dto);
            }
        }

        return responseList;
    }

    @Override
    public void exportToExcel(OutputStream outputStream, String aopYear, UUID plantFkId) throws Exception {

        List<CPPSRMapping> entities = repository.findByAopYearAndPlantFkId(aopYear, plantFkId);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("CPP_SRMapping");
            CellStyle headerStyle = Utility.createBoldBorderedStyle(workbook);
            CellStyle dataStyle = Utility.createBorderedStyle(workbook);

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
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            sheet.setColumnHidden(0, true);

            int rowNum = 1;
            for (CPPSRMapping e : entities) {
                Row row = sheet.createRow(rowNum++);
                int c = 0;

                Cell idCell = row.createCell(c++);
                idCell.setCellValue(e.getId() != null ? e.getId().toString() : "");
                idCell.setCellStyle(dataStyle);

                Cell cell1 = row.createCell(c++);
                cell1.setCellValue(e.getReceiverUtility() != null ? e.getReceiverUtility() : "");
                cell1.setCellStyle(dataStyle);

                Cell cell2 = row.createCell(c++);
                cell2.setCellValue(e.getReceiverUtilityId() != null ? e.getReceiverUtilityId() : "");
                cell2.setCellStyle(dataStyle);

                Cell cell3 = row.createCell(c++);
                cell3.setCellValue(e.getReceiverCostCenter() != null ? e.getReceiverCostCenter() : "");
                cell3.setCellStyle(dataStyle);

                Cell cell4 = row.createCell(c++);
                cell4.setCellValue(e.getReceiverCostCenterId() != null ? e.getReceiverCostCenterId() : "");
                cell4.setCellStyle(dataStyle);

                Cell cell5 = row.createCell(c++);
                cell5.setCellValue(e.getReceiverPlant() != null ? e.getReceiverPlant() : "");
                cell5.setCellStyle(dataStyle);

                Cell cell6 = row.createCell(c++);
                cell6.setCellValue(e.getReceiverPlantId() != null ? e.getReceiverPlantId() : "");
                cell6.setCellStyle(dataStyle);

                Cell cell7 = row.createCell(c++);
                cell7.setCellValue(e.getSenderCostCenter() != null ? e.getSenderCostCenter() : "");
                cell7.setCellStyle(dataStyle);

                Cell cell8 = row.createCell(c++);
                cell8.setCellValue(e.getSenderCostCenterId() != null ? e.getSenderCostCenterId() : "");
                cell8.setCellStyle(dataStyle);

                Cell cell9 = row.createCell(c++);
                cell9.setCellValue(e.getSenderPlant() != null ? e.getSenderPlant() : "");
                cell9.setCellStyle(dataStyle);

                Cell cell10 = row.createCell(c++);
                cell10.setCellValue(e.getSenderPlantId() != null ? e.getSenderPlantId() : "");
                cell10.setCellStyle(dataStyle);

                Cell cell11 = row.createCell(c++);
                cell11.setCellValue(e.getUtility() != null ? e.getUtility() : "");
                cell11.setCellStyle(dataStyle);

                Cell cell12 = row.createCell(c++);
                cell12.setCellValue(e.getUtilityId() != null ? e.getUtilityId() : "");
                cell12.setCellStyle(dataStyle);

                Cell cell13 = row.createCell(c++);
                cell13.setCellValue(e.getRemarks() != null ? e.getRemarks() : "");
                cell13.setCellStyle(dataStyle);

                Cell cell14 = row.createCell(c++);
                cell14.setCellValue(e.getAopYear() != null ? e.getAopYear() : "");
                cell14.setCellStyle(dataStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        }
    }
}