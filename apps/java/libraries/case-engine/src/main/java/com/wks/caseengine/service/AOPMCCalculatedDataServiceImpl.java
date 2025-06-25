package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.sql.DataSource;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.AOPMCCalculatedDataDTO;

import com.wks.caseengine.entity.AOPMCCalculatedData;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AOPMCCalculatedDataRepository;
import com.wks.caseengine.repository.AopCalculationRepository;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.Connection;

@Service
public class AOPMCCalculatedDataServiceImpl implements AOPMCCalculatedDataService {

	@Autowired
	private AOPMCCalculatedDataRepository aOPMCCalculatedDataRepository;

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@PersistenceContext
	private EntityManager entityManager;

	private DataSource dataSource;

	@Autowired
	private AopCalculationRepository aopCalculationRepository;

	@Autowired
	private ScreenMappingRepository screenMappingRepository;

	// Inject or set your DataSource (e.g., via constructor or setter)
	public AOPMCCalculatedDataServiceImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public AOPMessageVM getAOPMCCalculatedData(String plantId, String year) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			List<Object[]> obj = aOPMCCalculatedDataRepository.getDataMCUValuesAllData(year, plantId);
			List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTOList = new ArrayList<>();

			for (Object[] row : obj) {
				AOPMCCalculatedDataDTO aOPMCCalculatedDataDTO = new AOPMCCalculatedDataDTO();
				aOPMCCalculatedDataDTO.setId(row[0] != null ? row[0].toString() : null);
				aOPMCCalculatedDataDTO.setSiteFKId(row[1] != null ? row[1].toString() : null);
				aOPMCCalculatedDataDTO.setPlantFKId(row[2] != null ? row[2].toString() : null);
				aOPMCCalculatedDataDTO.setMaterialFKId(row[3] != null ? row[3].toString() : null);
				aOPMCCalculatedDataDTO.setApril(row[4] != null ? Double.parseDouble(row[4].toString()) : null);
				aOPMCCalculatedDataDTO.setMay(row[5] != null ? Double.parseDouble(row[5].toString()) : null);
				aOPMCCalculatedDataDTO.setJune(row[6] != null ? Double.parseDouble(row[6].toString()) : null);
				aOPMCCalculatedDataDTO.setJuly(row[7] != null ? Double.parseDouble(row[7].toString()) : null);
				aOPMCCalculatedDataDTO.setAugust(row[8] != null ? Double.parseDouble(row[8].toString()) : null);
				aOPMCCalculatedDataDTO.setSeptember(row[9] != null ? Double.parseDouble(row[9].toString()) : null);
				aOPMCCalculatedDataDTO.setOctober(row[10] != null ? Double.parseDouble(row[10].toString()) : null);
				aOPMCCalculatedDataDTO.setNovember(row[11] != null ? Double.parseDouble(row[11].toString()) : null);
				aOPMCCalculatedDataDTO.setDecember(row[12] != null ? Double.parseDouble(row[12].toString()) : null);
				aOPMCCalculatedDataDTO.setJanuary(row[13] != null ? Double.parseDouble(row[13].toString()) : null);
				aOPMCCalculatedDataDTO.setFebruary(row[14] != null ? Double.parseDouble(row[14].toString()) : null);
				aOPMCCalculatedDataDTO.setMarch(row[15] != null ? Double.parseDouble(row[15].toString()) : null);
				aOPMCCalculatedDataDTO.setFinancialYear(row[16] != null ? row[16].toString() : null);
				aOPMCCalculatedDataDTO.setRemarks(row[17] != null ? row[17].toString() : " ");
				aOPMCCalculatedDataDTO.setVerticalFKId(row[22] != null ? row[22].toString() : null);
				aOPMCCalculatedDataDTO.setProductName(row[24] != null ? row[24].toString() : null);
				aOPMCCalculatedDataDTOList.add(aOPMCCalculatedDataDTO);
			}
			Map<String, Object> map = new HashMap<>();

