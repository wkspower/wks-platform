package com.wks.caseengine.cpp.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.cpp.dto.SpinningMarginReportDTO;
import com.wks.caseengine.cpp.repository.CPPMonthlySpinningMarginMaintainedRepository;
import com.wks.caseengine.cpp.service.JMDSpinningMarginReportService;
import com.wks.caseengine.cpp.utility.ExcelCells;
import com.wks.caseengine.cpp.utility.ExcelStyles;
import com.wks.caseengine.cpp.utility.FiscalYearMonths;
import com.wks.caseengine.message.vm.AOPMessageVM;

import lombok.extern.slf4j.Slf4j;

/**
 * Read-only Spinning Margin Maintained report service implementation.
 *
 * <p>Reads directly from {@code dbo.CPPMonthlySpinningMarginMaintained}
 * for the selected plant(s) and AOP year, and exports the same data to Excel.</p>
 */
@Service
@Slf4j
public class JMDSpinningMarginReportServiceImpl implements JMDSpinningMarginReportService {

    @Autowired
    private CPPMonthlySpinningMarginMaintainedRepository repository;

    // ─────────────────────────────────────────────────────────
    // GET
    // ─────────────────────────────────────────────────────────

    @Override
    public AOPMessageVM getSpinningMarginReport(List<UUID> plantIds, String aopYear) {
        log.info("[SpinningMarginReport] GET - plantIds: {}, aopYear: {}", plantIds, aopYear);
        AOPMessageVM vm = new AOPMessageVM();

        if (plantIds == null || plantIds.isEmpty()) {
            vm.setCode(400);
            vm.setMessage("plantIds cannot be null or empty");
            vm.setData(new ArrayList<>());
            return vm;
        }

        if (aopYear == null || aopYear.isEmpty()) {
            vm.setCode(400);
            vm.setMessage("aopYear cannot be null or empty");
            vm.setData(new ArrayList<>());
            return vm;
        }

        try {
            List<Object[]> rawRows = repository.findByPlantIdsAndAopYear(plantIds, aopYear);
            log.info("[SpinningMarginReport] GET - found {} records", rawRows.size());

            List<SpinningMarginReportDTO> dtoList = new ArrayList<>();
            for (Object[] row : rawRows) {
                dtoList.add(mapRowToDto(row));
            }

            vm.setCode(200);
            vm.setMessage("Success");
            vm.setData(dtoList);
        } catch (Exception e) {
            log.error("[SpinningMarginReport] GET error: {}", e.getMessage(), e);
            vm.setCode(500);
            vm.setMessage("Error: " + e.getMessage());
            vm.setData(new ArrayList<>());
        }

        return vm;
    }

    // ─────────────────────────────────────────────────────────
    // EXPORT
    // ─────────────────────────────────────────────────────────

