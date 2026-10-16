package com.wks.caseengine.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.dto.ProposedAOPDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.utility.Utility;

@Service
public class ProposedAOPServiceImpl implements ProposedAOPService {

	private static final double EXCEL_MAX_NUMBER = 9.99999999999999E307;
	private static final double EXCEL_MIN_POSITIVE_NUMBER = 2.2250738585072014E-308;

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@Autowired
	private AopCalculationRepository aopCalculationRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ScreenMappingRepository screenMappingRepository;

	@Autowired
	private AOPConsumptionNormService aopConsumptionNormService;

	public AOPMessageVM getProposedAOP(UUID plantId, String aopYear, UUID gradeId) {

		Plants plants = plantsRepository.findById(plantId).orElseThrow(() -> new RuntimeException("Plant not found"));
		String verticalName = verticalRepository.findById(plants.getVerticalFKId())
				.orElseThrow(() -> new RuntimeException("Vertical not found")).getName();
		String siteName = siteRepository.findById(plants.getSiteFkId())
				.orElseThrow(() -> new RuntimeException("Site not found")).getName();

		String procedureName = verticalName + "_" + siteName + "_GetProposedAOP";
		List<ProposedAOPDTO> proposedAOP = fetchProposedAOPFromProcedure(plantId, aopYear, gradeId, procedureName);

		Map<String, Object> map = new HashMap<>();

		List<AopCalculation> aopCalculation = aopCalculationRepository
				.findByPlantIdAndAopYearAndCalculationScreen(plantId, aopYear, "proposed-aop");
		map.put("proposedAOP", proposedAOP);
		map.put("aopCalculation", aopCalculation);
		return AOPMessageVM.builder()
				.code(200)
				.message("Proposed AOPs fetched successfully")
				.data(map)
				.build();
	}

	public List<ProposedAOPDTO> fetchProposedAOPFromProcedure(UUID plantId, String aopYear, UUID gradeId,
			String procedureName) {

		String sql = "EXEC " + procedureName + " @plantId = ?, @aopYear = ?, @gradeId = ?";
		return jdbcTemplate.query(sql, (rs, rowNum) -> ProposedAOPDTO.builder()
				.id(rs.getString("Id") != null ? UUID.fromString(rs.getString("Id")) : null)
				.normParameterId(
						rs.getString("NormparameterId") != null ? UUID.fromString(rs.getString("NormparameterId"))
								: null)
				.normParameterTypeId(rs.getString("NormParameterTypeId") != null
						? UUID.fromString(rs.getString("NormParameterTypeId"))
						: null)
				.normParameterTypeDisplayName(rs.getString("NormParameterTypeDisplayName"))
				.productName(rs.getString("ProductName"))
				.uom(rs.getString("UOM"))
				.lastFY(rs.getDouble("LastFY"))
				.sysGrn(rs.getDouble("SysGrn"))
				.proposed(rs.getDouble("Proposed"))
				.remarks(rs.getString("Remarks"))
				.plantId(rs.getString("PlantId") != null ? UUID.fromString(rs.getString("PlantId")) : null)
				.aopYear(rs.getString("AopYear"))
				.gradeId(rs.getString("GradeId") != null ? UUID.fromString(rs.getString("GradeId")) : null)
				.build(),
				plantId.toString(), aopYear, gradeId.toString());
	}

	@Override
	@Transactional
	public AOPMessageVM saveProposedAOP(List<ProposedAOPDTO> dtoList) {
		try {

			List<ProposedAOPDTO> failedList = new ArrayList<>();

			UUID plantId = null;
			String year = null;
			for (ProposedAOPDTO dto : dtoList) {

				if (dto.getNormParameterId() == null || dto.getGradeId() == null || dto.getAopYear() == null) {
					throw new RuntimeException("NormParameterId, GradeId and AopYear are required");
				}
				if (plantId == null) {
					plantId = dto.getPlantId();
				}
				if (year == null) {
					year = dto.getAopYear();
				}

				String updateSql = "UPDATE MCUNormsValueGrade_Proposed " +
						"SET April = ?, May = ?, June = ?, July = ?, August = ?, September = ?, " +
						"October = ?, November = ?, December = ?, January = ?, February = ?, March = ?, Remarks = ? "
						+
						"WHERE Material_FK_Id = ? and Grade_FK_Id = ? and FinancialYear = ?";
				jdbcTemplate.update(updateSql,
						dto.getProposed(), dto.getProposed(), dto.getProposed(), dto.getProposed(),
						dto.getProposed(), dto.getProposed(), dto.getProposed(), dto.getProposed(),
						dto.getProposed(), dto.getProposed(), dto.getProposed(), dto.getProposed(),
						dto.getRemarks(),
						dto.getNormParameterId(), dto.getGradeId(), dto.getAopYear());

			}

			if (plantId != null && year != null) {
				List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("proposed-aop");
				for (ScreenMapping screenMapping : screenMappingList) {
					AopCalculation aopCalculation = new AopCalculation();
					aopCalculation.setAopYear(year);
					aopCalculation.setIsChanged(true);
					aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
					aopCalculation.setPlantId(plantId);
					aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
					aopCalculationRepository.save(aopCalculation);
				}
			}

			AOPMessageVM vm = new AOPMessageVM();
			vm.setCode(200);
			vm.setMessage("Proposed AOP saved successfully");
			vm.setData(failedList);
			return vm;
		} catch (Exception e) {
			throw new RuntimeException("Failed to save proposed AOP", e);
		}
	}

