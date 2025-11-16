package com.wks.caseengine.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wks.caseengine.dto.AOPMaintenanceDesignRemarksDTO;
import com.wks.caseengine.dto.BudgetMaintenanceDto;
import com.wks.caseengine.dto.DecokePlanningDTO;
import com.wks.caseengine.dto.MaintenanceDetailsDTO;
import com.wks.caseengine.dto.MaintenanceReportURLDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.BudgetMaintenance;
import com.wks.caseengine.entity.DecokeMaintenance;

import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.BudgetMaintenanceRepository;
import com.wks.caseengine.repository.DecokeMaintenanceRepository;
import com.wks.caseengine.repository.DecokePlanningRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

import com.wks.caseengine.utility.Utility;

@Service
public class MaintenanceCalculatedDataServiceImpl implements MaintenanceCalculatedDataService {

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private DecokePlanningRepository decokePlanningRepository;
	
	@Autowired
	private DecokeMaintenanceRepository decokeMaintenanceRepository;
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;
	
	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	@Autowired
	private BudgetMaintenanceRepository budgetMaintenanceRepository;
	
	@Autowired
	private ExcelUtilityService excelUtilityService;
	
	@Autowired
	private AOPMaintenanceDesignBasisService aopMaintenanceDesignBasisService;
	
	@Autowired
	private AOPMaintenanceDesignRemarksService aopMaintenanceDesignRemarksService;

	@Override
	public List<MaintenanceDetailsDTO> getMaintenanceCalculatedData(String plantId, String year) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_GETMaintenance";
			List<Object[]> list = executeDynamicStoredProcedure(storedProcedure, plantId, site.getId().toString(),
					vertical.getId().toString(), year);
			List<MaintenanceDetailsDTO> maintenanceDetailsDTOList = new ArrayList<>();
			for (Object[] row : list) {
				MaintenanceDetailsDTO dto = new MaintenanceDetailsDTO();
				dto.setName(row[2] != null ? row[2].toString() : null);
				dto.setJan(row[3] != null ? Double.valueOf(row[3].toString()) : null);
				dto.setFeb(row[4] != null ? Double.valueOf(row[4].toString()) : null);
				dto.setMar(row[5] != null ? Double.valueOf(row[5].toString()) : null);
				dto.setApril(row[6] != null ? Double.valueOf(row[6].toString()) : null);
				dto.setMay(row[7] != null ? Double.valueOf(row[7].toString()) : null);
				dto.setJune(row[8] != null ? Double.valueOf(row[8].toString()) : null);
				dto.setJuly(row[9] != null ? Double.valueOf(row[9].toString()) : null);
				dto.setAug(row[10] != null ? Double.valueOf(row[10].toString()) : null);
				dto.setSep(row[11] != null ? Double.valueOf(row[11].toString()) : null);
				dto.setOct(row[12] != null ? Double.valueOf(row[12].toString()) : null);
				dto.setNov(row[13] != null ? Double.valueOf(row[13].toString()) : null);
				dto.setDec(row[14] != null ? Double.valueOf(row[14].toString()) : null);
				maintenanceDetailsDTOList.add(dto);
			}