			List<AopCalculation> aopCalculation = aopCalculationRepository.findByPlantIdAndAopYearAndCalculationScreen(
					UUID.fromString(plantId), year, "production-volume-data");
			map.put("aopMCCalculatedDataDTOList", aOPMCCalculatedDataDTOList);
			map.put("aopCalculation", aopCalculation);
			aopMessageVM.setCode(200);
			aopMessageVM.setData(map);
			aopMessageVM.setMessage("Data fetched successfully");
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public List<AOPMCCalculatedDataDTO> editAOPMCCalculatedData(
			List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTOList) {
		try {
			String finYear = "";
			UUID plantId = null;

			for (AOPMCCalculatedDataDTO aOPMCCalculatedDataDTO : aOPMCCalculatedDataDTOList) {
				AOPMCCalculatedData aOPMCCalculatedData = new AOPMCCalculatedData();
				if (aOPMCCalculatedDataDTO.getId() == null || aOPMCCalculatedDataDTO.getId().contains("#")) {
					aOPMCCalculatedData.setId(null);
				} else {
					aOPMCCalculatedData.setId(UUID.fromString(aOPMCCalculatedDataDTO.getId()));
				}
				aOPMCCalculatedData.setPlantFKId(UUID.fromString(aOPMCCalculatedDataDTO.getPlantFKId()));
				plantId = UUID.fromString(aOPMCCalculatedDataDTO.getPlantFKId());
				aOPMCCalculatedData.setSiteFKId(UUID.fromString(aOPMCCalculatedDataDTO.getSiteFKId()));
				aOPMCCalculatedData.setVerticalFKId(UUID.fromString(aOPMCCalculatedDataDTO.getVerticalFKId()));
				aOPMCCalculatedData.setMaterialFKId(UUID.fromString(aOPMCCalculatedDataDTO.getMaterialFKId()));
				aOPMCCalculatedData.setJanuary(aOPMCCalculatedDataDTO.getJanuary());
				aOPMCCalculatedData.setFebruary(aOPMCCalculatedDataDTO.getFebruary());
				aOPMCCalculatedData.setMarch(aOPMCCalculatedDataDTO.getMarch());
				aOPMCCalculatedData.setApril(aOPMCCalculatedDataDTO.getApril());
				aOPMCCalculatedData.setMay(aOPMCCalculatedDataDTO.getMay());
				aOPMCCalculatedData.setJune(aOPMCCalculatedDataDTO.getJune());
				aOPMCCalculatedData.setJuly(aOPMCCalculatedDataDTO.getJuly());
				aOPMCCalculatedData.setAugust(aOPMCCalculatedDataDTO.getAugust());
				aOPMCCalculatedData.setSeptember(aOPMCCalculatedDataDTO.getSeptember());
				aOPMCCalculatedData.setOctober(aOPMCCalculatedDataDTO.getOctober());
				aOPMCCalculatedData.setNovember(aOPMCCalculatedDataDTO.getNovember());
				aOPMCCalculatedData.setDecember(aOPMCCalculatedDataDTO.getDecember());
				aOPMCCalculatedData.setJanuary(aOPMCCalculatedDataDTO.getJanuary());

				aOPMCCalculatedData.setFinancialYear(aOPMCCalculatedDataDTO.getFinancialYear());
				finYear = aOPMCCalculatedDataDTO.getFinancialYear();
				aOPMCCalculatedData.setRemarks(aOPMCCalculatedDataDTO.getRemarks());

				AOPMCCalculatedData saved =aOPMCCalculatedDataRepository.save(aOPMCCalculatedData);
				if (saved.getId() == null) {
					aOPMCCalculatedDataDTO.setErrDescription("No record found with this id" +aOPMCCalculatedDataDTO.getId());
					aOPMCCalculatedDataDTO.setSaveStatus("Failed");
				}
			}

			List<ScreenMapping> screenMappingList = screenMappingRepository
					.findByDependentScreen("production-volume-data");
			for (ScreenMapping screenMapping : screenMappingList) {
				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(finYear);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId((plantId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}

			// TODO Auto-generated method stub
			return aOPMCCalculatedDataDTOList;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to edit data", ex);
		}
	}

	@Override
	public AOPMessageVM getAOPMCCalculatedDataSP(String plantId, String finYear) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId))
					.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
			Sites site = siteRepository.findById(plant.getSiteFkId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

			UUID siteId = site.getId();
			UUID verticalId = vertical.getId();
			String storedProcedure = vertical.getName() + "_LoadMCValues";

			String callSql = "{call " + storedProcedure + "(?, ?, ?, ?)}";

			try (Connection connection = dataSource.getConnection();
					CallableStatement stmt = connection.prepareCall(callSql)) {

				// Set parameters in the correct order
				stmt.setString(1, finYear); // @finYear
				stmt.setString(2, plantId); // @plantId
				stmt.setString(3, verticalId.toString()); // @verticalId
				stmt.setString(4, siteId.toString()); // @siteId

				// Execute the stored procedure
				int rowsAffected = stmt.executeUpdate();

				// Optional: commit if auto-commit is off
				if (!connection.getAutoCommit()) {
					connection.commit();
				}

				aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId),
						finYear, "production-volume-data");

				List<ScreenMapping> screenMappingList = screenMappingRepository
						.findByDependentScreen("production-volume-data");
				for (ScreenMapping screenMapping : screenMappingList) {
					if (!screenMapping.getCalculationScreen().equalsIgnoreCase(screenMapping.getDependentScreen())) {

						AopCalculation aopCalculation = new AopCalculation();
						aopCalculation.setAopYear(finYear);
						aopCalculation.setIsChanged(true);
						aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
						aopCalculation.setPlantId(UUID.fromString(plantId));
						aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
						aopCalculationRepository.save(aopCalculation);
					}
				}