	@Override
	public AOPMessageVM calculateProposedAOP(UUID plantId, String aopYear) {

		Plants plants = plantsRepository.findById(plantId).orElseThrow(() -> new RuntimeException("Plant not found"));
		String verticalName = verticalRepository.findById(plants.getVerticalFKId())
				.orElseThrow(() -> new RuntimeException("Vertical not found")).getName();
		String siteName = siteRepository.findById(plants.getSiteFkId())
				.orElseThrow(() -> new RuntimeException("Site not found")).getName();

		String procedureName = verticalName + "_" + siteName + "_CalculateProposedAOP";

		Integer result = executeProposedAOPCalculationSP(String.valueOf(plantId), aopYear, procedureName);
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Calculate SP Executed successfully");
		aopMessageVM.setData(result);

		aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(plantId, aopYear,
				"proposed-aop");

		List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("proposed-aop");
		for (ScreenMapping screenMapping : screenMappingList) {
			AopCalculation aopCalculation = new AopCalculation();
			aopCalculation.setAopYear(aopYear);
			aopCalculation.setIsChanged(true);
			aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
			aopCalculation.setPlantId(plantId);
			aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
			aopCalculationRepository.save(aopCalculation);
		}
		return aopMessageVM;
	}

	public Integer executeProposedAOPCalculationSP(String plantId, String aopYear, String procedureName) {
		try {

			String callSql = "{call " + "[" + procedureName + "]" + "(?, ?)}";

			return jdbcTemplate.update(callSql, plantId, aopYear);

		} catch (Exception e) {
			throw new RuntimeException("Failed to execute stored procedure", e);
		}
	}

	// ─── Proposed AOP Export – Excel Builder ─────────────────────────────────────

	@Override
	public byte[] createProposedAOPExcel(UUID plantId, String aopYear, boolean isAfterSave,
			List<ProposedAOPDTO> dtoList) {
		try (Workbook workbook = new XSSFWorkbook();
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			if (isAfterSave) {
				// Error-report sheet: write all failed records into a single sheet
				writeProposedAOPSheet(workbook, "Errors",
						dtoList != null ? dtoList : Collections.emptyList(), true);
			} else {
				// Normal export: one sheet per grade
				Plants plants = plantsRepository.findById(plantId)
						.orElseThrow(() -> new RuntimeException("Plant not found"));
				String verticalName = verticalRepository.findById(plants.getVerticalFKId())
						.orElseThrow(() -> new RuntimeException("Vertical not found")).getName();
				String siteName = siteRepository.findById(plants.getSiteFkId())
						.orElseThrow(() -> new RuntimeException("Site not found")).getName();
				String procedureName = verticalName + "_" + siteName + "_GetProposedAOP";

				List<Map<String, Object>> gradeList = getExportGradeList(aopYear, plantId);
				for (Map<String, Object> grade : gradeList) {
					UUID gradeId = UUID.fromString(requiredGradeValue(grade, "gradeId").toString());
					String displayName = requiredGradeValue(grade, "displayName", "DisplayName").toString();
					List<ProposedAOPDTO> gradeDtoList = fetchProposedAOPFromProcedure(plantId, aopYear, gradeId,
							procedureName);
					writeProposedAOPSheet(workbook, displayName, gradeDtoList, false);
				}

				// Excel considers a workbook with no worksheets invalid and repairs it
				// when opened. This occurs when the grade view has no rows for a
				// particular plant/year.
				if (workbook.getNumberOfSheets() == 0) {
					writeProposedAOPSheet(workbook, "No Data", Collections.emptyList(), false);
				}
			}

			workbook.write(outputStream);
			byte[] workbookBytes = outputStream.toByteArray();
			validateGeneratedWorkbook(workbookBytes);
			return workbookBytes;
		} catch (Exception e) {
			throw new RuntimeException("Failed to create Proposed AOP Excel workbook", e);
		}
	}

	private List<Map<String, Object>> getExportGradeList(String aopYear, UUID plantId) {
		List<Map<String, Object>> gradeList = extractGradeList(
				aopConsumptionNormService.getConsumptionAOPGrades(aopYear, plantId.toString()));
		if (!gradeList.isEmpty()) {
			return gradeList;
		}

		// Preserve the existing consumption-grade source for working exports, but
		// fall back to the screen-specific grade source when that view has no rows.
		return extractGradeList(aopConsumptionNormService.getProposedAOPGrades(aopYear, plantId.toString()));
	}

	private List<Map<String, Object>> extractGradeList(AOPMessageVM gradesVM) {
		if (gradesVM == null || !(gradesVM.getData() instanceof List<?> data)) {
			return Collections.emptyList();
		}

		List<Map<String, Object>> gradeList = new ArrayList<>();
		for (Object item : data) {
			if (item instanceof Map<?, ?> map) {
				Map<String, Object> grade = new HashMap<>();
				map.forEach((key, value) -> grade.put(String.valueOf(key), value));
				gradeList.add(grade);
			}
		}
		return gradeList;
	}

	private Object requiredGradeValue(Map<String, Object> grade, String... keys) {
		for (String key : keys) {
			Object value = grade.get(key);
			if (value != null && !value.toString().trim().isEmpty()) {
				return value;
			}
		}
		throw new IllegalArgumentException("Grade data is missing required field: " + String.join("/", keys));
	}

	private void validateGeneratedWorkbook(byte[] workbookBytes) {
		try (Workbook validationWorkbook = new XSSFWorkbook(new ByteArrayInputStream(workbookBytes))) {
			if (validationWorkbook.getNumberOfSheets() == 0) {
				throw new IllegalStateException("Generated workbook has no worksheets");
			}
			for (int index = 0; index < validationWorkbook.getNumberOfSheets(); index++) {
				validationWorkbook.getSheetAt(index).iterator();
			}
		} catch (Exception e) {
			throw new IllegalStateException("Generated workbook package is invalid", e);
		}
	}