			return maintenanceDetailsDTOList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Transactional
	public List<Object[]> executeDynamicStoredProcedure(String procedureName, String plantId, String siteId,
			String verticalId, String aopYear) {
		try {
			String sql = "EXEC " + procedureName
					+ " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @aopYear = :aopYear";
			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("siteId", siteId);
			query.setParameter("verticalId", verticalId);
			query.setParameter("aopYear", aopYear);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM getMaintenanceDataForCracker(String plantId, String year) {

		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<Map<String, Object>> data = new ArrayList<>();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			String procedureName = "vwScrn" + vertical.getName() + "_" + site.getName() + "_Decoke_Maintenance";
			List<Object[]> results = getData(plantId, year, procedureName);
			// Variables to accumulate totals
			double sumFiveF = 0;
			double sumFourF = 0;
			double sumFourFD = 0;
			double sumCoilReplacement = 0;
			double sumShutdown = 0;
			double sumSlowdown = 0;
			double sumSAD = 0;
			double sumBBU = 0;
			double sumBBD = 0;
			double sumDemoSAD = 0;
			double sumDemoSD = 0;
			double sumDemoBBU = 0;
			double sumMnt = 0;
			double sumTotal = 0;
			double sumNumberOfDays = 0;
			double sumTotalSAD = 0;
			double sumDemoHHS=0;

			for (Object[] row : results) {
			    Map<String, Object> map = new HashMap<>();

			    map.put("id", row[0]);
			    map.put("monthName", row[1]);
			    map.put("coilReplacement", row[2]);
			    map.put("mnt", row[3]);
			    map.put("shutdown", row[4]);
			    map.put("slowdown", row[5]);
			    map.put("sad", row[6]);
			    map.put("bbd", row[7]);
			    map.put("bbu", row[8]);
			    map.put("demoHSS", row[9]);
			    map.put("demoBBU", row[10]);
			    map.put("demoSAD", row[11]);
			    map.put("demoSD", row[12]);
			    map.put("fourFD", row[13]);
			    map.put("fourF", row[14]);
			    map.put("fiveF", row[15]);
			    map.put("total", row[16]);
			    map.put("fourFHours", row[17]);
			    map.put("aopYear", row[18]);
			    map.put("plantId", row[19]);
			    String remarks = row[20] == null ? "" : row[20].toString();
			    map.put("remarks", remarks);
			    map.put("totalSAD", row[21]);
			    map.put("numberOfDays", row[22]);

			    // accumulate totals (check for nulls and cast appropriately)
			    sumCoilReplacement += (row[2] != null ? ((Number) row[2]).doubleValue() : 0);
			    sumMnt += (row[3] != null ? ((Number) row[3]).doubleValue() : 0);
			    sumShutdown += (row[4] != null ? ((Number) row[4]).doubleValue() : 0);
			    sumSlowdown += (row[5] != null ? ((Number) row[5]).doubleValue() : 0);
			    sumSAD += (row[6] != null ? ((Number) row[6]).doubleValue() : 0);
			    sumBBD += (row[7] != null ? ((Number) row[7]).doubleValue() : 0);
			    sumBBU += (row[8] != null ? ((Number) row[8]).doubleValue() : 0);
			    sumDemoSAD += (row[11] != null ? ((Number) row[11]).doubleValue() : 0);
			    sumDemoSD += (row[12] != null ? ((Number) row[12]).doubleValue() : 0);
			    sumDemoBBU += (row[10] != null ? ((Number) row[10]).doubleValue() : 0);
			    sumFourFD += (row[13] != null ? ((Number) row[13]).doubleValue() : 0);
			    sumFourF += (row[14] != null ? ((Number) row[14]).doubleValue() : 0);
			    sumFiveF += (row[15] != null ? ((Number) row[15]).doubleValue() : 0);
			    sumTotal += (row[16] != null ? ((Number) row[16]).doubleValue() : 0);
			    sumNumberOfDays += (row[22] != null ? ((Number) row[22]).doubleValue() : 0);
			    sumTotalSAD += (row[21] != null ? ((Number) row[21]).doubleValue() : 0);
			    sumDemoHHS+=(row[9] != null ? ((Number) row[9]).doubleValue() : 0);
			    data.add(map);
			}

			
			Map<String, Object> sumMap = new HashMap<>();
			sumMap.put("coilReplacement", sumCoilReplacement);
			sumMap.put("mnt", sumMnt);
			sumMap.put("shutdown", sumShutdown);
			sumMap.put("slowdown", sumSlowdown);
			sumMap.put("sad", sumSAD);
			sumMap.put("bbd", sumBBD);
			sumMap.put("bbu", sumBBU);
			sumMap.put("demoSAD", sumDemoSAD);
			sumMap.put("demoSD", sumDemoSD);
			sumMap.put("demoBBU", sumDemoBBU);
			sumMap.put("demoHHS", sumDemoHHS);
			sumMap.put("fourFD", sumFourFD);
			sumMap.put("fourF", sumFourF);
			sumMap.put("fiveF", sumFiveF);
			sumMap.put("total", sumTotal);
			sumMap.put("numberOfDays", sumNumberOfDays);
			sumMap.put("totalSAD", sumTotalSAD);
			
			sumMap.put("id", null);
			sumMap.put("monthName", "Total");
			List<AopCalculation> aopCalculation = aopCalculationRepository
					.findByPlantIdAndAopYearAndCalculationScreen(
							UUID.fromString(plantId), year, "Furnace-run-length");	
			data.add(sumMap);
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("data", data);
			map.put("aopCalculation", aopCalculation);
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(map);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	public byte[] maintenanceExport(String year, String plantId, boolean isAfterSave, List<DecokePlanningDTO> dtoList) {
	    try {
	        Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
	        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
	        Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
	        String procedureName = "vwScrn" + vertical.getName() + "_" + site.getName() + "_Decoke_Maintenance";

	        if (!isAfterSave) {
	            List<Object[]> results = getData(plantId, year, procedureName);
	            dtoList = setData(results);
	        }

	        Workbook workbook = new XSSFWorkbook();
	        Sheet sheet = workbook.createSheet("Sheet1");
	        int currentRow = 0;

	        List<List<Object>> rows = new ArrayList<>();

	        for (DecokePlanningDTO dto : dtoList) {
	            List<Object> list = new ArrayList<>();
	            list.add(dto.getMonthName());
	            list.add(dto.getFiveF());
	            list.add(dto.getFourF());
	            list.add(dto.getFourFD());
	            list.add(dto.getCoilReplacement());
	            list.add(dto.getShutdown());
	            list.add(dto.getSlowdown());
	            list.add(dto.getSad());
	            list.add(dto.getBbu());
	            list.add(dto.getBbd());
	            list.add(dto.getDemoSAD());
	            list.add(dto.getDemoSD());
	            list.add(dto.getDemoBBU());
	            list.add(dto.getDemoHSS());
	            list.add(dto.getMnt());
	            list.add(dto.getTotal());
	            list.add(dto.getNumberOfDays());
	            list.add(dto.getTotalSAD());
	            list.add(dto.getRemarks());
	            list.add(dto.getId());
	            if (isAfterSave) {
	                list.add(dto.getSaveStatus());
	                list.add(dto.getErrDescription());
	            }
	            rows.add(list);
	        }

	        List<String> innerHeaders = new ArrayList<>(Arrays.asList(
	            "Month", "5F", "4f", "4F With Demo", "IBR/Coil Replacement", "Shutdown(TA)",
	            "Slowdown", "SAD", "BBU", "BBD", "Demo SAD", "Demo SD", "Demo BBU/BBD",
	            "Demo HHS", "MNT", "Total", "No of Days", "No of SADs", "Remarks", "Id"
	        ));
	        if (isAfterSave) {
	            innerHeaders.add("Status");
	            innerHeaders.add("Error Description");
	        }

	        // Header style
	        CellStyle headerStyle = createBoldBorderedStyle(workbook);

	        // Gray style for the total row
	        CellStyle grayStyle = workbook.createCellStyle();
	        grayStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	        grayStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	        grayStyle.setBorderTop(BorderStyle.THIN);
	        grayStyle.setBorderBottom(BorderStyle.THIN);
	        grayStyle.setBorderLeft(BorderStyle.THIN);
	        grayStyle.setBorderRight(BorderStyle.THIN);

	        // Create header row
	        Row headerRow = sheet.createRow(currentRow++);
	        for (int col = 0; col < innerHeaders.size(); col++) {
	            Cell cell = headerRow.createCell(col);
	            cell.setCellValue(innerHeaders.get(col));
	            cell.setCellStyle(headerStyle);
	        }

	        // Create data rows
	        int startDataRow = currentRow;
	        for (List<Object> rowData : rows) {
	            Row row = sheet.createRow(currentRow++);
	            for (int col = 0; col < rowData.size(); col++) {
	                Cell cell = row.createCell(col);
	                Object value = rowData.get(col);
	                if (value instanceof Number) {
	                    cell.setCellValue(((Number) value).doubleValue());
	                } else if (value instanceof Boolean) {
	                    cell.setCellValue((Boolean) value);
	                } else if (value != null) {
	                    cell.setCellValue(value.toString());
	                } else {
	                    cell.setCellValue("");
	                }
	            }
	        }

	        // Apply gray background to the last (total) row
	        int totalRowIndex = sheet.getLastRowNum();
	        Row totalRow = sheet.getRow(totalRowIndex);
	        if (totalRow != null) {
	            for (int col = 0; col < innerHeaders.size(); col++) {
	                Cell cell = totalRow.getCell(col);
	                if (cell == null) cell = totalRow.createCell(col);
	                cell.setCellStyle(grayStyle);
	            }
	        }

	        sheet.setColumnHidden(19, true);

	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        workbook.write(outputStream);
	        workbook.close();
	        return outputStream.toByteArray();

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	public List<DecokePlanningDTO> setData(List<Object[]> results) {
	    List<DecokePlanningDTO> dtoList = new ArrayList<>();
	    if (results == null) {
	        return dtoList;
	    }

	    // Initialize sum variables
	    double sumCoilReplacement = 0;
	    double sumMnt = 0;
	    double sumShutdown = 0;
	    double sumSlowdown = 0;
	    double sumSAD = 0;
	    double sumBBD = 0;
	    double sumBBU = 0;
	    double sumDemoSAD = 0;
	    double sumDemoSD = 0;
	    double sumDemoBBU = 0;
	    double sumFourFD = 0;
	    double sumFourF = 0;
	    double sumFiveF = 0;
	    double sumTotal = 0;
	    double sumTotalSAD = 0;
	    double sumNumberOfDays = 0;
	    double sumDemoHHS=0;

	    for (Object[] row : results) {
	        DecokePlanningDTO dto = new DecokePlanningDTO();

	        // String fields
	        dto.setId(row[0] != null ? UUID.fromString(row[0].toString()) : null);
	        dto.setMonthName(row[1] != null ? row[1].toString() : null);
	        dto.setAopYear(row[18] != null ? row[18].toString() : null);
	        dto.setPlantId(row[19] != null ? UUID.fromString(row[19].toString()) : null);
	        dto.setRemarks(row[20] != null ? row[20].toString() : "");

	        // Numeric fields
	        dto.setCoilReplacement(row[2] != null ? Double.parseDouble(row[2].toString()) : 0.0);
	        dto.setMnt(row[3] != null ? Double.parseDouble(row[3].toString()) : 0.0);
	        dto.setShutdown(row[4] != null ? Double.parseDouble(row[4].toString()) : 0.0);
	        dto.setSlowdown(row[5] != null ? Double.parseDouble(row[5].toString()) : 0.0);
	        dto.setSad(row[6] != null ? Double.parseDouble(row[6].toString()) : 0.0);
	        dto.setBbd(row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0);
	        dto.setBbu(row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0);
	        dto.setDemoHSS(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0);
	        dto.setDemoBBU(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0);
	        dto.setDemoSAD(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0);
	        dto.setDemoSD(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0);
	        dto.setFourFD(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0);
	        dto.setFourF(row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0);
	        dto.setFiveF(row[15] != null ? Double.parseDouble(row[15].toString()) : 0.0);
	        dto.setTotal(row[16] != null ? Double.parseDouble(row[16].toString()) : 0.0);
	        dto.setFourFHours(row[17] != null ? Double.parseDouble(row[17].toString()) : 0.0);
	        dto.setTotalSAD(row[21] != null ? Double.parseDouble(row[21].toString()) : 0.0);
	        dto.setNumberOfDays(row[22] != null ? Double.parseDouble(row[22].toString()) : 0.0);

	        // Accumulate totals
	        sumCoilReplacement += dto.getCoilReplacement();
	        sumMnt += dto.getMnt();
	        sumShutdown += dto.getShutdown();
	        sumSlowdown += dto.getSlowdown();
	        sumSAD += dto.getSad();
	        sumBBD += dto.getBbd();
	        sumBBU += dto.getBbu();
	        sumDemoSAD += dto.getDemoSAD();
	        sumDemoSD += dto.getDemoSD();
	        sumDemoBBU += dto.getDemoBBU();
	        sumFourFD += dto.getFourFD();
	        sumFourF += dto.getFourF();
	        sumFiveF += dto.getFiveF();
	        sumTotal += dto.getTotal();
	        sumNumberOfDays += dto.getNumberOfDays();
	        sumTotalSAD += dto.getTotalSAD();
	        sumDemoHHS+=dto.getDemoHSS();

	        dtoList.add(dto);
	    }

	    // Add summary (Total) DTO
	    DecokePlanningDTO totalDto = new DecokePlanningDTO();
	    totalDto.setMonthName("Total");
	    totalDto.setCoilReplacement(sumCoilReplacement);
	    totalDto.setMnt(sumMnt);
	    totalDto.setShutdown(sumShutdown);
	    totalDto.setSlowdown(sumSlowdown);
	    totalDto.setSad(sumSAD);
	    totalDto.setBbd(sumBBD);
	    totalDto.setBbu(sumBBU);
	    totalDto.setDemoSAD(sumDemoSAD);
	    totalDto.setDemoSD(sumDemoSD);
	    totalDto.setDemoBBU(sumDemoBBU);
	    totalDto.setFourFD(sumFourFD);
	    totalDto.setFourF(sumFourF);
	    totalDto.setFiveF(sumFiveF);
	    totalDto.setTotal(sumTotal);
	    totalDto.setNumberOfDays(sumNumberOfDays);
	    totalDto.setTotalSAD(sumTotalSAD);
	    totalDto.setDemoHSS(sumDemoHHS);
	    totalDto.setRemarks("Total");

	    dtoList.add(totalDto);

	    return dtoList;
	}

	public List<Object[]> getData(String plantId, String aopYear, String viewName) {
		try {

			// 2. Construct SQL with dynamic view name
			String sql = "SELECT * FROM " + viewName +
					" WHERE PlantId = :plantId AND AOPYear = :aopYear " +
					"ORDER BY CASE MonthName " +
					"    WHEN 'April' THEN 1 " +
					"    WHEN 'May' THEN 2 " +
					"    WHEN 'June' THEN 3 " +
					"    WHEN 'July' THEN 4 " +
					"    WHEN 'August' THEN 5 " +
					"    WHEN 'September' THEN 6 " +
					"    WHEN 'October' THEN 7 " +
					"    WHEN 'November' THEN 8 " +
					"    WHEN 'December' THEN 9 " +
					"    WHEN 'January' THEN 10 " +
					"    WHEN 'February' THEN 11 " +
					"    WHEN 'March' THEN 12 " +
					"    ELSE 13 " +
					"END";

			// 3. Create and parameterize the native query
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);

			// 4. Execute
			return query.getResultList();

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid argument: " + e.getMessage(), e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data from view " + viewName, ex);
		}
	}

	@Override
	public AOPMessageVM updateMaintenanceDataForCracker(String plantId, String year,
			List<DecokePlanningDTO> decokePlanningDTOList) {
		List<DecokePlanningDTO> failedList=new ArrayList<>();
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<DecokeMaintenance> decokeMaintenanceList = new ArrayList<>();
		try {
			for (DecokePlanningDTO decokePlanningDTO : decokePlanningDTOList) {
				if (decokePlanningDTO.getSaveStatus() != null
						&& decokePlanningDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(decokePlanningDTO);
					continue;
				}
				Optional<DecokeMaintenance> decokePlanningop = decokeMaintenanceRepository
						.findById(decokePlanningDTO.getId());
				if (decokePlanningop.isPresent()) {
					DecokeMaintenance decokeMaintenance = decokePlanningop.get();
					decokeMaintenance.setMnt(decokePlanningDTO.getMnt());
					decokeMaintenance.setRemarks(decokePlanningDTO.getRemarks());
					decokeMaintenance.setBbd(decokePlanningDTO.getBbd());
					decokeMaintenance.setBbu(decokePlanningDTO.getBbu());
					decokeMaintenance.setDemoBbu(decokePlanningDTO.getDemoBBU());
					decokeMaintenance.setDemoHss(decokePlanningDTO.getDemoHSS());
					decokeMaintenance.setDemoSad(decokePlanningDTO.getDemoSAD());
					decokeMaintenance.setDemoSd(decokePlanningDTO.getDemoSD());
					decokeMaintenance.setFiveF(decokePlanningDTO.getFiveF());
					decokeMaintenance.setFourF(decokePlanningDTO.getFourF());
					decokeMaintenance.setFourFd(decokePlanningDTO.getFourFD());
					decokeMaintenance.setFourFHours(decokePlanningDTO.getFourFHours());
					decokeMaintenance.setIbr(decokePlanningDTO.getIbr());
					decokeMaintenance.setShoutdown(decokePlanningDTO.getShutdown());
					decokeMaintenance.setSad(decokePlanningDTO.getSad());
					decokeMaintenance.setSlowdown(decokePlanningDTO.getSlowdown());
					decokeMaintenance.setTotal(decokePlanningDTO.getTotal());
					decokeMaintenanceList.add(decokeMaintenanceRepository.save(decokeMaintenance));
				}
			}
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to update data", ex);
		}
		List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("maintenance-details");
		for (ScreenMapping screenMapping : screenMappingList) {
			AopCalculation aopCalculation = new AopCalculation();
			aopCalculation.setAopYear(year);
			aopCalculation.setIsChanged(true);
			aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
			aopCalculation.setPlantId(UUID.fromString(plantId));
			aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
			aopCalculationRepository.save(aopCalculation);
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Data updated successfully");
		aopMessageVM.setData(failedList);
		return aopMessageVM;

	}
	
	@Override
	public AOPMessageVM maintenanceImport(String year,UUID plantId,MultipartFile file) {
		// TODO Auto-generated method stub
		try {
			List<DecokePlanningDTO> data = readMaintenance(file.getInputStream(), plantId, year);
			 AOPMessageVM aopMessageVM = updateMaintenanceDataForCracker( plantId.toString(),  year, data);
			 List<DecokePlanningDTO> failedList = (List<DecokePlanningDTO>) aopMessageVM.getData();

			if (failedList != null && failedList.size() > 0) {
				byte[] fileByteArray = maintenanceExport(year, plantId.toString(), true, failedList);
				String base64File = Base64.getEncoder().encodeToString(fileByteArray);
				aopMessageVM.setData(base64File);
				aopMessageVM.setCode(400);
				aopMessageVM.setMessage("Partial data has been saved");
			} else {
				// aopMessageVM.setData();
				aopMessageVM.setCode(200);
				aopMessageVM.setMessage("All data has been saved");
			}

			return aopMessageVM;
			// return ResponseEntity.ok(data);
		} catch (Exception e) {
			e.printStackTrace();
			// return ResponseEntity.internalServerError().build();
		}
		return null;
	}
	
	public List<DecokePlanningDTO> readMaintenance(InputStream inputStream, UUID plantFKId, String year) {
		List<DecokePlanningDTO> dtoList = new ArrayList<>();

		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();

			if (rowIterator.hasNext())
				rowIterator.next(); // Skip header
			int totalRows = sheet.getLastRowNum(); 
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				if (row.getRowNum() == totalRows) {
			        continue;
			    }
				DecokePlanningDTO dto = new DecokePlanningDTO();
				try {
					dto.setMonthName(getStringCellValue(row.getCell(0), dto));
					dto.setId(UUID.fromString(getStringCellValue(row.getCell(19), dto)));
					dto.setFiveF(getNumericCellValue(row.getCell(1), dto));
					dto.setFourF(getNumericCellValue(row.getCell(2), dto));
					dto.setFourFD(getNumericCellValue(row.getCell(3), dto));
					dto.setCoilReplacement(getNumericCellValue(row.getCell(4), dto));
					dto.setShutdown(getNumericCellValue(row.getCell(5), dto));
					dto.setSlowdown(getNumericCellValue(row.getCell(6), dto));
					dto.setSad(getNumericCellValue(row.getCell(7), dto));
					dto.setBbu(getNumericCellValue(row.getCell(8), dto));
					dto.setBbd(getNumericCellValue(row.getCell(9), dto));
					dto.setDemoSAD(getNumericCellValue(row.getCell(10), dto));
					dto.setDemoSD(getNumericCellValue(row.getCell(11), dto));
					dto.setDemoBBU(getNumericCellValue(row.getCell(12), dto));
					dto.setDemoHSS(getNumericCellValue(row.getCell(13), dto));
					dto.setMnt(getNumericCellValue(row.getCell(14), dto));
					dto.setTotal(getNumericCellValue(row.getCell(15), dto));
					dto.setNumberOfDays(getNumericCellValue(row.getCell(16), dto));
					dto.setTotalSAD(getNumericCellValue(row.getCell(17), dto));
					dto.setRemarks(getStringCellValue(row.getCell(18), dto));
					
					
				} catch (Exception e) {
					e.printStackTrace();
					dto.setErrDescription(e.getMessage());
					dto.setSaveStatus("Failed");
				}
				dtoList.add(dto);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return dtoList;
	}

	@Override
	public AOPMessageVM getBudgetMaintenance(String plantId, String year,String budgetCategory) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<BudgetMaintenance> budgetMaintenanceList=null;
		
		List<Object[]> obj=findByYearAndPlantFkId( year, UUID.fromString(plantId),"vwBudgetMaintenance",budgetCategory);
		List<BudgetMaintenanceDto> budgetMaintenanceDtoList = new ArrayList<BudgetMaintenanceDto>();
		try {
			
			for (Object[] row : obj) {
			    BudgetMaintenanceDto dto = new BudgetMaintenanceDto();

			    int i = 0;
			    dto.setId(row[i++] != null ? UUID.fromString(row[i - 1].toString()) : null);
			    dto.setPlantId(row[i++] != null ? UUID.fromString(row[i - 1].toString()) : null);
			    dto.setPlantName((String) row[i++]);
			    dto.setCostName((String) row[i++]);
			    dto.setBudgetType((String) row[i++]);
			    dto.setBudgetCategory((String) row[i++]);
			    dto.setApr(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setMay(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setJun(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setJul(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setAug(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setSep(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setOct(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setNov(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setDec(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setJan(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setFeb(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setMar(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setRemark((String) row[i++]);
			    dto.setAopYear(row[i++] != null ? row[i - 1].toString() : null);
			    dto.setIsEditable(row[i++] != null ? Boolean.valueOf(row[i - 1].toString()) : null);
			    dto.setUpdatedBy((String) row[i++]);
			    dto.setModifiedOn((Date) row[i++]);
			    dto.setSequence(row[i++] != null ? ((Number) row[i - 1]).intValue() : null);
			    dto.setPercentChange(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setSymbol(row[i++] != null ? row[i - 1].toString() : "");
			   
			    budgetMaintenanceDtoList.add(dto);
			}
		}catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to get data", ex);
		}
		
		aopMessageVM.setCode(200);
		aopMessageVM.setData(budgetMaintenanceDtoList);
		aopMessageVM.setMessage("Data fetched successfully");
		return aopMessageVM;
	}
	
	public List<Object[]> findByYearAndPlantFkId(String year, UUID plantFkId, String viewName,String budgetCategory) {
		try {
			String sql = "SELECT Id, PlantId, PlantName, CostName, BudgetType, BudgetCategory, "
			           + "Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, "
			           + "Remark, AOPYear, IsEditable, UpdatedBy, ModifiedOn, Sequence, "
			           + "PercentChange, Symbol "
			           + "FROM " + viewName + " "
			           + "WHERE (AOPYear = :year AND AOPYear IS NOT NULL) "
			           + "AND PlantId = :plantFkId AND BudgetCategory = :budgetCategory order by budgetCategory  ASC, budgetType ASC , Sequence ASC ";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("year", year);
			query.setParameter("plantFkId", plantFkId);
			query.setParameter("budgetCategory", budgetCategory);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	
	public byte[] createExcel(String year, String plantId, boolean isAfterSave,
	        Map<String, List<BudgetMaintenanceDto>> mapForExcel) {
	    try {
	    	Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
	        String structureJson = getJson();
	        ObjectMapper mapper = new ObjectMapper();
	        Map<String, List<List<Object>>> data = new HashMap<>();
	        Map<String, Object> structure = mapper.readValue(structureJson, Map.class);
	        Map<String, List<BudgetMaintenanceDto>> budgetMaintenanceListMap = new HashMap<>();
	        if (!isAfterSave) {
	            // Fetch data for ConsumptionBudget
	            AOPMessageVM consumptionVm = getBudgetMaintenance(plantId, year, "ConsumptionBudget");
	            List<BudgetMaintenanceDto> consumptionData = (List<BudgetMaintenanceDto>) consumptionVm.getData();
	            if (consumptionData != null) {
	                budgetMaintenanceListMap.put("ConsumptionBudget", consumptionData);
	            }
	            AOPMessageVM procurementVm = getBudgetMaintenance(plantId, year, "ProcurementBudget");
	            List<BudgetMaintenanceDto> procurementData = (List<BudgetMaintenanceDto>) procurementVm.getData();
	            if (procurementData != null) {
	                budgetMaintenanceListMap.put("ProcurmentBudget", procurementData);
	            }
	        }
	        
	        Map<String, Object> sheetData = (Map<String, Object>) structure.get("BudgetMaintenance");
	        List<Map<String, String>> fields = (List<Map<String, String>>) sheetData.get("metadataFields"); 
	        
	        Map<String, Object> metadataValues = new HashMap<>();
	        for (Map<String, String> field : fields) {
	            String key = field.get("key");
	            switch (key) {
	                case "year":{
	                	metadataValues.put(key, year);
	                    break;
	                }  
	                case "plant":{
	                	metadataValues.put(key, plant.getDisplayName());
	                    break;
	                }
	                 case "site":{
	                	 metadataValues.put(key, site.getDisplayName());
		                 break;
	                 }	
	                 case "date":{
	                	 metadataValues.put(key, new Date());
		                 break;
	                 }	
	            }
	        }
	        List<Map<String, Object>> tables = (List<Map<String, Object>>) sheetData.get("tables");

	        for (Map<String, Object> table : tables) {
	            String title = (String) table.get("title");
	            String tableId = (String) table.get("tableId");
	            List<String> headers = (List<String>) table.get("headers");
	            Integer startingIndexofMonths = (Integer) table.get("startingIndexOfMonths");
	            List<List<String>> headersOuterTitles = (List<List<String>>) table.get("headersTitles");

	            headersOuterTitles.get(0).addAll(startingIndexofMonths, excelUtilityService.getAcademicYearMonths(year));

	            List<List<Object>> dataList = new ArrayList<>();
	            List<BudgetMaintenanceDto> sourceData = null;

	            if (isAfterSave) {
	                if (mapForExcel.containsKey(tableId)) {
	                    sourceData = mapForExcel.get(tableId);
	                    // Add saveStatus and errDescription headers for the after-save scenario
	                    headers.add("saveStatus");
	                    headers.add("errDescription");
	                    headersOuterTitles.get(0).add("SaveStatus");
	                    headersOuterTitles.get(0).add("ErrDescription");
	                }
	            } else {
	                sourceData = budgetMaintenanceListMap.get(tableId);
	            }

	            // If no data is available for the current table, continue to the next one
	            if (sourceData == null || sourceData.isEmpty()) {
	                table.put("hideTable", true);
	                continue;
	            }

	            // Populate the data rows using reflection
	            for (BudgetMaintenanceDto dto : sourceData) {
	            	if(dto.getCostName().equalsIgnoreCase("Total Cost")) {
	            		continue;
	            	}
	                List<Object> row = new ArrayList<>();
	                for (String fieldName : headers) {
	                    try {
	                        String methodName = "get" + capitalize(fieldName);
	                        Method method = dto.getClass().getMethod(methodName);
	                        Object value = method.invoke(dto);
	                        row.add(value);
	                    } catch (NoSuchMethodException e) {
	                        // Handle cases where a method for a header doesn't exist, e.g., for "saveStatus" or "errDescription"
	                        row.add(null);
	                    }
	                }
	                row.add(tableId);
	                dataList.add(row);
	            }
	            
	            data.put(tableId, dataList);
	        }
	        
	        return excelUtilityService.generateFlexibleExcelForBudgetMaintenance(structure, data, metadataValues,getBasisSummary(plantId,year),getRemarksSummary(plantId,year));

	    } catch (Exception e) {
	        e.printStackTrace();
	        // You might want to log the exception more professionally here
	        return null;
	    }
	}
	
	public String getBasisSummary(String plantId, String year) {
		AOPMessageVM designBasis= aopMaintenanceDesignBasisService.getMaintenanceDesignBasis(plantId,year);
        List<AOPMaintenanceDesignRemarksDTO> aopMaintenanceDesignRemarksDTOs =(List<AOPMaintenanceDesignRemarksDTO>) designBasis.getData();
        AOPMaintenanceDesignRemarksDTO aopMaintenanceDesignRemarksDTO = aopMaintenanceDesignRemarksDTOs.get(0);
        return aopMaintenanceDesignRemarksDTO.getSummary();
	}
	
	public String getRemarksSummary(String plantId, String year) {
		AOPMessageVM designBasis= aopMaintenanceDesignRemarksService.getMaintenanceDesignRemarks(plantId,year);
        List<AOPMaintenanceDesignRemarksDTO> aopMaintenanceDesignRemarksDTOs =(List<AOPMaintenanceDesignRemarksDTO>) designBasis.getData();
        AOPMaintenanceDesignRemarksDTO aopMaintenanceDesignRemarksDTO = aopMaintenanceDesignRemarksDTOs.get(0);
        return aopMaintenanceDesignRemarksDTO.getSummary();
	}

	
	private static String capitalize(String str) {
		if (str == null || str.isEmpty())
			return str;
		return str.substring(0, 1).toUpperCase() + str.substring(1);
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
	public AOPMessageVM updateBudgetMaintenance(List<BudgetMaintenanceDto> budgetMaintenanceDtos) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<BudgetMaintenanceDto> failedList= new ArrayList<BudgetMaintenanceDto>();
		List<BudgetMaintenance> budgetMaintenanceList=new ArrayList<BudgetMaintenance>();
		try {
			for(BudgetMaintenanceDto budgetMaintenanceDto:budgetMaintenanceDtos) {
				if (budgetMaintenanceDto.getSaveStatus() != null
						&& budgetMaintenanceDto.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(budgetMaintenanceDto);
					continue;
				}
				BudgetMaintenance budgetMaintenance=null;
				if(budgetMaintenanceDto.getId()==null) {
					budgetMaintenance=new BudgetMaintenance();
					budgetMaintenanceList.add(saveData(budgetMaintenance,budgetMaintenanceDto));
				}else {
					Optional<BudgetMaintenance> budgetMaintenanceOpt=budgetMaintenanceRepository.findById(budgetMaintenanceDto.getId());
					if(budgetMaintenanceOpt.isPresent()) {
						budgetMaintenance=budgetMaintenanceOpt.get();
						budgetMaintenanceDto.setPlantId(budgetMaintenance.getPlantId());
						budgetMaintenanceDto.setBudgetCategory(budgetMaintenance.getBudgetCategory());
						budgetMaintenanceDto.setBudgetType(budgetMaintenance.getBudgetType());
						budgetMaintenanceDto.setIsEditable(budgetMaintenance.getIsEditable());
						if(budgetMaintenance.getIsEditable()) {
							budgetMaintenanceList.add(saveData(budgetMaintenance,budgetMaintenanceDto));
						}
					}else {
						budgetMaintenanceDto.setSaveStatus("Failed");
						budgetMaintenanceDto.setErrDescription("No record found with given id");
						failedList.add(budgetMaintenanceDto);
					}
				}
					
			}
		}catch(Exception e) {
			throw new RuntimeException("Failed to update data", e);
		}
		Map<String,Object> map=new HashMap<>();
		map.put("Success", budgetMaintenanceList);
		map.put("Failed", failedList);
		aopMessageVM.setCode(200);
		aopMessageVM.setData(map);
		aopMessageVM.setMessage("Data updated successfully");
		// TODO Auto-generated method stub
		return aopMessageVM;
	}
	
	public BudgetMaintenance saveData(BudgetMaintenance budgetMaintenance,BudgetMaintenanceDto budgetMaintenanceDto) {
		budgetMaintenance.setApr(budgetMaintenanceDto.getApr());
		budgetMaintenance.setMay(budgetMaintenanceDto.getMay());
		budgetMaintenance.setJun(budgetMaintenanceDto.getJun());
		budgetMaintenance.setJul(budgetMaintenanceDto.getJul());
		budgetMaintenance.setAug(budgetMaintenanceDto.getAug());
		budgetMaintenance.setSep(budgetMaintenanceDto.getSep());
		budgetMaintenance.setOct(budgetMaintenanceDto.getOct());
		budgetMaintenance.setNov(budgetMaintenanceDto.getNov());
		budgetMaintenance.setDec(budgetMaintenanceDto.getDec());
		budgetMaintenance.setJan(budgetMaintenanceDto.getJan());
		budgetMaintenance.setFeb(budgetMaintenanceDto.getFeb());
		budgetMaintenance.setMar(budgetMaintenanceDto.getMar());
		budgetMaintenance.setBudgetCategory(budgetMaintenanceDto.getBudgetCategory());
		budgetMaintenance.setBudgetType(budgetMaintenanceDto.getBudgetType());
		budgetMaintenance.setCostName(budgetMaintenanceDto.getCostName());
		budgetMaintenance.setPlantId(budgetMaintenanceDto.getPlantId());
		budgetMaintenance.setPlantName(budgetMaintenanceDto.getPlantName());
		budgetMaintenance.setAopYear(budgetMaintenanceDto.getAopYear());
		budgetMaintenance.setRemark(budgetMaintenanceDto.getRemark());
		budgetMaintenance.setModifiedOn(new Date());
		budgetMaintenance.setUpdatedBy(Utility.getUserName());
		budgetMaintenance.setSymbol(budgetMaintenanceDto.getSymbol());
		budgetMaintenance.setPercentChange(budgetMaintenanceDto.getPercentChange());
		return budgetMaintenanceRepository.save(budgetMaintenance);
	}
	
	@Override
	public AOPMessageVM importExcel(String year, String plantFKId, String budgetCategory, MultipartFile file) {
		// TODO Auto-generated method stub
		if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
			throw new IllegalArgumentException("Invalid or empty Excel file.");
		}

		try {
			
			Map<String, List<BudgetMaintenanceDto>> map = readBudgetMaintenanceExcel(file.getInputStream(), year,plantFKId);
			
			Map<String, List<BudgetMaintenanceDto>> mapForExcel = new HashMap<>();
			List<BudgetMaintenanceDto> failedRecords = new ArrayList<>();
			for (String key : map.keySet()) {
			    AOPMessageVM vm = updateBudgetMaintenance(map.get(key));
			    Object dataObj = vm.getData();
			    if (dataObj instanceof Map) {
			        @SuppressWarnings("unchecked")
			        Map<String, Object> dataMap = (Map<String, Object>) dataObj;
			        Object failedObj = dataMap.get("Failed");
			        if (failedObj instanceof List) {
			            @SuppressWarnings("unchecked")
			            List<BudgetMaintenanceDto> failedList = (List<BudgetMaintenanceDto>) failedObj;
			            failedRecords.addAll(failedList);
			            mapForExcel.put(key, failedList);
			        } else {
			            mapForExcel.put(key, Collections.emptyList());
			        }
			    } else {
			        mapForExcel.put(key, Collections.emptyList());
			    }
			}

			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (failedRecords != null && failedRecords.size() > 0) {
				byte[] fileByteArray = createExcel(year, plantFKId, true, mapForExcel);
				String base64File = Base64.getEncoder().encodeToString(fileByteArray);
				aopMessageVM.setData(base64File);
				aopMessageVM.setCode(400);
				aopMessageVM.setMessage("Partial data has been saved");
			} else {
				aopMessageVM.setCode(200);
				aopMessageVM.setMessage("All data has been saved");
			}

			return aopMessageVM;
			// return ResponseEntity.ok(data);
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	public Map<String, List<BudgetMaintenanceDto>> readBudgetMaintenanceExcel(InputStream inputStream, String year,String plantId) {

		Map<String, List<BudgetMaintenanceDto>> map = new HashMap<>();
		String basisSummary = null;
	    String remarkSummary = null;
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {

				Sheet sheet = workbook.getSheetAt(0);
				Iterator<Row> rowIterator = sheet.iterator();
				int summaryRowStart = -1;
		        while (rowIterator.hasNext()) {
		            Row row = rowIterator.next();
		            Cell basisLabelCell = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

		            if (basisLabelCell != null && basisLabelCell.getCellType() == CellType.STRING) {
		                String cellValue = basisLabelCell.getStringCellValue().trim();
		                if ("Justification:".equalsIgnoreCase(cellValue)) {
		                    summaryRowStart = row.getRowNum();
		                    break; 
		                }
		            }
		        }
		        if (summaryRowStart != -1) {
		            int contentRow = summaryRowStart + 1;
		            Row contentDataRow = sheet.getRow(contentRow); 

		            if (contentDataRow != null) {
		                Cell basisCell = contentDataRow.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
		                if (basisCell != null) {
		                    basisSummary = getCellStringValue(basisCell); 
		                }
		                Cell remarkCell = contentDataRow.getCell(8, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
		                if (remarkCell != null) {
		                    remarkSummary = getCellStringValue(remarkCell); 
		                }
		            }
		        }
		        aopMaintenanceDesignRemarksService.updateMaintenanceDesignRemarks(plantId,year,remarkSummary);
		        aopMaintenanceDesignBasisService.updateMaintenanceDesignBasis(plantId,year,basisSummary);
		        		
				List<BudgetMaintenanceDto> budgetMaintenanceDto = new ArrayList<BudgetMaintenanceDto>();
				if (rowIterator.hasNext())
					rowIterator.next();

				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					Cell tableIdCell = row.getCell(19, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                	if (tableIdCell == null || tableIdCell.getCellType() != CellType.STRING) {
                    	continue;
                	}

                	BudgetMaintenanceDto dto = new BudgetMaintenanceDto();

					try {
						String cost=getStringCellValue(row.getCell(2), dto);
						if(cost!=null && cost.trim().equalsIgnoreCase("Total Cost")) {
							continue;
						}
						dto.setBudgetType(getStringCellValue(row.getCell(0), dto));
						dto.setPlantName(getStringCellValue(row.getCell(1), dto));
						dto.setCostName(getStringCellValue(row.getCell(2), dto));
						dto.setPercentChange(getNumericCellValue(row.getCell(3), dto));
						dto.setAopYear(year);
						dto.setApr(getNumericCellValue(row.getCell(4), dto));
						dto.setMay(getNumericCellValue(row.getCell(5), dto));
						dto.setJun(getNumericCellValue(row.getCell(6), dto));
						dto.setJul(getNumericCellValue(row.getCell(7), dto));
						dto.setAug(getNumericCellValue(row.getCell(8), dto));
						dto.setSep(getNumericCellValue(row.getCell(9), dto));
						dto.setOct(getNumericCellValue(row.getCell(10), dto));
						dto.setNov(getNumericCellValue(row.getCell(11), dto));
						dto.setDec(getNumericCellValue(row.getCell(12), dto));
						dto.setJan(getNumericCellValue(row.getCell(13), dto));
						dto.setFeb(getNumericCellValue(row.getCell(14), dto));
						dto.setMar(getNumericCellValue(row.getCell(15), dto));
						dto.setRemark(getStringCellValue(row.getCell(16), dto));
						String id=getStringCellValue(row.getCell(17), dto);
						if(id!=null) {
							dto.setId(UUID.fromString(id));
						}
						
						dto.setTableId(getStringCellValue(row.getCell(19), dto));

					} catch (Exception e) {
						e.printStackTrace();
						dto.setErrDescription(e.getMessage());
						dto.setSaveStatus("Failed");
					}
					map.putIfAbsent(dto.getTableId(), new ArrayList<>());

					map.get(dto.getTableId()).add(dto);
				}

		} catch (Exception e) {
			throw new RuntimeException("Failed to read Data", e);
		}

		return map;
	}
	
	
	private static String getStringCellValue(Cell cell, DecokePlanningDTO dto) {
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

	private static Double getNumericCellValue(Cell cell, DecokePlanningDTO dto) {
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

	public static Boolean getBooleanCellValue(Cell cell) {
		if (cell == null)
			return null;

		CellType type = cell.getCellType();
		if (type == CellType.FORMULA) {
			type = cell.getCachedFormulaResultType();
		}

		switch (type) {
			case BOOLEAN:
				return cell.getBooleanCellValue();
			case STRING:
				String text = cell.getStringCellValue().trim().toLowerCase();
				if ("true".equals(text))
					return true;
				if ("false".equals(text))
					return false;
				return null;
			case NUMERIC:
				double num = cell.getNumericCellValue();
				if (num == 1.0)
					return true;
				if (num == 0.0)
					return false;
				return null;
			case BLANK:
			case _NONE:
			default:
				return null;
		}
	}

	private static String getStringCellValue(Cell cell, BudgetMaintenanceDto dto) {
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
	
	private static String getCellStringValue(Cell cell) {
		try {
			if (cell == null)
				return null;
			cell.setCellType(CellType.STRING);
			return cell.getStringCellValue().trim();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return null;

	}

	private static Double getNumericCellValue(Cell cell, BudgetMaintenanceDto dto) {
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



	@Override
	public AOPMessageVM getMacroData(Double value, String year,String plantId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		
		Map<String,Object> map=new HashMap<String,Object>();
		try {
			Double obj=getData( value,  year, plantId);
				map.put("macroValue",obj);
		}catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setData(map);
		aopMessageVM.setMessage(plantId);
		// TODO Auto-generated method stub
		return aopMessageVM;
	}
	
	public Double getData(Double value, String aopYear, String plantId) {
	    try {
	    	String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
	        
	        String storedProcedure = "MacroTest";
	        if (!"MEG".equalsIgnoreCase(verticalName)) {
	            storedProcedure = verticalName + "_" + site.getName() + "_MacroTest";
	        }
	        
	        String sql = "EXEC " + storedProcedure
	                     + " @value = :value, @aopYear = :aopYear";
	        
	        Query query = entityManager.createNativeQuery(sql);
	        query.setParameter("value", value);
	        query.setParameter("aopYear", aopYear);
	        
	        Object singleResult = query.getSingleResult();  // expect exactly one result
	        
	        if (singleResult == null) {
	            return null;
	        }
	        
	        // Depending on what your database/stored proc returns, it may be a BigDecimal, Double, Number etc.
	        if (singleResult instanceof Number) {
	            return ((Number) singleResult).doubleValue();
	        } else {
	            // Unexpected type; try converting
	            return Double.parseDouble(singleResult.toString());
	        }
	        
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch data", ex);
	    }
	}

	String getJson() {
	    return """
	        {
	          "BudgetMaintenance": {
	            "columnCount": 20,
	            "metadataFields": [
	              {
	                "key": "year",
	                "title": "AOP Year"
	              },
	              {
	                "key": "plant",
	                "title": "Plant"
	              },
	              {
	                "key": "site",
	                "title": "Site"
	              },
	              {
	                "key": "date",
	                "title": "Date"
	              }
	            ],
	            "tables": [
	              {
	                "startRow": 0,
	                "headers": [
	                  "budgetType",
	                  "plantName",
	                  "costName",
	                  "percentChange",
	                  "apr",
	                  "may",
	                  "jun",
	                  "jul",
	                  "aug",
	                  "sep",
	                  "oct",
	                  "nov",
	                  "dec",
	                  "jan",
	                  "feb",
	                  "mar",
	                  "remark",
	                  "id",
	                  "isEditable"
	                ],
	                "startingIndexOfMonths": 4,
	                "hideTable": false,
	                "textBeforeTitle": "",
	                "title": "Consumption Budget",
	                "tableId": "ConsumptionBudget",
	                "dataInput": "Consumption Budget",
	                "isColumnMergeRequired": false,
	                "isRowMergeRequired": false,
	                "headersTitles": [
	                  [
	                    "Type",
	                    "Plant",
	                    "Cost",
	                    "% Change (+/-)",
	                    "Remarks",
	                    "Id",
	                    "Is Editable"
	                  ]
	                ],
	                "rows": [],
	                "hiddenColumns": [17, 18, 19],
	                "styles": {
	                  "boldColumns": [0],
	                  "borders": true
	                },
	                "autoMerge": {
	                  "columns": [],
	                  "rows": []
	                }
	              },
	              {
	                "startRow": 0,
	                "headers": [
	                  "budgetType",
	                  "plantName",
	                  "costName",
	                  "percentChange",
	                  "apr",
	                  "may",
	                  "jun",
	                  "jul",
	                  "aug",
	                  "sep",
	                  "oct",
	                  "nov",
	                  "dec",
	                  "jan",
	                  "feb",
	                  "mar",
	                  "remark",
	                  "id",
	                  "isEditable"
	                ],
	                "startingIndexOfMonths": 4,
	                "hideTable": false,
	                "textBeforeTitle": "",
	                "title": "Procurement Budget",
	                "tableId": "ProcurmentBudget",
	                "dataInput": "Procurement Budget",
	                "isColumnMergeRequired": false,
	                "isRowMergeRequired": false,
	                "headersTitles": [
	                  [
	                    "Type",
	                    "Plant",
	                    "Cost",
	                    "% Change (+/-)",
	                    "Remarks",
	                    "Id",
	                    "Is Editable"
	                  ]
	                ],
	                "rows": [],
	                "hiddenColumns": [17, 18, 19],
	                "styles": {
	                  "boldColumns": [0],
	                  "borders": true
	                },
	                "autoMerge": {
	                  "columns": [],
	                  "rows": []
	                }
	              }
	            ]
	          }
	        }
	        """;
	}
	@Override
	public AOPMessageVM getMaintenanceReportURLs(String plantId, String year, String type) {
		try {
			List<MaintenanceReportURLDTO> maintenanceReportURLDTOs = new ArrayList<MaintenanceReportURLDTO>();
			List<Object[]> obj = findByYearAndPlantIdAndType(year, UUID.fromString(plantId),type, "vwMaintenanceReports");
			for(Object[] row:obj) {
				MaintenanceReportURLDTO maintenanceReportURLDTO = new MaintenanceReportURLDTO();
				maintenanceReportURLDTO.setId(row[0]!=null ? row[0].toString():"");
				maintenanceReportURLDTO.setReportCode(row[1]!=null ? row[1].toString():"");
				maintenanceReportURLDTO.setPlantId(row[2]!=null ? row[2].toString():"");
				maintenanceReportURLDTO.setAopYear(row[3]!=null ? row[3].toString():"");
				maintenanceReportURLDTO.setReportURL(row[4]!=null ? row[4].toString():"");
				maintenanceReportURLDTOs.add(maintenanceReportURLDTO);
			}
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(maintenanceReportURLDTOs);
			aopMessageVM.setMessage("Data fetched successfully");
			return aopMessageVM;
		}catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
		
	}
	
	public List<Object[]> findByYearAndPlantIdAndType(String year, UUID plantId,String type, String viewName) {
		try {
			String sql = "SELECT " + "Id, ReportCode, PlantId, AOPYear, ReportURL "
					 + "FROM " + viewName + " "
					+ "WHERE ReportCode = :type";
					
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("type", type);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	
	
	

}
