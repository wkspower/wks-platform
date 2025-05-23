package com.wks.caseengine.report.export;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@Component
public class ExcelExportUtil {

    /**
     * Exports multiple datasets into a single Excel sheet, each as a separate table.
     *
     * @param dataMap      A map where each key is the table title and the value is the list of data rows.
     * @param outputStream The OutputStream to write the Excel file to.
     * @throws Exception If an error occurs during Excel file generation.
     */
    public static void exportToExcel(Map<String, List<Map<String, Object>>> dataMap, OutputStream outputStream) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Report");

        int rowIndex = 0;
        for (Map.Entry<String, List<Map<String, Object>>> entry : dataMap.entrySet()) {
            String tableTitle = entry.getKey();
            List<Map<String, Object>> tableData = entry.getValue();

            if (tableData == null || tableData.isEmpty()) {
                continue;
            }

            // Add table title
            Row titleRow = sheet.createRow(rowIndex++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(tableTitle);
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            // Add header row
            Row headerRow = sheet.createRow(rowIndex++);
            Map<String, Object> firstRow = tableData.get(0);
            int cellIndex = 0;
            for (String header : firstRow.keySet()) {
                Cell cell = headerRow.createCell(cellIndex++);
                cell.setCellValue(header);
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                cell.setCellStyle(headerStyle);
            }

            // Add data rows
            for (Map<String, Object> dataRow : tableData) {
                Row row = sheet.createRow(rowIndex++);
                cellIndex = 0;
                for (Object value : dataRow.values()) {
                    Cell cell = row.createCell(cellIndex++);
                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else if (value instanceof Boolean) {
                        cell.setCellValue((Boolean) value);
                    } else {
                        cell.setCellValue(value != null ? value.toString() : "");
                    }
                }
            }

            // Add an empty row between tables
            rowIndex++;
        }

        // Auto-size columns
        for (int i = 0; i < sheet.getRow(1).getPhysicalNumberOfCells(); i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(outputStream);
        workbook.close();
    }
}

