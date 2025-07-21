package com.wks.caseengine.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.utility.ExcelConstants;

@Service
public class ExcelUtilityServiceImpl implements ExcelUtilityService {

    public byte[] generateFlexibleExcel(Map<String, Object> structure, Map<String, List<List<Object>>> data) {
        try {
            Workbook workbook = new XSSFWorkbook();
            CellStyle borderStyle = createBorderedStyle(workbook);
            CellStyle boldStyle = createBoldStyle(workbook);

            System.out.println("postman structure" + structure);

            for (String sheetName : structure.keySet()) {
                Map<String, Object> sheetData = (Map<String, Object>) structure.get(sheetName);
                List<Map<String, Object>> tables = (List<Map<String, Object>>) sheetData.get(ExcelConstants.TABLES);
                int columnCount = (Integer) sheetData.get(ExcelConstants.COULMNCOUNT);

                Sheet sheet = workbook.createSheet(sheetName);
                int currentRow = 0;
                int tableCount = -1;
                System.out.println("data" + data);
                for (Map<String, Object> table : tables) {
                    String title = "";
                    tableCount++;
                    Integer startRow = (table.get(ExcelConstants.STARTROW) == null) ? currentRow
                            : (int) table.get(ExcelConstants.STARTROW);
                    List<List<String>> headers = (List<List<String>>) table.get(ExcelConstants.HEADERSTITLES);
                    if (headers.size() > columnCount) {
                        columnCount = headers.size();
                    }
                    List<List<Object>> rows = new ArrayList<>();

                    title = (String) table.get(ExcelConstants.TITLE);
                    String tableId = (String) table.get(ExcelConstants.TABLEID);
                    rows = data.get(tableId);
                    System.out.println("rows " + rows);
                    Boolean isColumnMergeRequired = (Boolean) table.get(ExcelConstants.IS_COLUMN_MERGE_REQUIRED);
                    Boolean isRowMergeRequired = (Boolean) table.get(ExcelConstants.IS_ROW_MERGE_REQUIRED);
                    List<Integer> hiddenColumnsList = (List<Integer>) table.get(ExcelConstants.HIDDEN_COLUMNS);

                    Map<String, Object> styles = (Map<String, Object>) table.get(ExcelConstants.STYLES);
                    Map<String, Object> autoMerge = (Map<String, Object>) table.get(ExcelConstants.AUTOMERGE);

                    Set<Integer> boldCols = new HashSet<>();
                    if (styles != null && styles.get(ExcelConstants.BOLDCOLUMNS) != null) {
                        for (int col : (List<Integer>) styles.get(ExcelConstants.BOLDCOLUMNS)) {
                            boldCols.add(col);
                        }
                    }

                    boolean borders = styles != null && Boolean.TRUE.equals(styles.get(ExcelConstants.BORDERS));

                    currentRow = Math.max(currentRow, startRow);
                    // currentRow += 1;
                    if (title != null && !title.isEmpty()) {
                        Row titleRow = sheet.createRow(currentRow++);
                        Cell titleCell = titleRow.createCell(0);
                        titleCell.setCellValue(title);
                        titleCell.setCellStyle(boldStyle);

                        currentRow++;
                    }

                    int headerStartRow = currentRow;
                    for (List<String> headerRowData : headers) {
                        Row headerRow = sheet.createRow(currentRow++);
                        for (int col = 0; col < headerRowData.size(); col++) {
                            Cell cell = headerRow.createCell(col);
                            cell.setCellValue(headerRowData.get(col));
                            cell.setCellStyle(createBoldBorderedStyle(workbook));
                        }
                    }
                    mergeHeaderCells(sheet, headers, headerStartRow);

                    int startDataRow = currentRow;

                    // Write structure rows
                    for (List<Object> rowData : rows) {
                        Row row = sheet.createRow(currentRow++);
                        for (int col = 0; col < rowData.size(); col++) {
                            Cell cell = row.createCell(col);
                            Object value = rowData.get(col);

                            if (value instanceof Number) {
                                cell.setCellValue(((Number) value).doubleValue()); // Handles Integer, Double, etc.
                            } else if (value instanceof Boolean) {
                                cell.setCellValue((Boolean) value);
                            } else if (value != null) {
                                cell.setCellValue(value.toString());
                            } else {
                                cell.setCellValue("");
                            }

                            if (boldCols.contains(col))
                                cell.setCellStyle(boldStyle);
                            if (borders)
                                cell.setCellStyle(borderStyle);
                        }
                    }

                    if (isColumnMergeRequired) {
                        // Auto merge rows
                        // Auto merge rows (vertical merge across rows in specific columns)
                        if (autoMerge != null && autoMerge.get(ExcelConstants.COLUMNS) != null) {
                            for (int colIndex : (List<Integer>) autoMerge.get(ExcelConstants.COLUMNS)) {
                                int mergeStart = startDataRow;
                                String lastVal = null;

                                for (int r = startDataRow; r < currentRow; r++) {
                                    Row row = sheet.getRow(r);
                                    Cell cell = (row != null) ? row.getCell(colIndex) : null;
                                    String val = getCellStringValue(cell);

                                    if (lastVal == null) {
                                        lastVal = val;
                                        mergeStart = r;
                                    } else if (!Objects.equals(lastVal, val)) {
                                        if (r - 1 > mergeStart) {
                                            sheet.addMergedRegion(
                                                    new CellRangeAddress(mergeStart, r - 1, colIndex, colIndex));
                                        }
                                        lastVal = val;
                                        mergeStart = r;
                                    }
                                }

                                if (currentRow - 1 > mergeStart) {
                                    sheet.addMergedRegion(
                                            new CellRangeAddress(mergeStart, currentRow - 1, colIndex, colIndex));
                                }
                            }
                        }
                    }

                    if (isRowMergeRequired) {
                        // Auto merge columns
                        // Auto merge columns (horizontal merge across columns in specific rows)
                        if (autoMerge != null && autoMerge.get(ExcelConstants.ROWS) != null) {
                            for (int rowIndex : (List<Integer>) autoMerge.get(ExcelConstants.ROWS)) {
                                int mergeStart = 0;
                                String lastVal = null;
                                Row row = sheet.getRow(startDataRow + rowIndex);
                                if (row == null) // continue;

                                    for (int c = 0; c < row.getLastCellNum(); c++) {
                                        Cell cell = row.getCell(c);
                                        String val = getCellStringValue(cell);

                                        if (lastVal == null) {
                                            lastVal = val;
                                            mergeStart = c;
                                        } else if (!Objects.equals(lastVal, val)) {
                                            if (c - 1 > mergeStart) {
                                                sheet.addMergedRegion(new CellRangeAddress(startDataRow + rowIndex,
                                                        startDataRow + rowIndex, mergeStart, c - 1));
                                            }
                                            lastVal = val;
                                            mergeStart = c;
                                        }
                                    }

                                // Check the last segment for merging
                                if (row.getLastCellNum() - 1 > mergeStart) {
                                    sheet.addMergedRegion(new CellRangeAddress(startDataRow + rowIndex,
                                            startDataRow + rowIndex, mergeStart, row.getLastCellNum() - 1));
                                }
                            }
                        }
                    }
                    for (Integer column : hiddenColumnsList) {
                        sheet.setColumnHidden(column, true);
                    }
                    currentRow += 1;

                }
                for (int i = 0; i < columnCount; i++) {
                    sheet.autoSizeColumn(i);
                }

                sheet.setDisplayGridlines(false);
            }

            // File outputDir = new File("output");
            // if (!outputDir.exists()) outputDir.mkdirs();

            try {// (FileOutputStream fileOut = new FileOutputStream("output/generated.xlsx")) {

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                workbook.write(outputStream);
                workbook.close();
                return outputStream.toByteArray();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private CellStyle createBorderedStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createBoldStyle(Workbook wb) {
        Font font = wb.createFont();
        font.setBold(true);
        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        return style;
    }

    private CellStyle createBoldBorderedStyle(Workbook workbook) {

        CellStyle style = createBorderedStyle(workbook);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private void mergeHeaderCells(Sheet sheet, List<List<String>> headers, int startRow) {
        int rows = headers.size();
        int cols = headers.get(0).size();

        // Horizontal merge
        for (int row = 0; row < rows; row++) {
            int col = 0;
            while (col < cols) {
                String cellValue = headers.get(row).get(col);
                int mergeStart = col;
                while (col + 1 < cols && cellValue.equals(headers.get(row).get(col + 1))) {
                    col++;
                }
                if (mergeStart != col) {
                    sheet.addMergedRegion(new CellRangeAddress(startRow + row, startRow + row, mergeStart, col));
                }
                col++;
            }
        }

        // Vertical merge
        for (int col = 0; col < cols; col++) {
            int row = 0;
            while (row < rows - 1) {
                String cellValue = headers.get(row).get(col);
                int mergeStart = row;
                while (row + 1 < rows && cellValue.equals(headers.get(row + 1).get(col))) {
                    row++;
                }
                if (mergeStart != row) {
                    sheet.addMergedRegion(new CellRangeAddress(startRow + mergeStart, startRow + row, col, col));
                }
                row++;
            }
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null)
            return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK, ERROR -> null;
            default -> null;
        };
    }

    private void createTitleBlock(Sheet sheet, String title, String plant, String date, Workbook workbook,
            String site) {
        int totalColumns = 13;
        int titleBlockHeight = 4;

        // Create rows and merge 13 columns for each
        for (int i = 0; i < titleBlockHeight; i++) {
            Row row = sheet.createRow(i);
            row.setHeightInPoints(25);
            // sheet.addMergedRegion(new CellRangeAddress(i, i, 0, totalColumns - 1));
        }

        // Styles
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);

        // Base style with no borders
        CellStyle baseStyle = workbook.createCellStyle();
        baseStyle.setAlignment(HorizontalAlignment.LEFT);
        baseStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        baseStyle.setFont(boldFont);

        // Centered style for title
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setFont(boldFont);

        // Border style for outer border only
        CellStyle outerBorderStyle = workbook.createCellStyle();
        outerBorderStyle.setBorderTop(BorderStyle.THIN);
        outerBorderStyle.setBorderBottom(BorderStyle.THIN);
        outerBorderStyle.setBorderLeft(BorderStyle.THIN);
        outerBorderStyle.setBorderRight(BorderStyle.THIN);

        // Fill content
        Cell cellProduct = sheet.getRow(1).createCell(0);
        cellProduct.setCellValue("Site: " + site);
        cellProduct.setCellStyle(baseStyle);

        Cell cellDate = sheet.getRow(2).createCell(0);
        cellDate.setCellValue("Date: " + date);
        cellDate.setCellStyle(baseStyle);

        Cell cellPlant = sheet.getRow(2).createCell(11);
        cellPlant.setCellValue("Plant: " + plant);
        cellPlant.setCellStyle(baseStyle);

        Cell cellTitle = sheet.getRow(3).createCell(5);
        cellTitle.setCellValue(title);
        cellTitle.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 5, 6));

        // Apply outer border only to corners
        for (int r = 0; r < titleBlockHeight; r++) {
            Row row = sheet.getRow(r);
            for (int c = 0; c < totalColumns; c++) {
                boolean isTop = r == 0;
                boolean isBottom = r == titleBlockHeight - 1;
                boolean isLeft = c == 0;
                boolean isRight = c == totalColumns - 1;

                if (isTop || isBottom || isLeft || isRight) {
                    Cell cell = row.getCell(c);
                    if (cell == null)
                        cell = row.createCell(c);

                    CellStyle edgeStyle = workbook.createCellStyle();
                    edgeStyle.cloneStyleFrom(cell.getCellStyle());

                    if (isTop)
                        edgeStyle.setBorderTop(BorderStyle.THIN);
                    if (isBottom)
                        edgeStyle.setBorderBottom(BorderStyle.THIN);
                    if (isLeft)
                        edgeStyle.setBorderLeft(BorderStyle.THIN);
                    if (isRight)
                        edgeStyle.setBorderRight(BorderStyle.THIN);

                    cell.setCellStyle(edgeStyle);
                }
            }
        }
    }

    // Utility to clear all borders from a style
    private void clearAllBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
    }

    public List<String> getAcademicYearMonths(String year) {
        List<String> months = new ArrayList<>();
        int startYear = Integer.parseInt(year.substring(0, 4));
        int nextYear = startYear + 1;

        // Apr to Dec of startYear
        for (int month = 4; month <= 12; month++) {
            String label = formatMonthYear(month, startYear);
            months.add(label);
        }

        // Jan to Mar of nextYear
        for (int month = 1; month <= 3; month++) {
            String label = formatMonthYear(month, nextYear);
            months.add(label);
        }

        return months;
    }

    private String formatMonthYear(int month, int year) {
        LocalDate date = LocalDate.of(year, month, 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-yy", Locale.ENGLISH);
        return date.format(formatter);
    }

}
