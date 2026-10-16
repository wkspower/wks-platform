package com.wks.caseengine.cpp.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.cpp.dto.CPPCostCenterDTO;
import com.wks.caseengine.cpp.dto.CPPPlantDTO;
import com.wks.caseengine.cpp.dto.CPPSRMappingDTO;
import com.wks.caseengine.cpp.dto.CPPSRMappingImportDTO;
import com.wks.caseengine.cpp.dto.SRMappingDTO;
import com.wks.caseengine.cpp.dto.SRMappingQtyDTO;
import com.wks.caseengine.cpp.dto.norm.NormParameterDTO;
import com.wks.caseengine.cpp.entity.CPPSRMapping;
import com.wks.caseengine.cpp.repository.CPPSRMappingRepository;
import com.wks.caseengine.cpp.service.CPPSRMappingService;
import com.wks.caseengine.cpp.utility.ExcelCells;
import com.wks.caseengine.cpp.utility.ExcelColumns;
import com.wks.caseengine.cpp.utility.ExcelStyles;
import com.wks.caseengine.cpp.utility.FiscalYearMonths;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.utility.Utility;

/**
 * Service implementation for managing CPP SR Mapping.
 */
@Service
public class CPPSRMappingServiceImpl implements CPPSRMappingService {

    private static final Logger logger = LoggerFactory.getLogger(CPPSRMappingServiceImpl.class);

    /** NormParameterType names considered "utility" parameters for SR Mapping. */
    private static final List<String> UTILITY_NORM_PARAMETER_TYPES =
            List.of("UtilityConsumption", "Utilities");

    private final CPPSRMappingRepository repository;

    @Autowired
    @Qualifier("db1JdbcTemplate")
    private JdbcTemplate db1JdbcTemplate;

    @Autowired
    private ScreenMappingRepository screenMappingRepository;

    @Autowired
    private AopCalculationRepository aopCalculationRepository;

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

    // ── SR Mapping by Plant ───────────────────────────────────────────────────

