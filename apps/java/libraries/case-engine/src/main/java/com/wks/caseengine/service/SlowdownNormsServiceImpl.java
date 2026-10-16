package com.wks.caseengine.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Iterator;
import javax.sql.DataSource;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import com.wks.caseengine.dto.NormAttributeTransactionsDTO;
import com.wks.caseengine.dto.ShutdownNormsValueDTO;
import com.wks.caseengine.dto.SlowdownNormsValueDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.GradeSlowdownNormsValue;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.SlowdownConsumption;
import com.wks.caseengine.entity.SlowdownNormsValue;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.PlantMaintenanceTransactionRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.SlowdownConsumptionRepository;
import com.wks.caseengine.repository.SlowdownNormsRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;
import com.wks.caseengine.repository.GradeSlowdownNormsValuesRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

@Service
public class SlowdownNormsServiceImpl implements SlowdownNormsService {

	@Autowired
	private SlowdownNormsRepository slowdownNormsRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;
	
	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	@Autowired
	private PlantMaintenanceTransactionRepository plantMaintenanceTransactionRepository;
	
	
	@Autowired
	private SlowdownConsumptionRepository slowdownConsumptionRepository;
	
	private DataSource dataSource;
	
	@Autowired
	private NormParametersRepository normParametersRepository;
	
	@Autowired
	private  PlantService plantService;
	
	@Autowired
	private GradeSlowdownNormsValuesRepository gradeSlowdownNormsValuesRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public SlowdownNormsServiceImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	@Transactional
	public AOPMessageVM getSlowdownNormsData(String year, String plantId,String gradeId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			UUID grade=null;
			List<Object[]> objList = null;
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Boolean elastomerJmdHiiR = vertical.getName().equalsIgnoreCase("ELASTOMER")  && site.getName().equalsIgnoreCase("JMD") && plant.getName().equalsIgnoreCase("HIIR");
			boolean ptapmdpia = vertical.getName().equalsIgnoreCase("PTA") && site.getName().equalsIgnoreCase("pmd") && plant.getName().equalsIgnoreCase("pia");
			if (vertical.getName().equalsIgnoreCase("MEG")) {
				String storedProcedure = vertical.getName() + "_" + site.getName() + "_SlowdownNormCalculation";

				int spResult = getSlowdownNormsSPData(
						storedProcedure,
						year,
						plant.getId().toString(),
						site.getId().toString(),
						vertical.getId().toString());

				objList = getSlowdownNorms(year, plant.getId(), "vwScrnSlowdownNorms");
			} else if (vertical.getName().equalsIgnoreCase("PVC")) {
				String storedProcedure = "vwScrn" + vertical.getName() + "SlowdownNorms";

				objList = getSlowdownNorms(year, plant.getId(), storedProcedure);
			}else if(elastomerJmdHiiR) {
				String storedProcedure = vertical.getName() + "_" + site.getName() + "_GetSlowdownnorms";
				return getGradeWiseSlowdownConsumptionDataFromSP(plant.getId().toString(),year, gradeId, storedProcedure);

			} else if (vertical.getName().equalsIgnoreCase("PTA") || vertical.getName().equalsIgnoreCase("ELASTOMER")
					|| vertical.getName().equalsIgnoreCase("AROMATICS") || vertical.getName().equalsIgnoreCase("VCM") || vertical.getName().equalsIgnoreCase("Chemical")) {
				String storedProcedure = vertical.getName() + "_" + site.getName() + "_GetSlowdownnorms";

				objList = getSlowdownConsumptionData(plant.getId().toString(),year, storedProcedure);
			} else {
				String viewName = "vwScrn" + vertical.getName() + "SlowdownNorms";
				
				if(gradeId!=null) {
					 grade=UUID.fromString(gradeId);
				}
				objList = getSlowdownNormsWithGrades(year, plant.getId(), viewName,grade);
			}

			List<SlowdownNormsValueDTO> slowdownNormsValueDTOList = new ArrayList<>();
			for (Object[] row : objList) {
				SlowdownNormsValueDTO slowdownNormsValueDTO = new SlowdownNormsValueDTO();
				slowdownNormsValueDTO.setId(row[0] != null ? row[0].toString() : null);
				slowdownNormsValueDTO.setSiteFkId(row[1] != null ? row[1].toString() : null);
				slowdownNormsValueDTO.setPlantFkId(row[2] != null ? row[2].toString() : null);
				slowdownNormsValueDTO.setVerticalFkId(row[3] != null ? row[3].toString() : null);
				slowdownNormsValueDTO.setMaterialFkId(row[4] != null ? row[4].toString() : null);
				slowdownNormsValueDTO.setApril(row[5] != null ? Double.parseDouble(row[5].toString()) : 0.0);
				slowdownNormsValueDTO.setMay(row[6] != null ? Double.parseDouble(row[6].toString()) : 0.0);
				slowdownNormsValueDTO.setJune(row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0);
				slowdownNormsValueDTO.setJuly(row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0);
				slowdownNormsValueDTO.setAugust(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0);
				slowdownNormsValueDTO.setSeptember(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0);
				slowdownNormsValueDTO.setOctober(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0);
				slowdownNormsValueDTO.setNovember(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0);
				slowdownNormsValueDTO.setDecember(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0);
				slowdownNormsValueDTO.setJanuary(row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0);
				slowdownNormsValueDTO.setFebruary(row[15] != null ? Double.parseDouble(row[15].toString()) : 0.0);
				slowdownNormsValueDTO.setMarch(row[16] != null ? Double.parseDouble(row[16].toString()) : 0.0);
				slowdownNormsValueDTO.setFinancialYear(row[17] != null ? row[17].toString() : null);
				slowdownNormsValueDTO.setRemarks(row[18] != null ? row[18].toString() : " ");
				slowdownNormsValueDTO.setCreatedOn(row[19] != null ? (Date) row[19] : null);
				slowdownNormsValueDTO.setModifiedOn(row[20] != null ? (Date) row[20] : null);
				slowdownNormsValueDTO.setMcuVersion(row[21] != null ? row[21].toString() : null);
				slowdownNormsValueDTO.setUpdatedBy(row[22] != null ? row[22].toString() : null);
				slowdownNormsValueDTO.setNormParameterTypeId(row[23] != null ? row[23].toString() : null);
				slowdownNormsValueDTO.setNormParameterTypeName(row[24] != null ? row[24].toString() : null);
				slowdownNormsValueDTO.setNormParameterTypeDisplayName(row[25] != null ? row[25].toString() : null);
				slowdownNormsValueDTO.setUOM(row[28] != null ? row[28].toString() : null);
				slowdownNormsValueDTO.setIsEditable(row[29] != null ? Boolean.valueOf(row[29].toString()) : null);
				slowdownNormsValueDTO.setProductName(row[30] != null ? row[30].toString() : null);

				if(ptapmdpia) {
					slowdownNormsValueDTO.setSapCode(row[31] != null ? row[31].toString() : null);
				}

				slowdownNormsValueDTOList.add(slowdownNormsValueDTO);
			}
			Map<String, Object> map = new HashMap<>();

			List<AopCalculation> aopCalculation = aopCalculationRepository
					.findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year, "slowdown-norms");
			map.put("slowdownNormsValueDTO", slowdownNormsValueDTOList);
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
	
