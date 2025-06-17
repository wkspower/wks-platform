package com.wks.caseengine.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.rest.entity.Site;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExcelServiceImpl implements ExcelService {

    @Autowired
    ExcelDataService excelDataService;

    @Autowired
    private PlantsRepository plantsRepository;
    @Autowired
    private VerticalsRepository verticalRepository;

    @Autowired
    private SiteRepository siteRepository;

    public byte[] generateFlexibleExcel(Map<String, Object> data1, String plantId, String year) {
        try {
            Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
            Sites site = siteRepository.findById(plant.getSiteFkId()).get();
            Workbook workbook = new XSSFWorkbook();
            CellStyle borderStyle = createBorderedStyle(workbook);
            CellStyle boldStyle = createBoldStyle(workbook);

            ObjectMapper mapper = new ObjectMapper();
            String previousYear = getPreviousYear(year);
            String previous2Year = getPrevious2Year(year);
            String previous3Year = getPrevious3Year(year);
            String nextYear = getNextYear(year);

            // Get month labels
            List<String> monthsList = getAcademicYearMonths(year);
            // String months = String.join("\", ", monthsList);

            String months = monthsList.stream()
                    .map(month -> "\"" + month + "\"")
                    .collect(Collectors.joining(", "));
            String dataStr = getData("\"" + year + "\"", previousYear, nextYear, months, previous2Year, previous3Year);
            System.out.println(dataStr);
            Map<String, Object> data = mapper.readValue(dataStr, Map.class);
            System.out.println("postman data" + data);
            LocalDate today = LocalDate.now();
            String formattedDate = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            for (String sheetName : data.keySet()) {
                Map<String, Object> sheetData = (Map<String, Object>) data.get(sheetName);
                List<Map<String, Object>> tables = (List<Map<String, Object>>) sheetData.get("tables");

                Sheet sheet = workbook.createSheet(sheetName);
                int currentRow = 0;
                Map<String, List<List<Object>>> monthWiseRawData = null;
                createTitleBlock(sheet, sheetName, plant.getDisplayName(), formattedDate, workbook,
                        site.getDisplayName());
                if (sheetName.equalsIgnoreCase("Monthwise Raw Data")) {
                    monthWiseRawData = excelDataService.getReportForMonthWiseConsumptionSummaryData(plantId, year);
                }
                int columnCount = 13;

                int tableCount = -1;
                for (Map<String, Object> table : tables) {
                    String title = "";
                    tableCount++;
                    Integer startRow = (table.get("startRow") == null) ? currentRow : (int) table.get("startRow");
                    List<List<String>> headers = (List<List<String>>) table.get("headers");
                    if (headers.size() > columnCount) {
                        columnCount = headers.size();
                    }
                    List<List<Object>> rows = new ArrayList<>();
                    if (sheetName.equalsIgnoreCase("Annual AOP Cost")) {
                        if (tableCount == 0) {
                            title = "Production Data";
                            Map<String,Object> map = excelDataService.getProductionAOPWorkflowData(plantId, year);
                                   
                            rows = (List<List<Object>>) map.get("rows"); 
                            List<String> headerList =  (List<String>) map.get("headers");  
                            headers.add(headerList);
                        }
                        if (tableCount == 1) {
                            title = "Annual AOP Cost";
                            Map<String,Object> map = excelDataService.getAnnualAOPWorkflowData(plantId, year);
                                   
                            rows = (List<List<Object>>) map.get("rows"); 
                            List<String> headerList =  (List<String>) map.get("headers");  
                            headers.add(headerList);
                        }
                    }else if (sheetName.equalsIgnoreCase("Plant Production Summary") && tableCount == 0) {
                        title = "Plant Production Summary (T-14)";
                        rows = excelDataService.getDataForProductionVolumeReport(plantId, year);
                    } else if (sheetName.equalsIgnoreCase("Monthwise production plan")) {
                        if (tableCount == 0) {
                            title = "Plant Production Summary (T-16)";
                            rows = excelDataService.getReportForMonthWiseProductionData(plantId, year);
                        }
                        if (tableCount == 1) {
                            title = "Main Products - Production for the budget year";
                            rows = excelDataService.getAOPData(plantId, year);
                        }
                    } else if (sheetName.equalsIgnoreCase("Monthwise Raw Data")) {

                        if (tableCount == 0) {
                            title = "Monthwise Consumption (T-18)";
                            rows = excelDataService.getReportForMonthWiseConsumptionForSelectivityData(plantId, year);
                        } else if (tableCount == 1) {

                            if (monthWiseRawData.containsKey("RawMaterial")) {
                                rows = monthWiseRawData.get("RawMaterial");
                                title = "RawMaterial";

                            } else {
                                continue;
                            }

                        } else if (tableCount == 2) {

                            if (monthWiseRawData.containsKey("ByProducts")) {
                                rows = monthWiseRawData.get("ByProducts");
                                title = "ByProducts";

                            } else {
                                continue;
                            }
                        } else if (tableCount == 3) {
                            if (monthWiseRawData.containsKey("CatChem")) {
                                rows = monthWiseRawData.get("CatChem");
                                title = "CatChem";
                            } else {
                                continue;
                            }
                        } else if (tableCount == 4) {
                            if (monthWiseRawData.containsKey("UtilityConsumption")) {
                                rows = monthWiseRawData.get("UtilityConsumption");
                                title = "UtilityConsumption";
                            } else {
                                continue;
                            }
                        } else if (tableCount == 5) {
                            if (monthWiseRawData.containsKey("Configuration")) {
                                rows = monthWiseRawData.get("Configuration");
                                title = "Configuration";
                            } else {
                                continue;
                            }
                        }
                    } else if (sheetName.equalsIgnoreCase("Turn Around Report")) {
                        if (tableCount == 0) {
                            title = "Turnaround Details (T-19A)";
                            rows = excelDataService.getReportForTurnAroundPlanData(plantId, year, "currentYear");
                        } else if (tableCount == 1) {
                            title = "Turnaround details for the previous years since commissioning";
                            rows = excelDataService.getReportForTurnAroundPlanData(plantId, year, "previousYear");
                        }

                    } else if (sheetName.equalsIgnoreCase("Annual Production Plan")) {
                        if (tableCount == 0) {
                            title = "Assumptions & remarks";
                            rows = excelDataService.getReportForPlantProductionPlanData(plantId, year, "assumptions");
                        } else if (tableCount == 1) {
                            title = "Max hourly rate achieved";
                            rows = excelDataService.getReportForPlantProductionPlanData(plantId, year, "maxRate");
                        } else if (tableCount == 2) {
                            title = "Calculation of Operating hours";
                            rows = excelDataService.getReportForPlantProductionPlanData(plantId, year, "OperatingHrs");
                        } else if (tableCount == 3) {
                            title = "Calculation of Average hourly rate";
                            rows = excelDataService.getReportForPlantProductionPlanData(plantId, year,
                                    "AverageHourlyRate");
                        } else if (tableCount == 4) {
                            title = "Production performance comparision with last 3 years";
                            rows = excelDataService.getReportForPlantProductionPlanData(plantId, year,
                                    "ProductionPerformance");
                        }

                    } else if (sheetName.equalsIgnoreCase("Plant Contribution")) {
                        if (tableCount == 0) {
                            title = "Product mix and Production";
                            rows = excelDataService.getReportForPlantContributionYearWise(plantId, year,
                                    "ProductMixAndProduction");
                        } else if (tableCount == 1) {
                            title = "By Products";
                            rows = excelDataService.getReportForPlantContributionYearWise(plantId, year, "ByProducts");
                        } else if (tableCount == 2) {
                            title = "Raw materials";
                            rows = excelDataService.getReportForPlantContributionYearWise(plantId, year, "RawMaterial");
                        } else if (tableCount == 3) {
                            title = "Cat Chem";
                            rows = excelDataService.getReportForPlantContributionYearWise(plantId, year, "CatChem");
                        } else if (tableCount == 4) {
                            title = "Utilities";
                            rows = excelDataService.getReportForPlantContributionYearWise(plantId, year, "Utilities");
                        } else if (tableCount == 5) {
                            title = "Other Variable Cost";
                            rows = excelDataService.getReportForPlantContributionYearWise(plantId, year,
                                    "OtherVariableCost");
                        } else if (tableCount == 6) {
                            title = "Production Cost Calculation";
                            rows = excelDataService.getReportForPlantContributionYearWise(plantId, year,
                                    "ProductionCostCalculations");
                        }
                    } else {
                        rows = (List<List<Object>>) table.get("rows");
                    }

                    Map<String, Object> styles = (Map<String, Object>) table.get("styles");
                    Map<String, Object> autoMerge = (Map<String, Object>) table.get("autoMerge");

                    Set<Integer> boldCols = new HashSet<>();
                    if (styles != null && styles.get("boldColumns") != null) {
                        for (int col : (List<Integer>) styles.get("boldColumns")) {
                            boldCols.add(col);
                        }
                    }

                    boolean borders = styles != null && Boolean.TRUE.equals(styles.get("borders"));

                    currentRow = Math.max(currentRow, startRow);
                    currentRow += 1;

                    // Write headers
                    // for (List<String> headerRow : headers) {
                    // Row row = sheet.createRow(currentRow++);
                    // for (int col = 0; col < headerRow.size(); col++) {
                    // Cell cell = row.createCell(col);
                    // cell.setCellValue(headerRow.get(col));
                    // if (boldCols.contains(col)) cell.setCellStyle(boldStyle);
                    // if (borders) cell.setCellStyle(borderStyle);
                    // }
                    // }

                    Row titleRow = sheet.createRow(currentRow++);
                    Cell titleCell = titleRow.createCell(0);
                    titleCell.setCellValue(title);
                    titleCell.setCellStyle(boldStyle);

                    currentRow++;
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

                    // Write data rows
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

                    // Auto merge rows
                    // Auto merge rows (vertical merge across rows in specific columns)
                    if (autoMerge != null && autoMerge.get("columns") != null) {
                        for (int colIndex : (List<Integer>) autoMerge.get("columns")) {
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

                    // Auto merge columns
                    // Auto merge columns (horizontal merge across columns in specific rows)
                    if (autoMerge != null && autoMerge.get("rows") != null) {
                        for (int rowIndex : (List<Integer>) autoMerge.get("rows")) {
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

    public static String getPreviousYear(String year) {
        int start = Integer.parseInt(year.substring(0, 4));
        int end = Integer.parseInt(year.substring(5));
        return "\"" + String.format("%d-%02d", start - 1, start % 100) + "\"";
    }

    public static String getPrevious2Year(String year) {
        int start = Integer.parseInt(year.substring(0, 4));
        int end = Integer.parseInt(year.substring(5));
        return "\"" + String.format("%d-%02d", start - 2, start - 1 % 100) + "\"";
    }

    public static String getPrevious3Year(String year) {
        int start = Integer.parseInt(year.substring(0, 4));
        int end = Integer.parseInt(year.substring(5));
        return "\"" + String.format("%d-%02d", start - 3, start - 2 % 100) + "\"";
    }

    public static String getNextYear(String year) {
        int start = Integer.parseInt(year.substring(0, 4));
        int end = Integer.parseInt(year.substring(5));
        return "\"" + String.format("%d-%02d", start + 1, (start + 2) % 100) + "\"";
    }

    public static List<String> getAcademicYearMonths(String year) {
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

    private static String formatMonthYear(int month, int year) {
        LocalDate date = LocalDate.of(year, month, 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-yy", Locale.ENGLISH);
        return date.format(formatter);
    }

    private String getData(String year, String previousYear, String nextYear, String months, String previous2Year,
            String previous3Year) {
        return "\r\n" + //
                        "{\r\n" + //
                        "\r\n" + //
                        "                     \"Annual AOP Cost\": { \r\n" + //
                        "                        \"tables\": [ \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                     \r\n" + //
                        "                                     \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            },\r\n" + //
                        "                             { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                     \r\n" + //
                        "                                     \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            } \r\n" + //
                        "                        ] \r\n" + //
                        "                    }, \r\n" + //
                        "                    \"Plant Production Summary\": { \r\n" + //
                        "                        \"tables\": [ \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"Item\", \r\n" + //
                        "                                        \"\", \r\n" + //
                                                                 previousYear  + ","+//
                        previousYear +","+
                        year +","+
                        "                                        \"Variance wrt current year budget\", \r\n" + //
                        "                                        \"Variance wrt current year budget\", \r\n" + //
                        "                                        \"Variance wrt current year actuals\", \r\n" + //
                        "                                        \"Variance wrt current year actuals\", \r\n" + //
                        "                                        \"Remark\" \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"serial No\", \r\n" + //
                        "                                        \"Production volume\", \r\n" + //
                        "                                        \"Unit\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Actual\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"MT\", \r\n" + //
                        "                                        \"%\", \r\n" + //
                        "                                        \"MT\", \r\n" + //
                        "                                        \"%\", \r\n" + //
                        "                                        \"Remark\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            } \r\n" + //
                        "                        ] \r\n" + //
                        "                    }, \r\n" + //
                        "                    \"Monthwise production plan\": { \r\n" + //
                        "                        \"tables\": [ \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \" \", \r\n" + //
                        "                                        \" \", \r\n" + //
                        previousYear +","+
                        previousYear +","+
                        previousYear +","+
                        previousYear +","+
                        previousYear +","+
                        previousYear +","+
                        year +","+
                        year +","+
                        year +","+
                        year +","+
                        year +
                        "                                    ], \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"EOE Production, MT\", \r\n" + //
                        "                                        \"EOE Production, MT\", \r\n" + //
                        "                                        \"Operating Hours\", \r\n" + //
                        "                                        \"Operating Hours\", \r\n" + //
                        "                                        \"Throughput TPH\", \r\n" + //
                        "                                        \"Throughput TPH\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"Remark\" \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"Sr No\", \r\n" + //
                        "                                        \"Month\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Actual\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Actual\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Actual\", \r\n" + //
                        "                                        \"Operating Hours\", \r\n" + //
                        "                                        \"MEG Throughput, TPH\", \r\n" + //
                        "                                        \"EO Throughput TPH\", \r\n" + //
                        "                                        \"EOE Throughput TPH\", \r\n" + //
                        "                                        \"TOTAL EOE, MT\", \r\n" + //
                        "                                        \"Remark\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            }, \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"particulars\", \r\n" + //
                months +
                        "                ,                        \"Total\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            } \r\n" + //
                        "                        ] \r\n" + //
                        "                    }, \r\n" + //
                        "                    \"Monthwise Raw Data\": { \r\n" + //
                        "                        \"tables\": [ \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"Parameters\", \r\n" + //
                        months +
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            }, \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"Parameters\", \r\n" + //
                        "                                        \"Unit\", \r\n" + //
                        "                                        \"Spec\", \r\n" + //
                months +
                        "                ,                        \"Total\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            }, \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"Parameters\", \r\n" + //
                        "                                        \"Unit\", \r\n" + //
                        "                                        \"Spec\", \r\n" + //
                months +
                        "                ,                        \"Total\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            }, \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"Parameters\", \r\n" + //
                        "                                        \"Unit\", \r\n" + //
                        "                                        \"Spec\", \r\n" + //
                months +
                        "                ,                        \"Total\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            }, \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"Parameters\", \r\n" + //
                        "                                        \"Unit\", \r\n" + //
                        "                                        \"Spec\", \r\n" + //
                months +
                        "                ,                        \"Total\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            }, \r\n" + //
                        "                             \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"Parameters\", \r\n" + //
                        "                                        \"Unit\", \r\n" + //
                        "                                        \"Spec\", \r\n" + //
                months +
                        "                ,                        \"Total\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            } \r\n" + //
                        "                        ] \r\n" + //
                        "                    }, \r\n" + //
                        "                     \"Turn Around Report\": { \r\n" + //
                        "                        \"tables\": [ \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"Turn Around Period\", \r\n" + //
                        "                                        \"Turn Around Period\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\" \r\n" + //
                        "                                         \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"serial No\", \r\n" + //
                        "                                        \"Activities\", \r\n" + //
                        "                                        \"From\", \r\n" + //
                        "                                        \"To\", \r\n" + //
                        "                                        \"Duration in Hrs\", \r\n" + //
                        "                                        \"Remark\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            }, \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"Turn Around Period\", \r\n" + //
                        "                                        \"Turn Around Period\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\" \r\n" + //
                        "                                         \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"serial No\", \r\n" + //
                        "                                        \"Activities\", \r\n" + //
                        "                                        \"From\", \r\n" + //
                        "                                        \"To\", \r\n" + //
                        "                                        \"Duration in Hrs\", \r\n" + //
                        "                                        \"Remark\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            } \r\n" + //
                        "                 \r\n" + //
                        "                        ] \r\n" + //
                        "                    }, \r\n" + //
                        "                     \"Annual Production Plan\": { \r\n" + //
                        "                        \"tables\": [ \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"Sr. No.\", \r\n" + //
                        "                                        \"Assumptions and remarks\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            }, \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"SrNo\", \r\n" + //
                        "                                        \"Max hourly rate achived\", \r\n" + //
                        "                                        \"Value\", \r\n" + //
                        "                                        \"UOM\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            }, \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"SrNo\", \r\n" + //
                        "                                        \"Calculation of operating hours\", \r\n" + //
                        "                                        \"Value\", \r\n" + //
                        "                                        \"Hours\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            }, \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"SrNo\", \r\n" + //
                        "                                        \"Throughput limiting causes\", \r\n" + //
                        "                                        \"Achivable hourly rate\", \r\n" + //
                        "                                        \"Op. Hrs.\", \r\n" + //
                        "                                        \"Period from\", \r\n" + //
                        "                                        \"Period to\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            }, \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        previous3Year + ","+
                        previous3Year +","+
                        previous2Year +","+
                        previous2Year +","+
                        previousYear +","+
                        previousYear +","+
                        year +
                        "                                    ], \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"SrNo\", \r\n" + //
                        "                                        \"Item\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Actual\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Actual\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Actual\", \r\n" + //
                        "                                        \"Budget\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            } \r\n" + //
                        "                        ] \r\n" + //
                        "                    }, \r\n" + //
                        "                     \r\n" + //
                        "                     \"Plant Contribution\": { \r\n" + //
                        "                        \"tables\": [ \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"Price\", \r\n" + //
                        "                                        \"Production, MT\", \r\n" + //
                        "                                        \"Production, MT\", \r\n" + //
                        "                                        \"Production, MT\" \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    [ \r\n" + //
                        "                                    \" \", \r\n" + //
                        "                                    \" \", \r\n" + //
                        "                                    \" \", \r\n" + //
                        "                                    \"Price\", \r\n" + //
                        previousYear +","+
                        previousYear +","+
                        "                                     "+year +  "\r\n" + //
                        "                                    ], \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"SL No\", \r\n" + //
                        "                                        \"Product Name\", \r\n" + //
                        "                                        \"Unit\", \r\n" + //
                        "                                        \"Rs/MT\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Actual\", \r\n" + //
                        "                                        \"Budget\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                     \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            }, \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"Price\", \r\n" + //
                        "                                        \"Norm Unit/MT\", \r\n" + //
                        "                                        \"Norm Unit/MT\", \r\n" + //
                        "                                        \"Norm Unit/MT\", \r\n" + //
                        "                                        \"Cost Rs/MT\", \r\n" + //
                        "                                        \"Cost Rs/MT\", \r\n" + //
                        "                                        \"Cost Rs/MT\" \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \" \", \r\n" + //
                        "                                        \" \", \r\n" + //
                        "                                        \" \", \r\n" + //
                        "                                        \"Price\", \r\n" + //
                        previousYear +","+
                        previousYear +","+
                        year +","+
                        previousYear +","+
                        previousYear +","+
                        year +
                        "                 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"Sr No\", \r\n" + //
                        "                                        \"By Product Name\", \r\n" + //
                        "                                        \"Unit\", \r\n" + //
                        "                                        \"Rs/MT\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Actual\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Actual\", \r\n" + //
                        "                                        \"Budget\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            }, \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"Price\", \r\n" + //
                        "                                        \"Norm Unit/MT\", \r\n" + //
                        "                                        \"Norm Unit/MT\", \r\n" + //
                        "                                        \"Norm Unit/MT\", \r\n" + //
                        "                                        \"Cost Rs/MT\", \r\n" + //
                        "                                        \"Cost Rs/MT\", \r\n" + //
                        "                                        \"Cost Rs/MT\" \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \" \", \r\n" + //
                        "                                        \" \", \r\n" + //
                        "                                        \" \", \r\n" + //
                        "                                        \"Price\", \r\n" + //
                        previousYear +","+
                        previousYear +","+
                        year +","+
                        previousYear +","+
                        previousYear +","+
                        year +
                        "                 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"Sr No\", \r\n" + //
                        "                                        \"Raw Material Name\", \r\n" + //
                        "                                        \"Unit\", \r\n" + //
                        "                                        \"Rs/MT\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Actual\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Actual\", \r\n" + //
                        "                                        \"Budget\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            }, \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"Price\", \r\n" + //
                        "                                        \"Norm Unit/MT\", \r\n" + //
                        "                                        \"Norm Unit/MT\", \r\n" + //
                        "                                        \"Norm Unit/MT\", \r\n" + //
                        "                                        \"Cost Rs/MT\", \r\n" + //
                        "                                        \"Cost Rs/MT\", \r\n" + //
                        "                                        \"Cost Rs/MT\" \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \" \", \r\n" + //
                        "                                        \" \", \r\n" + //
                        "                                        \" \", \r\n" + //
                        "                                        \"Price\", \r\n" + //
                        previousYear +","+
                        previousYear +","+
                        year +","+
                        previousYear +","+
                        previousYear +","+
                        year +
                        "                 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"Sr No\", \r\n" + //
                        "                                        \"Catalyst Name\", \r\n" + //
                        "                                        \"Unit\", \r\n" + //
                        "                                        \"Rs/MT\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Actual\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Actual\", \r\n" + //
                        "                                        \"Budget\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            }, \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"Price\", \r\n" + //
                        "                                        \"Norm Unit/MT\", \r\n" + //
                        "                                        \"Norm Unit/MT\", \r\n" + //
                        "                                        \"Norm Unit/MT\", \r\n" + //
                        "                                        \"Cost Rs/MT\", \r\n" + //
                        "                                        \"Cost Rs/MT\", \r\n" + //
                        "                                        \"Cost Rs/MT\" \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \" \", \r\n" + //
                        "                                        \" \", \r\n" + //
                        "                                        \" \", \r\n" + //
                        "                                        \"Price\", \r\n" + //
                        previousYear +","+
                        previousYear +","+
                        year +","+
                        previousYear +","+
                        previousYear +","+
                        year +
                        "                 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"Sr No\", \r\n" + //
                        "                                        \"By Utility Name\", \r\n" + //
                        "                                        \"Unit\", \r\n" + //
                        "                                        \"Rs/MT\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Actual\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Actual\", \r\n" + //
                        "                                        \"Budget\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            }, \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        previousYear +","+
                        previousYear +","+
                        year +
                        "                                    ], \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"Sr No\", \r\n" + //
                        "                                        \"Other Cost\", \r\n" + //
                        "                                        \"Unit\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Actual\", \r\n" + //
                        "                                        \"Budget\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            }, \r\n" + //
                        "                            { \r\n" + //
                        "                                \"startRow\": 8, \r\n" + //
                        "                                \"headers\": [ \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"\", \r\n" + //
                        "                                        \"\", \r\n" + //
                        //"                  \"\", \r\n" + //
                        previousYear +","+
                        previousYear +","+
                        year +
                        "                                    ], \r\n" + //
                        "                                    [ \r\n" + //
                        "                                        \"Sr No\", \r\n" + //
                        "                                        \"Production cost Calculation\", \r\n" + //
                        //"                  \"Unit\", \r\n" + //
                        "                                        \"Budget\", \r\n" + //
                        "                                        \"Actual\", \r\n" + //
                        "                                        \"Budget\" \r\n" + //
                        "                                    ] \r\n" + //
                        "                                ], \r\n" + //
                        "                                \"rows\": [], \r\n" + //
                        "                                \"styles\": { \r\n" + //
                        "                                    \"boldColumns\": [ \r\n" + //
                        "                                        0 \r\n" + //
                        "                                    ], \r\n" + //
                        "                                    \"borders\": true \r\n" + //
                        "                                }, \r\n" + //
                        "                                \"autoMerge\": { \r\n" + //
                        "                                    \"columns\": [], \r\n" + //
                        "                                    \"rows\": [] \r\n" + //
                        "                                } \r\n" + //
                        "                            } \r\n" + //
                        "                             \r\n" + //
                        "                        ] \r\n" + //
                        "                    } \r\n" + //
                        "                }";
    }
}
