package com.wks.caseengine.cpp.serviceimpl;

import java.math.BigDecimal;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wks.caseengine.cpp.dto.norm.CPPNormsRequestDTO;
import com.wks.caseengine.cpp.dto.norm.CPPNormsResponseDTO;
import com.wks.caseengine.cpp.repository.CPPNormsRepository;
import com.wks.caseengine.cpp.service.CPPNormsService;
import com.wks.caseengine.message.vm.AOPMessageVM;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CPPNormsServiceImpl implements CPPNormsService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CPPNormsRepository cppNormsRepository;

    private final ObjectMapper objectMapper;

    public CPPNormsServiceImpl() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
    }

    @Override
    public AOPMessageVM getCPPNorms(UUID cppPlantId, String financialYear, String fromDate, String toDate) {
        log.info("=== Starting getCPPNorms ===");
        log.info("CPPPlantId: {}, FinancialYear: {}, FromDate: {}, ToDate: {}", cppPlantId, financialYear, fromDate, toDate);

        AOPMessageVM vm = new AOPMessageVM();

        try {
            if (cppPlantId == null) {
                log.error("CPPPlantId is null");
                vm.setCode(400);
                vm.setMessage("CPPPlantId cannot be null");
                vm.setData(new ArrayList<>());
                return vm;
            }

            if (financialYear == null || financialYear.isEmpty()) {
                log.error("FinancialYear is null or empty");
                vm.setCode(400);
                vm.setMessage("FinancialYear cannot be null or empty");
                vm.setData(new ArrayList<>());
                return vm;
            }

            if (fromDate != null && !fromDate.isEmpty() && toDate != null && !toDate.isEmpty()) {
                log.info("FromDate and ToDate provided, calculating utility norms first...");
                try {
                    AOPMessageVM calcResult = calculateUtilityNorms(financialYear, fromDate, toDate);
                    
                    if (calcResult.getCode() != 200) {
                        log.warn("Failed to calculate utility norms: {}. Continuing with existing calculated norms.", calcResult.getMessage());
                    } else {
                        log.info("Utility norms calculated successfully: {}", calcResult.getMessage());
                    }
                } catch (Exception calcEx) {
                    log.warn("Exception during utility norms calculation: {}. Continuing with existing calculated norms.", calcEx.getMessage());
                }
            } else {
                log.info("FromDate/ToDate not provided, fetching with existing calculated norms");
            }

            StoredProcedureQuery sp = entityManager
                    .createStoredProcedureQuery("dbo.CPP_GetCPPNorms")
                    .registerStoredProcedureParameter(1, String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(2, String.class, ParameterMode.IN);

            sp.setParameter(1, cppPlantId.toString());
            sp.setParameter(2, financialYear);

            log.info("Executing stored procedure dbo.CPP_GetCPPNorms ...");
            sp.execute();

            @SuppressWarnings("unchecked")
            List<Object[]> rawResults = sp.getResultList();
            log.info("Raw result count: {}", rawResults.size());

            List<CPPNormsResponseDTO> dtoList = new ArrayList<>();

            for (Object[] row : rawResults) {
                CPPNormsResponseDTO dto = new CPPNormsResponseDTO();
                
                int idx = 0;
                dto.setId(row[idx++] != null ? ((Number) row[idx - 1]).longValue() : null);
                dto.setCppNormsId(row[idx++] != null ? UUID.fromString(row[idx - 1].toString()) : null);
                dto.setNormsHeaderFkId(row[idx++] != null ? UUID.fromString(row[idx - 1].toString()) : null);
                dto.setGeneratingPlantName(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setUtilityName(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setUtilityId(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setUom(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setAccountName(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setMaterialName(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setMaterialId(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setIssuingPlantName(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setIssuingUom(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setAopYear(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setNormTypeFkId(row[idx++] != null ? ((Number) row[idx - 1]).intValue() : null);
                dto.setNormTypeName(row[idx++] != null ? row[idx - 1].toString() : null);
                
                dto.setAprNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setMayNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setJunNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setJulNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setAugNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setSepNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setOctNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setNovNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setDecNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setJanNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setFebNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setMarNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                
                dto.setRemarks(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setModifiedBy(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setModifiedDate(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setActualNorm(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : BigDecimal.ZERO);
                dto.setApplyActualNormToAll(row[idx++] != null ? (Boolean) row[idx - 1] : false);

                dtoList.add(dto);
            }

            log.info("Successfully mapped {} CPPNorms records", dtoList.size());

            vm.setCode(200);
            vm.setMessage("Success");
            vm.setData(dtoList);

        } catch (Exception e) {
            log.error("=== ERROR in getCPPNorms ===", e);
            vm.setCode(500);
            vm.setMessage("Error: " + e.getMessage());
            vm.setData(new ArrayList<>());
        }

        return vm;
    }

    @Override
    public byte[] exportCPPNorms(UUID cppPlantId, String financialYear, boolean isAfterSave, List<CPPNormsResponseDTO> dtoList) {
        try {
            if (!isAfterSave) {
                AOPMessageVM result = getCPPNorms(cppPlantId, financialYear, null, null);
                if (result.getData() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<CPPNormsResponseDTO> data = (List<CPPNormsResponseDTO>) result.getData();
                    dtoList = data;
                }
            }

            if (dtoList == null) {
                dtoList = new ArrayList<>();
            }

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("CPP Norms");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            int rowNum = 0;
            int col = 0;

            Row headerRow = sheet.createRow(rowNum++);

            headerRow.createCell(col).setCellValue("Generating Plant");
            headerRow.getCell(col++).setCellStyle(headerStyle);
            headerRow.createCell(col).setCellValue("Utility");
            headerRow.getCell(col++).setCellStyle(headerStyle);
            headerRow.createCell(col).setCellValue("Utility ID");
            headerRow.getCell(col++).setCellStyle(headerStyle);
            headerRow.createCell(col).setCellValue("UOM");
            headerRow.getCell(col++).setCellStyle(headerStyle);
            headerRow.createCell(col).setCellValue("Account");
            headerRow.getCell(col++).setCellStyle(headerStyle);
            headerRow.createCell(col).setCellValue("Material");
            headerRow.getCell(col++).setCellStyle(headerStyle);
            headerRow.createCell(col).setCellValue("SAP Code");
            headerRow.getCell(col++).setCellStyle(headerStyle);
            headerRow.createCell(col).setCellValue("Issuing Plant");
            headerRow.getCell(col++).setCellStyle(headerStyle);
            headerRow.createCell(col).setCellValue("Issuing UOM");
            headerRow.getCell(col++).setCellStyle(headerStyle);
            headerRow.createCell(col).setCellValue("AOP Year");
            headerRow.getCell(col++).setCellStyle(headerStyle);
            headerRow.createCell(col).setCellValue("Norm Type");
            headerRow.getCell(col++).setCellStyle(headerStyle);

            String startYearSuffix = financialYear.substring(2, 4);
            String endYearSuffix = financialYear.substring(5, 7);
            String[] months = {"Apr-" + startYearSuffix, "May-" + startYearSuffix, "Jun-" + startYearSuffix, "Jul-" + startYearSuffix,
                    "Aug-" + startYearSuffix, "Sep-" + startYearSuffix, "Oct-" + startYearSuffix, "Nov-" + startYearSuffix,
                    "Dec-" + startYearSuffix, "Jan-" + endYearSuffix, "Feb-" + endYearSuffix, "Mar-" + endYearSuffix};

            int monthStartCol = col;
            for (String month : months) {
                headerRow.createCell(col).setCellValue(month);
                headerRow.getCell(col++).setCellStyle(headerStyle);
            }

            int remarksCol = col;
            headerRow.createCell(col).setCellValue("Remarks");
            headerRow.getCell(col++).setCellStyle(headerStyle);

            int idCol = col;
            headerRow.createCell(col).setCellValue("id");
            headerRow.getCell(col++).setCellStyle(headerStyle);

            int cppNormsIdCol = col;
            headerRow.createCell(col).setCellValue("cppNormsId");
            headerRow.getCell(col++).setCellStyle(headerStyle);

            int normsHeaderFkIdCol = col;
            headerRow.createCell(col).setCellValue("normsHeaderFkId");
            headerRow.getCell(col++).setCellStyle(headerStyle);

            int normTypeFkIdCol = col;
            headerRow.createCell(col).setCellValue("normTypeFkId");
            headerRow.getCell(col++).setCellStyle(headerStyle);

            if (isAfterSave) {
                headerRow.createCell(col).setCellValue("Status");
                headerRow.getCell(col++).setCellStyle(headerStyle);
                headerRow.createCell(col).setCellValue("Error Description");
                headerRow.getCell(col++).setCellStyle(headerStyle);
            }

            int totalColumns = col;

            for (CPPNormsResponseDTO dto : dtoList) {
                Row row = sheet.createRow(rowNum++);
                col = 0;

                setStringCellValue(row.createCell(col++), dto.getGeneratingPlantName(), dataStyle);
                setStringCellValue(row.createCell(col++), dto.getUtilityName(), dataStyle);
                setStringCellValue(row.createCell(col++), dto.getUtilityId(), dataStyle);
                setStringCellValue(row.createCell(col++), dto.getUom(), dataStyle);
                setStringCellValue(row.createCell(col++), dto.getAccountName(), dataStyle);
                setStringCellValue(row.createCell(col++), dto.getMaterialName(), dataStyle);
                setStringCellValue(row.createCell(col++), dto.getMaterialId(), dataStyle);
                setStringCellValue(row.createCell(col++), dto.getIssuingPlantName(), dataStyle);
                setStringCellValue(row.createCell(col++), dto.getIssuingUom(), dataStyle);
                setStringCellValue(row.createCell(col++), dto.getAopYear(), dataStyle);
                setStringCellValue(row.createCell(col++), dto.getNormTypeName(), dataStyle);

                setBigDecimalCellValue(row.createCell(monthStartCol + 0), dto.getAprNorms(), dataStyle);
                setBigDecimalCellValue(row.createCell(monthStartCol + 1), dto.getMayNorms(), dataStyle);
                setBigDecimalCellValue(row.createCell(monthStartCol + 2), dto.getJunNorms(), dataStyle);
                setBigDecimalCellValue(row.createCell(monthStartCol + 3), dto.getJulNorms(), dataStyle);
                setBigDecimalCellValue(row.createCell(monthStartCol + 4), dto.getAugNorms(), dataStyle);
                setBigDecimalCellValue(row.createCell(monthStartCol + 5), dto.getSepNorms(), dataStyle);
                setBigDecimalCellValue(row.createCell(monthStartCol + 6), dto.getOctNorms(), dataStyle);
                setBigDecimalCellValue(row.createCell(monthStartCol + 7), dto.getNovNorms(), dataStyle);
                setBigDecimalCellValue(row.createCell(monthStartCol + 8), dto.getDecNorms(), dataStyle);
                setBigDecimalCellValue(row.createCell(monthStartCol + 9), dto.getJanNorms(), dataStyle);
                setBigDecimalCellValue(row.createCell(monthStartCol + 10), dto.getFebNorms(), dataStyle);
                setBigDecimalCellValue(row.createCell(monthStartCol + 11), dto.getMarNorms(), dataStyle);
                col = monthStartCol + 12;

                setStringCellValue(row.createCell(col++), dto.getRemarks(), dataStyle);
                setStringCellValue(row.createCell(col++), dto.getId() != null ? dto.getId().toString() : null, dataStyle);
                setStringCellValue(row.createCell(col++), dto.getCppNormsId() != null ? dto.getCppNormsId().toString() : null, dataStyle);
                setStringCellValue(row.createCell(col++), dto.getNormsHeaderFkId() != null ? dto.getNormsHeaderFkId().toString() : null, dataStyle);
                setStringCellValue(row.createCell(col++), dto.getNormTypeFkId() != null ? dto.getNormTypeFkId().toString() : null, dataStyle);

                if (isAfterSave) {
                    setStringCellValue(row.createCell(col++), dto.getSaveStatus(), dataStyle);
                    setStringCellValue(row.createCell(col++), dto.getErrDescription(), dataStyle);
                }
            }

            sheet.setColumnHidden(idCol, true);
            sheet.setColumnHidden(cppNormsIdCol, true);
            sheet.setColumnHidden(normsHeaderFkIdCol, true);
            sheet.setColumnHidden(normTypeFkIdCol, true);

            for (int i = 0; i < totalColumns; i++) {
                if (i == remarksCol) {
                    sheet.setColumnWidth(i, 8000);
                    continue;
                }
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Error exporting CPP norms", e);
            return null;
        }
    }

    @Override
    @Transactional
    public AOPMessageVM importExcel(UUID cppPlantId, String financialYear, MultipartFile file, String modifiedBy) {
        try {
            List<CPPNormsResponseDTO> data = readCPPNorms(file.getInputStream());

            List<CPPNormsRequestDTO> updateRequests = convertToUpdateRequests(data);
            AOPMessageVM saveResponse = saveOrUpdateCPPNorms(updateRequests, financialYear, modifiedBy);

            if (saveResponse.getCode() == 200) {
                AOPMessageVM ok = new AOPMessageVM();
                ok.setCode(200);
                ok.setMessage("All data has been saved");
                return ok;
            }

            if (saveResponse.getCode() == 207) {
                List<String> errorMessages = new ArrayList<>();
                if (saveResponse.getData() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> errs = (List<String>) saveResponse.getData();
                    errorMessages = errs;
                }

                List<CPPNormsResponseDTO> failedRecords = applyErrorsToDtos(data, errorMessages);
                byte[] fileByteArray = exportCPPNorms(cppPlantId, financialYear, true, failedRecords);
                String base64File = Base64.getEncoder().encodeToString(fileByteArray);

                AOPMessageVM aopMessageVM = new AOPMessageVM();
                aopMessageVM.setCode(400);
                aopMessageVM.setMessage("Partial data has been saved");
                aopMessageVM.setData(base64File);
                return aopMessageVM;
            }

            AOPMessageVM errorVM = new AOPMessageVM();
            errorVM.setCode(saveResponse.getCode());
            errorVM.setMessage(saveResponse.getMessage());
            errorVM.setData(saveResponse.getData());
            return errorVM;

        } catch (Exception e) {
            log.error("Error importing CPP norms", e);
            AOPMessageVM errorVM = new AOPMessageVM();
            errorVM.setCode(500);
            errorVM.setMessage("Error importing file: " + e.getMessage());
            return errorVM;
        }
    }

    private List<CPPNormsResponseDTO> readCPPNorms(InputStream inputStream) {
        List<CPPNormsResponseDTO> dataList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                CPPNormsResponseDTO dto = new CPPNormsResponseDTO();

                try {
                    int col = 0;
                    dto.setGeneratingPlantName(getStringCellValue(row.getCell(col++)));
                    dto.setUtilityName(getStringCellValue(row.getCell(col++)));
                    dto.setUtilityId(getStringCellValue(row.getCell(col++)));
                    dto.setUom(getStringCellValue(row.getCell(col++)));
                    dto.setAccountName(getStringCellValue(row.getCell(col++)));
                    dto.setMaterialName(getStringCellValue(row.getCell(col++)));
                    dto.setMaterialId(getStringCellValue(row.getCell(col++)));
                    dto.setIssuingPlantName(getStringCellValue(row.getCell(col++)));
                    dto.setIssuingUom(getStringCellValue(row.getCell(col++)));
                    dto.setAopYear(getStringCellValue(row.getCell(col++)));
                    dto.setNormTypeName(getStringCellValue(row.getCell(col++)));

                    dto.setAprNorms(getBigDecimalCellValue(row.getCell(col++)));
                    dto.setMayNorms(getBigDecimalCellValue(row.getCell(col++)));
                    dto.setJunNorms(getBigDecimalCellValue(row.getCell(col++)));
                    dto.setJulNorms(getBigDecimalCellValue(row.getCell(col++)));
                    dto.setAugNorms(getBigDecimalCellValue(row.getCell(col++)));
                    dto.setSepNorms(getBigDecimalCellValue(row.getCell(col++)));
                    dto.setOctNorms(getBigDecimalCellValue(row.getCell(col++)));
                    dto.setNovNorms(getBigDecimalCellValue(row.getCell(col++)));
                    dto.setDecNorms(getBigDecimalCellValue(row.getCell(col++)));
                    dto.setJanNorms(getBigDecimalCellValue(row.getCell(col++)));
                    dto.setFebNorms(getBigDecimalCellValue(row.getCell(col++)));
                    dto.setMarNorms(getBigDecimalCellValue(row.getCell(col++)));

                    dto.setRemarks(getStringCellValue(row.getCell(col++)));

                    String idStr = getStringCellValue(row.getCell(col++));
                    if (idStr != null && !idStr.isEmpty()) {
                        dto.setId(Long.parseLong(idStr));
                    }

                    String cppNormsIdStr = getStringCellValue(row.getCell(col++));
                    if (cppNormsIdStr != null && !cppNormsIdStr.isEmpty()) {
                        dto.setCppNormsId(UUID.fromString(cppNormsIdStr));
                    }

                    String normsHeaderFkIdStr = getStringCellValue(row.getCell(col++));
                    if (normsHeaderFkIdStr != null && !normsHeaderFkIdStr.isEmpty()) {
                        dto.setNormsHeaderFkId(UUID.fromString(normsHeaderFkIdStr));
                    }

                    String normTypeFkIdStr = getStringCellValue(row.getCell(col++));
                    if (normTypeFkIdStr != null && !normTypeFkIdStr.isEmpty()) {
                        dto.setNormTypeFkId(Integer.parseInt(normTypeFkIdStr));
                    }

                    if (dto.getNormsHeaderFkId() == null) {
                        dto.setSaveStatus("Failed");
                        dto.setErrDescription("NormsHeaderFkId is missing");
                    }

                } catch (Exception e) {
                    dto.setSaveStatus("Failed");
                    dto.setErrDescription(e.getMessage());
                }

                dataList.add(dto);
            }
        } catch (Exception e) {
            log.error("Error reading CPP norms file", e);
        }

        return dataList;
    }

    private List<CPPNormsRequestDTO> convertToUpdateRequests(List<CPPNormsResponseDTO> dtoList) {
        List<CPPNormsRequestDTO> requests = new ArrayList<>();
        for (CPPNormsResponseDTO dto : dtoList) {
            if (dto.getSaveStatus() != null && dto.getSaveStatus().equalsIgnoreCase("Failed")) {
                continue;
            }

            CPPNormsRequestDTO request = new CPPNormsRequestDTO();
            request.setCppNormsId(dto.getCppNormsId());
            request.setNormsHeaderFkId(dto.getNormsHeaderFkId());
            request.setAopYear(dto.getAopYear());
            request.setNormTypeFkId(dto.getNormTypeFkId());
            request.setAprNorms(dto.getAprNorms());
            request.setMayNorms(dto.getMayNorms());
            request.setJunNorms(dto.getJunNorms());
            request.setJulNorms(dto.getJulNorms());
            request.setAugNorms(dto.getAugNorms());
            request.setSepNorms(dto.getSepNorms());
            request.setOctNorms(dto.getOctNorms());
            request.setNovNorms(dto.getNovNorms());
            request.setDecNorms(dto.getDecNorms());
            request.setJanNorms(dto.getJanNorms());
            request.setFebNorms(dto.getFebNorms());
            request.setMarNorms(dto.getMarNorms());
            request.setRemarks(dto.getRemarks());
            requests.add(request);
        }
        return requests;
    }

    private List<CPPNormsResponseDTO> applyErrorsToDtos(List<CPPNormsResponseDTO> dtoList, List<String> errorMessages) {
        List<CPPNormsResponseDTO> failed = new ArrayList<>();

        for (CPPNormsResponseDTO dto : dtoList) {
            if (dto.getSaveStatus() != null && dto.getSaveStatus().equalsIgnoreCase("Failed")) {
                failed.add(dto);
            }
        }

        for (String msg : errorMessages) {
            UUID normsHeaderFkId = extractNormsHeaderFkId(msg);
            if (normsHeaderFkId == null) {
                continue;
            }
            for (CPPNormsResponseDTO dto : dtoList) {
                if (dto.getNormsHeaderFkId() != null && dto.getNormsHeaderFkId().equals(normsHeaderFkId)) {
                    dto.setSaveStatus("Failed");
                    dto.setErrDescription(msg);
                    if (!failed.contains(dto)) {
                        failed.add(dto);
                    }
                    break;
                }
            }
        }

        return failed;
    }

    private UUID extractNormsHeaderFkId(String errorMessage) {
        try {
            if (errorMessage == null) {
                return null;
            }
            String token = "NormsHeaderFkId ";
            int start = errorMessage.indexOf(token);
            if (start < 0) {
                return null;
            }
            String after = errorMessage.substring(start + token.length());
            String idStr = after.split("[: ]", 2)[0].trim();
            if (idStr.isEmpty()) {
                return null;
            }
            return UUID.fromString(idStr);
        } catch (Exception e) {
            return null;
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void setStringCellValue(Cell cell, String value, CellStyle style) {
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void setBigDecimalCellValue(Cell cell, BigDecimal value, CellStyle style) {
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        } else {
            cell.setCellValue("");
        }
        cell.setCellStyle(style);
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        try {
            switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d)) {
                    return String.valueOf((long) d);
                }
                return String.valueOf(d);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal getBigDecimalCellValue(Cell cell) {
        String str = getStringCellValue(cell);
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(str.trim());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional
    public AOPMessageVM saveOrUpdateCPPNorms(List<CPPNormsRequestDTO> dtoList, String financialYear, String modifiedBy) {
        log.info("=== Starting saveOrUpdateCPPNorms ===");
        log.info("Total records to process: {}", dtoList.size());
        log.info("Financial Year: {}", financialYear);

        AOPMessageVM vm = new AOPMessageVM();

        try {
            if (dtoList == null || dtoList.isEmpty()) {
                vm.setCode(400);
                vm.setMessage("Request body cannot be empty");
                return vm;
            }

            int successCount = 0;
            int errorCount = 0;
            List<String> errorMessages = new ArrayList<>();

            for (CPPNormsRequestDTO dto : dtoList) {
                try {
                    log.info("Processing record for NormsHeaderFkId: {}", dto.getNormsHeaderFkId());

                    StoredProcedureQuery sp = entityManager
                            .createStoredProcedureQuery("dbo.CPP_UpdateCPPNorms")
                            .registerStoredProcedureParameter("Id", UUID.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("NormsHeaderFkId", UUID.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("FinancialYear", String.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("AOPYear", String.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("NormTypeFkId", Integer.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Apr_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("May_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Jun_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Jul_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Aug_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Sep_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Oct_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Nov_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Dec_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Jan_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Feb_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Mar_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Remarks", String.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("ApplyActualNormToAll", Boolean.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("ModifiedBy", String.class, ParameterMode.IN);

                    sp.setParameter("Id", dto.getCppNormsId());
                    sp.setParameter("NormsHeaderFkId", dto.getNormsHeaderFkId());
                    sp.setParameter("FinancialYear", financialYear);
                    sp.setParameter("AOPYear", dto.getAopYear());
                    sp.setParameter("NormTypeFkId", dto.getNormTypeFkId());
                    sp.setParameter("Apr_Norms", dto.getAprNorms());
                    sp.setParameter("May_Norms", dto.getMayNorms());
                    sp.setParameter("Jun_Norms", dto.getJunNorms());
                    sp.setParameter("Jul_Norms", dto.getJulNorms());
                    sp.setParameter("Aug_Norms", dto.getAugNorms());
                    sp.setParameter("Sep_Norms", dto.getSepNorms());
                    sp.setParameter("Oct_Norms", dto.getOctNorms());
                    sp.setParameter("Nov_Norms", dto.getNovNorms());
                    sp.setParameter("Dec_Norms", dto.getDecNorms());
                    sp.setParameter("Jan_Norms", dto.getJanNorms());
                    sp.setParameter("Feb_Norms", dto.getFebNorms());
                    sp.setParameter("Mar_Norms", dto.getMarNorms());
                    sp.setParameter("Remarks", dto.getRemarks());
                    sp.setParameter("ApplyActualNormToAll", dto.getApplyActualNormToAll() != null ? dto.getApplyActualNormToAll() : false);
                    sp.setParameter("ModifiedBy", modifiedBy);

                    sp.execute();
                    successCount++;
                    
                    log.info("Successfully processed record for NormsHeaderFkId: {}", dto.getNormsHeaderFkId());

                } catch (Exception e) {
                    errorCount++;
                    String errorMsg = "Error processing NormsHeaderFkId " + dto.getNormsHeaderFkId() + ": " + e.getMessage();
                    errorMessages.add(errorMsg);
                    log.error(errorMsg, e);
                }
            }

            log.info("Processing complete. Success: {}, Errors: {}", successCount, errorCount);

            if (errorCount > 0) {
                vm.setCode(207); // Multi-Status
                vm.setMessage(String.format("Processed %d records. Success: %d, Errors: %d", 
                    dtoList.size(), successCount, errorCount));
                vm.setData(errorMessages);
            } else {
                vm.setCode(200);
                vm.setMessage(String.format("Successfully processed all %d records", successCount));
                vm.setData(null);
            }

        } catch (Exception e) {
            log.error("=== ERROR in saveOrUpdateCPPNorms ===", e);
            vm.setCode(500);
            vm.setMessage("Error: " + e.getMessage());
            vm.setData(null);
        }

        return vm;
    }

    private AOPMessageVM calculateUtilityNorms(String financialYear, String fromDate, String toDate) {
        log.info("=== Starting calculateUtilityNorms ===");
        log.info("FinancialYear: {}, FromDate: {}, ToDate: {}", financialYear, fromDate, toDate);

        AOPMessageVM vm = new AOPMessageVM();

        try {
            if (financialYear == null || financialYear.isEmpty()) {
                log.error("FinancialYear is null or empty");
                vm.setCode(400);
                vm.setMessage("FinancialYear cannot be null or empty");
                vm.setData(new ArrayList<>());
                return vm;
            }

            if (fromDate == null || fromDate.isEmpty()) {
                log.error("FromDate is null or empty");
                vm.setCode(400);
                vm.setMessage("FromDate cannot be null or empty");
                vm.setData(new ArrayList<>());
                return vm;
            }

            if (toDate == null || toDate.isEmpty()) {
                log.error("ToDate is null or empty");
                vm.setCode(400);
                vm.setMessage("ToDate cannot be null or empty");
                vm.setData(new ArrayList<>());
                return vm;
            }

            StoredProcedureQuery sp = entityManager
                    .createStoredProcedureQuery("dbo.CPP_FixedUtilityCalculatedNorms")
                    .registerStoredProcedureParameter(1, String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(2, String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(3, String.class, ParameterMode.IN);

            sp.setParameter(1, financialYear);
            sp.setParameter(2, fromDate);
            sp.setParameter(3, toDate);

            log.info("Executing stored procedure dbo.CPP_FixedUtilityCalculatedNorms ...");
            sp.execute();

            @SuppressWarnings("unchecked")
            List<Object[]> rawResults = sp.getResultList();
            log.info("Calculated norms result count: {}", rawResults.size());

            vm.setCode(200);
            vm.setMessage(String.format("Successfully calculated and saved %d utility norms records", rawResults.size()));
            vm.setData(rawResults.size());

        } catch (Exception e) {
            log.error("=== ERROR in calculateUtilityNorms ===", e);
            vm.setCode(500);
            vm.setMessage("Error: " + e.getMessage());
            vm.setData(0);
        }

        return vm;
    }
}