    @Override
    public AOPMessageVM getSRMappingByPlant(String plantIds, String financialYear) {
        logger.info("getSRMappingByPlant: plantIds={}, financialYear={}", plantIds, financialYear);
        AOPMessageVM response = new AOPMessageVM();
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(db1JdbcTemplate)
                    .withProcedureName("CPP_GetSRMappingByPlant");

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("PlantIds",      plantIds)
                    .addValue("FinancialYear", financialYear);

            Map<String, Object> result = jdbcCall.execute(params);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows =
                    (List<Map<String, Object>>) result.get("#result-set-1");

            List<SRMappingDTO> data = new ArrayList<>();
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    SRMappingDTO dto = new SRMappingDTO();

                    dto.setId                  (toUuid(row, "ID"));
                    dto.setCppPlantId          (toUuid(row, "CPPPlantId"));
                    dto.setSenderPlantId       (toUuid(row, "SenderPlantId"));
                    dto.setReceiverPlantId     (toUuid(row, "ReceiverPlantId"));

                    dto.setSenderPlantName     (str(row, "SenderPlantName"));
                    dto.setSenderPlantCode     (str(row, "SenderPlantCode"));
                    dto.setSenderUtilityId     (toUuid(row, "SenderUtilityId"));
                    dto.setSenderUtilityName   (str(row, "SenderUtilityName"));
                    dto.setSenderUtilityCode   (str(row, "SenderUtilityCode"));
                    dto.setSenderUtilityUOM    (str(row, "SenderUtilityUOM"));
                    dto.setSenderCostCenterId  (toUuid(row, "SenderCostCenterId"));
                    dto.setSenderCostCenterName(str(row, "SenderCostCenterName"));
                    dto.setSenderCostCenterCode(str(row, "SenderCostCenterCode"));

                    dto.setReceiverPlantName     (str(row, "ReceiverPlantName"));
                    dto.setReceiverPlantCode     (str(row, "ReceiverPlantCode"));
                    dto.setReceiverUtilityId     (toUuid(row, "ReceiverUtilityId"));
                    dto.setReceiverUtilityName   (str(row, "ReceiverUtilityName"));
                    dto.setReceiverUtilityCode   (str(row, "ReceiverUtilityCode"));
                    dto.setReceiverUtilityUOM    (str(row, "ReceiverUtilityUOM"));
                    dto.setReceiverCostCenterId  (toUuid(row, "ReceiverCostCenterId"));
                    dto.setReceiverCostCenterName(str(row, "ReceiverCostCenterName"));
                    dto.setReceiverCostCenterCode(str(row, "ReceiverCostCenterCode"));

                    dto.setRemarks(str(row, "Remarks"));
                    data.add(dto);
                }
            }

            logger.info("getSRMappingByPlant: {} records returned", data.size());
            response.setCode(200);
            response.setMessage(data.size() + " record(s) found.");
            response.setData(data);

        } catch (Exception e) {
            logger.error("getSRMappingByPlant error: {}", e.getMessage(), e);
            response.setCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }

    // ── Export SR Mapping by Plant ────────────────────────────────────────────

    @Override
    public byte[] exportSRMappingByPlant(String plantIds, String financialYear) {
        logger.info("[Export SR Mapping By Plant] plantIds: {}, financialYear: {}", plantIds, financialYear);
        try {
            AOPMessageVM response = getSRMappingByPlant(plantIds, financialYear);

            @SuppressWarnings("unchecked")
            List<SRMappingDTO> dtoList = (List<SRMappingDTO>) response.getData();
            if (dtoList == null) {
                dtoList = new ArrayList<>();
            }

            return buildSRMappingByPlantExcel(dtoList);
        } catch (Exception e) {
            logger.error("[Export SR Mapping By Plant] Error: {}", e.getMessage(), e);
            return null;
        }
    }

    private byte[] buildSRMappingByPlantExcel(List<SRMappingDTO> dtoList) throws Exception {
        org.apache.poi.ss.usermodel.Workbook workbook = new XSSFWorkbook();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("CPP_SRMapping");

        CellStyle headerStyle = ExcelStyles.createHeaderStyle(workbook);
        CellStyle dataStyle   = ExcelStyles.createDataStyle(workbook);

        // ── Header row ───────────────────────────────────────────────────────
        List<String> headers = new ArrayList<>();
        headers.add("Sender Plant Name");
        headers.add("Sender Plant Code");
        headers.add("Sender Utility Name");
        headers.add("Sender Utility Code");
        headers.add("Sender Utility UOM");
        headers.add("Sender Cost Center Name");
        headers.add("Sender Cost Center Code");
        headers.add("Receiver Plant Name");
        headers.add("Receiver Plant Code");
        headers.add("Receiver Utility Name");
        headers.add("Receiver Utility Code");
        headers.add("Receiver Utility UOM");
        headers.add("Receiver Cost Center Name");
        headers.add("Receiver Cost Center Code");
        headers.add("Remarks");
        // Hidden ID column (last)
        headers.add("id");

        Row headerRow = sheet.createRow(0);
        for (int c = 0; c < headers.size(); c++) {
            ExcelCells.setString(headerRow.createCell(c), headers.get(c), headerStyle);
        }

        // ── Data rows ────────────────────────────────────────────────────────
        int rowNum = 1;
        for (SRMappingDTO dto : dtoList) {
            Row row = sheet.createRow(rowNum++);
            int col = 0;

            ExcelCells.setString(row.createCell(col++), dto.getSenderPlantName(),       dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSenderPlantCode(),       dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSenderUtilityName(),     dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSenderUtilityCode(),     dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSenderUtilityUOM(),      dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSenderCostCenterName(),  dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSenderCostCenterCode(),  dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverPlantName(),     dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverPlantCode(),     dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverUtilityName(),   dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverUtilityCode(),   dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverUtilityUOM(),    dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverCostCenterName(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverCostCenterCode(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getRemarks(),               dataStyle);
            // Hidden id (last column)
            ExcelCells.setString(row.createCell(col++),
                    dto.getId() != null ? dto.getId().toString() : "", dataStyle);
        }

        // Auto-size visible columns, then hide the id column
        ExcelColumns.autoSize(sheet, headers.size(), -1);
        int idColIndex = headers.size() - 1;
        ExcelColumns.hideColumns(sheet, idColIndex);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }

    /** Null-safe string from result-set row. */
    private String str(Map<String, Object> row, String key) {
        Object val = row.get(key);
        return val != null ? val.toString() : null;
    }

    /** Null-safe UUID from result-set row. */
    private UUID toUuid(Map<String, Object> row, String key) {
        String val = str(row, key);
        if (val == null || val.isBlank()) return null;
        try { return UUID.fromString(val); }
        catch (IllegalArgumentException e) {
            logger.warn("Could not parse UUID for column {}: {}", key, val);
            return null;
        }
    }

    /** Null-safe UUID from a raw string value. */
    private UUID toUuidStr(String val) {
        if (val == null || val.isBlank()) return null;
        try { return UUID.fromString(val); }
        catch (IllegalArgumentException e) {
            logger.warn("Could not parse UUID: {}", val);
            return null;
        }
    }

    // ── Cost Center Dropdown ──────────────────────────────────────────────

    @Override
    public AOPMessageVM getCostCenters(String plantIds) {
        logger.info("getCostCenters: plantIds={}", plantIds);
        AOPMessageVM response = new AOPMessageVM();
        try {
            List<CPPCostCenterDTO> data;

            boolean hasFilter = (plantIds != null && !plantIds.isBlank());

            if (!hasFilter) {
                // No filter — return all active cost-centers
                String sql = "SELECT CostCenterId, CostCenterName, CostCenterCode, CPP_Plant_FK_Id as cppPlantFkId" +
                             "FROM CPPCostCentersMaster " +
                             "WHERE IsActive = 1 " +
                             "ORDER BY CostCenterName";

                data = db1JdbcTemplate.query(sql, (rs, rowNum) ->
                        new CPPCostCenterDTO(
                                UUID.fromString(rs.getString("CostCenterId")),
                                rs.getString("CostCenterName"),
                                rs.getString("CostCenterCode"),
                                rs.getString("cppPlantFkId")
                        ));
            } else {
                // Split comma-separated GUIDs and build IN (...) clause
                String[] ids = plantIds.split(",");
                List<Object> params = new ArrayList<>();
                StringBuilder inClause = new StringBuilder();
                for (int i = 0; i < ids.length; i++) {
                    if (i > 0) inClause.append(",");
                    inClause.append("?");
                    params.add(ids[i].trim());
                }

                String sql = "SELECT CostCenterId, CostCenterName, CostCenterCode, CPP_Plant_FK_Id as cppPlantFkId " +
                             "FROM CPPCostCentersMaster " +
                             "WHERE IsActive = 1 " +
                             "AND CPP_Plant_FK_Id IN (" + inClause + ") " +
                             "ORDER BY CostCenterName";

                data = db1JdbcTemplate.query(sql, params.toArray(), (rs, rowNum) ->
                        new CPPCostCenterDTO(
                                UUID.fromString(rs.getString("CostCenterId")),
                                rs.getString("CostCenterName"),
                                rs.getString("CostCenterCode"),
                                rs.getString("cppPlantFkId") 
                        ));
            }

            logger.info("getCostCenters: {} records returned", data.size());
            response.setCode(200);
            response.setMessage(data.size() + " record(s) found.");
            response.setData(data);

        } catch (Exception e) {
            logger.error("getCostCenters error: {}", e.getMessage(), e);
            response.setCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }

    // ── Plants Dropdown ──────────────────────────────────────────────────

    @Override
    public AOPMessageVM getPlants(String sourceNames) {
        logger.info("getPlants: sourceNames={}", sourceNames);
        AOPMessageVM response = new AOPMessageVM();
        try {
            List<Object> params = new ArrayList<>();
            boolean hasFilter = (sourceNames != null && !sourceNames.isBlank());

            // ── Query 1: Plants via SourceName (for receiver dropdown) ──
            StringBuilder sql1 = new StringBuilder(
                    "SELECT p.Id AS plantId, p.DisplayName AS plantName, p.PlantCode AS plantCode, " +
                    "p.SourceName AS sourceName, " +
                    "p.Site_FK_Id AS siteId, s.Name AS siteName, s.DisplayName AS siteDisplayName, " +
                    "p.Vertical_FK_Id AS verticalId, v.Name AS verticalName, v.DisplayName AS verticalDisplayName " +
                    "FROM Plants p " +
                    "LEFT JOIN Sites s ON s.Id = p.Site_FK_Id " +
                    "LEFT JOIN Verticals v ON v.Id = p.Vertical_FK_Id " +
                    "WHERE p.IsActive = 1 ");

            if (hasFilter) {
                String[] parts = sourceNames.split(",");
                StringBuilder inClause = new StringBuilder();
                for (int i = 0; i < parts.length; i++) {
                    if (i > 0) inClause.append(",");
                    inClause.append("?");
                    params.add(parts[i].trim());
                }
                sql1.append("AND p.SourceName IN (").append(inClause).append(") ");
            }
            sql1.append("ORDER BY p.DisplayName");

            List<CPPPlantDTO> data = db1JdbcTemplate.query(sql1.toString(), (rs, rowNum) -> {
                CPPPlantDTO dto = new CPPPlantDTO();
                dto.setPlantId(toUuidStr(rs.getString("plantId")));
                dto.setPlantName(rs.getString("plantName"));
                dto.setPlantCode(rs.getString("plantCode"));
                dto.setSourceName(rs.getString("sourceName"));
                dto.setSiteId(toUuidStr(rs.getString("siteId")));
                dto.setSiteName(rs.getString("siteName"));
                dto.setSiteDisplayName(rs.getString("siteDisplayName"));
                dto.setVerticalId(toUuidStr(rs.getString("verticalId")));
                dto.setVerticalName(rs.getString("verticalName"));
                dto.setVerticalDisplayName(rs.getString("verticalDisplayName"));
                dto.setPlantSource("sourceName");
                return dto;
            }, params.toArray());

            // ── Query 2: Process plants via CPPProcessPlantsMapping (for sender dropdown) ──
            if (hasFilter) {
                StringBuilder sql2 = new StringBuilder(
                        "SELECT PP.Id AS plantId, PP.DisplayName AS plantName, PP.PlantCode AS plantCode, " +
                        "PP.SourceName AS sourceName, " +
                        "PP.Site_FK_Id AS siteId, s.Name AS siteName, s.DisplayName AS siteDisplayName, " +
                        "PP.Vertical_FK_Id AS verticalId, v.Name AS verticalName, v.DisplayName AS verticalDisplayName " +
                        "FROM CPPProcessPlantsMapping M " +
                        "INNER JOIN Plants PP ON PP.Id = M.ProcessPlant_FK_Id " +
                        "LEFT JOIN Sites s ON s.Id = PP.Site_FK_Id " +
                        "LEFT JOIN Verticals v ON v.Id = PP.Vertical_FK_Id " +
                        "WHERE M.IsActive = 1 AND PP.IsActive = 1 AND M.CPPPlant_FK_Id IN (");

                List<Object> params2 = new ArrayList<>();
                String[] parts = sourceNames.split(",");
                for (int i = 0; i < parts.length; i++) {
                    if (i > 0) sql2.append(",");
                    sql2.append("?");
                    params2.add(parts[i].trim());
                }
                sql2.append(") ORDER BY PP.DisplayName");

                List<CPPPlantDTO> processPlants = db1JdbcTemplate.query(sql2.toString(), (rs, rowNum) -> {
                    CPPPlantDTO dto = new CPPPlantDTO();
                    dto.setPlantId(toUuidStr(rs.getString("plantId")));
                    dto.setPlantName(rs.getString("plantName"));
                    dto.setPlantCode(rs.getString("plantCode"));
                    dto.setSourceName(rs.getString("sourceName"));
                    dto.setSiteId(toUuidStr(rs.getString("siteId")));
                    dto.setSiteName(rs.getString("siteName"));
                    dto.setSiteDisplayName(rs.getString("siteDisplayName"));
                    dto.setVerticalId(toUuidStr(rs.getString("verticalId")));
                    dto.setVerticalName(rs.getString("verticalName"));
                    dto.setVerticalDisplayName(rs.getString("verticalDisplayName"));
                    dto.setPlantSource("processMapping");
                    return dto;
                }, params2.toArray());

                data.addAll(processPlants);
            }

            logger.info("getPlants: {} records returned ({} from SourceName, {} from CPPProcessPlantsMapping)",
                    data.size(),
                    data.stream().filter(d -> "sourceName".equals(d.getPlantSource())).count(),
                    data.stream().filter(d -> "processMapping".equals(d.getPlantSource())).count());
            response.setCode(200);
            response.setMessage(data.size() + " record(s) found.");
            response.setData(data);

        } catch (Exception e) {
            logger.error("getPlants error: {}", e.getMessage(), e);
            response.setCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }

    // ── Update Mappings ──────────────────────────────────────────────────

    @Override
    @Transactional
    public AOPMessageVM updateSRMappingsByPlant(List<SRMappingDTO> dtoList, String financialYear) {
        logger.info("updateSRMappingsByPlant: processing {} records, financialYear={}", dtoList == null ? 0 : dtoList.size(), financialYear);
        AOPMessageVM response = new AOPMessageVM();
        try {
            if (dtoList == null || dtoList.isEmpty()) {
                response.setCode(400);
                response.setMessage("No records provided for update.");
                return response;
            }

            int createdCount = 0;       // completely new SR mapping + child records created
            int childInsertedCount = 0; // existing SR mapping, child records added for new year
            int alreadyExistCount = 0;  // existing SR mapping, child records already present for year
            Set<UUID> uniquePlantIds = new LinkedHashSet<>();
            for (SRMappingDTO dto : dtoList) {

                // ── Step 1: Update Receiver Cost Center (Utility_CostCenter_FK_Id) ──────────
                // CPPCostCentersMaster is a master table – no new records are created.
                // Update CostCenterName, DisplayName, CostCenterCode for the given receiverCostCenterId.
                // UUID resolvedReceiverCostCenterId = updateCostCenter(
                //         dto.getReceiverCostCenterId(),
                //         dto.getReceiverCostCenterName(),
                //         dto.getReceiverCostCenterCode()
                // );

                // ── Step 2: Update Sender Cost Center (Generation_CostCenter_FK_Id) ─────────
                // Update CostCenterName, DisplayName, CostCenterCode for the given senderCostCenterId.
                // UUID resolvedSenderCostCenterId = updateCostCenter(
                //         dto.getSenderCostCenterId(),
                //         dto.getSenderCostCenterName(),
                //         dto.getSenderCostCenterCode()
                // );

                // ── Step 3: Receiver NormParameter ID — taken directly from payload ──────────
                // The frontend provides receiverUtilityId via a dropdown; no DB lookup needed.
                UUID resolvedReceiverUtilityId = dto.getReceiverUtilityId();

                // ── Step 4: Sender NormParameter ID — taken directly from payload ────────────
                // The frontend provides senderUtilityId via a dropdown; no DB lookup needed.
                UUID resolvedSenderUtilityId = dto.getSenderUtilityId();

                // ── Step 5: Check if CPP_SR_Mapping_Master already exists with same NormParameter IDs ─
                // If both resolved NormParameter IDs match an existing master record, reuse it and skip
                // INSERT/UPDATE of the master entirely — only child-table data will be handled.
                UUID srMappingId = null;
                boolean existingMasterFound = false;

                if (resolvedSenderUtilityId != null && resolvedReceiverUtilityId != null) {
                    List<String> existingMaster = db1JdbcTemplate.queryForList(
                            "SELECT TOP 1 ID FROM CPP_SR_Mapping_Master WITH(NOLOCK) " +
                            "WHERE Utility_NormParameter_FK_Id = ? AND Generation_NormParameter_FK_Id = ?",
                            String.class,
                            resolvedSenderUtilityId.toString(),
                            resolvedReceiverUtilityId.toString());
                    if (!existingMaster.isEmpty()) {
                        srMappingId = UUID.fromString(existingMaster.get(0));
                        existingMasterFound = true;
                        logger.info("updateSRMappingsByPlant: found existing CPP_SR_Mapping_Master ID={} for NormParameter combination — skipping master insert/update", srMappingId);
                    }
                }

                if (!existingMasterFound) {
                    // ── Original INSERT or UPDATE of CPP_SR_Mapping_Master ──────────────────────
                    if (dto.getId() == null) {
                        // INSERT – generate a new UUID and persist
                        srMappingId = UUID.randomUUID();
                        String insertSql = "INSERT INTO CPP_SR_Mapping_Master " +
                                "(ID, CPP_Plant_FK_Id, Utility_NormParameter_FK_Id, Utility_CostCenter_FK_Id, " +
                                " Generation_NormParameter_FK_Id, Generation_CostCenter_FK_Id, Remarks, IsActive, CreatedDate, UpdatedDate) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, 1, GETDATE(), GETDATE())";
                        db1JdbcTemplate.update(insertSql,
                                srMappingId.toString(),
                                dto.getReceiverPlantId()       != null ? dto.getReceiverPlantId().toString()    : null,
                                resolvedSenderUtilityId        != null ? resolvedSenderUtilityId.toString()      : null,
                                dto.getSenderCostCenterId()     != null ? dto.getSenderCostCenterId().toString()   : null,
                                resolvedReceiverUtilityId      != null ? resolvedReceiverUtilityId.toString()    : null,
                                dto.getReceiverCostCenterId()   != null ? dto.getReceiverCostCenterId().toString() : null,
                                dto.getRemarks()
                        );
                        logger.info("updateSRMappingsByPlant: inserted new CPP_SR_Mapping_Master ID={}", srMappingId);

                    } else {
                        // UPDATE – use existing ID
                        srMappingId = dto.getId();
                        String updateSql = "UPDATE CPP_SR_Mapping_Master SET " +
                                "CPP_Plant_FK_Id = ?, " +
                                "Utility_NormParameter_FK_Id = ?, " +
                                "Utility_CostCenter_FK_Id = ?, " +
                                "Generation_NormParameter_FK_Id = ?, " +
                                "Generation_CostCenter_FK_Id = ?, " +
                                "Remarks = ?, " +
                                "UpdatedDate = GETDATE() " +
                                "WHERE ID = ?";
                        db1JdbcTemplate.update(updateSql,
                                dto.getReceiverPlantId()       != null ? dto.getReceiverPlantId().toString()    : null,
                                resolvedSenderUtilityId        != null ? resolvedSenderUtilityId.toString()      : null,
                                dto.getSenderCostCenterId()     != null ? dto.getSenderCostCenterId().toString()   : null,
                                resolvedReceiverUtilityId      != null ? resolvedReceiverUtilityId.toString()    : null,
                                dto.getReceiverCostCenterId()   != null ? dto.getReceiverCostCenterId().toString() : null,
                                dto.getRemarks(),
                                srMappingId.toString()
                        );
                        logger.info("updateSRMappingsByPlant: updated CPP_SR_Mapping_Master ID={}", srMappingId);
                    }
                }

                // ── Step 6: Sync NormsHeader and child tables (only for NMD sites) ────────────

                if (existingMasterFound) {
                    // Existing master found — locate its NormsHeader, then check/insert child records
                    List<String> normsHeaderList = db1JdbcTemplate.queryForList(
                            "SELECT TOP 1 Id FROM NormsHeader WITH(NOLOCK) WHERE CPP_SR_Mapping_Master_Fk_Id = ?",
                            String.class, srMappingId.toString());
                    if (!normsHeaderList.isEmpty()) {
                        UUID normsHeaderId = UUID.fromString(normsHeaderList.get(0));
                        String result = insertChildRecordsIfAbsent(normsHeaderId, financialYear);
                        if ("ALREADY_EXISTS".equals(result)) {
                            alreadyExistCount++;
                        } else {
                            childInsertedCount++;
                            // Update Remarks and UpdatedDate on the existing master record
                            db1JdbcTemplate.update(
                                    "UPDATE CPP_SR_Mapping_Master SET Remarks = ?, UpdatedDate = GETDATE() WHERE ID = ?",
                                    dto.getRemarks(), srMappingId.toString());
                            logger.info("updateSRMappingsByPlant: updated Remarks+UpdatedDate on CPP_SR_Mapping_Master ID={} after child insert", srMappingId);
                        }
                    } else {
                        // No NormsHeader yet on this existing master — create one, then insert child records
                        logger.warn("updateSRMappingsByPlant: existing SR Mapping ID={} has no NormsHeader — creating one", srMappingId);
                        UUID newNormsHeaderId = resolveOrUpdateNormsHeader(dto, srMappingId, resolvedReceiverUtilityId, resolvedSenderUtilityId);
                        if (newNormsHeaderId != null && financialYear != null && !financialYear.isBlank()) {
                            insertChildRecordsIfAbsent(newNormsHeaderId, financialYear);
                            childInsertedCount++;
                            // Update Remarks and UpdatedDate on the existing master record
                            db1JdbcTemplate.update(
                                    "UPDATE CPP_SR_Mapping_Master SET Remarks = ?, UpdatedDate = GETDATE() WHERE ID = ?",
                                    dto.getRemarks(), srMappingId.toString());
                            logger.info("updateSRMappingsByPlant: updated Remarks+UpdatedDate on CPP_SR_Mapping_Master ID={} after NormsHeader+child insert", srMappingId);
                        }
                    }
                } else {
                    // New or updated master — original NormsHeader + child-table flow
                    UUID newNormsHeaderId = resolveOrUpdateNormsHeader(dto, srMappingId, resolvedReceiverUtilityId, resolvedSenderUtilityId);
                    if (newNormsHeaderId != null && financialYear != null && !financialYear.isBlank()) {
                        insertChildRecordsIfAbsent(newNormsHeaderId, financialYear);
                    }
                    createdCount++;
                }


                // Collect unique cppPlantId values for AopCalculation flag update
                if (dto.getCppPlantId() != null) {
                    uniquePlantIds.add(dto.getCppPlantId());
                }
            }
            if ((createdCount + childInsertedCount + alreadyExistCount) > 0 && !uniquePlantIds.isEmpty()) {
                List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("cpp-sr-mapping");
                for (UUID plantUUID : uniquePlantIds) {
                    for (ScreenMapping screenMapping : screenMappingList) {
                        AopCalculation aopCalculation = new AopCalculation();
                        aopCalculation.setAopYear(financialYear);
                        aopCalculation.setIsChanged(true);
                        aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
                        aopCalculation.setPlantId(plantUUID);
                        aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
                        aopCalculationRepository.save(aopCalculation);
                    }
                }
            }

            // Build response message based on per-record outcomes
            List<String> msgParts = new ArrayList<>();
            if (createdCount > 0) {
                msgParts.add(createdCount + " record(s) created successfully.");
            }
            if (childInsertedCount > 0) {
                msgParts.add(childInsertedCount + " record(s) created for financial year " + financialYear + ".");
            }
            if (alreadyExistCount > 0) {
                msgParts.add(alreadyExistCount + " record(s) already exist for financial year " + financialYear + ".");
            }
            String msg = msgParts.isEmpty()
                    ? "No records were processed."
                    : String.join(" ", msgParts);

            response.setCode(200);
            response.setMessage(msg);
            response.setData(null);

        } catch (Exception e) {
            logger.error("updateSRMappingsByPlant error: {}", e.getMessage(), e);
            response.setCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }

    @Override
    @Transactional
    public AOPMessageVM updateSRMappingCostCenters(List<SRMappingDTO> dtoList) {
        logger.info("updateSRMappingCostCenters: processing {} records", dtoList == null ? 0 : dtoList.size());
        AOPMessageVM response = new AOPMessageVM();
        try {
            if (dtoList == null || dtoList.isEmpty()) {
                response.setCode(400);
                response.setMessage("No records provided for cost-center update.");
                return response;
            }

            int updatedCount = 0;
            int skippedCount = 0;

            for (SRMappingDTO dto : dtoList) {
                if (dto.getId() == null) {
                    logger.warn("updateSRMappingCostCenters: skipping record with null id");
                    skippedCount++;
                    continue;
                }

                // Build dynamic SQL so only provided cost-center columns are
                // updated (allows updating just sender OR just receiver).
                List<String> setClauses = new ArrayList<>();
                List<Object> params = new ArrayList<>();

                if (dto.getSenderCostCenterId() != null) {
                    setClauses.add("Utility_CostCenter_FK_Id = ?");
                    params.add(dto.getSenderCostCenterId().toString());
                }
                if (dto.getReceiverCostCenterId() != null) {
                    setClauses.add("Generation_CostCenter_FK_Id = ?");
                    params.add(dto.getReceiverCostCenterId().toString());
                }
                if (dto.getRemarks() != null) {
                    setClauses.add("Remarks = ?");
                    params.add(dto.getRemarks());
                }

                if (setClauses.isEmpty()) {
                    logger.warn("updateSRMappingCostCenters: no fields to update for ID={}", dto.getId());
                    skippedCount++;
                    continue;
                }

                setClauses.add("UpdatedDate = GETDATE()");
                params.add(dto.getId().toString());

                String updateSql = "UPDATE CPP_SR_Mapping_Master SET " +
                        String.join(", ", setClauses) + " WHERE ID = ?";

                int rows = db1JdbcTemplate.update(updateSql, params.toArray());

                if (rows > 0) {
                    updatedCount++;
                    logger.info("updateSRMappingCostCenters: updated cost centers for ID={}", dto.getId());
                } else {
                    skippedCount++;
                    logger.warn("updateSRMappingCostCenters: no row found for ID={}", dto.getId());
                }
            }

            String msg = updatedCount + " record(s) cost-center updated successfully.";
            if (skippedCount > 0) {
                msg += " " + skippedCount + " record(s) skipped (missing id or not found).";
            }

            response.setCode(200);
            response.setMessage(msg);
            response.setData(null);

        } catch (Exception e) {
            logger.error("updateSRMappingCostCenters error: {}", e.getMessage(), e);
            response.setCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }

    // ── Delete SR Mapping ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public AOPMessageVM deleteSRMapping(UUID id, String financialYear) {
        logger.info("deleteSRMapping: id={}, financialYear={}", id, financialYear);
        AOPMessageVM response = new AOPMessageVM();
        try {
            if (id == null) {
                response.setCode(400);
                response.setMessage("Id must not be null.");
                return response;
            }
            if (financialYear == null || financialYear.trim().isEmpty()) {
                response.setCode(400);
                response.setMessage("FinancialYear must not be null or empty.");
                return response;
            }

            // ── Step 1: Verify the master record exists ─────────────────────────────────
            List<String> masterCheck = db1JdbcTemplate.queryForList(
                    "SELECT TOP 1 ID FROM CPP_SR_Mapping_Master WITH(NOLOCK) WHERE ID = ?",
                    String.class, id.toString());

            if (masterCheck.isEmpty()) {
                logger.warn("deleteSRMapping: no CPP_SR_Mapping_Master found for id={}", id);
                response.setCode(404);
                response.setMessage("SR Mapping record not found for id: " + id);
                return response;
            }

            // ── Step 2: Fetch all NormsHeader IDs linked to this SR Mapping ────────────
            List<String> normsHeaderIds = db1JdbcTemplate.queryForList(
                    "SELECT Id FROM NormsHeader WITH(NOLOCK) WHERE CPP_SR_Mapping_Master_Fk_Id = ?",
                    String.class, id.toString());

            logger.info("deleteSRMapping: found {} NormsHeader record(s) linked to id={}", normsHeaderIds.size(), id);

            if (!normsHeaderIds.isEmpty()) {
                // Build IN-clause: ?,?,?...
                String inClause = normsHeaderIds.stream()
                        .map(h -> "?")
                        .collect(java.util.stream.Collectors.joining(","));
                Object[] headerIdArgs = normsHeaderIds.toArray();

                // ── Step 3: Delete NormsMonthDetail filtered by financialYear via FinancialYearMonth ──
                // Parse "2025-26" → startYear=2025, endYear=2026
                // FinancialYear months: Apr-Dec of startYear + Jan-Mar of endYear
                int startYear;
                int endYear;
                try {
                    String[] parts = financialYear.split("-");
                    startYear = Integer.parseInt(parts[0].trim());
                    endYear = startYear + 1;
                } catch (Exception e) {
                    logger.error("deleteSRMapping: could not parse financialYear='{}'", financialYear);
                    response.setCode(400);
                    response.setMessage("Invalid financialYear format. Expected 'YYYY-YY' e.g. '2025-26'.");
                    return response;
                }

                // Build args: headerIds... + startYear + endYear
                Object[] monthDetailArgs = new Object[normsHeaderIds.size() + 2];
                System.arraycopy(headerIdArgs, 0, monthDetailArgs, 0, normsHeaderIds.size());
                monthDetailArgs[normsHeaderIds.size()] = startYear;
                monthDetailArgs[normsHeaderIds.size() + 1] = endYear;

                int deletedMonthDetail = db1JdbcTemplate.update(
                        "DELETE nmd FROM NormsMonthDetail nmd " +
                        "INNER JOIN FinancialYearMonth fym WITH(NOLOCK) ON nmd.FinancialYearMonth_FK_Id = fym.Id " +
                        "WHERE nmd.NormsHeader_FK_Id IN (" + inClause + ") " +
                        "AND ((fym.Year = ? AND fym.Month >= 4) OR (fym.Year = ? AND fym.Month <= 3))",
                        monthDetailArgs);
                logger.info("deleteSRMapping: deleted {} NormsMonthDetail row(s) for financialYear={}", deletedMonthDetail, financialYear);

                // ── Step 4: Delete CPPNorms filtered by financialYear ──────────────────────
                // Build args: headerIds... + financialYear
                Object[] normsArgs = new Object[normsHeaderIds.size() + 1];
                System.arraycopy(headerIdArgs, 0, normsArgs, 0, normsHeaderIds.size());
                normsArgs[normsHeaderIds.size()] = financialYear;

                int deletedNorms = db1JdbcTemplate.update(
                        "DELETE FROM CPPNorms WHERE NormsHeader_FK_Id IN (" + inClause + ") AND FinancialYear = ?",
                        normsArgs);
                logger.info("deleteSRMapping: deleted {} CPPNorms row(s) for financialYear={}", deletedNorms, financialYear);

                // ── Step 5: Delete CPPMonthWisePrice filtered by financialYear ────────────
                int deletedMonthWisePrice = db1JdbcTemplate.update(
                        "DELETE FROM CPPMonthWisePrice WHERE NormsHeader_FK_Id IN (" + inClause + ") AND FinancialYear = ?",
                        normsArgs);
                logger.info("deleteSRMapping: deleted {} CPPMonthWisePrice row(s) for financialYear={}", deletedMonthWisePrice, financialYear);

                // NOTE: NormsHeader and CPP_SR_Mapping_Master are intentionally NOT deleted —
                // only child/transaction data is cleared.
            }

            response.setCode(200);
            response.setMessage("SR Mapping child data cleared successfully for financialYear: " + financialYear);
            response.setData(null);

        } catch (Exception e) {
            logger.error("deleteSRMapping error for id={}: {}", id, e.getMessage(), e);
            response.setCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }


    /**
     * Returns true if the given plantId belongs to a Site whose Name = 'NMD'.
     * Lookup path: Plants.Site_FK_Id → Sites.Name.
     *
     * @param cppPlantId the Plant UUID from the payload
     * @return true when the site name is 'NMD', false otherwise (or when plantId is null)
     */
    private boolean isNmdSite(UUID cppPlantId) {
        if (cppPlantId == null) {
            return false;
        }
        try {
            String sql = "SELECT TOP 1 s.Name " +
                    "FROM Plants p " +
                    "JOIN Sites s ON s.Id = p.Site_FK_Id " +
                    "WHERE p.Id = ?";
            List<String> results = db1JdbcTemplate.queryForList(sql, String.class, cppPlantId.toString());
            if (results.isEmpty()) {
                logger.warn("isNmdSite: no site found for cppPlantId={}", cppPlantId);
                return false;
            }
            boolean isNmd = "NMD".equalsIgnoreCase(results.get(0));
            logger.info("isNmdSite: cppPlantId={} → site='{}', isNmd={}", cppPlantId, results.get(0), isNmd);
            return isNmd;
        } catch (Exception e) {
            logger.error("isNmdSite error for cppPlantId={}: {}", cppPlantId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Creates or updates a NormsHeader row that is linked to the given CPP_SR_Mapping_Master record.
     *
     * <p>Lookup: search NormsHeader WHERE CPP_SR_Mapping_Master_Fk_Id = srMappingId.
     * <ul>
     *   <li>Found  → UPDATE in-place using the same Id.</li>
     *   <li>Not found → INSERT a new row with a fresh UUID.</li>
     * </ul>
     *
     * <p>Column mapping (from payload / resolved IDs):
     * <pre>
     *   Plant_FK_Id          ← dto.receiverPlantId
     *   UtilityName          ← dto.receiverUtilityName
     *   UtilityId            ← resolvedReceiverUtilityId
     *   UtilityUOM           ← dto.receiverUtilityUOM
     *   AccountName          ← "Utilities"  (hardcoded)
     *   MaterialName         ← dto.senderUtilityName
     *   IssuingPlantName     ← dto.senderPlantName
     *   IssuingPlant_FK_Id   ← dto.senderPlantId
     *   NormParameter_FK_Id  ← resolvedSenderUtilityId
     *   // [PENDING-DB] Utility_NormParameter_FK_Id ← resolvedReceiverUtilityId
     *   IsActive             ← 1
     *   IssuingUOM           ← dto.senderUtilityUOM
     *   DisplayOrder         ← 1
     *   MaterialId           ← dto.senderUtilityCode
     *   Remarks              ← dto.remarks
     *   plantCode            ← dto.receiverPlantCode
     *   CPP_SR_Mapping_Master_Fk_Id ← srMappingId
     * </pre>
     */
    /**
     * Creates or updates a NormsHeader row linked to the given CPP_SR_Mapping_Master record.
     *
     * @return the UUID of the newly inserted NormsHeader row, or {@code null} if the row already
     *         existed and was updated (so that Step 7 only fires for new inserts).
     */
    private UUID resolveOrUpdateNormsHeader(SRMappingDTO dto, UUID srMappingId,
                                             UUID resolvedReceiverUtilityId, UUID resolvedSenderUtilityId) {
        if (srMappingId == null) {
            logger.warn("resolveOrUpdateNormsHeader: skipped – srMappingId is null");
            return null;
        }
        try {
            // ── Step 1: Check for an ORPHAN NormsHeader ──────────────────────────────────
            // Since every request always creates a brand-new SR Mapping record (new UUID),
            // searching by CPP_SR_Mapping_Master_Fk_Id = srMappingId would always return empty.
            // Instead, go straight to the orphan check:
            // find a NormsHeader that matches Plant_FK_Id + UtilityUOM + NormParameter_FK_Id
            // but has no SR Mapping linked yet (CPP_SR_Mapping_Master_Fk_Id IS NULL).
            // If found → link it; if not → INSERT a new row.

            String orphanCheckSql = "SELECT TOP 1 Id FROM NormsHeader " +
                    "WHERE Plant_FK_Id = ? " +
                    "AND UtilityName = ? " +
                    "AND NormParameter_FK_Id = ? " +
                    // [PENDING-DB] receiver norm parameter column not yet added to NormsHeader
                    "AND Utility_NormParameter_FK_Id = ? " +
                    "AND CPP_SR_Mapping_Master_Fk_Id IS NULL";

            List<String> orphanResults = db1JdbcTemplate.queryForList(
                    orphanCheckSql,
                    String.class,
                    dto.getReceiverPlantId()  != null ? dto.getReceiverPlantId().toString()  : null,
                    dto.getReceiverUtilityName(),
                    resolvedSenderUtilityId   != null ? resolvedSenderUtilityId.toString()   : null,
                    // [PENDING-DB] 
                    resolvedReceiverUtilityId != null ? resolvedReceiverUtilityId.toString() : null
            );

            if (!orphanResults.isEmpty()) {
                // ── Orphan NormsHeader found — link it to the new SR Mapping ─────────────────
                UUID orphanId = UUID.fromString(orphanResults.get(0));
                db1JdbcTemplate.update(
                        "UPDATE NormsHeader SET CPP_SR_Mapping_Master_Fk_Id = ? WHERE Id = ?",
                        srMappingId.toString(),
                        orphanId.toString()
                );
                logger.info("resolveOrUpdateNormsHeader: linked orphan NormsHeader Id={} to srMappingId={} via CPP_SR_Mapping_Master_Fk_Id",
                        orphanId, srMappingId);
                return orphanId; // Existing row reused → Step 7 will check/insert child records for the financial year
            }

            // ── Step 2: No orphan found — INSERT a new NormsHeader row ───────────────────────
            UUID newId = UUID.randomUUID();
            String insertSql = "INSERT INTO NormsHeader " +
                    "(Id, Plant_FK_Id, UtilityName, UtilityId, UtilityUOM, AccountName, " +
                    " MaterialName, IssuingPlantName, IssuingPlant_FK_Id, NormParameter_FK_Id, " +
                    // [PENDING-DB] 
                    " Utility_NormParameter_FK_Id, " +
                    " IsActive, IssuingUOM, DisplayOrder, MaterialId, Remarks, plantCode, " +
                    " CPP_SR_Mapping_Master_Fk_Id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, 1, ?, ?, ?, ?)";
            db1JdbcTemplate.update(insertSql,
                    newId.toString(),
                    dto.getReceiverPlantId()       != null ? dto.getReceiverPlantId().toString()    : null,  // Plant_FK_Id
                    dto.getReceiverUtilityName(),                                                             // UtilityName
                    dto.getReceiverUtilityCode(),                                                             // UtilityId
                    dto.getReceiverUtilityUOM(),                                                              // UtilityUOM
                    "Utilities",                                                                              // AccountName
                    dto.getSenderUtilityName(),                                                               // MaterialName
                    dto.getSenderPlantName(),                                                                 // IssuingPlantName
                    dto.getSenderPlantId()            != null ? dto.getSenderPlantId().toString()          : null,  // IssuingPlant_FK_Id
                    resolvedSenderUtilityId          != null ? resolvedSenderUtilityId.toString()          : null,  // NormParameter_FK_Id (sender)
                    // [PENDING-DB] 
                    resolvedReceiverUtilityId != null ? resolvedReceiverUtilityId.toString() : null,  // Utility_NormParameter_FK_Id (receiver)
                    dto.getSenderUtilityUOM(),                                                                // IssuingUOM
                    dto.getSenderUtilityCode(),                                                               // MaterialId
                    dto.getRemarks(),                                                                         // Remarks
                    dto.getSenderPlantCode(),                                                                 // plantCode
                    srMappingId.toString()
            );
            logger.info("resolveOrUpdateNormsHeader: inserted new NormsHeader Id={} for srMappingId={}", newId, srMappingId);
            return newId; // New row → Step 7 will insert child records for the financial year

        } catch (Exception e) {
            logger.error("resolveOrUpdateNormsHeader error for srMappingId={}: {}", srMappingId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Inserts 12 NormsMonthDetail records (one per month) for a newly created NormsHeader.
     *
     * <p>The financial year string (e.g. "2025-26") is parsed to extract the base year (2025).
     * All 12 {@code FinancialYearMonth} rows for that year are fetched and one
     * {@code NormsMonthDetail} row is created per month.
     *
     * <p>Column defaults:
     * <pre>
     *   ScenarioType  = NULL
     *   Norms         = 0
     *   Quantity      = 0
     *   Amount        = 0
     *   Price         = 0
     *   DisplayOrder  = 1
     *   GenerationUOM = NULL
     *   QTY           = 0
     *   Remarks       = NULL
     * </pre>
     *
     * @param normsHeaderId  the Id of the newly inserted NormsHeader row
     * @param financialYear  financial year string, e.g. "2025-26"
     */
    private void insertNormsMonthDetails(UUID normsHeaderId, String financialYear) {
        if (normsHeaderId == null || financialYear == null || financialYear.isBlank()) {
            logger.warn("insertNormsMonthDetails: skipped – normsHeaderId={}, financialYear={}", normsHeaderId, financialYear);
            return;
        }
        try {
            // Parse financial year "2025-26" → startYear=2025, endYear=2026
            int startYear;
            int endYear;
            try {
                startYear = Integer.parseInt(financialYear.split("-")[0].trim());
                endYear   = startYear + 1;
            } catch (NumberFormatException e) {
                logger.error("insertNormsMonthDetails: could not parse year from financialYear='{}'", financialYear);
                return;
            }

            // Fetch the 12 FinancialYearMonth rows that span the financial year:
            //   Apr–Dec of startYear  +  Jan–Mar of endYear
            // Mirrors: WHERE (Year = @StartYear AND Month >= 4) OR (Year = @EndYear AND Month <= 3)
            String fymSql = "SELECT Id FROM FinancialYearMonth " +
                    "WHERE (Year = ? AND Month >= 4) OR (Year = ? AND Month <= 3) " +
                    "ORDER BY Year, Month";
            List<String> fymIds = db1JdbcTemplate.queryForList(fymSql, String.class, startYear, endYear);

            if (fymIds.isEmpty()) {
                logger.warn("insertNormsMonthDetails: no FinancialYearMonth records found for financialYear={}", financialYear);
                return;
            }

            String insertSql = "INSERT INTO NormsMonthDetail " +
                    "(Id, NormsHeader_FK_Id, FinancialYearMonth_FK_Id, ScenarioType, " +
                    " Norms, Quantity, Amount, Price, DisplayOrder, GenerationUOM, QTY, Remarks) " +
                    "VALUES (?, ?, ?, NULL, 0, 0, 0, 0, 1, NULL, 0, NULL)";

            for (String fymId : fymIds) {
                UUID newDetailId = UUID.randomUUID();
                db1JdbcTemplate.update(insertSql,
                        newDetailId.toString(),
                        normsHeaderId.toString(),
                        fymId
                );
                logger.info("insertNormsMonthDetails: inserted NormsMonthDetail Id={} for NormsHeader={}, FinancialYearMonth={}",
                        newDetailId, normsHeaderId, fymId);
            }

            logger.info("insertNormsMonthDetails: inserted {} month records for NormsHeader={}, financialYear={}",
                    fymIds.size(), normsHeaderId, financialYear);

        } catch (Exception e) {
            logger.error("insertNormsMonthDetails error for normsHeaderId={}: {}", normsHeaderId, e.getMessage(), e);
        }
    }

    /**
     * Inserts a default CPPNorms row for a newly created NormsHeader.
     *
     * <p>Fixed defaults:
     * <ul>
     *   <li>NormType_FK_Id       = 6</li>
     *   <li>All month norms      = 0 (Apr … Mar)</li>
     *   <li>ApplyActualNormToAll = 1</li>
     *   <li>CreatedBy / ModifiedBy = "SYSTEM"</li>
     *   <li>CreatedDate / ModifiedDate = GETDATE()</li>
     *   <li>Remarks = NULL</li>
     *   <li>FinancialYear and AOPYear are both set to financialYear (they are always equal)</li>
     * </ul>
     *
     * @param normsHeaderId the Id of the newly inserted NormsHeader row
     * @param financialYear financial year string, e.g. "2025-26" (used for both FinancialYear and AOPYear)
     */
    private void insertCppNorms(UUID normsHeaderId, String financialYear) {
        if (normsHeaderId == null || financialYear == null || financialYear.isBlank()) {
            logger.warn("insertCppNorms: skipped – normsHeaderId={}, financialYear={}", normsHeaderId, financialYear);
            return;
        }
        try {
            UUID newId = UUID.randomUUID();
            String insertSql = "INSERT INTO CPPNorms " +
                    "(Id, NormsHeader_FK_Id, FinancialYear, AOPYear, NormType_FK_Id, " +
                    " Apr_Norms, May_Norms, Jun_Norms, Jul_Norms, Aug_Norms, Sep_Norms, " +
                    " Oct_Norms, Nov_Norms, Dec_Norms, Jan_Norms, Feb_Norms, Mar_Norms, " +
                    " Remarks, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, ApplyActualNormToAll) " +
                    "VALUES (?, ?, ?, ?, 6, " +
                    " 0, 0, 0, 0, 0, 0, " +
                    " 0, 0, 0, 0, 0, 0, " +
                    " 'Add new record', 'SYSTEM', GETDATE(), 'SYSTEM', GETDATE(), 1)";

            db1JdbcTemplate.update(insertSql,
                    newId.toString(),
                    normsHeaderId.toString(),
                    financialYear,   // FinancialYear
                    financialYear    // AOPYear (same value)
            );

            logger.info("insertCppNorms: inserted CPPNorms Id={} for NormsHeader={}, financialYear={}",
                    newId, normsHeaderId, financialYear);

        } catch (Exception e) {
            logger.error("insertCppNorms error for normsHeaderId={}: {}", normsHeaderId, e.getMessage(), e);
        }
    }

    /**
     * Inserts a default CPPMonthWisePrice row for a newly created NormsHeader.
     *
     * <p>Fixed defaults:
     * <ul>
     *   <li>All month prices       = 0 (Apr … Mar)</li>
     *   <li>Remarks                = "Added new norms"</li>
     *   <li>PriceSource            = "Calculation"</li>
     *   <li>ValueType              = "Calculation"</li>
     *   <li>ModifiedBy             = "SYSTEM"</li>
     *   <li>CreatedDate / UpdatedDate = GETDATE()</li>
     *   <li>FinancialYear and AOPYear are both set to financialYear (they are always equal)</li>
     * </ul>
     *
     * @param normsHeaderId the Id of the newly inserted NormsHeader row
     * @param financialYear financial year string, e.g. "2025-26" (used for both FinancialYear and AOPYear)
     */
    private void insertCppMonthWisePrice(UUID normsHeaderId, String financialYear) {
        if (normsHeaderId == null || financialYear == null || financialYear.isBlank()) {
            logger.warn("insertCppMonthWisePrice: skipped – normsHeaderId={}, financialYear={}", normsHeaderId, financialYear);
            return;
        }
        try {
            UUID newId = UUID.randomUUID();
            String insertSql = "INSERT INTO CPPMonthWisePrice " +
                    "(Id, NormsHeader_FK_Id, FinancialYear, AOPYear, " +
                    " Apr_Price, May_Price, Jun_Price, Jul_Price, Aug_Price, Sep_Price, " +
                    " Oct_Price, Nov_Price, Dec_Price, Jan_Price, Feb_Price, Mar_Price, " +
                    " Remarks, PriceSource, CreatedDate, UpdatedDate, ModifiedBy, ValueType) " +
                    "VALUES (?, ?, ?, ?, " +
                    " 0, 0, 0, 0, 0, 0, " +
                    " 0, 0, 0, 0, 0, 0, " +
                    " 'Added new norms', 'Calculation', GETDATE(), GETDATE(), 'SYSTEM', 'Calculation')";

            db1JdbcTemplate.update(insertSql,
                    newId.toString(),
                    normsHeaderId.toString(),
                    financialYear,   // FinancialYear
                    financialYear    // AOPYear (same value)
            );

            logger.info("insertCppMonthWisePrice: inserted CPPMonthWisePrice Id={} for NormsHeader={}, financialYear={}",
                    newId, normsHeaderId, financialYear);

        } catch (Exception e) {
            logger.error("insertCppMonthWisePrice error for normsHeaderId={}: {}", normsHeaderId, e.getMessage(), e);
        }
    }

    /**
     * Checks whether child records (CPPNorms, NormsMonthDetail, CPPMonthWisePrice) already exist
     * for the given NormsHeader and financial year.
     * <p>
     * Each child table is checked <strong>independently</strong>: if a table is missing its records
     * for the requested year they are inserted regardless of the state of the other tables.
     * <ul>
     *   <li>All 3 already present → returns "ALREADY_EXISTS".</li>
     *   <li>At least 1 was inserted → returns "INSERTED".</li>
     *   <li>Null / blank inputs    → returns "SKIPPED".</li>
     *   <li>Unexpected error       → returns "ERROR".</li>
     * </ul>
     *
     * @param normsHeaderId UUID of the NormsHeader row
     * @param financialYear financial year string, e.g. "2025-26"
     * @return "ALREADY_EXISTS", "INSERTED", "SKIPPED", or "ERROR"
     */
    private String insertChildRecordsIfAbsent(UUID normsHeaderId, String financialYear) {
        if (normsHeaderId == null || financialYear == null || financialYear.isBlank()) {
            logger.warn("insertChildRecordsIfAbsent: skipped – normsHeaderId={}, financialYear={}", normsHeaderId, financialYear);
            return "SKIPPED";
        }
        try {
            boolean anyInserted = false;

            // ── 1. CPPNorms ───────────────────────────────────────────────────────────────────
            List<String> cppNormsExisting = db1JdbcTemplate.queryForList(
                    "SELECT TOP 1 Id FROM CPPNorms WITH(NOLOCK) WHERE NormsHeader_FK_Id = ? AND FinancialYear = ?",
                    String.class, normsHeaderId.toString(), financialYear);

            if (cppNormsExisting.isEmpty()) {
                insertCppNorms(normsHeaderId, financialYear);
                logger.info("insertChildRecordsIfAbsent: inserted CPPNorms for NormsHeader={}, financialYear={}", normsHeaderId, financialYear);
                anyInserted = true;
            } else {
                logger.info("insertChildRecordsIfAbsent: CPPNorms already present for NormsHeader={}, financialYear={} — skipping", normsHeaderId, financialYear);
            }

            // ── 2. NormsMonthDetail ───────────────────────────────────────────────────────────
            // NormsMonthDetail has no FinancialYear column; year is resolved via FinancialYearMonth.
            // Parse "2025-26" → startYear=2025, endYear=2026 and check using the same range used on insert.
            int startYear;
            int endYear;
            try {
                startYear = Integer.parseInt(financialYear.split("-")[0].trim());
                endYear   = startYear + 1;
            } catch (NumberFormatException e) {
                logger.error("insertChildRecordsIfAbsent: could not parse year from financialYear='{}' — skipping NormsMonthDetail check", financialYear);
                startYear = -1;
                endYear   = -1;
            }

            if (startYear > 0) {
                List<String> monthDetailExisting = db1JdbcTemplate.queryForList(
                        "SELECT TOP 1 nmd.Id FROM NormsMonthDetail nmd WITH(NOLOCK) " +
                        "INNER JOIN FinancialYearMonth fym WITH(NOLOCK) ON nmd.FinancialYearMonth_FK_Id = fym.Id " +
                        "WHERE nmd.NormsHeader_FK_Id = ? " +
                        "AND ((fym.Year = ? AND fym.Month >= 4) OR (fym.Year = ? AND fym.Month <= 3))",
                        String.class, normsHeaderId.toString(), startYear, endYear);

                if (monthDetailExisting.isEmpty()) {
                    insertNormsMonthDetails(normsHeaderId, financialYear);
                    logger.info("insertChildRecordsIfAbsent: inserted NormsMonthDetail for NormsHeader={}, financialYear={}", normsHeaderId, financialYear);
                    anyInserted = true;
                } else {
                    logger.info("insertChildRecordsIfAbsent: NormsMonthDetail already present for NormsHeader={}, financialYear={} — skipping", normsHeaderId, financialYear);
                }
            }

            // ── 3. CPPMonthWisePrice ──────────────────────────────────────────────────────────
            List<String> monthWisePriceExisting = db1JdbcTemplate.queryForList(
                    "SELECT TOP 1 Id FROM CPPMonthWisePrice WITH(NOLOCK) WHERE NormsHeader_FK_Id = ? AND FinancialYear = ?",
                    String.class, normsHeaderId.toString(), financialYear);

            if (monthWisePriceExisting.isEmpty()) {
                insertCppMonthWisePrice(normsHeaderId, financialYear);
                logger.info("insertChildRecordsIfAbsent: inserted CPPMonthWisePrice for NormsHeader={}, financialYear={}", normsHeaderId, financialYear);
                anyInserted = true;
            } else {
                logger.info("insertChildRecordsIfAbsent: CPPMonthWisePrice already present for NormsHeader={}, financialYear={} — skipping", normsHeaderId, financialYear);
            }

            if (anyInserted) {
                logger.info("insertChildRecordsIfAbsent: completed — one or more child tables inserted for NormsHeader={}, financialYear={}", normsHeaderId, financialYear);
                return "INSERTED";
            } else {
                logger.info("insertChildRecordsIfAbsent: all child records already present for NormsHeader={}, financialYear={}", normsHeaderId, financialYear);
                return "ALREADY_EXISTS";
            }

        } catch (Exception e) {
            logger.error("insertChildRecordsIfAbsent error for normsHeaderId={}: {}", normsHeaderId, e.getMessage(), e);
            return "ERROR";
        }
    }


    /**
     * Updates CostCenterName, DisplayName, and CostCenterCode in CPPCostCentersMaster
     * for the given costCenterId.
     * CPPCostCentersMaster is a master table – no new records are ever inserted here.
     *
     * @param costCenterId the existing CostCenterId from the payload (senderCostCenterId / receiverCostCenterId)
     * @param name         new CostCenterName / DisplayName
     * @param code         new CostCenterCode
     * @return the same costCenterId, or null if input is incomplete
     */
    private UUID updateCostCenter(UUID costCenterId, String name, String code) {
        if (costCenterId == null) {
            logger.warn("updateCostCenter: skipped – costCenterId is null");
            return null;
        }
        try {
            String updateSql = "UPDATE CPPCostCentersMaster " +
                    "SET CostCenterName = ?, DisplayName = ?, CostCenterCode = ? " +
                    "WHERE CostCenterId = ?";
            int rows = db1JdbcTemplate.update(updateSql, name, name, code, costCenterId.toString());
            if (rows == 0) {
                logger.warn("updateCostCenter: no row found for CostCenterId={} – record not updated", costCenterId);
            } else {
                logger.info("updateCostCenter: updated CostCenterId={}, name='{}', code='{}'", costCenterId, name, code);
            }
            return costCenterId;
        } catch (Exception e) {
            logger.error("updateCostCenter error (costCenterId={}): {}", costCenterId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Finds a matching record in NormParameters by (Plant_FK_Id, NormType_FK_Id, Name/DisplayName).
     * - If a match is found  → updates DisplayName, UOM, and SAPMaterialCode only (NormParameterType_FK_Id
     *                          and CalculationType are left unchanged).
     * - If no match is found → fetches NormParameterType_FK_Id where NormParameterType.Name = 'Configuration',
     *                          then inserts a fully populated new record.
     *
     * @param utilityName utility name  (mapped to Name and DisplayName)
     * @param utilityCode utility code  (mapped to SAPMaterialCode)
     * @param uom         unit of measure (mapped to UOM)
     * @param plantId     Plant_FK_Id   (receiverPlantId for receiver, senderPlantId for sender)
     * @param normTypeId  NormType_FK_Id: 1 = receiver (Utility), 2 = sender (Generation)
     * @return resolved or newly created NormParameter Id, or null if inputs are incomplete
     */
    private UUID resolveOrCreateNormParameter(String utilityName, String utilityCode, String uom, UUID plantId, int normTypeId) {
        if (utilityName == null || plantId == null) {
            logger.warn("resolveOrCreateNormParameter: skipped due to null input (name={}, plant={})", utilityName, plantId);
            return null;
        }
        try {
            // ── Search for an existing matching NormParameter ─────────────────────────────
            String searchSql = "SELECT TOP 1 Id FROM NormParameters " +
                    "WHERE Plant_FK_Id = ? AND NormType_FK_Id = ? AND (Name = ? OR DisplayName = ?)";
            List<String> results = db1JdbcTemplate.queryForList(searchSql, String.class,
                    plantId.toString(), normTypeId, utilityName, utilityName);

            if (!results.isEmpty()) {
                UUID existingId = UUID.fromString(results.get(0));
                logger.info("resolveOrCreateNormParameter: reusing existing ID={} for name='{}', plant={}, normType={}", existingId, utilityName, plantId, normTypeId);
                // Update only the display/sync fields; leave NormParameterType_FK_Id and CalculationType as-is.
                String updateSql = "UPDATE NormParameters SET DisplayName = ?, UOM = ?, SAPMaterialCode = ? WHERE Id = ?";
                db1JdbcTemplate.update(updateSql, utilityName, uom, utilityCode, existingId.toString());
                return existingId;
            }

            // ── Lookup NormParameterType_FK_Id where Name = 'Configuration' ──────────────
            String normParamTypeSql = "SELECT TOP 1 Id FROM NormParameterType WHERE Name = 'Configuration' AND IsActive = 1";
            List<String> normParamTypeResults = db1JdbcTemplate.queryForList(normParamTypeSql, String.class);
            String normParameterTypeFkId = normParamTypeResults.isEmpty() ? null : normParamTypeResults.get(0);
            if (normParameterTypeFkId == null) {
                logger.warn("resolveOrCreateNormParameter: NormParameterType 'Configuration' not found; NormParameterType_FK_Id will be NULL");
            }

            // ── Insert a new NormParameter record ─────────────────────────────────────────
            UUID newId = UUID.randomUUID();
            String insertSql = "INSERT INTO NormParameters " +
                    "(Id, Name, DisplayName, UOM, Plant_FK_Id, NormType_FK_Id, NormParameterType_FK_Id, " +
                    " SAPMaterialCode, ExecuteQuery, DependantAttributeId, Type, " +
                    " IsHistorical, DisplayOrder, IsEditable, IsVisible, CalculationType) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NULL, NULL, NULL, 1, 1, 1, 1, NULL)";
            db1JdbcTemplate.update(insertSql,
                    newId.toString(),
                    utilityName,
                    utilityName,
                    uom,
                    plantId.toString(),
                    normTypeId,
                    normParameterTypeFkId,
                    utilityCode
            );
            logger.info("resolveOrCreateNormParameter: created new ID={} for name='{}', plant={}, normType={}, normParamType={}", newId, utilityName, plantId, normTypeId, normParameterTypeFkId);
            return newId;

        } catch (Exception e) {
            logger.error("resolveOrCreateNormParameter error (name={}, plant={}, normType={}): {}", utilityName, plantId, normTypeId, e.getMessage(), e);
            return null;
        }
    }

    // ── Norm Parameters by Source Plant ──────────────────────────────────────

    @Override
    public AOPMessageVM getNormParametersBySourcePlant(String plantId, Integer normTypeId) {
        logger.info("getNormParametersBySourcePlant: plantId={}, normTypeId={}", plantId, normTypeId);
        AOPMessageVM response = new AOPMessageVM();
        try {
            String normTypeFilter = UTILITY_NORM_PARAMETER_TYPES.stream()
                    .map(t -> "?")
                    .collect(java.util.stream.Collectors.joining(","));

            // Plants linked to the CPP plant via two paths:
            //   1. Plants.SourceName = cppPlantId (internal/regular plants)
            //   2. CPPProcessPlantsMapping.CPPPlant_FK_Id = cppPlantId (outside/process plants)
            // NormParameters are fetched for all such plants.
            StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT np.*, nt.NormName AS NormTypeName " +
                "FROM NormParameters np " +
                "INNER JOIN NormTypes nt ON nt.Id = np.NormType_FK_Id " +
                "INNER JOIN NormParameterType npt ON npt.Id = np.NormParameterType_FK_Id " +
                "INNER JOIN Plants p ON p.Id = np.Plant_FK_Id " +
                "LEFT JOIN CPPProcessPlantsMapping m ON m.ProcessPlant_FK_Id = p.Id AND m.IsActive = 1 " +
                "WHERE npt.IsActive = 1 AND npt.Name IN (" + normTypeFilter + ") " +
                "AND (p.SourceName = CAST(? AS VARCHAR(36)) OR m.CPPPlant_FK_Id = CAST(? AS VARCHAR(36)))"
            );
            List<Object> params = new ArrayList<>();
            params.addAll(UTILITY_NORM_PARAMETER_TYPES);
            params.add(plantId);  // SourceName match
            params.add(plantId);  // CPPProcessPlantsMapping match

            if (normTypeId != null) {
                sql.append(" AND np.NormType_FK_Id = ?");
                params.add(normTypeId);
            }
            sql.append(" ORDER BY nt.NormName, np.DisplayOrder, np.Name");

            List<Map<String, Object>> rows = db1JdbcTemplate.queryForList(sql.toString(), params.toArray());

            List<NormParameterDTO> data = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                NormParameterDTO dto = new NormParameterDTO();
                dto.setId(toUuid(row, "Id"));
                dto.setName(str(row, "Name"));
                dto.setDisplayName(str(row, "DisplayName"));
                dto.setUom(str(row, "UOM"));
                dto.setExpression(str(row, "Expression"));
                dto.setExecuteQuery(str(row, "ExecuteQuery"));
                dto.setDependantAttributeId(toUuid(row, "DependantAttributeId"));
                dto.setType(str(row, "Type"));
                dto.setNormParameterTypeFkId(toUuid(row, "NormParameterType_FK_Id"));
                dto.setPlantFkId(toUuid(row, "Plant_FK_Id"));
                dto.setNormTypeFkId(toInt(row, "NormType_FK_Id"));
                dto.setIsHistorical(toBool(row, "IsHistorical"));
                dto.setDisplayOrder(toInt(row, "DisplayOrder"));
                dto.setIsEditable(toBool(row, "IsEditable"));
                dto.setIsVisible(toBool(row, "IsVisible"));
                dto.setCalculationType(str(row, "CalculationType"));
                dto.setSapMaterialCode(str(row, "SAPMaterialCode"));
                dto.setNormTypeName(str(row, "NormTypeName"));
                data.add(dto);
            }

            logger.info("getNormParametersBySourcePlant: {} records returned", data.size());
            response.setCode(200);
            response.setMessage(data.size() + " record(s) found.");
            response.setData(data);

        } catch (Exception e) {
            logger.error("getNormParametersBySourcePlant error: {}", e.getMessage(), e);
            response.setCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }

    private Integer toInt(Map<String, Object> row, String key) {
        Object val = row.get(key);
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); }
        catch (NumberFormatException e) {
            logger.warn("Could not parse Integer for column {}: {}", key, val);
            return null;
        }
    }

    private Boolean toBool(Map<String, Object> row, String key) {
        Object val = row.get(key);
        if (val == null) return null;
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof Number) return ((Number) val).intValue() != 0;
        return Boolean.parseBoolean(val.toString());
    }

    /** Null-safe Long from result-set row. */
    private Long toLong(Map<String, Object> row, String key) {
        Object val = row.get(key);
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        try { return Long.parseLong(val.toString()); }
        catch (NumberFormatException e) {
            logger.warn("Could not parse Long for column {}: {}", key, val);
            return null;
        }
    }

    /** Null-safe Double from result-set row. */
    private Double toDouble(Map<String, Object> row, String key) {
        Object val = row.get(key);
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); }
        catch (NumberFormatException e) {
            logger.warn("Could not parse Double for column {}: {}", key, val);
            return null;
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SR MAPPING QTY  (SP: CPP_GetSRMappingQTY)
    //  GET + EXPORT
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public AOPMessageVM getSRMappingQty(String plantIds, String financialYear) {
        logger.info("getSRMappingQty: plantIds={}, financialYear={}", plantIds, financialYear);
        AOPMessageVM response = new AOPMessageVM();
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(db1JdbcTemplate)
                    .withProcedureName("CPP_GetSRMappingQTY");

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("PlantIds",      plantIds)
                    .addValue("FinancialYear", financialYear);

            Map<String, Object> result = jdbcCall.execute(params);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows =
                    (List<Map<String, Object>>) result.get("#result-set-1");

            List<SRMappingQtyDTO> data = new ArrayList<>();
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    SRMappingQtyDTO dto = new SRMappingQtyDTO();

                    dto.setId                    (toLong  (row, "id"));
                    dto.setCppPlantId            (str     (row, "cppPlantId"));
                    dto.setCppPlantName          (str     (row, "cppPlantName"));

                    dto.setSenderPlantName       (str     (row, "senderPlantName"));
                    dto.setSenderPlantCode       (str     (row, "senderPlantCode"));
                    dto.setSenderPlantId         (toUuid  (row, "senderPlantId"));

                    dto.setSenderUtilityId       (toUuid  (row, "senderUtilityId"));
                    dto.setSenderUtilityName     (str     (row, "senderUtilityName"));
                    dto.setSenderUtilityCode     (str     (row, "senderUtilityCode"));
                    dto.setSenderUtilityUOM      (str     (row, "senderUtilityUOM"));

                    dto.setSenderCostCenterId    (toUuid  (row, "senderCostCenterId"));
                    dto.setSenderCostCenterName  (str     (row, "senderCostCenterName"));
                    dto.setSenderCostCenterCode  (str     (row, "senderCostCenterCode"));

                    dto.setReceiverPlantName     (str     (row, "receiverPlantName"));
                    dto.setReceiverPlantCode     (str     (row, "receiverPlantCode"));
                    dto.setReceiverPlantId       (toUuid  (row, "receiverPlantId"));

                    dto.setReceiverUtilityId     (toUuid  (row, "receiverUtilityId"));
                    dto.setReceiverUtilityName   (str     (row, "receiverUtilityName"));
                    dto.setReceiverUtilityCode   (str     (row, "receiverUtilityCode"));
                    dto.setReceiverUtilityUOM    (str     (row, "receiverUtilityUOM"));

                    dto.setReceiverCostCenterId  (toUuid  (row, "receiverCostCenterId"));
                    dto.setReceiverCostCenterName(str     (row, "receiverCostCenterName"));
                    dto.setReceiverCostCenterCode(str     (row, "receiverCostCenterCode"));

                    dto.setRemarks               (str     (row, "remarks"));

                    // Monthly QTY (Apr → Mar)
                    dto.setApr(toDouble(row, "apr"));
                    dto.setMay(toDouble(row, "may"));
                    dto.setJun(toDouble(row, "jun"));
                    dto.setJul(toDouble(row, "jul"));
                    dto.setAug(toDouble(row, "aug"));
                    dto.setSep(toDouble(row, "sep"));
                    dto.setOct(toDouble(row, "oct"));
                    dto.setNov(toDouble(row, "nov"));
                    dto.setDec(toDouble(row, "dec"));
                    dto.setJan(toDouble(row, "jan"));
                    dto.setFeb(toDouble(row, "feb"));
                    dto.setMar(toDouble(row, "mar"));

                    data.add(dto);
                }
            }

            logger.info("getSRMappingQty: {} records returned", data.size());
            response.setCode(200);
            response.setMessage(data.size() + " record(s) found.");
            response.setData(data);

        } catch (Exception e) {
            logger.error("getSRMappingQty error: {}", e.getMessage(), e);
            response.setCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }

    @Override
    public byte[] exportSRMappingQty(String plantIds, String financialYear) {
        logger.info("[Export SR Mapping QTY] plantIds: {}, financialYear: {}", plantIds, financialYear);
        try {
            AOPMessageVM response = getSRMappingQty(plantIds, financialYear);
            @SuppressWarnings("unchecked")
            List<SRMappingQtyDTO> dtoList = (List<SRMappingQtyDTO>) response.getData();
            if (dtoList == null) {
                dtoList = new ArrayList<>();
            }

            // Preserve SP ordering: senderPlantName → receiverPlantName → receiverCostCenterName
            dtoList.sort(Comparator
                    .comparing((SRMappingQtyDTO d) -> d.getSenderPlantName() != null ? d.getSenderPlantName() : "")
                    .thenComparing(d -> d.getReceiverPlantName() != null ? d.getReceiverPlantName() : "")
                    .thenComparing(d -> d.getReceiverCostCenterName() != null ? d.getReceiverCostCenterName() : ""));

            return buildSRMappingQtyExcel(dtoList, financialYear);
        } catch (Exception e) {
            logger.error("[Export SR Mapping QTY] Error: {}", e.getMessage(), e);
            return null;
        }
    }

    private byte[] buildSRMappingQtyExcel(List<SRMappingQtyDTO> dtoList, String financialYear) throws Exception {
        org.apache.poi.ss.usermodel.Workbook workbook = new XSSFWorkbook();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("CPP_SRMapping_QTY");

        CellStyle headerStyle  = ExcelStyles.createHeaderStyle(workbook);
        CellStyle dataStyle    = ExcelStyles.createDataStyle(workbook);

        String[] monthHeaders = FiscalYearMonths.getMonthHeaders(financialYear);

        // Header row
        List<String> headers = new ArrayList<>();
        headers.add("CPP Plant Id");
        headers.add("CPP Plant Name");
        headers.add("Sender Plant Name");
        headers.add("Sender Plant Code");
        headers.add("Sender Utility Name");
        headers.add("Sender Utility Code");
        headers.add("Sender Utility UOM");
        headers.add("Sender Cost Center Name");
        headers.add("Sender Cost Center Code");
        headers.add("Receiver Plant Name");
        headers.add("Receiver Plant Code");
        headers.add("Receiver Utility Name");
        headers.add("Receiver Utility Code");
        headers.add("Receiver Utility UOM");
        headers.add("Receiver Cost Center Name");
        headers.add("Receiver Cost Center Code");
        // 12 months × 1 (QTY)
        for (String mh : monthHeaders) {
            headers.add(mh);
        }
        // Hidden id column
        headers.add("id");

        Row headerRow = sheet.createRow(0);
        for (int c = 0; c < headers.size(); c++) {
            ExcelCells.setString(headerRow.createCell(c), headers.get(c), headerStyle);
        }

        // Data rows
        int rowNum = 1;
        for (SRMappingQtyDTO dto : dtoList) {
            Row row = sheet.createRow(rowNum++);
            int col = 0;

            ExcelCells.setString(row.createCell(col++), dto.getCppPlantId(),           dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getCppPlantName(),         dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSenderPlantName(),      dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSenderPlantCode(),      dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSenderUtilityName(),    dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSenderUtilityCode(),    dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSenderUtilityUOM(),     dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSenderCostCenterName(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSenderCostCenterCode(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverPlantName(),    dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverPlantCode(),    dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverUtilityName(),  dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverUtilityCode(),  dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverUtilityUOM(),   dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverCostCenterName(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverCostCenterCode(), dataStyle);

            // Monthly QTY (Apr → Mar)
            ExcelCells.setDouble(row.createCell(col++), dto.getApr(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMay(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getJun(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getJul(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getAug(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getSep(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getOct(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getNov(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getDec(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getJan(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getFeb(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMar(), dataStyle);

            // Hidden id
            ExcelCells.setString(row.createCell(col++),
                    dto.getId() != null ? dto.getId().toString() : "", dataStyle);
        }

        // Auto-size all columns first (before hiding, so autoSizeColumn
        // does not reset the hidden state of CPP Plant Id / id columns)
        ExcelColumns.autoSize(sheet, headers.size(), -1);

        // Hide CPP Plant Id (index 0) and id (last column)
        int idColIndex = headers.size() - 1;
        ExcelColumns.hideColumns(sheet, 0, idColIndex);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }
}