				aopMessageVM.setCode(200);
				aopMessageVM.setMessage("SP Executed successfully");
				aopMessageVM.setData(rowsAffected);
				return aopMessageVM;

			} catch (SQLException e) {
				e.printStackTrace();
				return aopMessageVM;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return aopMessageVM;
		}
	}

	public byte[] createExcel(String year, UUID plantFKId, boolean isAfterSave,List<AOPMCCalculatedDataDTO> dtoList) {
		try {
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("Sheet1");
			int currentRow = 0;
			// List<List<Object>> rows = new ArrayList<>();

			List<List<Object>> rows = new ArrayList<>();
			if (!isAfterSave) {
				List<Object[]> obj = aOPMCCalculatedDataRepository.getDataMCUValuesAllData(year, plantFKId.toString());
				for (Object[] row : obj) {

					List<Object> list = new ArrayList<>();
					list.add(row[24] != null ? row[24].toString() : null);
					list.add(row[4] != null ? Double.parseDouble(row[4].toString()) : null);
					list.add(row[5] != null ? Double.parseDouble(row[5].toString()) : null);
					list.add(row[6] != null ? Double.parseDouble(row[6].toString()) : null);
					list.add(row[7] != null ? Double.parseDouble(row[7].toString()) : null);
					list.add(row[8] != null ? Double.parseDouble(row[8].toString()) : null);
					list.add(row[9] != null ? Double.parseDouble(row[9].toString()) : null);
					list.add(row[10] != null ? Double.parseDouble(row[10].toString()) : null);
					list.add(row[11] != null ? Double.parseDouble(row[11].toString()) : null);
					list.add(row[12] != null ? Double.parseDouble(row[12].toString()) : null);
					list.add(row[13] != null ? Double.parseDouble(row[13].toString()) : null);
					list.add(row[14] != null ? Double.parseDouble(row[14].toString()) : null);
					list.add(row[15] != null ? Double.parseDouble(row[15].toString()) : null);
					list.add(row[17] != null ? row[17].toString() : " ");

					list.add(row[0] != null ? row[0].toString() : null);
					list.add(row[1] != null ? row[1].toString() : null);
					list.add(row[2] != null ? row[2].toString() : null);
					list.add(row[3] != null ? row[3].toString() : null);
					list.add(row[16] != null ? row[16].toString() : null);
					list.add(row[22] != null ? row[22].toString() : null);
					rows.add(list);
				}
			}else {
				for(AOPMCCalculatedDataDTO aopMCCalculatedDataDTO : dtoList) {
					List<Object> list = new ArrayList<>();
					list.add(aopMCCalculatedDataDTO.getProductName());
					list.add(aopMCCalculatedDataDTO.getApril());
					list.add(aopMCCalculatedDataDTO.getMay());
					list.add(aopMCCalculatedDataDTO.getJune());
					list.add(aopMCCalculatedDataDTO.getJuly());
					list.add(aopMCCalculatedDataDTO.getAugust());
					list.add(aopMCCalculatedDataDTO.getSeptember());
					list.add(aopMCCalculatedDataDTO.getOctober());
					list.add(aopMCCalculatedDataDTO.getNovember());
					list.add(aopMCCalculatedDataDTO.getDecember());
					list.add(aopMCCalculatedDataDTO.getJanuary());
					list.add(aopMCCalculatedDataDTO.getFebruary());
					list.add(aopMCCalculatedDataDTO.getMarch());
					list.add(aopMCCalculatedDataDTO.getRemarks());
					list.add(aopMCCalculatedDataDTO.getId());
					list.add(aopMCCalculatedDataDTO.getSiteFKId());
					list.add(aopMCCalculatedDataDTO.getPlantFKId());
					list.add(aopMCCalculatedDataDTO.getMaterialFKId());
					list.add(aopMCCalculatedDataDTO.getFinancialYear());
					list.add(aopMCCalculatedDataDTO.getVerticalFKId());
					list.add(aopMCCalculatedDataDTO.getSaveStatus());
					list.add(aopMCCalculatedDataDTO.getErrDescription());
				}
			}
			
			// Data rows
			
			List<String> innerHeaders = new ArrayList<>();
			innerHeaders.add("Particulars");
			List<String> monthsList = getAcademicYearMonths(year);
			innerHeaders.addAll(monthsList);
			innerHeaders.add("Remarks");
			innerHeaders.add("Id");
			innerHeaders.add("SiteFKId");
			innerHeaders.add("PlantFKId");
			innerHeaders.add("MaterialFKId");
			innerHeaders.add("FinancialYear");
			innerHeaders.add("VerticalFKId");
			if(isAfterSave){
				innerHeaders.add("Status");
				innerHeaders.add("Error Description");
			}
			List<List<String>> headers = new ArrayList<>();
			headers.add(innerHeaders);

			for (List<String> headerRowData : headers) {
				Row headerRow = sheet.createRow(currentRow++);
				for (int col = 0; col < headerRowData.size(); col++) {
					Cell cell = headerRow.createCell(col);
					cell.setCellValue(headerRowData.get(col));
					cell.setCellStyle(createBoldBorderedStyle(workbook));
				}
			}
			for (List<Object> rowData : rows) {
				Row row1 = sheet.createRow(currentRow++);
				for (int col = 0; col < rowData.size(); col++) {
					Cell cell = row1.createCell(col);
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

				}
			}
			sheet.setColumnHidden(14, true);
			sheet.setColumnHidden(15, true);
			sheet.setColumnHidden(16, true);
			sheet.setColumnHidden(17, true);
			sheet.setColumnHidden(18, true);
			sheet.setColumnHidden(19, true);
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

	@Override
	public byte[] importExcel(String year, UUID plantFKId, MultipartFile file) {
		// TODO Auto-generated method stub
		try {
			List<AOPMCCalculatedDataDTO> data = readData(file.getInputStream(), plantFKId, year);

			List<AOPMCCalculatedDataDTO> savedData =editAOPMCCalculatedData(data);
			return createExcel(year, plantFKId, true, savedData);
			
			// return ResponseEntity.ok(data);
		} catch (Exception e) {
			e.printStackTrace();
			// return ResponseEntity.internalServerError().build();
		}
		return null;
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

	public List<AOPMCCalculatedDataDTO> readData(InputStream inputStream, UUID plantFKId, String year) {
		List<AOPMCCalculatedDataDTO> prodList = new ArrayList<>();

		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();

			if (rowIterator.hasNext())
				rowIterator.next(); // Skip header

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				AOPMCCalculatedDataDTO dto = new AOPMCCalculatedDataDTO();
				try {
					dto.setProductName(getStringCellValue(row.getCell(0), dto));
					dto.setApril(getNumericCellValue(row.getCell(1), dto));
					dto.setMay(getNumericCellValue(row.getCell(2), dto));
					dto.setJune(getNumericCellValue(row.getCell(3), dto));
					dto.setJuly(getNumericCellValue(row.getCell(4), dto));
					dto.setAugust(getNumericCellValue(row.getCell(5), dto));
					dto.setSeptember(getNumericCellValue(row.getCell(6), dto));
					dto.setOctober(getNumericCellValue(row.getCell(7), dto));
					dto.setNovember(getNumericCellValue(row.getCell(8), dto));
					dto.setDecember(getNumericCellValue(row.getCell(9), dto));
					dto.setJanuary(getNumericCellValue(row.getCell(10), dto));
					dto.setFebruary(getNumericCellValue(row.getCell(11), dto));
					dto.setMarch(getNumericCellValue(row.getCell(12), dto));
					dto.setRemarks(getStringCellValue(row.getCell(13), dto));
					dto.setId(getStringCellValue(row.getCell(14), dto));
					dto.setSiteFKId(getStringCellValue(row.getCell(15), dto));
					dto.setPlantFKId(getStringCellValue(row.getCell(16), dto));
					dto.setMaterialFKId(getStringCellValue(row.getCell(17), dto));
					dto.setFinancialYear(getStringCellValue(row.getCell(18), dto));
					dto.setVerticalFKId(getStringCellValue(row.getCell(19), dto));
				} catch (Exception e) {
					e.printStackTrace();
					dto.setErrDescription(e.getMessage());
					dto.setSaveStatus("Failed");
				}
				prodList.add(dto);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return prodList;
	}

	private static String getStringCellValue(Cell cell, AOPMCCalculatedDataDTO dto) {
		try {
			if (cell == null)
				return null;
			cell.setCellType(CellType.STRING);
			return cell.getStringCellValue().trim();
		} catch (Exception e) {
			dto.setSaveStatus("Failed");
			dto.setErrDescription("Please enter correct values");
			e.printStackTrace();
		}
		return null;

	}

	private static Double getNumericCellValue(Cell cell, AOPMCCalculatedDataDTO dto) {
		if (cell == null)
			return null;
		if (cell.getCellType() == CellType.NUMERIC) {
			return cell.getNumericCellValue();
		} else if (cell.getCellType() == CellType.STRING) {
			try {
				return Double.parseDouble(cell.getStringCellValue().trim());
			} catch (NumberFormatException e) {
				dto.setSaveStatus("Failed");
				dto.setErrDescription("Please enter numeric values");
			}
		}
		return null;
	}

}