    @Override
    public byte[] exportSpinningMarginReport(List<UUID> plantIds, String aopYear) throws IOException {
        log.info("[SpinningMarginReport] EXPORT - plantIds: {}, aopYear: {}", plantIds, aopYear);

        try {
            AOPMessageVM result = getSpinningMarginReport(plantIds, aopYear);

            List<SpinningMarginReportDTO> dtoList = new ArrayList<>();
            if (result.getData() instanceof List) {
                @SuppressWarnings("unchecked")
                List<SpinningMarginReportDTO> data = (List<SpinningMarginReportDTO>) result.getData();
                dtoList = data;
            }

            if (dtoList == null || dtoList.isEmpty()) {
                log.warn("[SpinningMarginReport] EXPORT - no data found");
                dtoList = new ArrayList<>();
            }

            return generateExcel(dtoList, aopYear);
        } catch (IOException e) {
            log.error("[SpinningMarginReport] EXPORT IOException: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[SpinningMarginReport] EXPORT error: {}", e.getMessage(), e);
            throw new IOException("Failed to export Spinning Margin report: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────

    /**
     * Map a native query result row to the report DTO.
     *
     * Expected column order (see
     * {@link CPPMonthlySpinningMarginMaintainedRepository#findByPlantIdsAndAopYear}):
     *   0: Id, 1: AOPYear, 2: MarginType, 3: Plant_FK_Id,
     *   4: PlantName, 5..16: Apr .. Mar
     */
    private SpinningMarginReportDTO mapRowToDto(Object[] row) {
        SpinningMarginReportDTO dto = new SpinningMarginReportDTO();
        dto.setId(row[0] != null ? UUID.fromString(row[0].toString()) : null);
        dto.setAopYear(row[1] != null ? row[1].toString() : null);
        dto.setMarginType(row[2] != null ? row[2].toString() : null);
        dto.setPlantFkId(row[3] != null ? UUID.fromString(row[3].toString()) : null);
        dto.setPlantName(row[4] != null ? row[4].toString() : null);
        dto.setApr(toDoubleObj(row[5]));
        dto.setMay(toDoubleObj(row[6]));
        dto.setJun(toDoubleObj(row[7]));
        dto.setJul(toDoubleObj(row[8]));
        dto.setAug(toDoubleObj(row[9]));
        dto.setSep(toDoubleObj(row[10]));
        dto.setOct(toDoubleObj(row[11]));
        dto.setNov(toDoubleObj(row[12]));
        dto.setDec(toDoubleObj(row[13]));
        dto.setJan(toDoubleObj(row[14]));
        dto.setFeb(toDoubleObj(row[15]));
        dto.setMar(toDoubleObj(row[16]));
        return dto;
    }

    private Double toDoubleObj(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Normalise various incoming financial-year formats to the format
     * stored in the table: 'YYYY-YY' e.g. '2026-27'.
     *
     * Supported inputs: '2026-27', '2026/27', '2026-2027', '2026', '26-27'
     */
    private String normaliseFinancialYear(String fy) {
        if (fy == null) return "";
        String s = fy.trim().replace("/", "-");

        // Already in short form: '2026-27'
        if (s.matches("\\d{4}-\\d{2}")) return s;

        // Long form: '2026-2027'
        if (s.matches("\\d{4}-\\d{4}")) {
            return s.substring(0, 4) + "-" + s.substring(7, 9);
        }

        // Only start year: '2026'
        if (s.matches("\\d{4}")) {
            int startYear = Integer.parseInt(s);
            return startYear + "-" + String.format("%02d", (startYear + 1) % 100);
        }

        // Short form with 2-digit years: '26-27'
        if (s.matches("\\d{2}-\\d{2}")) {
            return "20" + s.substring(0, 2) + "-" + s.substring(3, 5);
        }

        return s;
    }

    private byte[] generateExcel(List<SpinningMarginReportDTO> dtoList, String aopYear) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Spinning Margin Report");

        CellStyle headerStyle  = ExcelStyles.createHeaderStyle(workbook);
        CellStyle dataStyle    = ExcelStyles.createDataStyle(workbook);
        CellStyle numericStyle = ExcelStyles.createNumericStyle(workbook, "0.##########");

        int rowNum = 0;
        int col    = 0;

        Row headerRow = sheet.createRow(rowNum++);

        ExcelCells.setString(headerRow.createCell(col++), "Plant Name",  headerStyle);
        ExcelCells.setString(headerRow.createCell(col++), "Type", headerStyle);

        // FiscalYearMonths expects a "YYYY-..." form; normalise first so any
        // incoming UI format (e.g. "2026", "26-27") is handled safely.
        String[] months = FiscalYearMonths.getMonthHeaders(normaliseFinancialYear(aopYear));

        int monthStartCol = col;
        for (String month : months) {
            ExcelCells.setString(headerRow.createCell(col++), month, headerStyle);
        }

        int totalColumns = col;

        for (SpinningMarginReportDTO dto : dtoList) {
            Row row = sheet.createRow(rowNum++);
            col = 0;

            ExcelCells.setString(row.createCell(col++),
                    dto.getPlantName() != null ? dto.getPlantName() : "", dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getMarginType(), dataStyle);

            ExcelCells.setDouble(row.createCell(monthStartCol),      dto.getApr(), numericStyle);
            ExcelCells.setDouble(row.createCell(monthStartCol + 1),  dto.getMay(), numericStyle);
            ExcelCells.setDouble(row.createCell(monthStartCol + 2),  dto.getJun(), numericStyle);
            ExcelCells.setDouble(row.createCell(monthStartCol + 3),  dto.getJul(), numericStyle);
            ExcelCells.setDouble(row.createCell(monthStartCol + 4),  dto.getAug(), numericStyle);
            ExcelCells.setDouble(row.createCell(monthStartCol + 5),  dto.getSep(), numericStyle);
            ExcelCells.setDouble(row.createCell(monthStartCol + 6),  dto.getOct(), numericStyle);
            ExcelCells.setDouble(row.createCell(monthStartCol + 7),  dto.getNov(), numericStyle);
            ExcelCells.setDouble(row.createCell(monthStartCol + 8),  dto.getDec(), numericStyle);
            ExcelCells.setDouble(row.createCell(monthStartCol + 9),  dto.getJan(), numericStyle);
            ExcelCells.setDouble(row.createCell(monthStartCol + 10), dto.getFeb(), numericStyle);
            ExcelCells.setDouble(row.createCell(monthStartCol + 11), dto.getMar(), numericStyle);
        }

        for (int i = 0; i < totalColumns; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }
}