	public List<Object[]> getSlowdownConsumptionData(String plantId, String aopYear,String storedProcedure) {
		try {
			
			String sql = "EXEC " + storedProcedure
					+ " @plantId = :plantId, @FinYear = :aopYear";

			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public AOPMessageVM getGradeWiseSlowdownConsumptionDataFromSP(String plantId, String aopYear,String gradeId,String storedProcedure) {
		try {
			
			String sql = "EXEC " + storedProcedure
					+ " @plantId = :plantId, @FinYear = :aopYear, @GradeId = :gradeId";

			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);
			query.setParameter("gradeId", gradeId);

			List<Object[]> objList =  query.getResultList();

			List<SlowdownNormsValueDTO> slowdownNormsValueDTOList = new ArrayList<>();
			for (Object[] row : objList) {
				SlowdownNormsValueDTO slowdownNormsValueDTO = new SlowdownNormsValueDTO();
				slowdownNormsValueDTO.setId(row[0] != null ? row[0].toString() : null);
				slowdownNormsValueDTO.setSiteFkId(row[1] != null ? row[1].toString() : null);
				slowdownNormsValueDTO.setPlantFkId(row[2] != null ? row[2].toString() : null);
				slowdownNormsValueDTO.setVerticalFkId(row[3] != null ? row[3].toString() : null);
				slowdownNormsValueDTO.setMaterialFkId(row[4] != null ? row[4].toString() : null);
				slowdownNormsValueDTO.setGradeId(row[5] != null ? row[5].toString() : null);
				slowdownNormsValueDTO.setApril(row[6] != null ? Double.parseDouble(row[6].toString()) : 0.0);
				slowdownNormsValueDTO.setMay(row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0);
				slowdownNormsValueDTO.setJune(row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0);
				slowdownNormsValueDTO.setJuly(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0);
				slowdownNormsValueDTO.setAugust(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0);
				slowdownNormsValueDTO.setSeptember(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0);
				slowdownNormsValueDTO.setOctober(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0);
				slowdownNormsValueDTO.setNovember(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0);
				slowdownNormsValueDTO.setDecember(row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0);
				slowdownNormsValueDTO.setJanuary(row[15] != null ? Double.parseDouble(row[15].toString()) : 0.0);
				slowdownNormsValueDTO.setFebruary(row[16] != null ? Double.parseDouble(row[16].toString()) : 0.0);
				slowdownNormsValueDTO.setMarch(row[17] != null ? Double.parseDouble(row[17].toString()) : 0.0);
				slowdownNormsValueDTO.setFinancialYear(row[18] != null ? row[18].toString() : null);
				slowdownNormsValueDTO.setRemarks(row[19] != null ? row[19].toString() : " ");
				slowdownNormsValueDTO.setCreatedOn(row[20] != null ? (Date) row[20] : null);
				slowdownNormsValueDTO.setModifiedOn(row[21] != null ? (Date) row[21] : null);
				slowdownNormsValueDTO.setMcuVersion(row[22] != null ? row[22].toString() : null);
				slowdownNormsValueDTO.setUpdatedBy(row[23] != null ? row[23].toString() : null);
				slowdownNormsValueDTO.setNormParameterTypeId(row[24] != null ? row[24].toString() : null);
				slowdownNormsValueDTO.setNormParameterTypeName(row[25] != null ? row[25].toString() : null);
				slowdownNormsValueDTO.setNormParameterTypeDisplayName(row[26] != null ? row[26].toString() : null);
				slowdownNormsValueDTO.setUOM(row[29] != null ? row[29].toString() : null);
				slowdownNormsValueDTO.setIsEditable(row[30] != null ? Boolean.valueOf(row[30].toString()) : null);
				slowdownNormsValueDTO.setProductName(row[31] != null ? row[31].toString() : null);

				slowdownNormsValueDTOList.add(slowdownNormsValueDTO);
			}
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(slowdownNormsValueDTOList);
			aopMessageVM.setMessage("Data fetched successfully");
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	public byte[] exportSlowdownNorms(String year, UUID plantFKId, boolean isAfterSave, List<SlowdownNormsValueDTO> dtoList) {
		try {
			AOPMessageVM gradesVM = getUniqueGrades(year, plantFKId.toString());
			List<Map<String, String>> gradeInfoList = extractGradeInfo(gradesVM);
			Workbook workbook = new XSSFWorkbook();
			CellStyle lockedStyle = Utility.createLockedStyle(workbook);
			CellStyle unlockedStyle = Utility.createUnlockedStyle(workbook);

			for (Map<String, String> gradeInfo : gradeInfoList) {
				
				String currentGradeId = gradeInfo.get("gradeId");
				String sheetName = Utility.sanitizeSheetName(gradeInfo.get("displayName"));
				
				AOPMessageVM aopMessageVM =null;
				List<SlowdownNormsValueDTO> currentDtoList = new ArrayList<>();
				List<Boolean> isEditable = new ArrayList<>();
				if(!isAfterSave){
					 aopMessageVM = getSlowdownNormsData( year,plantFKId.toString(), currentGradeId);
				}
				if (aopMessageVM!=null && aopMessageVM.getData() != null) {
					
					Map<String, Object> responseMap = (Map<String, Object>) aopMessageVM.getData();
					currentDtoList = (List<SlowdownNormsValueDTO>) responseMap.get("slowdownNormsValueDTO");
				} else if (isAfterSave) {
					currentDtoList = dtoList.stream()
				            .filter(dto -> currentGradeId.equals(dto.getGradeId()))
				            .collect(Collectors.toList()); 
				} else {
                    continue; 
                }
                
				Sheet sheet = workbook.createSheet(sheetName);
				int currentRow = 0;

				List<List<Object>> rows = new ArrayList<>();
				for (SlowdownNormsValueDTO dto : currentDtoList) {
					List<Object> list = new ArrayList<>();
					list.add(dto.getNormParameterTypeDisplayName());
					list.add(dto.getProductName());
					list.add(dto.getUOM());
					list.add(dto.getApril());
					list.add(dto.getMay());
					list.add(dto.getJune());
					list.add(dto.getJuly());
					list.add(dto.getAugust());
					list.add(dto.getSeptember());
					list.add(dto.getOctober());
					list.add(dto.getNovember());
					list.add(dto.getDecember());
					list.add(dto.getJanuary());
					list.add(dto.getFebruary());
					list.add(dto.getMarch());
					list.add(dto.getRemarks());
					list.add(dto.getId()); 
					isEditable.add(dto.getIsEditable());
					
					if (isAfterSave) {
						list.add(dto.getSaveStatus());
						list.add(dto.getErrDescription());
					}
					rows.add(list);
				}

				
				List<String> innerHeaders = new ArrayList<>();
				innerHeaders.add("Type");
				innerHeaders.add("Particulars");
				innerHeaders.add("UOM");
				List<String> monthsList = Utility.getAcademicYearMonths(year);
				innerHeaders.addAll(monthsList);
				innerHeaders.add("Remarks");
				innerHeaders.add("Id");
				if (isAfterSave) {
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
						cell.setCellStyle(Utility.createBoldBorderedStyle(workbook));
					}
				}
				
				for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
					List<Object> rowData = rows.get(rowIndex);
					boolean isRowEditable = true;
					
					if (rowIndex < isEditable.size() && isEditable.get(rowIndex) != null) {
						isRowEditable = isEditable.get(rowIndex);
					}
					
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
						
						if (isRowEditable) {
							cell.setCellStyle(unlockedStyle);
						} else {
							cell.setCellStyle(lockedStyle);
						}
					}
				}
				sheet.setColumnHidden(16, true);
				
			} 
			
			try {
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
	
	public byte[] exportSlowdownNormsAllGrades(
	        String year, 
	        UUID plantFKId, 
	        boolean isAfterSave, 
	        List<SlowdownNormsValueDTO> dtoList,
			String maintenanceName) {
	    try {
	        AOPMessageVM gradesVM = getUniqueGrades(year, plantFKId.toString());
	        List<Map<String, String>> gradeInfoList = extractGradeInfo(gradesVM);
	        
	        if (gradeInfoList == null || gradeInfoList.isEmpty()) {
	            return null;
	        }

		List<Integer> editableMonths = null;
		if(maintenanceName != null && !maintenanceName.isEmpty()) {
			editableMonths = getSlowdownMonths(plantFKId, maintenanceName, year, null);
		}

	

        // Academic year month order: Apr(4), May(5), Jun(6), Jul(7), Aug(8), Sep(9), Oct(10), Nov(11), Dec(12), Jan(1), Feb(2), Mar(3)
        final int[] ACADEMIC_MONTH_ORDER = {4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3};
        final int MONTH_COL_START = 3;
        final int MONTH_COL_END   = 14;

        Workbook workbook = new XSSFWorkbook();
	        CellStyle lockedStyle = Utility.createLockedStyle(workbook);
	        lockedStyle.setLocked(true); // Ensure locked style is explicitly set to locked

	        CellStyle unlockedStyle = Utility.createUnlockedStyle(workbook);
	        unlockedStyle.setLocked(false); // Ensure unlocked style is explicitly set to NOT locked

	        for (Map<String, String> gradeInfo : gradeInfoList) {
	            String currentGradeId = gradeInfo.get("gradeId");
	            String displayName = gradeInfo.get("displayName");
	            String sheetName = Utility.sanitizeSheetName(displayName != null ? displayName : "Grade_" + currentGradeId);
	            
	            List<SlowdownNormsValueDTO> currentDtoList = new ArrayList<>();

	           
	            if (!isAfterSave) {
	                AOPMessageVM aopMessageVM = getSlowdownNormsData(year, plantFKId.toString(), currentGradeId);
	                if (aopMessageVM != null && aopMessageVM.getData() != null) {
	                    Map<String, Object> responseMap = (Map<String, Object>) aopMessageVM.getData();
	                    currentDtoList = (List<SlowdownNormsValueDTO>) responseMap.get("slowdownNormsValueDTO");
	                }
	            } else if (dtoList != null) {
	                currentDtoList = dtoList.stream()
	                        .filter(dto -> currentGradeId.equals(dto.getGradeId()))
	                        .collect(Collectors.toList());
	            }

	            if (currentDtoList == null || currentDtoList.isEmpty()) {
	                continue; 
	            }

	            Sheet sheet = workbook.createSheet(sheetName);
	            sheet.protectSheet("protection_password");

	            int currentRow = 0;

	            
	            List<String> headers = new ArrayList<>(Arrays.asList("Type", "Particulars", "UOM"));
	            headers.addAll(Utility.getAcademicYearMonths(year));
	            headers.add("Remarks");
	            headers.add("Id");
	            headers.add("Material Id");
	            if (isAfterSave) {
	                headers.add("Status");
	                headers.add("Error Description");
	            }

	            Row headerRow = sheet.createRow(currentRow++);
	            CellStyle headerStyle = Utility.createBoldBorderedStyle(workbook);
	            headerStyle.setLocked(true); 

	            for (int col = 0; col < headers.size(); col++) {
	                Cell cell = headerRow.createCell(col);
	                cell.setCellValue(headers.get(col));
	                cell.setCellStyle(headerStyle);
	            }

	            
	            for (SlowdownNormsValueDTO dto : currentDtoList) {
	                Row row = sheet.createRow(currentRow++);
	                List<Object> rowValues = new ArrayList<>();
	                rowValues.add(dto.getNormParameterTypeDisplayName());
	                rowValues.add(dto.getProductName());
	                rowValues.add(dto.getUOM());
	                rowValues.add(dto.getApril());
	                rowValues.add(dto.getMay());
	                rowValues.add(dto.getJune());
	                rowValues.add(dto.getJuly());
	                rowValues.add(dto.getAugust());
	                rowValues.add(dto.getSeptember());
	                rowValues.add(dto.getOctober());
	                rowValues.add(dto.getNovember());
	                rowValues.add(dto.getDecember());
	                rowValues.add(dto.getJanuary());
	                rowValues.add(dto.getFebruary());
	                rowValues.add(dto.getMarch());
	                rowValues.add(dto.getRemarks());
	                rowValues.add(dto.getId());
	                rowValues.add(dto.getMaterialFkId());

	                if (isAfterSave) {
	                    rowValues.add(dto.getSaveStatus());
	                    rowValues.add(dto.getErrDescription());
	                }

                
                boolean isRowEditable = (dto.getIsEditable() != null) ? dto.getIsEditable() : true;
                CellStyle rowStyle = isRowEditable ? unlockedStyle : lockedStyle;

                for (int col = 0; col < rowValues.size(); col++) {
                    Cell cell = row.createCell(col);
                    Object val = rowValues.get(col);
                    // Determine per-column cell style for month columns
                    CellStyle cellStyle;
                    if (col >= MONTH_COL_START && col <= MONTH_COL_END) {
                        int monthNumber = ACADEMIC_MONTH_ORDER[col - MONTH_COL_START];
                        if (editableMonths != null && !editableMonths.contains(monthNumber)) {
                            // Month is not in the allowed list ? always locked
                            cellStyle = lockedStyle;
                        } else {
                            // No restriction or month is allowed ? follow row editability
                            cellStyle = rowStyle;
                        }
                    } else {
                        cellStyle = rowStyle;
                    }

	                    if (val instanceof Number) {
	                        cell.setCellValue(((Number) val).doubleValue());
	                    } else if (val instanceof Boolean) {
	                        cell.setCellValue((Boolean) val);
	                    } else {
	                        cell.setCellValue(val != null ? val.toString() : "");
	                    }
	                    cell.setCellStyle(cellStyle);
	                }
	            }

	           
	            sheet.setColumnHidden(16, true); 
	            sheet.setColumnHidden(17, true); 
	            for (int i = 0; i < 3; i++) { 
	                sheet.autoSizeColumn(i); 
	            }
	        }

	        
	        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
	            workbook.write(outputStream);
	            workbook.close();
	            return outputStream.toByteArray();
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	public List<Map<String, String>> extractGradeInfo(AOPMessageVM grades) {
	    List<Map<String, String>> gradeInfoList = new ArrayList<>();

	    Object data = grades.getData();

	    if (data instanceof List) {
	        try {
	            @SuppressWarnings("unchecked")
	            List<Map<String, Object>> gradeList = (List<Map<String, Object>>) data;
	            
	            for (Map<String, Object> gradeMap : gradeList) {
	                Object gradeIdObj = gradeMap.get("gradeId");
	                Object displayNameObj = gradeMap.get("displayName");
	                
	                if (gradeIdObj != null && displayNameObj != null) {
	                    Map<String, String> infoMap = new HashMap<>();
	                    infoMap.put("gradeId", gradeIdObj.toString());
	                    infoMap.put("displayName", displayNameObj.toString());
	                    gradeInfoList.add(infoMap);
	                }
	            }
	        } catch (ClassCastException e) {
	            System.err.println("Error casting data to List<Map<String, Object>>: " + e.getMessage());
	        }
	    }

	    return gradeInfoList;
	}

	@Transactional
	public int getSlowdownNormsSPData(String procedureName, String finYear, String plantId,
			String siteId, String verticalId) {
		try {
			String sql = "EXEC " + procedureName
					+ " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @finYear = :finYear";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("siteId", siteId);
			query.setParameter("verticalId", verticalId);
			query.setParameter("finYear", finYear);

			return query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();

		}
		return 0;
	}

	@Override
	public List<SlowdownNormsValueDTO> saveSlowdownNormsData(List<SlowdownNormsValueDTO> slowdownNormsValueDTOList) {
	    String year = null;
	    UUID plantId = null;
	    List<SlowdownNormsValueDTO> failedList = new ArrayList<SlowdownNormsValueDTO>();
	    try {
			
			Plants plants = plantsRepository.findById(UUID.fromString(slowdownNormsValueDTOList.get(0).getPlantFkId())).orElse(null);
			Verticals vertical = verticalRepository.findById(plants.getVerticalFKId()).orElse(null);
			Sites site = siteRepository.findById(plants.getSiteFkId()).orElse(null);
			boolean ElastomerJMDHIIR = vertical.getName().equalsIgnoreCase("Elastomer") && site.getName().equalsIgnoreCase("JMD") && plants.getName().equalsIgnoreCase("HIIR");

			if(ElastomerJMDHIIR) {  
				failedList = saveSlowdownNormsDataHIIR(slowdownNormsValueDTOList, null);
				return failedList;
			}
	        for (SlowdownNormsValueDTO slowdownNormsValueDTO : slowdownNormsValueDTOList) {
	            if (slowdownNormsValueDTO.getSaveStatus() != null
	                    && slowdownNormsValueDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
	                failedList.add(slowdownNormsValueDTO);
	                continue;
	            }

	            year = slowdownNormsValueDTO.getFinancialYear();
	            plantId = UUID.fromString(slowdownNormsValueDTO.getPlantFkId());
	            SlowdownNormsValue existingEntity = null;
	            if (slowdownNormsValueDTO.getId() != null && !slowdownNormsValueDTO.getId().isEmpty()) {
					
	                existingEntity = slowdownNormsRepository.findById(UUID.fromString(slowdownNormsValueDTO.getId())).orElse(null);
	            } else {
	                UUID siteId = slowdownNormsValueDTO.getSiteFkId() != null ? UUID.fromString(slowdownNormsValueDTO.getSiteFkId()) : null;
	                UUID verticalId = slowdownNormsValueDTO.getVerticalFkId() != null ? UUID.fromString(slowdownNormsValueDTO.getVerticalFkId()) : null;
	                UUID materialId = slowdownNormsValueDTO.getMaterialFkId() != null ? UUID.fromString(slowdownNormsValueDTO.getMaterialFkId()) : null;
	                
	                UUID existingId = slowdownNormsRepository.findIdByFilters(plantId, siteId, verticalId, materialId, year);
	                if (existingId != null) {
	                    existingEntity = slowdownNormsRepository.findById(existingId).orElse(null);
	                }
	            }
	            if (existingEntity != null) {
	                boolean monthChanged = false;

	                if (!Objects.equals(existingEntity.getApril(), Optional.ofNullable(slowdownNormsValueDTO.getApril()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getMay(), Optional.ofNullable(slowdownNormsValueDTO.getMay()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getJune(), Optional.ofNullable(slowdownNormsValueDTO.getJune()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getJuly(), Optional.ofNullable(slowdownNormsValueDTO.getJuly()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getAugust(), Optional.ofNullable(slowdownNormsValueDTO.getAugust()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getSeptember(), Optional.ofNullable(slowdownNormsValueDTO.getSeptember()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getOctober(), Optional.ofNullable(slowdownNormsValueDTO.getOctober()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getNovember(), Optional.ofNullable(slowdownNormsValueDTO.getNovember()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getDecember(), Optional.ofNullable(slowdownNormsValueDTO.getDecember()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getJanuary(), Optional.ofNullable(slowdownNormsValueDTO.getJanuary()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getFebruary(), Optional.ofNullable(slowdownNormsValueDTO.getFebruary()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getMarch(), Optional.ofNullable(slowdownNormsValueDTO.getMarch()).orElse(0.0))) monthChanged = true;

	                boolean remarkChanged = !Objects.equals(existingEntity.getRemarks(), slowdownNormsValueDTO.getRemarks());

	                if (monthChanged && !remarkChanged) {
	                    slowdownNormsValueDTO.setSaveStatus("Failed");
	                    slowdownNormsValueDTO.setErrDescription("Please update remark");
	                    failedList.add(slowdownNormsValueDTO);
	                    continue; 
	                }
	            }

	            SlowdownNormsValue slowdownNormsValue = (existingEntity != null) ? existingEntity : new SlowdownNormsValue();
	            
	            try {
	                slowdownNormsValue.setApril(Optional.ofNullable(slowdownNormsValueDTO.getApril()).orElse(0.0));
	                slowdownNormsValue.setMay(Optional.ofNullable(slowdownNormsValueDTO.getMay()).orElse(0.0));
	                slowdownNormsValue.setJune(Optional.ofNullable(slowdownNormsValueDTO.getJune()).orElse(0.0));
	                slowdownNormsValue.setJuly(Optional.ofNullable(slowdownNormsValueDTO.getJuly()).orElse(0.0));
	                slowdownNormsValue.setAugust(Optional.ofNullable(slowdownNormsValueDTO.getAugust()).orElse(0.0));
	                slowdownNormsValue.setSeptember(Optional.ofNullable(slowdownNormsValueDTO.getSeptember()).orElse(0.0));
	                slowdownNormsValue.setOctober(Optional.ofNullable(slowdownNormsValueDTO.getOctober()).orElse(0.0));
	                slowdownNormsValue.setNovember(Optional.ofNullable(slowdownNormsValueDTO.getNovember()).orElse(0.0));
	                slowdownNormsValue.setDecember(Optional.ofNullable(slowdownNormsValueDTO.getDecember()).orElse(0.0));
	                slowdownNormsValue.setJanuary(Optional.ofNullable(slowdownNormsValueDTO.getJanuary()).orElse(0.0));
	                slowdownNormsValue.setFebruary(Optional.ofNullable(slowdownNormsValueDTO.getFebruary()).orElse(0.0));
	                slowdownNormsValue.setMarch(Optional.ofNullable(slowdownNormsValueDTO.getMarch()).orElse(0.0));
	            } catch (Exception e) {
	            	e.printStackTrace();
	                slowdownNormsValueDTO.setSaveStatus("Failed");
	                slowdownNormsValueDTO.setErrDescription("Please enter numeric values");
	                failedList.add(slowdownNormsValueDTO);
	                continue;
	            }

	            if (existingEntity != null) {
	                slowdownNormsValue.setModifiedOn(new Date());
	            } else {
	                slowdownNormsValue.setCreatedOn(new Date());
	                if (slowdownNormsValueDTO.getSiteFkId() != null) slowdownNormsValue.setSiteFkId(UUID.fromString(slowdownNormsValueDTO.getSiteFkId()));
	                if (slowdownNormsValueDTO.getPlantFkId() != null) slowdownNormsValue.setPlantFkId(UUID.fromString(slowdownNormsValueDTO.getPlantFkId()));
	                if (slowdownNormsValueDTO.getVerticalFkId() != null) slowdownNormsValue.setVerticalFkId(UUID.fromString(slowdownNormsValueDTO.getVerticalFkId()));
	                if (slowdownNormsValueDTO.getMaterialFkId() != null) slowdownNormsValue.setMaterialFkId(UUID.fromString(slowdownNormsValueDTO.getMaterialFkId()));
	                if (slowdownNormsValueDTO.getNormParameterTypeId() != null) {
	                    slowdownNormsValue.setNormParameterTypeFkId(UUID.fromString(slowdownNormsValueDTO.getNormParameterTypeId()));
	                }
	            }

	            slowdownNormsValue.setFinancialYear(year);
	            slowdownNormsValue.setRemarks(slowdownNormsValueDTO.getRemarks());
	            slowdownNormsValue.setMcuVersion("V1");
	            slowdownNormsValue.setUpdatedBy(Utility.getUserName());

	            slowdownNormsRepository.save(slowdownNormsValue);
	            System.out.println("Data Saved Successfully");
	        }

	        List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("slowdown-norms");
	        for (ScreenMapping screenMapping : screenMappingList) {
	            AopCalculation aopCalculation = new AopCalculation();
	            aopCalculation.setAopYear(year);
	            aopCalculation.setIsChanged(true);
	            aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
	            aopCalculation.setPlantId(plantId);
	            aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
	            aopCalculationRepository.save(aopCalculation);
	        }

	        return failedList;
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        throw new RuntimeException("Failed to update data", ex);
	    }
	} 

	

	@Override
	public List<SlowdownNormsValueDTO> saveSlowdownNormsDataHIIR(List<SlowdownNormsValueDTO> slowdownNormsValueDTOList, String maintenanceName) {
	    String year = null;
	    UUID plantId = null;
	    List<SlowdownNormsValueDTO> failedList = new ArrayList<SlowdownNormsValueDTO>();
	    try {
	        for (SlowdownNormsValueDTO slowdownNormsValueDTO : slowdownNormsValueDTOList) {
	            if (slowdownNormsValueDTO.getSaveStatus() != null
	                    && slowdownNormsValueDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
	                failedList.add(slowdownNormsValueDTO);
	                continue;
	            }

			

	            year = slowdownNormsValueDTO.getFinancialYear();
	            plantId = UUID.fromString(slowdownNormsValueDTO.getPlantFkId());
	            GradeSlowdownNormsValue existingEntity = null;
	            if (slowdownNormsValueDTO.getId() != null && !slowdownNormsValueDTO.getId().isEmpty()) {
	                existingEntity = gradeSlowdownNormsValuesRepository.findById(UUID.fromString(slowdownNormsValueDTO.getId())).orElse(null);
	            } else {
	                UUID siteId = slowdownNormsValueDTO.getSiteFkId() != null ? UUID.fromString(slowdownNormsValueDTO.getSiteFkId()) : null;
	                UUID verticalId = slowdownNormsValueDTO.getVerticalFkId() != null ? UUID.fromString(slowdownNormsValueDTO.getVerticalFkId()) : null;
	                UUID materialId = slowdownNormsValueDTO.getMaterialFkId() != null ? UUID.fromString(slowdownNormsValueDTO.getMaterialFkId()) : null;
	                
	                UUID existingId = gradeSlowdownNormsValuesRepository.findIdByFilters(plantId, siteId, verticalId, materialId, year);
	                if (existingId != null) {
	                    existingEntity = gradeSlowdownNormsValuesRepository.findById(existingId).orElse(null);
	                }
	            }

				List<Integer> editableMonths = null;
				if(maintenanceName != null && !maintenanceName.isEmpty() && existingEntity != null) { 

					editableMonths = getSlowdownMonths(plantId, maintenanceName, year, null);
				

					// set the non-editable months of slowdownNormsValueDTO to existing entity
				
					for (int month = 1; month <= 12; month++) { 

						if (editableMonths.contains(month)) {
							continue;
						}

						switch(month) {

							case 1:
								slowdownNormsValueDTO.setJanuary(Optional.ofNullable(existingEntity.getJanuary()).orElse(0.0));
								break;
							case 2:
								slowdownNormsValueDTO.setFebruary(Optional.ofNullable(existingEntity.getFebruary()).orElse(0.0));
								break;
							case 3:
								slowdownNormsValueDTO.setMarch(Optional.ofNullable(existingEntity.getMarch()).orElse(0.0));
								break;

							case 4:
								slowdownNormsValueDTO.setApril(Optional.ofNullable(existingEntity.getApril()).orElse(0.0));
								break;
							case 5:
								slowdownNormsValueDTO.setMay(Optional.ofNullable(existingEntity.getMay()).orElse(0.0));
								break;

							case 6:
								slowdownNormsValueDTO.setJune(Optional.ofNullable(existingEntity.getJune()).orElse(0.0));
								break;
							case 7:
								slowdownNormsValueDTO.setJuly(Optional.ofNullable(existingEntity.getJuly()).orElse(0.0));
								break;
							case 8:
								slowdownNormsValueDTO.setAugust(Optional.ofNullable(existingEntity.getAugust()).orElse(0.0));
								break;
							case 9:
								slowdownNormsValueDTO.setSeptember(Optional.ofNullable(existingEntity.getSeptember()).orElse(0.0));
								break;
							case 10:
								slowdownNormsValueDTO.setOctober(Optional.ofNullable(existingEntity.getOctober()).orElse(0.0));
								break;
							case 11:
								slowdownNormsValueDTO.setNovember(Optional.ofNullable(existingEntity.getNovember()).orElse(0.0));
								break;

							case 12:
								slowdownNormsValueDTO.setDecember(Optional.ofNullable(existingEntity.getDecember()).orElse(0.0));
								break;
						
						}
					}
					
				
			}
	            if (existingEntity != null) {
	                boolean monthChanged = false;

	                if (!Objects.equals(existingEntity.getApril(), Optional.ofNullable(slowdownNormsValueDTO.getApril()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getMay(), Optional.ofNullable(slowdownNormsValueDTO.getMay()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getJune(), Optional.ofNullable(slowdownNormsValueDTO.getJune()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getJuly(), Optional.ofNullable(slowdownNormsValueDTO.getJuly()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getAugust(), Optional.ofNullable(slowdownNormsValueDTO.getAugust()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getSeptember(), Optional.ofNullable(slowdownNormsValueDTO.getSeptember()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getOctober(), Optional.ofNullable(slowdownNormsValueDTO.getOctober()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getNovember(), Optional.ofNullable(slowdownNormsValueDTO.getNovember()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getDecember(), Optional.ofNullable(slowdownNormsValueDTO.getDecember()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getJanuary(), Optional.ofNullable(slowdownNormsValueDTO.getJanuary()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getFebruary(), Optional.ofNullable(slowdownNormsValueDTO.getFebruary()).orElse(0.0))) monthChanged = true;
	                if (!Objects.equals(existingEntity.getMarch(), Optional.ofNullable(slowdownNormsValueDTO.getMarch()).orElse(0.0))) monthChanged = true;

	                boolean remarkChanged = !Objects.equals(existingEntity.getRemarks(), slowdownNormsValueDTO.getRemarks());

	                if (monthChanged && !remarkChanged) {
	                    slowdownNormsValueDTO.setSaveStatus("Failed");
	                    slowdownNormsValueDTO.setErrDescription("Please update remark");
	                    failedList.add(slowdownNormsValueDTO);
	                    continue; 
	                }
	            }

	            GradeSlowdownNormsValue slowdownNormsValue = (existingEntity != null) ? existingEntity : new GradeSlowdownNormsValue();
	            
	            try {
	                slowdownNormsValue.setApril(Optional.ofNullable(slowdownNormsValueDTO.getApril()).orElse(0.0));
	                slowdownNormsValue.setMay(Optional.ofNullable(slowdownNormsValueDTO.getMay()).orElse(0.0));
	                slowdownNormsValue.setJune(Optional.ofNullable(slowdownNormsValueDTO.getJune()).orElse(0.0));
	                slowdownNormsValue.setJuly(Optional.ofNullable(slowdownNormsValueDTO.getJuly()).orElse(0.0));
	                slowdownNormsValue.setAugust(Optional.ofNullable(slowdownNormsValueDTO.getAugust()).orElse(0.0));
	                slowdownNormsValue.setSeptember(Optional.ofNullable(slowdownNormsValueDTO.getSeptember()).orElse(0.0));
	                slowdownNormsValue.setOctober(Optional.ofNullable(slowdownNormsValueDTO.getOctober()).orElse(0.0));
	                slowdownNormsValue.setNovember(Optional.ofNullable(slowdownNormsValueDTO.getNovember()).orElse(0.0));
	                slowdownNormsValue.setDecember(Optional.ofNullable(slowdownNormsValueDTO.getDecember()).orElse(0.0));
	                slowdownNormsValue.setJanuary(Optional.ofNullable(slowdownNormsValueDTO.getJanuary()).orElse(0.0));
	                slowdownNormsValue.setFebruary(Optional.ofNullable(slowdownNormsValueDTO.getFebruary()).orElse(0.0));
	                slowdownNormsValue.setMarch(Optional.ofNullable(slowdownNormsValueDTO.getMarch()).orElse(0.0));
	            } catch (Exception e) {
	            	e.printStackTrace();
	                slowdownNormsValueDTO.setSaveStatus("Failed");
	                slowdownNormsValueDTO.setErrDescription("Please enter numeric values");
	                failedList.add(slowdownNormsValueDTO);
	                continue;
	            }

	            if (existingEntity != null) {
	                slowdownNormsValue.setModifiedOn(new Date());
	            } else {
	                slowdownNormsValue.setCreatedOn(new Date());
	                if (slowdownNormsValueDTO.getSiteFkId() != null) slowdownNormsValue.setSiteFkId(UUID.fromString(slowdownNormsValueDTO.getSiteFkId()));
	                if (slowdownNormsValueDTO.getPlantFkId() != null) slowdownNormsValue.setPlantFkId(UUID.fromString(slowdownNormsValueDTO.getPlantFkId()));
	                if (slowdownNormsValueDTO.getVerticalFkId() != null) slowdownNormsValue.setVerticalFkId(UUID.fromString(slowdownNormsValueDTO.getVerticalFkId()));
	                if (slowdownNormsValueDTO.getMaterialFkId() != null) slowdownNormsValue.setMaterialFkId(UUID.fromString(slowdownNormsValueDTO.getMaterialFkId()));
	                if (slowdownNormsValueDTO.getNormParameterTypeId() != null) {
	                    slowdownNormsValue.setNormParameterTypeFkId(UUID.fromString(slowdownNormsValueDTO.getNormParameterTypeId()));
	                }
	            }

	            slowdownNormsValue.setFinancialYear(year);
	            slowdownNormsValue.setRemarks(slowdownNormsValueDTO.getRemarks());
	            slowdownNormsValue.setMcuVersion("V1");
	            slowdownNormsValue.setUpdatedBy(Utility.getUserName());

	            gradeSlowdownNormsValuesRepository.save(slowdownNormsValue);
	            System.out.println("Data Saved Successfully");
	        }

	        List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("slowdown-norms");
	        for (ScreenMapping screenMapping : screenMappingList) {
	            AopCalculation aopCalculation = new AopCalculation();
	            aopCalculation.setAopYear(year);
	            aopCalculation.setIsChanged(true);
	            aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
	            aopCalculation.setPlantId(plantId);
	            aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
	            aopCalculationRepository.save(aopCalculation);
	        }

	        return failedList;
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        throw new RuntimeException("Failed to update data", ex);
	    }
	} 

	@Override
	@Transactional
	public List<SlowdownNormsValueDTO> getSlowdownNormsSPData(String year, String plantId) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_CalculateConsumptionAOPValues";
			List<Object[]> list = getCalculatedSlowdownNormsSP(storedProcedure, year, plant.getId().toString(),
					site.getId().toString(), vertical.getId().toString());
			List<SlowdownNormsValueDTO> slowdownNormsValueDTOList = new ArrayList<>();
			for (Object[] row : list) {
				SlowdownNormsValueDTO slowdownNormsValueDTO = new SlowdownNormsValueDTO();
				slowdownNormsValueDTO.setNormParameterTypeDisplayName(row[0] != null ? row[0].toString() : null);
				slowdownNormsValueDTO.setUOM(row[1] != null ? row[1].toString() : null);
				slowdownNormsValueDTO.setSiteFkId(row[2] != null ? row[2].toString() : null);
				slowdownNormsValueDTO.setVerticalFkId(row[3] != null ? row[3].toString() : null);
				slowdownNormsValueDTO.setAOPCaseId(row[4] != null ? row[4].toString() : null);
				slowdownNormsValueDTO.setAOPStatus(row[5] != null ? row[5].toString() : null);
				slowdownNormsValueDTO.setRemarks(row[6] != null ? row[6].toString() : "");
				slowdownNormsValueDTO.setMaterialFkId(row[7] != null ? row[7].toString() : null);
				slowdownNormsValueDTO.setJanuary(row[8] != null ? Double.parseDouble(row[8].toString()) : null);
				slowdownNormsValueDTO.setFebruary(row[9] != null ? Double.parseDouble(row[9].toString()) : null);
				slowdownNormsValueDTO.setMarch(row[10] != null ? Double.parseDouble(row[10].toString()) : null);
				slowdownNormsValueDTO.setApril(row[11] != null ? Double.parseDouble(row[11].toString()) : null);
				slowdownNormsValueDTO.setMay(row[12] != null ? Double.parseDouble(row[12].toString()) : null);
				slowdownNormsValueDTO.setJune(row[13] != null ? Double.parseDouble(row[13].toString()) : null);
				slowdownNormsValueDTO.setJuly(row[14] != null ? Double.parseDouble(row[14].toString()) : null);
				slowdownNormsValueDTO.setAugust(row[15] != null ? Double.parseDouble(row[15].toString()) : null);
				slowdownNormsValueDTO.setSeptember(row[16] != null ? Double.parseDouble(row[16].toString()) : null);
				slowdownNormsValueDTO.setOctober(row[17] != null ? Double.parseDouble(row[17].toString()) : null);
				slowdownNormsValueDTO.setNovember(row[18] != null ? Double.parseDouble(row[18].toString()) : null);
				slowdownNormsValueDTO.setDecember(row[19] != null ? Double.parseDouble(row[19].toString()) : null);
				slowdownNormsValueDTO.setFinancialYear(row[20] != null ? row[20].toString() : null);
				slowdownNormsValueDTO.setPlantFkId(row[21] != null ? row[21].toString() : null);
				slowdownNormsValueDTOList.add(slowdownNormsValueDTO);
			}

			return slowdownNormsValueDTOList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Transactional
	public List<Object[]> getCalculatedSlowdownNormsSP(String procedureName, String finYear, String plantId,
			String siteId, String verticalId) {
		try {
			// Create a native query to execute the stored procedure
			String sql = "EXEC " + procedureName
					+ " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @finYear = :finYear";

			Query query = entityManager.createNativeQuery(sql);

			// Set parameters
			query.setParameter("plantId", plantId);
			query.setParameter("siteId", siteId);
			query.setParameter("verticalId", verticalId);
			query.setParameter("finYear", finYear);

			return query.getResultList(); // Fetch results instead of executing an update
		} catch (Exception e) {
			e.printStackTrace(); // Log detailed exception for debugging
			return Collections.emptyList(); // Return an empty list instead of 0
		}
	}

	public List<Object[]> getSlowdownNorms(String year, UUID plantId, String viewName) {
		try {
			String sql = "SELECT TOP (1000) [Id], [Site_FK_Id], [Plant_FK_Id], [Vertical_FK_Id], "
					+ "[Material_FK_Id], [April], [May], [June], [July], [August], [September], "
					+ "[October], [November], [December], [January], [February], [March], "
					+ "[FinancialYear], [Remarks], [CreatedOn], [ModifiedOn], [MCUVersion], "
					+ "[UpdatedBy], [NormParameterTypeId], [NormParameterTypeName], "
					+ "[NormParameterTypeDisplayName], [NormTypeDisplayOrder], [MaterialDisplayOrder], [UOM],[isEditable],[DisplayName] "
					+ "FROM " + viewName + " "
					+ "WHERE Plant_FK_Id = :plantId AND (FinancialYear = :year OR FinancialYear IS NULL) "
					+ "ORDER BY NormTypeDisplayOrder,MaterialDisplayOrder";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("year", year);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	public List<Object[]> getSlowdownNormsWithGrades(String year, UUID plantId, String viewName,UUID gradeId) {
		try {
			String sql = "SELECT TOP (1000) [Id], [Site_FK_Id], [Plant_FK_Id], [Vertical_FK_Id], "
					+ "[Material_FK_Id], [April], [May], [June], [July], [August], [September], "
					+ "[October], [November], [December], [January], [February], [March], "
					+ "[FinancialYear], [Remarks], [CreatedOn], [ModifiedOn], [MCUVersion], "
					+ "[UpdatedBy], [NormParameterTypeId], [NormParameterTypeName], "
					+ "[NormParameterTypeDisplayName], [NormTypeDisplayOrder], [MaterialDisplayOrder], [UOM],[isEditable],[DisplayName] "
					+ "FROM " + viewName + " "
					+ "WHERE Plant_FK_Id = :plantId AND (FinancialYear = :year OR FinancialYear IS NULL) AND (:gradeId IS NULL OR Grade_FK_Id = :gradeId) "
					+ "ORDER BY NormTypeDisplayOrder";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("gradeId", gradeId);
			query.setParameter("year", year);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}


	@Override
	@Transactional
	public List getSlowdownMonths(UUID plantId, String maintenanceName,String year,String gradeId) {
		String verticalName = plantsRepository.findVerticalNameByPlantId((plantId));
		Plants plant = plantsRepository.findById(plantId).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		boolean pvc = verticalName.equalsIgnoreCase("PVC") && (site.getName().equalsIgnoreCase("VMD") || site.getName().equalsIgnoreCase("DMD"));
		try {
			if(verticalName.equalsIgnoreCase("PE") || verticalName.equalsIgnoreCase("PP") || verticalName.equalsIgnoreCase("PET") || pvc) {
				UUID grade=null;
				if(gradeId!=null) {
					 grade=UUID.fromString(gradeId);
				}
				return	slowdownNormsRepository.getSlowdownMonthsWithGrades(plantId,maintenanceName,year,grade);
			}else if(verticalName.equalsIgnoreCase("VCM") || verticalName.equalsIgnoreCase("Chemical")){
				return	slowdownNormsRepository.getVCMSlowdownMonths(plantId,maintenanceName,year);
			}else if(verticalName.equalsIgnoreCase("PTA")){
				return	slowdownNormsRepository.getPTASlowdownMonths(plantId,maintenanceName,year);
			}else {
				return	slowdownNormsRepository.getSlowdownMonths(plantId,maintenanceName,year);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	@Transactional
	public List getSlowdownMonthsImport(UUID plantId, String maintenanceName,String year) {
		String verticalName = plantsRepository.findVerticalNameByPlantId((plantId));
		
		try {
				
				return	slowdownNormsRepository.getSlowdownMonthsWithGradesImport(plantId,maintenanceName,year);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public byte[] exportSlowdownConsumption(String year, UUID plantFKId, boolean isAfterSave, List<SlowdownNormsValueDTO> dtoList,String gradeId) {
		try {

			Plants plant = plantsRepository.findById(plantFKId).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			boolean ptapmdpia = vertical.getName().equalsIgnoreCase("PTA") && site.getName().equalsIgnoreCase("pmd") && plant.getName().equalsIgnoreCase("pia");

			if(ptapmdpia) {
				// seperate method to include sap code column
				return exportSlowdownConsumptionWithSapCode(year, plantFKId, isAfterSave, dtoList, gradeId);
			}
			
			AOPMessageVM aopMessageVM = getSlowdownNormsData( year,  plantFKId.toString(), gradeId);
					
			List<Boolean> isEditable = new ArrayList<>();

			if (!isAfterSave) {
				Map<String, Object> responseMap = (Map<String, Object>) aopMessageVM.getData();
				dtoList = (List<SlowdownNormsValueDTO>) responseMap.get("slowdownNormsValueDTO");
			}

		// Fetch allowed months from getSlowdownMonths to control month-column editability
		Set<Integer> allowedMonths = new HashSet<>();
		List<?> slowdownMonthsList = getSlowdownMonths(plantFKId, "Slowdown", year, gradeId);
		if (slowdownMonthsList != null) {
			for (Object m : slowdownMonthsList) {
				if (m instanceof Integer) allowedMonths.add((Integer) m);
			}
		}

		// Column index (0-based) ? month number mapping for the 12 month columns
		// Cols 3?11: Apr(4)?Dec(12); Cols 12?14: Jan(1)?Mar(3)
		Map<Integer, Integer> colToMonth = new HashMap<>();
		colToMonth.put(3,  4);  // April
		colToMonth.put(4,  5);  // May
		colToMonth.put(5,  6);  // June
		colToMonth.put(6,  7);  // July
		colToMonth.put(7,  8);  // August
		colToMonth.put(8,  9);  // September
		colToMonth.put(9,  10); // October
		colToMonth.put(10, 11); // November
		colToMonth.put(11, 12); // December
		colToMonth.put(12, 1);  // January
		colToMonth.put(13, 2);  // February
		colToMonth.put(14, 3);  // March

		Workbook workbook = new XSSFWorkbook();

		Sheet sheet = workbook.createSheet("Sheet1");
		int currentRow = 0;
		// List<List<Object>> rows = new ArrayList<>();

		List<List<Object>> rows = new ArrayList<>();
		
		// Create styles for locking/unlocking cells
		CellStyle lockedStyle = workbook.createCellStyle();
		lockedStyle.setLocked(true);
		lockedStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		lockedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		CellStyle unlockedStyle = workbook.createCellStyle();
		unlockedStyle.setLocked(false);
			// Data rows
			for (SlowdownNormsValueDTO dto : dtoList) {
				//if (isAfterSave) {
					List<Object> list = new ArrayList<>();
					list.add(dto.getNormParameterTypeDisplayName());
					list.add(dto.getProductName());
					list.add(dto.getUOM());
					list.add(dto.getApril());
					list.add(dto.getMay());
					list.add(dto.getJune());
					list.add(dto.getJuly());
					list.add(dto.getAugust());
					list.add(dto.getSeptember());
					list.add(dto.getOctober());
					list.add(dto.getNovember());
					list.add(dto.getDecember());
					list.add(dto.getJanuary());
					list.add(dto.getFebruary());
					list.add(dto.getMarch());
					list.add(dto.getRemarks());
					list.add(dto.getId());
					list.add(dto.getMaterialFkId());
					isEditable.add(dto.getIsEditable());
					// list.add(dto.getMaterialFkId());
					 //list.add(dto.getIsEditable());
					if (isAfterSave) {
						list.add(dto.getSaveStatus());
						list.add(dto.getErrDescription());
					}
					rows.add(list);
				//}
			}

			List<String> innerHeaders = new ArrayList<>();
			innerHeaders.add("Type");
			innerHeaders.add("Particulars");
			innerHeaders.add("UOM");
			List<String> monthsList = getAcademicYearMonths(year);
			innerHeaders.addAll(monthsList);
			innerHeaders.add("Remarks");
			innerHeaders.add("Id");
			innerHeaders.add("Material Id");
			// innerHeaders.add("NormParamterId");
			 //innerHeaders.add("IsEditable");
			if (isAfterSave) {
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
					cell.setCellStyle(Utility.createBoldBorderedStyle(workbook));
				}
			}
			for (List<Object> rowData : rows) {
				boolean isRowEditable=true;
				if(isEditable.get(currentRow-1)!=null) {
					isRowEditable = isEditable.get(currentRow-1);
				}
				 
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
					// Month columns are editable only when the row is editable AND
				// the month is in the allowed months list returned by getSlowdownMonths.
				// Non-month columns follow row-level editability only.
				boolean cellEditable;
				if (colToMonth.containsKey(col)) {
					cellEditable = isRowEditable && allowedMonths.contains(colToMonth.get(col));
				} else {
					cellEditable = isRowEditable;
				}
				cell.setCellStyle(cellEditable ? unlockedStyle : lockedStyle);

				}
			}
			sheet.setColumnHidden(16, true);
			sheet.setColumnHidden(17, true);
			//sheet.setColumnHidden(18, true);
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

	// ref: exportSlowdownConsumption | new method to include sap code column
	@Override
	public byte[] exportSlowdownConsumptionWithSapCode(String year, UUID plantFKId, boolean isAfterSave, List<SlowdownNormsValueDTO> dtoList, String gradeId) {
		try {
			AOPMessageVM aopMessageVM = getSlowdownNormsData(year, plantFKId.toString(), gradeId);

			List<Boolean> isEditable = new ArrayList<>();

			if (!isAfterSave) {
				Map<String, Object> responseMap = (Map<String, Object>) aopMessageVM.getData();
				dtoList = (List<SlowdownNormsValueDTO>) responseMap.get("slowdownNormsValueDTO");
			}

			Set<Integer> allowedMonths = new HashSet<>();
			List<?> slowdownMonthsList = getSlowdownMonths(plantFKId, "Slowdown", year, gradeId);
			if (slowdownMonthsList != null) {
				for (Object m : slowdownMonthsList) {
					if (m instanceof Integer) allowedMonths.add((Integer) m);
				}
			}

			// Column layout (0-based):
			// 0:Type, 1:Particulars, 2:UOM, 3:Sap Code (locked), 4-15:months, 16:Remarks, 17:Id (hidden), 18:Material Id (hidden)
			Map<Integer, Integer> colToMonth = new HashMap<>();
			colToMonth.put(4,  4);  // April
			colToMonth.put(5,  5);  // May
			colToMonth.put(6,  6);  // June
			colToMonth.put(7,  7);  // July
			colToMonth.put(8,  8);  // August
			colToMonth.put(9,  9);  // September
			colToMonth.put(10, 10); // October
			colToMonth.put(11, 11); // November
			colToMonth.put(12, 12); // December
			colToMonth.put(13, 1);  // January
			colToMonth.put(14, 2);  // February
			colToMonth.put(15, 3);  // March

			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("Sheet1");
			int currentRow = 0;

			List<List<Object>> rows = new ArrayList<>();

			CellStyle lockedStyle = workbook.createCellStyle();
			lockedStyle.setLocked(true);
			lockedStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			lockedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			CellStyle unlockedStyle = workbook.createCellStyle();
			unlockedStyle.setLocked(false);

			for (SlowdownNormsValueDTO dto : dtoList) {
				List<Object> list = new ArrayList<>();
				list.add(dto.getNormParameterTypeDisplayName());
				list.add(dto.getProductName());
				list.add(dto.getUOM());
				list.add(dto.getSapCode());
				list.add(dto.getApril());
				list.add(dto.getMay());
				list.add(dto.getJune());
				list.add(dto.getJuly());
				list.add(dto.getAugust());
				list.add(dto.getSeptember());
				list.add(dto.getOctober());
				list.add(dto.getNovember());
				list.add(dto.getDecember());
				list.add(dto.getJanuary());
				list.add(dto.getFebruary());
				list.add(dto.getMarch());
				list.add(dto.getRemarks());
				list.add(dto.getId());
				list.add(dto.getMaterialFkId());
				isEditable.add(dto.getIsEditable());
				if (isAfterSave) {
					list.add(dto.getSaveStatus());
					list.add(dto.getErrDescription());
				}
				rows.add(list);
			}

			List<String> innerHeaders = new ArrayList<>();
			innerHeaders.add("Type");
			innerHeaders.add("Particulars");
			innerHeaders.add("UOM");
			innerHeaders.add("Sap Code");
			List<String> monthsList = getAcademicYearMonths(year);
			innerHeaders.addAll(monthsList);
			innerHeaders.add("Remarks");
			innerHeaders.add("Id");
			innerHeaders.add("Material Id");
			if (isAfterSave) {
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
					cell.setCellStyle(Utility.createBoldBorderedStyle(workbook));
				}
			}

			for (List<Object> rowData : rows) {
				boolean isRowEditable = true;
				if (isEditable.get(currentRow - 1) != null) {
					isRowEditable = isEditable.get(currentRow - 1);
				}

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

					boolean cellEditable;
					if (col == 3) {
						// Sap Code is always locked — read-only display column
						cellEditable = false;
					} else if (colToMonth.containsKey(col)) {
						cellEditable = isRowEditable && allowedMonths.contains(colToMonth.get(col));
					} else {
						cellEditable = isRowEditable;
					}
					cell.setCellStyle(cellEditable ? unlockedStyle : lockedStyle);
				}
			}

			sheet.setColumnHidden(17, true);
			sheet.setColumnHidden(18, true);

			try {
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
	
	@Override
	public AOPMessageVM importSlowdownConsumption(String year, UUID plantFKId, String gradeId, MultipartFile file) {
		// TODO Auto-generated method stub
		try {
			Plants plant = plantsRepository.findById(plantFKId).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

			boolean ptapmdpia = vertical.getName().equalsIgnoreCase("PTA") && site.getName().equalsIgnoreCase("pmd") && plant.getName().equalsIgnoreCase("pia");

			if(ptapmdpia) { 
				// seperate method to handle sap code column
				return importSlowdownConsumptionWithSapCode(year, plantFKId, gradeId, file);
			}

			List<SlowdownNormsValueDTO> data=null;
				data = readSlowdownConsumptions(file.getInputStream(), plantFKId, year);
			
				List<SlowdownNormsValueDTO> failedList = saveSlowdownNormsData(data);
				

			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (failedList != null && failedList.size() > 0) {
				byte[] fileByteArray =null;
				
					 fileByteArray = exportSlowdownConsumption(year, plantFKId, true, failedList,gradeId);
				
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

	// ref: importSlowdownConsumption | new method to include sap code column
	@Override
	public AOPMessageVM importSlowdownConsumptionWithSapCode(String year, UUID plantFKId, String gradeId, MultipartFile file) {
		try {
			Plants plant = plantsRepository.findById(plantFKId).get();
			List<SlowdownNormsValueDTO> data = null;
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			data = readSlowdownConsumptionsWithSapCode(file.getInputStream(), plantFKId, year);

			List<SlowdownNormsValueDTO> failedList = saveSlowdownNormsData(data);

			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (failedList != null && failedList.size() > 0) {
				byte[] fileByteArray = exportSlowdownConsumptionWithSapCode(year, plantFKId, true, failedList, gradeId);
				String base64File = Base64.getEncoder().encodeToString(fileByteArray);
				aopMessageVM.setData(base64File);
				aopMessageVM.setCode(400);
				aopMessageVM.setMessage("Partial data has been saved");
			} else {
				aopMessageVM.setCode(200);
				aopMessageVM.setMessage("All data has been saved");
			}

			return aopMessageVM;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<SlowdownNormsValueDTO> readSlowdownConsumptionsWithSapCode(InputStream inputStream, UUID plantFKId, String year) {
		List<SlowdownNormsValueDTO> configList = new ArrayList<>();

	    Plants plant = plantsRepository.findById(plantFKId)
	        .orElseThrow(() -> new RuntimeException("Plant not found"));
	    Sites site = siteRepository.findById(plant.getSiteFkId())
	        .orElseThrow(() -> new RuntimeException("Site not found"));
	    Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
	        .orElseThrow(() -> new RuntimeException("Vertical not found"));

	    Set<Integer> activeMonths = new HashSet<>();

	    try (Workbook workbook = new XSSFWorkbook(inputStream)) {
	        List<Integer> slowdown = getSlowdownMonths(plantFKId, "Slowdown", year, null);
	        if (slowdown != null) activeMonths.addAll(slowdown);

	        Sheet sheet = workbook.getSheetAt(0);
	        if (sheet != null) {
	            Iterator<Row> rowIterator = sheet.iterator();

	            if (rowIterator.hasNext()) rowIterator.next(); // Skip header

	            while (rowIterator.hasNext()) {
	                Row row = rowIterator.next();
	                if (row.getPhysicalNumberOfCells() == 0) continue;

	                SlowdownNormsValueDTO dto = new SlowdownNormsValueDTO();
	                try {
	                    dto.setNormParameterTypeDisplayName(getStringCellValue(row.getCell(0), dto));
	                    dto.setProductName(getStringCellValue(row.getCell(1), dto));
	                    dto.setUOM(getStringCellValue(row.getCell(2), dto));
	                    // col 3 is Sap Code — read-only display column, not imported
	                    dto.setFinancialYear(year);
	                    if (activeMonths.contains(4))  dto.setApril(getNumericCellValue(row.getCell(4), dto));
	                    if (activeMonths.contains(5))  dto.setMay(getNumericCellValue(row.getCell(5), dto));
	                    if (activeMonths.contains(6))  dto.setJune(getNumericCellValue(row.getCell(6), dto));
	                    if (activeMonths.contains(7))  dto.setJuly(getNumericCellValue(row.getCell(7), dto));
	                    if (activeMonths.contains(8))  dto.setAugust(getNumericCellValue(row.getCell(8), dto));
	                    if (activeMonths.contains(9))  dto.setSeptember(getNumericCellValue(row.getCell(9), dto));
	                    if (activeMonths.contains(10)) dto.setOctober(getNumericCellValue(row.getCell(10), dto));
	                    if (activeMonths.contains(11)) dto.setNovember(getNumericCellValue(row.getCell(11), dto));
	                    if (activeMonths.contains(12)) dto.setDecember(getNumericCellValue(row.getCell(12), dto));
	                    if (activeMonths.contains(1))  dto.setJanuary(getNumericCellValue(row.getCell(13), dto));
	                    if (activeMonths.contains(2))  dto.setFebruary(getNumericCellValue(row.getCell(14), dto));
	                    if (activeMonths.contains(3))  dto.setMarch(getNumericCellValue(row.getCell(15), dto));
	                    dto.setRemarks(getStringCellValue(row.getCell(16), dto));
	                    dto.setId(getStringCellValue(row.getCell(17), dto));
	                    dto.setPlantFkId(plantFKId.toString());
	                    dto.setSiteFkId(site.getId().toString());
	                    dto.setVerticalFkId(vertical.getId().toString());
	                    dto.setMaterialFkId(getStringCellValue(row.getCell(18), dto));

	                } catch (Exception e) {
	                    e.printStackTrace();
	                    dto.setErrDescription(e.getMessage());
	                    dto.setSaveStatus("Failed");
	                }
	                configList.add(dto);
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return configList;
	}

	public List<SlowdownNormsValueDTO> readSlowdownConsumptions(InputStream inputStream, UUID plantFKId, String year) {
	    List<SlowdownNormsValueDTO> configList = new ArrayList<>();
	    
	    Plants plant = plantsRepository.findById(plantFKId)
	        .orElseThrow(() -> new RuntimeException("Plant not found"));
	    Sites site = siteRepository.findById(plant.getSiteFkId())
	        .orElseThrow(() -> new RuntimeException("Site not found"));
	    Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
	        .orElseThrow(() -> new RuntimeException("Vertical not found"));

	    Set<Integer> activeMonths = new HashSet<>();

	    try (Workbook workbook = new XSSFWorkbook(inputStream)) {
	       
	        List<Integer> slowdown = getSlowdownMonths(plantFKId, "Slowdown", year, null);
	        
	        if (slowdown != null) activeMonths.addAll(slowdown);

	        Sheet sheet = workbook.getSheetAt(0);
	        if (sheet != null) {
	            Iterator<Row> rowIterator = sheet.iterator();

	            if (rowIterator.hasNext()) rowIterator.next(); // Skip header

	            while (rowIterator.hasNext()) {
	                Row row = rowIterator.next();
	                if (row.getPhysicalNumberOfCells() == 0) continue;

	                SlowdownNormsValueDTO dto = new SlowdownNormsValueDTO();
	                try {
	                    dto.setNormParameterTypeDisplayName(getStringCellValue(row.getCell(0), dto));
	                    dto.setProductName(getStringCellValue(row.getCell(1), dto));
	                    dto.setUOM(getStringCellValue(row.getCell(2), dto));
	                    dto.setFinancialYear(year);
	                    if (activeMonths.contains(4)) dto.setApril(getNumericCellValue(row.getCell(3), dto));
	                    if (activeMonths.contains(5)) dto.setMay(getNumericCellValue(row.getCell(4), dto));
	                    if (activeMonths.contains(6)) dto.setJune(getNumericCellValue(row.getCell(5), dto));
	                    if (activeMonths.contains(7)) dto.setJuly(getNumericCellValue(row.getCell(6), dto));
	                    if (activeMonths.contains(8)) dto.setAugust(getNumericCellValue(row.getCell(7), dto));
	                    if (activeMonths.contains(9)) dto.setSeptember(getNumericCellValue(row.getCell(8), dto));
	                    if (activeMonths.contains(10)) dto.setOctober(getNumericCellValue(row.getCell(9), dto));
	                    if (activeMonths.contains(11)) dto.setNovember(getNumericCellValue(row.getCell(10), dto));
	                    if (activeMonths.contains(12)) dto.setDecember(getNumericCellValue(row.getCell(11), dto));
	                    if (activeMonths.contains(1)) dto.setJanuary(getNumericCellValue(row.getCell(12), dto));
	                    if (activeMonths.contains(2)) dto.setFebruary(getNumericCellValue(row.getCell(13), dto));
	                    if (activeMonths.contains(3)) dto.setMarch(getNumericCellValue(row.getCell(14), dto));

	                    dto.setRemarks(getStringCellValue(row.getCell(15), dto));
	                    dto.setId(getStringCellValue(row.getCell(16), dto));
	                    dto.setPlantFkId(plantFKId.toString());
	                    dto.setSiteFkId(site.getId().toString());
	                    dto.setVerticalFkId(vertical.getId().toString());
	                    dto.setMaterialFkId(getStringCellValue(row.getCell(17), dto));
	         
	                } catch (Exception e) {
	                    e.printStackTrace();
	                    dto.setErrDescription(e.getMessage());
	                    dto.setSaveStatus("Failed");
	                }
	                configList.add(dto);
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return configList;
	}
	
	@Override
	public AOPMessageVM gradeWiseImportExcel(String year, UUID plantFKId, MultipartFile file, String maintenanceName) {
		
		try {

		


			Plants plant = plantsRepository.findById(plantFKId).get();
			List<SlowdownNormsValueDTO> data=null;
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			
				 data= readSlowdownConsumption(file.getInputStream(), plantFKId, year);
				 List<SlowdownNormsValueDTO> failedRecords = saveSlowdownNormsDataHIIR(data, maintenanceName);
			
			
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		if (failedRecords != null && failedRecords.size() > 0) {
			byte[] fileByteArray = exportSlowdownNormsAllGrades(
			         year,
			         plantFKId,
			         true,
			         failedRecords,
			         maintenanceName);

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
	
	public List<SlowdownNormsValueDTO> readSlowdownConsumption(InputStream inputStream, UUID plantFKId, String year) {
	    List<SlowdownNormsValueDTO> configList = new ArrayList<>();
	    Plants plant = plantsRepository.findById(plantFKId).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		
	    Map<String, String> gradeMap = getGradeNameId(year, plantFKId);
	    Set<Integer> activeMonths = new HashSet<>();
	    try (Workbook workbook = new XSSFWorkbook(inputStream)) {
	    	for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
	            Sheet sheet = workbook.getSheetAt(i);
	            if (sheet == null) {
	                continue;
	            }
	            String sheetName = sheet.getSheetName();
	            String gradeId = gradeMap.get(Utility.sanitizeSheetName(sheetName));
                List<Integer> slowdown = getSlowdownMonthsImport(plantFKId, "Slowdown",year);
                if (slowdown != null) activeMonths.addAll(slowdown);
	    	}
	        
	        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
	            Sheet sheet = workbook.getSheetAt(i);
	            if (sheet == null) {
	                continue;
	            }
	            String sheetName = sheet.getSheetName();
	            String gradeId = gradeMap.get(Utility.sanitizeSheetName(sheetName));
	            Iterator<Row> rowIterator = sheet.iterator();
	            if (rowIterator.hasNext()) {
	                rowIterator.next(); 
	            }
	            while (rowIterator.hasNext()) {
	                Row row = rowIterator.next();
	                if (row.getPhysicalNumberOfCells() == 0) {
	                    continue; 
	                }
	                SlowdownNormsValueDTO dto = new SlowdownNormsValueDTO();
	                try {
	                    dto.setNormParameterTypeDisplayName(getStringCellValue(row.getCell(0), dto));
	                    dto.setProductName(getStringCellValue(row.getCell(1), dto));
	                    dto.setUOM(getStringCellValue(row.getCell(2), dto));
	                    dto.setFinancialYear(year);
	                    dto.setPlantFkId(plantFKId.toString());
	                    if (activeMonths.contains(4)) dto.setApril(getNumericCellValue(row.getCell(3), dto));
	                    if (activeMonths.contains(5)) dto.setMay(getNumericCellValue(row.getCell(4), dto));
	                    if (activeMonths.contains(6)) dto.setJune(getNumericCellValue(row.getCell(5), dto));
	                    if (activeMonths.contains(7)) dto.setJuly(getNumericCellValue(row.getCell(6), dto));
	                    if (activeMonths.contains(8)) dto.setAugust(getNumericCellValue(row.getCell(7), dto));
	                    if (activeMonths.contains(9)) dto.setSeptember(getNumericCellValue(row.getCell(8), dto));
	                    if (activeMonths.contains(10)) dto.setOctober(getNumericCellValue(row.getCell(9), dto));
	                    if (activeMonths.contains(11)) dto.setNovember(getNumericCellValue(row.getCell(10), dto));
	                    if (activeMonths.contains(12)) dto.setDecember(getNumericCellValue(row.getCell(11), dto));
	                    if (activeMonths.contains(1)) dto.setJanuary(getNumericCellValue(row.getCell(12), dto));
	                    if (activeMonths.contains(2)) dto.setFebruary(getNumericCellValue(row.getCell(13), dto));
	                    if (activeMonths.contains(3)) dto.setMarch(getNumericCellValue(row.getCell(14), dto));
	                    dto.setRemarks(getStringCellValue(row.getCell(15), dto));
	                    dto.setId(getStringCellValue(row.getCell(16), dto)); 
	                    dto.setMaterialFkId(getStringCellValue(row.getCell(17), dto));
	                    dto.setSiteFkId(site.getId().toString());
	                    dto.setVerticalFkId(vertical.getId().toString());
	                    dto.setGradeId(gradeId);

	                } catch (Exception e) {
	                    e.printStackTrace();
	                    dto.setErrDescription(e.getMessage());
	                    dto.setSaveStatus("Failed");
	                }
	                configList.add(dto);
	            }
	        } 

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return configList;
	}
	
	private Map<String, String> getGradeNameIdMap(String year, UUID plantFKId) {
	    AOPMessageVM gradesVM = getUniqueGrades(year, plantFKId.toString());
	    List<Map<String, String>> gradeInfoList = extractGradeInfo(gradesVM); 

	    Map<String, String> nameIdMap = new HashMap<>();
	    for (Map<String, String> info : gradeInfoList) {
			String sanitizedName = Utility.sanitizeSheetName(info.get("name"));
	        nameIdMap.put(sanitizedName, info.get("gradeId"));
	    }
	    return nameIdMap;
	}
	
	private Map<String, String> getGradeNameId(String year, UUID plantFKId) {
	    AOPMessageVM gradesVM = getUniqueGrades(year, plantFKId.toString());
	    List<Map<String, String>> gradeInfoList = extractGradeInfo(gradesVM); 

	    Map<String, String> nameIdMap = new HashMap<>();
	    for (Map<String, String> info : gradeInfoList) {
	        String originalName = info.get("displayName"); 
	        if (originalName != null) {
	            String sanitizedName = Utility.sanitizeSheetName(originalName);
	            nameIdMap.put(sanitizedName, info.get("gradeId"));
	        }
	    }
	    return nameIdMap;
	}
	
	private static String getStringCellValue(Cell cell, SlowdownNormsValueDTO dto) {
	    try {
	        if (cell == null) return null;
	        
	        cell.setCellType(CellType.STRING);
	        String value = cell.getStringCellValue().trim();
	        return value.isEmpty() ? null : value;
	        
	    } catch (Exception e) {
	        dto.setSaveStatus("Failed");
	        dto.setErrDescription("Please enter correct values");
	        e.printStackTrace();
	    }
	    return null;
	}
	private static Double getNumericCellValue(Cell cell, SlowdownNormsValueDTO dto) {
	    if (cell == null || cell.getCellType() == CellType.BLANK) {
	        return null;
	    }

	    if (cell.getCellType() == CellType.NUMERIC) {
	        return cell.getNumericCellValue();
	    } 
	    
	    if (cell.getCellType() == CellType.STRING) {
	        String cellValue = cell.getStringCellValue().trim();
	        if (cellValue.isEmpty()) {
	            return null; 
	        }

	        try {
	            return Double.parseDouble(cellValue);
	        } catch (NumberFormatException e) {
	            dto.setSaveStatus("Failed");
	            dto.setErrDescription("Please enter numeric values");
	        }
	    }
	    
	    if (cell.getCellType() == CellType.FORMULA) {
	        try {
	            return cell.getNumericCellValue();
	        } catch (Exception e) {
	            return null;
	        }
	    }

	    return null;
	}

	
	@Override
	@Transactional
	public AOPMessageVM getCalculateSlowdownNorms(String year, String plantId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		String storedProcedure = vertical.getName() + "_" + site.getName() + "_SlowdownConsumptionCalculation";
		System.out.println("storedProcedure" + storedProcedure);
		int result = executeDynamicUpdateProcedure(storedProcedure, plantId, site.getId().toString(),
				vertical.getId().toString(), year);
		aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year,
				"slowdown-norms-configuration-calculate");
		List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("slowdown-norms-configuration-calculate");
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
		aopMessageVM.setMessage("SP Executed successfully");
		aopMessageVM.setData(result);
		return aopMessageVM;
	}
	
	@Override
	@Transactional
	public AOPMessageVM calculateSlowdownNorms(String year, String plantId) {
		try {
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_CalculateSlowdownNorms";
			int result = executeDynamicUpdateProcedure(storedProcedure, plantId, site.getId().toString(),
					vertical.getId().toString(), year);
			aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year,
					"slowdown-norms");
			
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("SP Executed successfully");
			aopMessageVM.setData(result);
			return aopMessageVM;
		}catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to execute stored procedure data", ex);
		}
		
	}
	
	public int executeDynamicUpdateProcedure(String procedureName,
            String plantId,
            String siteId,
            String verticalId,
            String finYear) {
			String callSql = "{call " + procedureName + "(?, ?, ?, ?)}";
			
			try (Connection connection = dataSource.getConnection();
			CallableStatement stmt = connection.prepareCall(callSql)) {
			
			stmt.setString(1, plantId);
			stmt.setString(2, siteId);
			stmt.setString(3, verticalId);
			stmt.setString(4, finYear);
			
			int rowsAffected = stmt.executeUpdate();
			
			if (!connection.getAutoCommit()) {
			connection.commit();
			}
			
			return rowsAffected;
			
			} catch (SQLException e) {
			// wrap and rethrow
			throw new RuntimeException("Failed to execute stored procedure: " + procedureName, e);
			}
		}
	
	@Override
	public AOPMessageVM getSlowdownNormsDynamicColumns(String auditYear, UUID plantId) {
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    List<Map<String, String>> listOfMaps = new ArrayList<>();

	    
	    {
	        Map<String, String> map = new HashMap<>();
	        map.put("field", "particulars");
	        map.put("title", "Particulars");
	        listOfMaps.add(map);
	    }

	    
	    List<String> months = Arrays.asList(
	        "January", "February", "March", "April", "May", "June",
	        "July", "August", "September", "October", "November", "December"
	    );
	    String monthPattern = String.join("|", months);
	    Pattern monthSuffixPattern = Pattern.compile("_(?i)(" + monthPattern + ")$");

	    try {
	    	Plants plant = plantsRepository.findById(plantId).orElseThrow();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String procedureName = vertical.getName()+"_GetSlowdownConsumption";
	        List<String> data = getColumnNames(procedureName, plantId.toString(), auditYear);

	        // 3. Process each dynamic column
	        for (String row : data) {
	            Map<String, String> map = new HashMap<>();
	            map.put("field", row);

	            String title = row;
	            Matcher m = monthSuffixPattern.matcher(row);
	            if (m.find()) {
	                title = row.replaceFirst("_(?=[^_]+$)", " (") + ")";
	            }
	            map.put("title", title);

	            listOfMaps.add(map);
	        }

	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid data format", e);
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch data", ex);
	    }

	    aopMessageVM.setCode(200);
	    aopMessageVM.setMessage("Data fetched successfully");
	    aopMessageVM.setData(listOfMaps);
	    return aopMessageVM;
	}

	public List<String> getColumnNames(String procedureName, String plantId, String aopYear) {
	    return entityManager.unwrap(Session.class).doReturningWork(connection -> {
	        List<String> columnNames = new ArrayList<>();

	        String sql = "EXEC " + procedureName + " @plantId = ?, @aopYear = ?";
	        try (PreparedStatement ps = connection.prepareStatement(sql)) {
	            ps.setString(1, plantId);
	            ps.setString(2, aopYear);

	            try (ResultSet rs = ps.executeQuery()) {
	                ResultSetMetaData rsMetaData = rs.getMetaData();
	                for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
	                    columnNames.add(rsMetaData.getColumnLabel(i));
	                }
	            }
	        }
	        return columnNames;
	    });
	}
	
	@Override
    public AOPMessageVM getSlowdownNormsConfigurationData(String plantId, String year) {
	 AOPMessageVM aopMessageVM = new AOPMessageVM();
	 Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		boolean meg = vertical.getName().equalsIgnoreCase("MEG");
		String procedureName = vertical.getName()+"_GetSlowdownConsumption";
        try {
            // Get the data
            List<Object[]> rows = getData(plantId, year,procedureName);

            
            
            List<String> columnNames = getColumnNames(procedureName, plantId, year);

            // Prepare the list of maps
            List<Map<String, Object>> resultList = new ArrayList<>();

            for (Object[] row : rows) {
                Map<String, Object> rowMap = new LinkedHashMap<>();
                for (int i = 0; i < columnNames.size(); i++) {
                    rowMap.put(columnNames.get(i), row[i]);
                }
                resultList.add(rowMap);
            }
            Map<String, Object> map = new HashMap<>();

			List<AopCalculation> aopCalculation = aopCalculationRepository
					.findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year, "slowdown-norms-configuration-calculate");

					if(meg) {
				List<Map<String, Object>> result =		fetchNormsTransactionsSlowdownConfiguration(plantId, year);
					
			map.put("SlowdownConfiguration", result);
			}
			map.put("resultList", resultList);
			map.put("aopCalculation", aopCalculation);
            aopMessageVM.setCode(200);
    		aopMessageVM.setData(map);
    		aopMessageVM.setMessage("Data updated successfully");
    		return aopMessageVM;
            
        } catch (Exception ex) {
            throw new RuntimeException("Failed to fetch data", ex);
        }
    }
	
	public List<Object[]> getData(String plantId, String aopYear,String procedureName) {
		
		try {
			
			String sql = "EXEC " + procedureName +
					" @plantId = :plantId, @aopYear = :aopYear";

			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<Map<String, Object>> fetchNormsTransactionsSlowdownConfiguration(String plantId, String year) { 

		String sql = "SELECT * FROM NormsTransactionsSlowdownConfiguration " +
             "WHERE Plant_FK_Id = ? " +
             "AND AOPYear = ?";

List<Map<String, Object>> result = jdbcTemplate.queryForList(
    sql,
    plantId,
    year
);
return result;
	}

	@Override
	public AOPMessageVM saveSlowdownNormsConfigurationData(String plantId, String year,
			List<NormAttributeTransactionsDTO> normAttributeTransactionsDTOList) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		
		List<SlowdownConsumption> slowdownConsumptionList = new ArrayList<>();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).orElseThrow();
		boolean meg = vertical.getName().equalsIgnoreCase("MEG");
		
		try {
			for(NormAttributeTransactionsDTO normAttributeTransactionsDTO:normAttributeTransactionsDTOList) {
				String rawDesc = normAttributeTransactionsDTO.getDescription();
				int month=extractMonthNumber(rawDesc);
				String cleanDesc = stripTrailingSuffix(rawDesc);
				UUID maintenanceId=plantMaintenanceTransactionRepository.findTransactionIdByDynamicParams("Slowdown",year,UUID.fromString(plantId),cleanDesc);
				if(maintenanceId==null) {
					throw new RuntimeException("No Maintenance Id found with "+normAttributeTransactionsDTO.getDescription());
				}

				SlowdownConsumption  slowdownConsumption= slowdownConsumptionRepository.findByParameterFKIdAndAuditYear(UUID.fromString(plantId),normAttributeTransactionsDTO.getNormParameterFKId(),year,maintenanceId,month);
				if(slowdownConsumption!=null) {

				Double	oldVal=slowdownConsumption.getParameterValue();
				Double	newVal=Double.parseDouble(normAttributeTransactionsDTO.getAttributeValue());

				if (meg && newVal != null && !Objects.equals(oldVal, newVal)) { 
					slowdownConsumptionUpdateTracker(normAttributeTransactionsDTO, oldVal, newVal, year, plantId);
				}
					slowdownConsumption.setParameterValue(Double.parseDouble(normAttributeTransactionsDTO.getAttributeValue()));
					slowdownConsumption.setUpdatedOn(new Date());
					slowdownConsumption.setUpdatedBy(Utility.getUserName());
					slowdownConsumptionList.add(slowdownConsumptionRepository.save(slowdownConsumption));
				}else {
					slowdownConsumption = new SlowdownConsumption();
					slowdownConsumption.setParameterValue(Double.parseDouble(normAttributeTransactionsDTO.getAttributeValue()));
					slowdownConsumption.setAopYear(year);
					slowdownConsumption.setCreatedOn(new Date());
					slowdownConsumption.setPlantMaintenanceFkId(maintenanceId);
					slowdownConsumption.setAopMonth(month);
					slowdownConsumption.setNormParameterFkId(normAttributeTransactionsDTO.getNormParameterFKId());
					slowdownConsumption.setCreatedBy(Utility.getUserName());
					slowdownConsumption.setPlantFkId(UUID.fromString(plantId));
					slowdownConsumptionList.add(slowdownConsumptionRepository.save(slowdownConsumption));
				}

				Map<String, Object> map = new HashMap<>();

				List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("slowdown-norms");
				for (ScreenMapping screenMapping : screenMappingList) {
					AopCalculation aopCalculation = new AopCalculation();
					aopCalculation.setAopYear(year);
					aopCalculation.setIsChanged(true);
					aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
					aopCalculation.setPlantId(UUID.fromString(plantId));
					aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
					aopCalculationRepository.save(aopCalculation);
				}

			}
		}catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to save/update data", ex);
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setData(slowdownConsumptionList);
		aopMessageVM.setMessage("Data updated successfully");
		return aopMessageVM;
	}

public void slowdownConsumptionUpdateTracker(NormAttributeTransactionsDTO dto, Double oldval, Double newval, String aopYear, String plantFkId) {

	String rawDesc = dto.getDescription();
				int month=extractMonthNumber(rawDesc);
				//String cleanDesc = stripTrailingSuffix(rawDesc);

				jdbcTemplate.update(
					"INSERT INTO NormsTransactionsSlowdownConfiguration " +
					"(Id, Plant_FK_Id, AOPYear, NormParameter_FK_Id, ColumnName, AttributeValue, Version) " +
					"VALUES (NEWID(), ?, ?, ?, ?, ?,?)",
					plantFkId,
					aopYear,
					dto.getNormParameterFKId(),   
					rawDesc,
					newval,
					1
					
				);



}

	private String stripTrailingSuffix(String description) {
	    return description.replaceAll("_[^_]*$", "");
	}
	
	public static int extractMonthNumber(String description) {
        
        int u = description.lastIndexOf('_');
        if (u < 0 || u == description.length() - 1) {
            throw new IllegalArgumentException("No month suffix found.");
        }
        String monthName = description.substring(u + 1);
        try {
            
            Month m = Month.valueOf(monthName.toUpperCase());
            return m.getValue(); 
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unknown month: " + monthName, ex);
        }
    }
	
	@Override
	public AOPMessageVM getUniqueGrades(String year, String plantId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			// Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Boolean elastomer = vertical.getName().equalsIgnoreCase("ELASTOMER")  && site.getName().equalsIgnoreCase("JMD") && plant.getName().equalsIgnoreCase("HIIR");
			String viewName="vwScrn"+vertical.getName()+"SlowdownNorms";
			if(elastomer) {
				viewName="vwScrn"+vertical.getName()+site.getName()+"SlowdownNorms";
			}
			List<String> grades=fetchUniqueGradeFkIds(viewName,UUID.fromString(plantId),year);
			List<Map<String, String>> listOfMaps = new ArrayList<>();

			for (String grade : grades) {
			    String productName = normParametersRepository.findNormParameterIdByGrade(UUID.fromString(grade));
			    Map<String, String> singleEntryMap = new HashMap<>();
			    singleEntryMap.put("gradeId", grade);
			    singleEntryMap.put("displayName", productName);
			    listOfMaps.add(singleEntryMap);
			}
			
			aopMessageVM.setCode(200);
			aopMessageVM.setData(listOfMaps);
			aopMessageVM.setMessage("Data fetched successfully");
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		// TODO Auto-generated method stub
		return aopMessageVM;
	}
	
	 public List<String> fetchUniqueGradeFkIds(String viewName, UUID plantFkId, String financialYear) {
	        String sql = "SELECT DISTINCT Grade_Fk_Id FROM " + viewName +
	                     " WHERE Plant_Fk_Id = :plantFkId AND FinancialYear = :financialYear";

	        Query query = entityManager.createNativeQuery(sql);
	        query.setParameter("plantFkId", plantFkId);
	        query.setParameter("financialYear", financialYear);

	        @SuppressWarnings("unchecked")
	        List<String> results = query.getResultList();
	        return results;
	    }

	// --- Export: Slowdown Norms Configuration --------------------------------

	@Override
	@SuppressWarnings("unchecked")
	public byte[] exportSlowdownNormsConfigurationData(String plantId, String year) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String procedureName = vertical.getName() + "_GetSlowdownConsumption";

			// Column definitions (field ? title) from the dynamic-columns API
			AOPMessageVM columnsVM = getSlowdownNormsDynamicColumns(year, UUID.fromString(plantId));
			List<Map<String, String>> columnDefs = (List<Map<String, String>>) columnsVM.getData();

			// Raw SP data and column name list
			List<String> spColumnNames = getColumnNames(procedureName, plantId, year);
			List<Object[]> spRows = getData(plantId, year, procedureName);

			// SP column-name ? index map for fast lookup
			Map<String, Integer> colIndexMap = new LinkedHashMap<>();
			for (int i = 0; i < spColumnNames.size(); i++) {
				colIndexMap.put(spColumnNames.get(i), i);
			}

			// Fixed/metadata fields that are NOT dynamic slowdown columns
			Set<String> fixedFields = new HashSet<>(Arrays.asList(
					"particulars", "NormTypeName", "NormParameter_FK_Id",
					"DisplayName", "UOM", "IsEditable"));

			// Dynamic slowdown columns in API-returned order
			List<Map<String, String>> dynamicCols = columnDefs.stream()
					.filter(col -> !fixedFields.contains(col.get("field")))
					.collect(Collectors.toList());

		// -- Build workbook ----------------------------------------------
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Slowdown Norms");

		// Styles
		CellStyle hiddenStyle = workbook.createCellStyle();
		hiddenStyle.setLocked(true);
		hiddenStyle.setBorderTop(BorderStyle.THIN);
		hiddenStyle.setBorderBottom(BorderStyle.THIN);
		hiddenStyle.setBorderLeft(BorderStyle.THIN);
		hiddenStyle.setBorderRight(BorderStyle.THIN);

		CellStyle lockedStyle = workbook.createCellStyle();
		lockedStyle.setLocked(true);
		lockedStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		lockedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		lockedStyle.setBorderTop(BorderStyle.THIN);
		lockedStyle.setBorderBottom(BorderStyle.THIN);
		lockedStyle.setBorderLeft(BorderStyle.THIN);
		lockedStyle.setBorderRight(BorderStyle.THIN);

		CellStyle unlockedStyle = workbook.createCellStyle();
		unlockedStyle.setLocked(false);
		unlockedStyle.setBorderTop(BorderStyle.THIN);
		unlockedStyle.setBorderBottom(BorderStyle.THIN);
		unlockedStyle.setBorderLeft(BorderStyle.THIN);
		unlockedStyle.setBorderRight(BorderStyle.THIN);

		// Wrap-text variants for the Remark column
		CellStyle wrapLockedStyle = workbook.createCellStyle();
		wrapLockedStyle.setLocked(true);
		wrapLockedStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		wrapLockedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		wrapLockedStyle.setWrapText(true);
		wrapLockedStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.TOP);
		wrapLockedStyle.setBorderTop(BorderStyle.THIN);
		wrapLockedStyle.setBorderBottom(BorderStyle.THIN);
		wrapLockedStyle.setBorderLeft(BorderStyle.THIN);
		wrapLockedStyle.setBorderRight(BorderStyle.THIN);

		CellStyle wrapUnlockedStyle = workbook.createCellStyle();
		wrapUnlockedStyle.setLocked(false);
		wrapUnlockedStyle.setWrapText(true);
		wrapUnlockedStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.TOP);
		wrapUnlockedStyle.setBorderTop(BorderStyle.THIN);
		wrapUnlockedStyle.setBorderBottom(BorderStyle.THIN);
		wrapUnlockedStyle.setBorderLeft(BorderStyle.THIN);
		wrapUnlockedStyle.setBorderRight(BorderStyle.THIN);

		// Right-aligned variants for dynamic columns (cols 5+)
		CellStyle lockedRightStyle = workbook.createCellStyle();
		lockedRightStyle.cloneStyleFrom(lockedStyle);
		lockedRightStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.RIGHT);

		CellStyle unlockedRightStyle = workbook.createCellStyle();
		unlockedRightStyle.cloneStyleFrom(unlockedStyle);
		unlockedRightStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.RIGHT);

		CellStyle wrapLockedRightStyle = workbook.createCellStyle();
		wrapLockedRightStyle.cloneStyleFrom(wrapLockedStyle);
		wrapLockedRightStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.RIGHT);

		CellStyle wrapUnlockedRightStyle = workbook.createCellStyle();
		wrapUnlockedRightStyle.cloneStyleFrom(wrapUnlockedStyle);
		wrapUnlockedRightStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.RIGHT);

		CellStyle headerStyle = Utility.createBoldBorderedStyle(workbook);

		// -- Identify the Remark column among dynamic columns --
		// (matched by field or title containing "remark", case-insensitive)
		int remarkDynIdx = -1;
		for (int i = 0; i < dynamicCols.size(); i++) {
			String field = dynamicCols.get(i).get("field");
			String title = dynamicCols.get(i).get("title");
			if ((field != null && field.toLowerCase(Locale.ROOT).contains("remark"))
					|| (title != null && title.toLowerCase(Locale.ROOT).contains("remark"))) {
				remarkDynIdx = i;
				break;
			}
		}
		// Absolute sheet-column index for the Remark column (-1 if absent)
		// Layout: Col 0 (hidden) NormParameter_FK_Id | Col 1 (hidden) IsEditable
		//         Col 2 Particulars | Col 3 Type | Col 4 UOM | Col 5+ dynamic slowdown columns
		final int remarkSheetCol = remarkDynIdx >= 0 ? 5 + remarkDynIdx : -1;

		// -- Header row --
		Row headerRow = sheet.createRow(0);

		Cell h0 = headerRow.createCell(0);
		h0.setCellValue("NormParameter_FK_Id");
		h0.setCellStyle(headerStyle);

		Cell h1 = headerRow.createCell(1);
		h1.setCellValue("IsEditable");
		h1.setCellStyle(headerStyle);

		Cell h2 = headerRow.createCell(2);
		h2.setCellValue("Particulars");
		h2.setCellStyle(headerStyle);

		Cell h3 = headerRow.createCell(3);
		h3.setCellValue("Type");
		h3.setCellStyle(headerStyle);

		Cell h4 = headerRow.createCell(4);
		h4.setCellValue("UOM");
		h4.setCellStyle(headerStyle);

		for (int i = 0; i < dynamicCols.size(); i++) {
			Cell hCell = headerRow.createCell(5 + i);
			hCell.setCellValue(dynamicCols.get(i).get("title"));
			hCell.setCellStyle(headerStyle);
		}

		// -- Data rows --
		Integer isEditableIdx  = colIndexMap.get("IsEditable");
		Integer normParamIdx   = colIndexMap.get("NormParameter_FK_Id");
		Integer displayNameIdx = colIndexMap.get("DisplayName");
		Integer normTypeNameIdx = colIndexMap.get("NormTypeName");
		Integer uomIdx         = colIndexMap.get("UOM");

		// Track max character widths per column for auto-sizing (cols 2+)
		int totalCols = 5 + dynamicCols.size();
		int[] maxColChars = new int[totalCols];
		// Seed with header text lengths
		maxColChars[2] = "Particulars".length();
		maxColChars[3] = "Type".length();
		maxColChars[4] = "UOM".length();
		for (int i = 0; i < dynamicCols.size(); i++) {
			String title = dynamicCols.get(i).get("title");
			maxColChars[5 + i] = title != null ? title.length() : 0;
		}

		int rowIdx = 1;
		for (Object[] spRow : spRows) {
			boolean isEditable = resolveIsEditable(spRow, isEditableIdx);

			Row dataRow = sheet.createRow(rowIdx++);

			// Col 0 ? NormParameter_FK_Id (hidden)
			Cell c0 = dataRow.createCell(0);
			c0.setCellValue(spRow[normParamIdx] != null ? spRow[normParamIdx].toString() : "");
			c0.setCellStyle(hiddenStyle);

			// Col 1 ? IsEditable (hidden)
			Cell c1 = dataRow.createCell(1);
			c1.setCellValue(String.valueOf(isEditable));
			c1.setCellStyle(hiddenStyle);

			// Col 2 ? Particulars (DisplayName ? always read-only)
			String particularsVal = spRow[displayNameIdx] != null ? spRow[displayNameIdx].toString() : "";
			Cell c2 = dataRow.createCell(2);
			c2.setCellValue(particularsVal);
			c2.setCellStyle(lockedStyle);
			maxColChars[2] = Math.max(maxColChars[2], particularsVal.length());

			// Col 3 ? Type (NormTypeName ? always read-only)
			String typeVal = normTypeNameIdx != null && spRow[normTypeNameIdx] != null ? spRow[normTypeNameIdx].toString() : "";
			Cell c3 = dataRow.createCell(3);
			c3.setCellValue(typeVal);
			c3.setCellStyle(lockedStyle);
			maxColChars[3] = Math.max(maxColChars[3], typeVal.length());

			// Col 4 ? UOM (always read-only)
			String uomVal = uomIdx != null && spRow[uomIdx] != null ? spRow[uomIdx].toString() : "";
			Cell c4 = dataRow.createCell(4);
			c4.setCellValue(uomVal);
			c4.setCellStyle(lockedStyle);
			maxColChars[4] = Math.max(maxColChars[4], uomVal.length());

		// Col 5+ ? dynamic slowdown columns
		boolean rowHasWrappedContent = false;
		for (int i = 0; i < dynamicCols.size(); i++) {
			String field    = dynamicCols.get(i).get("field");
			int    sheetCol = 5 + i;
			boolean isRemark = (sheetCol == remarkSheetCol);

			Cell cell = dataRow.createCell(sheetCol);
			Integer spColIdx = colIndexMap.get(field);
			String cellText = "";
			if (spColIdx != null && spRow[spColIdx] != null) {
				Object val = spRow[spColIdx];
				if (!isRemark && val instanceof Number) {
					cell.setCellValue(((Number) val).doubleValue());
					cellText = val.toString();
				} else {
					cellText = val.toString();
					cell.setCellValue(cellText);
				}
			} else {
				cell.setCellValue("");
			}

			if (isRemark) {
				cell.setCellStyle(isEditable ? wrapUnlockedRightStyle : wrapLockedRightStyle);
				if (!cellText.isEmpty()) rowHasWrappedContent = true;
			} else {
				cell.setCellStyle(isEditable ? unlockedRightStyle : lockedRightStyle);
				maxColChars[sheetCol] = Math.max(maxColChars[sheetCol], cellText.length());
			}
		}

			// Let Excel auto-fit the row height when wrapped content is present
			if (rowHasWrappedContent) {
				dataRow.setHeight((short) -1);
			}
		}

		// -- Column widths --
		// Hidden metadata columns
		sheet.setColumnHidden(0, true);
		sheet.setColumnHidden(1, true);

		// Remark column: fixed generous width (? 60 characters)
		if (remarkSheetCol >= 0) {
			sheet.setColumnWidth(remarkSheetCol, 60 * 256);
		}

		// All other visible columns: content-driven width with a small padding buffer
		for (int col = 2; col < totalCols; col++) {
			if (col == remarkSheetCol) continue;
			// 256 units per character; add ~4-char padding; cap at Excel's max (255 chars)
			int width = Math.min((maxColChars[col] + 4) * 256, 255 * 256);
			// Enforce a comfortable minimum of 10 characters
			width = Math.max(width, 10 * 256);
			sheet.setColumnWidth(col, width);
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		workbook.write(out);
		workbook.close();
		return out.toByteArray();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// --- Import: Slowdown Norms Configuration --------------------------------

	@Override
	@SuppressWarnings("unchecked")
	public AOPMessageVM importSlowdownNormsConfigurationData(String plantId, String year, MultipartFile file) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			// Rebuild dynamic column list to know which Excel columns map to which fields
			AOPMessageVM columnsVM = getSlowdownNormsDynamicColumns(year, UUID.fromString(plantId));
			List<Map<String, String>> columnDefs = (List<Map<String, String>>) columnsVM.getData();

			Set<String> fixedFields = new HashSet<>(Arrays.asList(
					"particulars", "NormTypeName", "NormParameter_FK_Id",
					"DisplayName", "UOM", "IsEditable"));

			// Dynamic slowdown field names in the same order as the exported sheet (col 4+)
			List<String> dynamicFields = columnDefs.stream()
					.filter(col -> !fixedFields.contains(col.get("field")))
					.map(col -> col.get("field"))
					.collect(Collectors.toList());

			List<NormAttributeTransactionsDTO> dtoList = new ArrayList<>();

			try (InputStream is = file.getInputStream();
				 Workbook workbook = new XSSFWorkbook(is)) {

				Sheet sheet = workbook.getSheetAt(0);

				// Row 0 is the header ? start from row 1
				for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
					Row row = sheet.getRow(rowNum);
					if (row == null) continue;

					// Col 0 ? NormParameter_FK_Id (hidden)
					String normParamIdStr = getCellStringValue(row.getCell(0));
					if (normParamIdStr == null || normParamIdStr.trim().isEmpty()) continue;

					UUID normParameterId;
					try {
						normParameterId = UUID.fromString(normParamIdStr.trim());
					} catch (IllegalArgumentException ex) {
						continue; // skip rows with invalid UUID
					}

					// Col 1 ? IsEditable (hidden) ? skip non-editable rows
					String isEditableStr = getCellStringValue(row.getCell(1));
					boolean isEditable = "true".equalsIgnoreCase(isEditableStr != null ? isEditableStr.trim() : "");
					if (!isEditable) continue;

				// Col 5+ ? dynamic slowdown column values (Col 3 = Type is read-only, Col 4 = UOM)
				for (int i = 0; i < dynamicFields.size(); i++) {
					Cell cell = row.getCell(5 + i);
						if (cell == null) continue;
						String value = getCellStringValue(cell);
						if (value == null || value.trim().isEmpty()) continue;

						NormAttributeTransactionsDTO dto = new NormAttributeTransactionsDTO();
						dto.setNormParameterFKId(normParameterId);
						dto.setDescription(dynamicFields.get(i));
						dto.setAttributeValue(value.trim());
						dtoList.add(dto);
					}
				}
			}

			if (!dtoList.isEmpty()) {
				return saveSlowdownNormsConfigurationData(plantId, year, dtoList);
			}

			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("No editable data found to import");
			return aopMessageVM;

		} catch (Exception e) {
			e.printStackTrace();
			aopMessageVM.setCode(500);
			aopMessageVM.setMessage("Import failed: " + e.getMessage());
			return aopMessageVM;
		}
	}

	// --- Private helpers -----------------------------------------------------

	private boolean resolveIsEditable(Object[] spRow, Integer isEditableIdx) {
		if (isEditableIdx == null || spRow[isEditableIdx] == null) return true;
		Object val = spRow[isEditableIdx];
		if (val instanceof Boolean) return (Boolean) val;
		if (val instanceof Number) return ((Number) val).intValue() != 0;
		String s = val.toString().trim();
		return "true".equalsIgnoreCase(s) || "1".equals(s);
	}

	private String getCellStringValue(Cell cell) {
		if (cell == null) return "";
		switch (cell.getCellType()) {
			case STRING:  return cell.getStringCellValue();
			case NUMERIC: return String.valueOf(cell.getNumericCellValue());
			case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
			case FORMULA:
				try { return String.valueOf(cell.getNumericCellValue()); }
				catch (Exception e) { return cell.getStringCellValue(); }
			default: return "";
		}
	}


}