	private void writeProposedAOPSheet(Workbook workbook, String sheetName,
			List<ProposedAOPDTO> dtoList, boolean isAfterSave) {

		boolean isAllGrade = "All Grade".equals(sheetName);
		Sheet sheet = workbook.createSheet(createUniqueSheetName(workbook, sheetName));
		int currentRow = 0;

		// Columns 0-5: visible editable/locked data
		// Columns 6-11: hidden ID columns (NormParameterId, GradeId, AopYear, Id,
		// NormParameterTypeId, PlantId)
		// Columns 12-13: isAfterSave only (Status, Error Description)
		List<String> headerNames = new ArrayList<>(Arrays.asList(
				"Particulars", "UOM", "Last FY", "Sys Gen", "Proposed", "Remarks",
				"NormParameterId", "GradeId", "AopYear", "Id", "NormParameterTypeId", "PlantId"));
		if (isAfterSave) {
			headerNames.add("Status");
			headerNames.add("Error Description");
		}

		CellStyle headerStyle = Utility.createBoldBorderedStyle(workbook);
		CellStyle lockedStyle = Utility.createBorderedLockedStyle(workbook);
		CellStyle unlockedStyle = Utility.createBorderedUnlockedStyle(workbook);
		CellStyle wrapUnlocked = Utility.createBorderedWrapUnlockedStyle(workbook);
		CellStyle wrapLocked = Utility.createBorderedWrapLockedStyle(workbook);
		CellStyle borderedStyle = Utility.createBorderedStyle(workbook);

		Row headerRow = sheet.createRow(currentRow++);
		for (int col = 0; col < headerNames.size(); col++) {
			Cell cell = headerRow.createCell(col);
			cell.setCellValue(headerNames.get(col));
			cell.setCellStyle(headerStyle);
		}

		for (ProposedAOPDTO dto : dtoList) {
			Row row = sheet.createRow(currentRow++);

			Cell particularsCell = row.createCell(0);
			particularsCell.setCellValue(Utility.sanitizeCellString(dto.getProductName()));
			particularsCell.setCellStyle(lockedStyle);

			Cell uomCell = row.createCell(1);
			uomCell.setCellValue(Utility.sanitizeCellString(dto.getUom()));
			uomCell.setCellStyle(lockedStyle);

			Cell lastFYCell = row.createCell(2);
			setExcelCompatibleNumericValue(lastFYCell, dto.getLastFY());
			lastFYCell.setCellStyle(lockedStyle);

			Cell sysGenCell = row.createCell(3);
			setExcelCompatibleNumericValue(sysGenCell, dto.getSysGrn());
			sysGenCell.setCellStyle(lockedStyle);

			Cell proposedCell = row.createCell(4);
			setExcelCompatibleNumericValue(proposedCell, dto.getProposed());
			proposedCell.setCellStyle(isAllGrade ? lockedStyle : unlockedStyle);

			Cell remarksCell = row.createCell(5);
			remarksCell.setCellValue(Utility.sanitizeCellString(dto.getRemarks()));
			remarksCell.setCellStyle(isAllGrade ? wrapLocked : wrapUnlocked);

			Cell normParamCell = row.createCell(6);
			normParamCell.setCellValue(dto.getNormParameterId() != null ? dto.getNormParameterId().toString() : "");
			normParamCell.setCellStyle(lockedStyle);

			Cell gradeIdCell = row.createCell(7);
			gradeIdCell.setCellValue(dto.getGradeId() != null ? dto.getGradeId().toString() : "");
			gradeIdCell.setCellStyle(lockedStyle);

			Cell aopYearCell = row.createCell(8);
			aopYearCell.setCellValue(Utility.sanitizeCellString(dto.getAopYear()));
			aopYearCell.setCellStyle(lockedStyle);

			Cell idCell = row.createCell(9);
			idCell.setCellValue(dto.getId() != null ? dto.getId().toString() : "");
			idCell.setCellStyle(lockedStyle);

			Cell normParamTypeIdCell = row.createCell(10);
			normParamTypeIdCell.setCellValue(
					dto.getNormParameterTypeId() != null ? dto.getNormParameterTypeId().toString() : "");
			normParamTypeIdCell.setCellStyle(lockedStyle);

			Cell plantIdCell = row.createCell(11);
			plantIdCell.setCellValue(dto.getPlantId() != null ? dto.getPlantId().toString() : "");
			plantIdCell.setCellStyle(lockedStyle);

			if (isAfterSave) {
				Cell statusCell = row.createCell(12);
				statusCell.setCellValue(Utility.sanitizeCellString(dto.getSaveStatus()));
				statusCell.setCellStyle(borderedStyle);

				Cell errCell = row.createCell(13);
				errCell.setCellValue(Utility.sanitizeCellString(dto.getErrDescription()));
				errCell.setCellStyle(borderedStyle);
			}

			row.setHeight((short) -1);
		}

		int totalCols = isAfterSave ? 14 : 12;
		for (int col = 0; col < totalCols; col++) {
			if (col == 5 || col == 13) {
				sheet.setColumnWidth(col, 15000);
			} else {
				sheet.autoSizeColumn(col);
			}
		}

		sheet.setColumnHidden(6, true);
		sheet.setColumnHidden(7, true);
		sheet.setColumnHidden(8, true);
		sheet.setColumnHidden(9, true);
		sheet.setColumnHidden(10, true);
		sheet.setColumnHidden(11, true);

		sheet.protectSheet("");
	}

	private String createUniqueSheetName(Workbook workbook, String requestedName) {
		String baseName = Utility.sanitizeSheetName(requestedName);
		String candidate = baseName;
		int sequence = 2;
		while (workbook.getSheet(candidate) != null) {
			String suffix = " (" + sequence++ + ")";
			candidate = baseName.substring(0, Math.min(baseName.length(), 31 - suffix.length())) + suffix;
		}
		return candidate;
	}

	private void setExcelCompatibleNumericValue(Cell cell, Double value) {
		if (value == null) {
			return;
		}

		double absoluteValue = Math.abs(value);
		boolean isExcelNumber = Double.isFinite(value)
				&& absoluteValue <= EXCEL_MAX_NUMBER
				&& (absoluteValue == 0 || absoluteValue >= EXCEL_MIN_POSITIVE_NUMBER);
		if (isExcelNumber) {
			cell.setCellValue(value);
		} else {
			// OOXML can contain Java doubles outside Excel's numeric range, but
			// desktop Excel repairs those cells. Text preserves the source value
			// and remains parseable by the existing import logic.
			cell.setCellValue(Double.toString(value));
		}
	}

	// ─── Proposed AOP Import – Excel Reader ──────────────────────────────────────

	public List<ProposedAOPDTO> readProposedAOPExcel(InputStream inputStream) {
		List<ProposedAOPDTO> resultList = new ArrayList<>();
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			int sheetCount = workbook.getNumberOfSheets();
			for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
				Sheet sheet = workbook.getSheetAt(sheetIndex);
				Iterator<Row> rowIterator = sheet.iterator();

				if (rowIterator.hasNext())
					rowIterator.next(); // skip header row

				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();

					// Skip completely empty rows
					boolean isEmpty = true;
					for (int c = 0; c < row.getLastCellNum(); c++) {
						Cell cell = row.getCell(c);
						if (cell != null && cell.getCellType() != CellType.BLANK
								&& !cell.toString().trim().isEmpty()) {
							isEmpty = false;
							break;
						}
					}
					if (isEmpty)
						continue;

					ProposedAOPDTO dto = new ProposedAOPDTO();
					try {
						Cell particularsCell = row.getCell(0);
						if (particularsCell != null) {
							dto.setProductName(particularsCell.toString().trim());
						}

						Cell uomCell = row.getCell(1);
						if (uomCell != null) {
							dto.setUom(uomCell.toString().trim());
						}

						Cell lastFYCell = row.getCell(2);
						if (lastFYCell != null && lastFYCell.getCellType() != CellType.BLANK) {
							if (lastFYCell.getCellType() == CellType.NUMERIC) {
								dto.setLastFY(lastFYCell.getNumericCellValue());
							} else {
								String val = lastFYCell.toString().trim();
								if (!val.isEmpty())
									dto.setLastFY(Double.parseDouble(val));
							}
						}

						Cell sysGenCell = row.getCell(3);
						if (sysGenCell != null && sysGenCell.getCellType() != CellType.BLANK) {
							if (sysGenCell.getCellType() == CellType.NUMERIC) {
								dto.setSysGrn(sysGenCell.getNumericCellValue());
							} else {
								String val = sysGenCell.toString().trim();
								if (!val.isEmpty())
									dto.setSysGrn(Double.parseDouble(val));
							}
						}

						Cell proposedCell = row.getCell(4);
						if (proposedCell != null && proposedCell.getCellType() != CellType.BLANK) {
							if (proposedCell.getCellType() == CellType.NUMERIC) {
								dto.setProposed(proposedCell.getNumericCellValue());
							} else {
								String val = proposedCell.toString().trim();
								if (!val.isEmpty())
									dto.setProposed(Double.parseDouble(val));
							}
						}

						Cell remarksCell = row.getCell(5);
						if (remarksCell != null) {
							dto.setRemarks(remarksCell.toString().trim());
						}

						Cell normParamCell = row.getCell(6);
						if (normParamCell != null) {
							String val = normParamCell.toString().trim();
							if (!val.isEmpty())
								dto.setNormParameterId(UUID.fromString(val));
						}

						Cell gradeIdCell = row.getCell(7);
						if (gradeIdCell != null) {
							String val = gradeIdCell.toString().trim();
							if (!val.isEmpty())
								dto.setGradeId(UUID.fromString(val));
						}

						Cell aopYearCell = row.getCell(8);
						if (aopYearCell != null) {
							String val = aopYearCell.toString().trim();
							if (!val.isEmpty())
								dto.setAopYear(val);
						}

						Cell idCell = row.getCell(9);
						if (idCell != null) {
							String val = idCell.toString().trim();
							if (!val.isEmpty())
								dto.setId(UUID.fromString(val));
						}

						Cell normParamTypeIdCell = row.getCell(10);
						if (normParamTypeIdCell != null) {
							String val = normParamTypeIdCell.toString().trim();
							if (!val.isEmpty())
								dto.setNormParameterTypeId(UUID.fromString(val));
						}

						Cell plantIdCell = row.getCell(11);
						if (plantIdCell != null) {
							String val = plantIdCell.toString().trim();
							if (!val.isEmpty())
								dto.setPlantId(UUID.fromString(val));
						}

					} catch (Exception e) {
						e.printStackTrace();
						dto.setSaveStatus("Failed");
						dto.setErrDescription(e.getMessage() != null ? e.getMessage() : "Failed to read row");
					}
					resultList.add(dto);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to read Proposed AOP Excel", e);
		}
		return resultList;
	}

	// ─── Proposed AOP Import – API
	// ────────────────────────────────────────────────

	@Override
	@Transactional
	public AOPMessageVM importProposedAOPExcel(MultipartFile file) {
		if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
			throw new IllegalArgumentException("Invalid or empty Excel file.");
		}
		try {
			List<ProposedAOPDTO> data = readProposedAOPExcel(file.getInputStream());
			List<ProposedAOPDTO> failedRecords = new ArrayList<>();

			// Cache grades per (plantId_aopYear) to avoid redundant DB calls
			Map<String, List<Map<String, Object>>> gradesCache = new HashMap<>();

			for (ProposedAOPDTO dto : data) {
				if ("Failed".equals(dto.getSaveStatus())) {
					failedRecords.add(dto);
					continue;
				}

				// Skip rows belonging to an "All Grade" grade silently
				if (dto.getPlantId() != null && dto.getAopYear() != null && dto.getGradeId() != null) {
					String plantIdStr = dto.getPlantId().toString();
					String aopYearStr = dto.getAopYear();
					String cacheKey = plantIdStr + "_" + aopYearStr;

					List<Map<String, Object>> grades = gradesCache.computeIfAbsent(cacheKey, k -> {
						AOPMessageVM gradesVM = aopConsumptionNormService.getProposedAOPGrades(aopYearStr,
								plantIdStr);
						@SuppressWarnings("unchecked")
						List<Map<String, Object>> list = (List<Map<String, Object>>) gradesVM.getData();
						return list != null ? list : new ArrayList<>();
					});

					boolean isAllGrade = grades.stream()
							.anyMatch(g -> g.get("gradeId") != null
									&& g.get("gradeId").toString()
											.equalsIgnoreCase(dto.getGradeId().toString())
									&& g.get("DisplayName") != null
									&& "All Grade".equalsIgnoreCase(g.get("DisplayName").toString()));

					if (isAllGrade) {
						continue;
					}
				}

				try {
					saveProposedAOP(Collections.singletonList(dto));
				} catch (IllegalArgumentException e) {
					dto.setSaveStatus("Failed");
					dto.setErrDescription(e.getMessage() != null ? e.getMessage() : "Invalid argument");
					failedRecords.add(dto);
				} catch (Exception e) {
					throw new RestInvalidArgumentException("Failed to import Proposed AOP data", e);
				}
			}

			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (!failedRecords.isEmpty()) {
				// isAfterSave=true: dtoList is used directly, no DB fetch required
				byte[] fileByteArray = createProposedAOPExcel(null, null, true, failedRecords);
				String base64File = Base64.getEncoder().encodeToString(fileByteArray);
				aopMessageVM.setData(base64File);
				aopMessageVM.setCode(400);
				aopMessageVM.setMessage("Partial data has been saved");
			} else {
				aopMessageVM.setCode(200);
				aopMessageVM.setMessage("All data has been saved");
			}
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid argument", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to import Proposed AOP data", ex);
		}
	}

	public AOPMessageVM getProposedSteadyState(UUID plantId, String aopYear) {

		Plants plants = plantsRepository.findById(plantId).orElseThrow(() -> new RuntimeException("Plant not found"));
		String verticalName = verticalRepository.findById(plants.getVerticalFKId())
				.orElseThrow(() -> new RuntimeException("Vertical not found")).getName();
		String siteName = siteRepository.findById(plants.getSiteFkId())
				.orElseThrow(() -> new RuntimeException("Site not found")).getName();

		String procedureName = verticalName + "_" + siteName + "_GetProposedAOP";
		List<ProposedAOPDTO> proposedAOP = fetchProposedSteadyStateFromProcedure(plantId, aopYear, procedureName);

		Map<String, Object> map = new HashMap<>();

		List<AopCalculation> aopCalculation = aopCalculationRepository
				.findByPlantIdAndAopYearAndCalculationScreen(plantId, aopYear, "proposed-aop");
		map.put("proposedAOP", proposedAOP);
		map.put("aopCalculation", aopCalculation);
		return AOPMessageVM.builder()
				.code(200)
				.message("Proposed AOPs fetched successfully")
				.data(map)
				.build();
	}

	public List<ProposedAOPDTO> fetchProposedSteadyStateFromProcedure(UUID plantId, String aopYear,
			String procedureName) {

		String sql = "EXEC " +  "[" + procedureName + "]" + " @plantId = ?, @aopYear = ?";
		return jdbcTemplate.query(sql, (rs, rowNum) -> {
				Boolean isEditable = null;
				Object isEditableObj = rs.getObject("IsEditable");
				if (isEditableObj != null) {
					if (isEditableObj instanceof Boolean) {
						isEditable = (Boolean) isEditableObj;
					} else if (isEditableObj instanceof Number) {
						isEditable = ((Number) isEditableObj).intValue() == 1;
					}
				}

				return ProposedAOPDTO.builder()
						.id(rs.getString("Id") != null ? UUID.fromString(rs.getString("Id")) : null)
						.normParameterId(
								rs.getString("NormparameterId") != null ? UUID.fromString(rs.getString("NormparameterId"))
										: null)
						.normParameterTypeId(rs.getString("NormParameterTypeId") != null
								? UUID.fromString(rs.getString("NormParameterTypeId"))
								: null)
						.normParameterTypeDisplayName(rs.getString("NormParameterTypeDisplayName"))
						.productName(rs.getString("ProductName"))
						.uom(rs.getString("UOM"))
						.lastFY(rs.getDouble("LastFY"))
						.actualLastFY(rs.getDouble("ActualLastFY"))
						.sysGrn(rs.getDouble("SysGrn"))
						.proposed(rs.getDouble("Proposed"))
						.remarks(rs.getString("Remarks"))
						.plantId(rs.getString("PlantId") != null ? UUID.fromString(rs.getString("PlantId")) : null)
						.aopYear(rs.getString("AopYear"))
						.sapCode(rs.getString("SapMaterialCode"))
						.isEditable(isEditable)
						.build();
				},
				plantId.toString(), aopYear);
	}

	@Override
	@Transactional
	public List<ProposedAOPDTO> saveProposedSteadyState(List<ProposedAOPDTO> dtoList) {
		try {

			List<ProposedAOPDTO> failedList = new ArrayList<>();

			UUID plantId = null;
			String year = null;
			for (ProposedAOPDTO dto : dtoList) {

				if (dto.getNormParameterId() == null || dto.getAopYear() == null) {
					throw new RuntimeException("NormParameterId and AopYear are required");
				}
				if (plantId == null) {
					plantId = dto.getPlantId();
				}
				if (year == null) {
					year = dto.getAopYear();
				}

				// Remark validation: if Proposed value changed, Remark must also be updated
				if (!isRemarkValidForProposedChange(dto)) {
					dto.setSaveStatus("Failed");
					dto.setErrDescription(
							"Please update Remark");
					failedList.add(dto);
					continue;
				}

				// update MCUNormsValue_Proposed
				String update_NormsValue_Proposed = "UPDATE MCUNormsValue_Proposed " +
						"SET April = ?, May = ?, June = ?, July = ?, August = ?, September = ?, " +
						"October = ?, November = ?, December = ?, January = ?, February = ?, March = ?, Remarks = ? "
						+
						"WHERE Material_FK_Id = ? and FinancialYear = ? and Plant_FK_Id = ?";
				jdbcTemplate.update(update_NormsValue_Proposed,
						dto.getProposed(), dto.getProposed(), dto.getProposed(), dto.getProposed(),
						dto.getProposed(), dto.getProposed(), dto.getProposed(), dto.getProposed(),
						dto.getProposed(), dto.getProposed(), dto.getProposed(), dto.getProposed(),
						dto.getRemarks(),
						dto.getNormParameterId(), dto.getAopYear(), dto.getPlantId());

						
				// update MCUNormsValue
				String update_NormsValue = "UPDATE MCUNormsValue " +
						"SET April = ?, May = ?, June = ?, July = ?, August = ?, September = ?, " +
						"October = ?, November = ?, December = ?, January = ?, February = ?, March = ?, Remarks = ? "
						+
						"WHERE Material_FK_Id = ? and FinancialYear = ? and Plant_FK_Id = ?";

						jdbcTemplate.update(update_NormsValue,
							dto.getProposed(), dto.getProposed(), dto.getProposed(), dto.getProposed(),
							dto.getProposed(), dto.getProposed(), dto.getProposed(), dto.getProposed(),
							dto.getProposed(), dto.getProposed(), dto.getProposed(), dto.getProposed(),
							dto.getRemarks(),
							dto.getNormParameterId(), dto.getAopYear(), dto.getPlantId());

			}

			// sp call logic 

			Plants plants = plantsRepository.findById(plantId).orElseThrow(() -> new RuntimeException("Plant not found"));
			String verticalName = verticalRepository.findById(plants.getVerticalFKId()).orElseThrow(() -> new RuntimeException("Vertical not found")).getName();
			String siteName = siteRepository.findById(plants.getSiteFkId()).orElseThrow(() -> new RuntimeException("Site not found")).getName();
	
			String procedureName = verticalName + "_" + siteName + "_CalculateTotalNorms";

			Integer result = executeCalculateSP(String.valueOf(plantId), year, procedureName);

			if (plantId != null && year != null) {
				List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("proposed-aop");
				for (ScreenMapping screenMapping : screenMappingList) {
					AopCalculation aopCalculation = new AopCalculation();
					aopCalculation.setAopYear(year);
					aopCalculation.setIsChanged(true);
					aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
					aopCalculation.setPlantId(plantId);
					aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
					aopCalculationRepository.save(aopCalculation);
				}
			}

		 return failedList;
		} catch (Exception e) {
			throw new RuntimeException("Failed to save proposed AOP", e);
		}
	}


	private boolean isRemarkValidForProposedChange(ProposedAOPDTO dto) {
		String fetchSql = "SELECT TOP 1 April, Remarks FROM MCUNormsValue_Proposed " +
				"WHERE Material_FK_Id = ? AND FinancialYear = ? AND Plant_FK_Id = ?";

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(fetchSql,
				dto.getNormParameterId(), dto.getAopYear(), dto.getPlantId());

		if (rows.isEmpty()) {
			return true; // No existing record – nothing to compare against
		}

		Map<String, Object> existing = rows.get(0);

		Double existingProposed = existing.get("April") != null
				? ((Number) existing.get("April")).doubleValue()
				: null;
		String existingRemarks = existing.get("Remarks") != null
				? existing.get("Remarks").toString().trim()
				: "";

		Double incomingProposed = dto.getProposed();
		String incomingRemarks = dto.getRemarks() != null ? dto.getRemarks().trim() : "";

		// Determine whether the proposed value has changed
		boolean proposedChanged;
		if (existingProposed == null && incomingProposed == null) {
			proposedChanged = false;
		} else if (existingProposed == null || incomingProposed == null) {
			proposedChanged = true;
		} else {
			proposedChanged = Double.compare(existingProposed, incomingProposed) != 0;
		}

		if (!proposedChanged) {
			return true; // Value unchanged – remark update not required
		}

		// Proposed changed – remark must also have changed
		return !existingRemarks.equals(incomingRemarks);
	}

	public Integer executeCalculateSP( String plantId, String aopYear, String procedureName) {
		try {

			String callSql = "{call " + "[" + procedureName + "]" + "(?, ?)}";


			return jdbcTemplate.update(callSql, plantId, aopYear);

		} catch (Exception e) {
			throw new RuntimeException("Failed to execute stored procedure", e);
		}
	}

	// ─── Proposed Steady State Export – Excel Builder ────────────────────────────

	@Override
	public byte[] createProposedSteadyStateExcel(UUID plantId, String aopYear, boolean isAfterSave,
			List<ProposedAOPDTO> dtoList) {
		try (Workbook workbook = new XSSFWorkbook();
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			if (isAfterSave) {
				writeProposedSteadyStateSheet(workbook, "Errors",
						dtoList != null ? dtoList : Collections.emptyList(), true);
			} else {

				Map<String, Object> responseData =
				(Map<String, Object>) getProposedSteadyState(plantId, aopYear).getData();
		
		       List<ProposedAOPDTO> data =
				(List<ProposedAOPDTO>) responseData.get("proposedAOP");
				
				writeProposedSteadyStateSheet(workbook, "Proposed Steady State", data, false);

				if (workbook.getNumberOfSheets() == 0) {
					writeProposedSteadyStateSheet(workbook, "No Data", Collections.emptyList(), false);
				}
			}

			workbook.write(outputStream);
			byte[] workbookBytes = outputStream.toByteArray();
			validateGeneratedWorkbook(workbookBytes);
			return workbookBytes;
		} catch (Exception e) {
			throw new RuntimeException("Failed to create Proposed Steady State Excel workbook", e);
		}
	}

	private void writeProposedSteadyStateSheet(Workbook workbook, String sheetName,
			List<ProposedAOPDTO> dtoList, boolean isAfterSave) {

		Sheet sheet = workbook.createSheet(createUniqueSheetName(workbook, sheetName));
		int currentRow = 0;

		// Columns 0-7: visible data | Columns 8-12: hidden ID columns
		// (NormParameterId, AopYear, Id, NormParameterTypeId, PlantId)
		// Columns 13-14: isAfterSave only (Status, Error Description)
		// Col 0: SAP MAT Code | Col 1: Particulars
		List<String> headerNames = new ArrayList<>(Arrays.asList(
				"SAP MAT Code", "Particulars", "UOM / MT", "Last FY", "Actual Last FY", "Sys Gen", "Proposed", "Remarks",
				"NormParameterId", "AopYear", "Id", "NormParameterTypeId", "PlantId"));
		if (isAfterSave) {
			headerNames.add("Status");
			headerNames.add("Error Description");
		}

		CellStyle headerStyle = Utility.createBoldBorderedStyle(workbook);
		CellStyle lockedStyle = Utility.createBorderedLockedStyle(workbook);
		CellStyle unlockedStyle = Utility.createBorderedUnlockedStyle(workbook);
		CellStyle wrapUnlocked = Utility.createBorderedWrapUnlockedStyle(workbook);
		CellStyle borderedStyle = Utility.createBorderedStyle(workbook);

		Row headerRow = sheet.createRow(currentRow++);
		for (int col = 0; col < headerNames.size(); col++) {
			Cell cell = headerRow.createCell(col);
			cell.setCellValue(headerNames.get(col));
			cell.setCellStyle(headerStyle);
		}

		for (ProposedAOPDTO dto : dtoList) {
			Row row = sheet.createRow(currentRow++);

			// col 0: SAP MAT Code (read-only display column)
			Cell sapCodeCell = row.createCell(0);
			sapCodeCell.setCellValue(Utility.sanitizeCellString(dto.getSapCode()));
			sapCodeCell.setCellStyle(lockedStyle);

			// col 1: Particulars
			Cell particularsCell = row.createCell(1);
			particularsCell.setCellValue(Utility.sanitizeCellString(dto.getProductName()));
			particularsCell.setCellStyle(lockedStyle);

			// col 2: UOM
			Cell uomCell = row.createCell(2);
			uomCell.setCellValue(Utility.sanitizeCellString(dto.getUom()));
			uomCell.setCellStyle(lockedStyle);

			// col 3: Last FY
			Cell lastFYCell = row.createCell(3);
			setExcelCompatibleNumericValue(lastFYCell, dto.getLastFY());
			lastFYCell.setCellStyle(lockedStyle);

			// col 4: Actual Last FY
			Cell actualLastFYCell = row.createCell(4);
			setExcelCompatibleNumericValue(actualLastFYCell, dto.getActualLastFY());
			actualLastFYCell.setCellStyle(lockedStyle);

			// col 5: Sys Gen
			Cell sysGenCell = row.createCell(5);
			setExcelCompatibleNumericValue(sysGenCell, dto.getSysGrn());
			sysGenCell.setCellStyle(lockedStyle);

			// col 6: Proposed
			Cell proposedCell = row.createCell(6);
			setExcelCompatibleNumericValue(proposedCell, dto.getProposed());
			proposedCell.setCellStyle(unlockedStyle);

			// col 7: Remarks
			Cell remarksCell = row.createCell(7);
			remarksCell.setCellValue(Utility.sanitizeCellString(dto.getRemarks()));
			remarksCell.setCellStyle(wrapUnlocked);

			// col 8: NormParameterId (hidden)
			Cell normParamCell = row.createCell(8);
			normParamCell.setCellValue(dto.getNormParameterId() != null ? dto.getNormParameterId().toString() : "");
			normParamCell.setCellStyle(lockedStyle);

			// col 9: AopYear (hidden)
			Cell aopYearCell = row.createCell(9);
			aopYearCell.setCellValue(Utility.sanitizeCellString(dto.getAopYear()));
			aopYearCell.setCellStyle(lockedStyle);

			// col 10: Id (hidden)
			Cell idCell = row.createCell(10);
			idCell.setCellValue(dto.getId() != null ? dto.getId().toString() : "");
			idCell.setCellStyle(lockedStyle);

			// col 11: NormParameterTypeId (hidden)
			Cell normParamTypeIdCell = row.createCell(11);
			normParamTypeIdCell.setCellValue(
					dto.getNormParameterTypeId() != null ? dto.getNormParameterTypeId().toString() : "");
			normParamTypeIdCell.setCellStyle(lockedStyle);

			// col 12: PlantId (hidden)
			Cell plantIdCell = row.createCell(12);
			plantIdCell.setCellValue(dto.getPlantId() != null ? dto.getPlantId().toString() : "");
			plantIdCell.setCellStyle(lockedStyle);

			if (isAfterSave) {
				// col 13: Status
				Cell statusCell = row.createCell(13);
				statusCell.setCellValue(Utility.sanitizeCellString(dto.getSaveStatus()));
				statusCell.setCellStyle(borderedStyle);

				// col 14: Error Description
				Cell errCell = row.createCell(14);
				errCell.setCellValue(Utility.sanitizeCellString(dto.getErrDescription()));
				errCell.setCellStyle(borderedStyle);
			}

			row.setHeight((short) -1);
		}

		int totalCols = isAfterSave ? 15 : 13;
		for (int col = 0; col < totalCols; col++) {
			if (col == 7 || col == 14) {
				sheet.setColumnWidth(col, 15000);
			} else {
				sheet.autoSizeColumn(col);
			}
		}

		sheet.setColumnHidden(8, true);
		sheet.setColumnHidden(9, true);
		sheet.setColumnHidden(10, true);
		sheet.setColumnHidden(11, true);
		sheet.setColumnHidden(12, true);

		sheet.protectSheet("");
	}

	// ─── Proposed Steady State Import – Excel Reader ─────────────────────────────

	public List<ProposedAOPDTO> readProposedSteadyStateExcel(InputStream inputStream) {
		List<ProposedAOPDTO> resultList = new ArrayList<>();
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			int sheetCount = workbook.getNumberOfSheets();
			for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
				Sheet sheet = workbook.getSheetAt(sheetIndex);
				Iterator<Row> rowIterator = sheet.iterator();

				if (rowIterator.hasNext())
					rowIterator.next(); // skip header row

				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();

					boolean isEmpty = true;
					for (int c = 0; c < row.getLastCellNum(); c++) {
						Cell cell = row.getCell(c);
						if (cell != null && cell.getCellType() != CellType.BLANK
								&& !cell.toString().trim().isEmpty()) {
							isEmpty = false;
							break;
						}
					}
					if (isEmpty)
						continue;

					ProposedAOPDTO dto = new ProposedAOPDTO();
					try {
						// col 0: SAP MAT Code – read-only display column; not stored in DTO

						// col 1: Particulars
						Cell particularsCell = row.getCell(1);
						if (particularsCell != null) {
							dto.setProductName(particularsCell.toString().trim());
						}

						// col 2: UOM
						Cell uomCell = row.getCell(2);
						if (uomCell != null) {
							dto.setUom(uomCell.toString().trim());
						}

						// col 3: Last FY
						Cell lastFYCell = row.getCell(3);
						if (lastFYCell != null && lastFYCell.getCellType() != CellType.BLANK) {
							if (lastFYCell.getCellType() == CellType.NUMERIC) {
								dto.setLastFY(lastFYCell.getNumericCellValue());
							} else {
								String val = lastFYCell.toString().trim();
								if (!val.isEmpty())
									dto.setLastFY(Double.parseDouble(val));
							}
						}

						// col 4: Actual Last FY
						Cell actualLastFYCell = row.getCell(4);
						if (actualLastFYCell != null && actualLastFYCell.getCellType() != CellType.BLANK) {
							if (actualLastFYCell.getCellType() == CellType.NUMERIC) {
								dto.setActualLastFY(actualLastFYCell.getNumericCellValue());
							} else {
								String val = actualLastFYCell.toString().trim();
								if (!val.isEmpty())
									dto.setActualLastFY(Double.parseDouble(val));
							}
						}

						// col 5: Sys Gen
						Cell sysGenCell = row.getCell(5);
						if (sysGenCell != null && sysGenCell.getCellType() != CellType.BLANK) {
							if (sysGenCell.getCellType() == CellType.NUMERIC) {
								dto.setSysGrn(sysGenCell.getNumericCellValue());
							} else {
								String val = sysGenCell.toString().trim();
								if (!val.isEmpty())
									dto.setSysGrn(Double.parseDouble(val));
							}
						}

						// col 6: Proposed
						Cell proposedCell = row.getCell(6);
						if (proposedCell != null && proposedCell.getCellType() != CellType.BLANK) {
							if (proposedCell.getCellType() == CellType.NUMERIC) {
								dto.setProposed(proposedCell.getNumericCellValue());
							} else {
								String val = proposedCell.toString().trim();
								if (!val.isEmpty())
									dto.setProposed(Double.parseDouble(val));
							}
						}

						// col 7: Remarks
						Cell remarksCell = row.getCell(7);
						if (remarksCell != null) {
							dto.setRemarks(remarksCell.toString().trim());
						}

						// col 8: NormParameterId (no GradeId column in steady-state sheet)
						Cell normParamCell = row.getCell(8);
						if (normParamCell != null) {
							String val = normParamCell.toString().trim();
							if (!val.isEmpty())
								dto.setNormParameterId(UUID.fromString(val));
						}

						// col 9: AopYear
						Cell aopYearCell = row.getCell(9);
						if (aopYearCell != null) {
							String val = aopYearCell.toString().trim();
							if (!val.isEmpty())
								dto.setAopYear(val);
						}

						// col 10: Id
						Cell idCell = row.getCell(10);
						if (idCell != null) {
							String val = idCell.toString().trim();
							if (!val.isEmpty())
								dto.setId(UUID.fromString(val));
						}

						// col 11: NormParameterTypeId
						Cell normParamTypeIdCell = row.getCell(11);
						if (normParamTypeIdCell != null) {
							String val = normParamTypeIdCell.toString().trim();
							if (!val.isEmpty())
								dto.setNormParameterTypeId(UUID.fromString(val));
						}

						// col 12: PlantId
						Cell plantIdCell = row.getCell(12);
						if (plantIdCell != null) {
							String val = plantIdCell.toString().trim();
							if (!val.isEmpty())
								dto.setPlantId(UUID.fromString(val));
						}

					} catch (Exception e) {
						e.printStackTrace();
						dto.setSaveStatus("Failed");
						dto.setErrDescription(e.getMessage() != null ? e.getMessage() : "Failed to read row");
					}
					resultList.add(dto);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to read Proposed Steady State Excel", e);
		}
		return resultList;
	}

	// ─── Proposed Steady State Import – API ──────────────────────────────────────

	@Override
	@Transactional
	public AOPMessageVM importProposedSteadyState(MultipartFile file) {
		if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
			throw new IllegalArgumentException("Invalid or empty Excel file.");
		}
		try {
			List<ProposedAOPDTO> data = readProposedSteadyStateExcel(file.getInputStream());
			List<ProposedAOPDTO> failedRecords = new ArrayList<>();

			for (ProposedAOPDTO dto : data) {
				if ("Failed".equals(dto.getSaveStatus())) {
					failedRecords.add(dto);
					continue;
				}

				try {
				List<ProposedAOPDTO> failedToSaveRecords = saveProposedSteadyState(Collections.singletonList(dto));
				failedRecords.addAll(failedToSaveRecords);
				} catch (IllegalArgumentException e) {
					dto.setSaveStatus("Failed");
					dto.setErrDescription(e.getMessage() != null ? e.getMessage() : "Invalid argument");
					failedRecords.add(dto);
				} catch (Exception e) {
					throw new RestInvalidArgumentException("Failed to import Proposed Steady State data", e);
				}
			}

			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (!failedRecords.isEmpty()) {
				byte[] fileByteArray = createProposedSteadyStateExcel(null, null, true, failedRecords);
				String base64File = Base64.getEncoder().encodeToString(fileByteArray);
				aopMessageVM.setData(base64File);
				aopMessageVM.setCode(400);
				aopMessageVM.setMessage("Partial data has been saved");
			} else {
				aopMessageVM.setCode(200);
				aopMessageVM.setMessage("All data has been saved");
			}
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid argument", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to import Proposed Steady State data", ex);
		}
	}

}
