package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Connection;
import jakarta.persistence.Query;
import javax.sql.DataSource;

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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.MCUNormsValueDTO;
import com.wks.caseengine.dto.ShutdownConsumptionDTO;
import com.wks.caseengine.dto.ShutdownNormsValueDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.GradeShutdownNormsValue;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.ShutdownNormsValue;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.GradeShutdownNormsValueRepository;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.ShutdownNormsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

@Service
public class ShutdownNormsServiceImpl implements ShutdownNormsService {

	@Autowired
	private ShutdownNormsRepository shutdownNormsRepository;

	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	PlantsRepository plantsRepository;
	@Autowired
	SiteRepository siteRepository;
	@Autowired
	VerticalsRepository verticalRepository;
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;
	
	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	@Autowired
	private NormParametersRepository normParametersRepository;
	
	@Autowired
	private GradeShutdownNormsValueRepository gradeShutdownNormsValueRepository;
	
	private DataSource dataSource;

	// Inject or set your DataSource (e.g., via constructor or setter)
	public ShutdownNormsServiceImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public AOPMessageVM getShutdownNormsData(String year, String plantId,String gradeId) {
		try {
			List<Object[]> objList = null;
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			// Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			if (vertical.getName().equalsIgnoreCase("VCM") || vertical.getName().equalsIgnoreCase("AROMATICS") || vertical.getName().equalsIgnoreCase("ELASTOMER") || vertical.getName().equalsIgnoreCase("MEG") || vertical.getName().equalsIgnoreCase("PTA")) {
				//objList = getShutdownNormsMEG(year, plant.getId(), "vwScrnShutdownNorms");
				// view converted to sp
				String storedProcedure = verticalName + "_" + site.getName() + "_GetShutdownnorms";
				objList = getShutdownConsumptionData( plantId,year, storedProcedure);
			}else if (  vertical.getName().equalsIgnoreCase("PVC") ) {
				String viewName="vwScrn"+vertical.getName()+"ShutdownNorms";
				objList = getShutdownNormsMEG(year, plant.getId(), viewName);
			}else if(vertical.getName().equalsIgnoreCase("CRACKER")) {
				List<Object[]> obj=getShutdownConsumptionData(plantId,year);
				return getData(obj, plantId, year);
			}
			else {
				String storedProcedure = verticalName + "_" + site.getName() + "_GetShutdownnorms";
				objList = getShutdownConsumptionData( plantId,year, storedProcedure);
				return getShutdownGradeData(objList, plantId, year,gradeId);
			} 
			// List<Object[]> objList = shutdownNormsRepository.findByYearAndPlantFkId(year,
			// UUID.fromString(plantId));
			System.out.println("obj.size(): " + objList.size());
			List<ShutdownNormsValueDTO> shutdownNormsValueDTOList = new ArrayList<>();
			for (Object[] row : objList) {
				ShutdownNormsValueDTO shutdownNormsValueDTO = new ShutdownNormsValueDTO();
				shutdownNormsValueDTO.setId(row[0] != null ? row[0].toString() : null);
				shutdownNormsValueDTO.setSiteFkId(row[1] != null ? row[1].toString() : null);
				shutdownNormsValueDTO.setPlantFkId(row[2] != null ? row[2].toString() : null);
				shutdownNormsValueDTO.setVerticalFkId(row[3] != null ? row[3].toString() : null);
				shutdownNormsValueDTO.setMaterialFkId(row[4] != null ? row[4].toString() : null);
				shutdownNormsValueDTO.setApril(row[5] != null ? Double.parseDouble(row[5].toString()) : 0.0);
				shutdownNormsValueDTO.setMay(row[6] != null ? Double.parseDouble(row[6].toString()) : 0.0);
				shutdownNormsValueDTO.setJune(row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0);
				shutdownNormsValueDTO.setJuly(row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0);
				shutdownNormsValueDTO.setAugust(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0);
				shutdownNormsValueDTO.setSeptember(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0);
				shutdownNormsValueDTO.setOctober(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0);
				shutdownNormsValueDTO.setNovember(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0);
				shutdownNormsValueDTO.setDecember(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0);
				shutdownNormsValueDTO.setJanuary(row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0);
				shutdownNormsValueDTO.setFebruary(row[15] != null ? Double.parseDouble(row[15].toString()) : 0.0);
				shutdownNormsValueDTO.setMarch(row[16] != null ? Double.parseDouble(row[16].toString()) : 0.0);

				shutdownNormsValueDTO.setFinancialYear(row[17] != null ? row[17].toString() : null);
				shutdownNormsValueDTO.setRemarks(row[18] != null ? row[18].toString() : " ");
				shutdownNormsValueDTO.setCreatedOn(row[19] != null ? (Date) row[19] : null);
				shutdownNormsValueDTO.setModifiedOn(row[20] != null ? (Date) row[20] : null);
				shutdownNormsValueDTO.setMcuVersion(row[21] != null ? row[21].toString() : null);
				shutdownNormsValueDTO.setUpdatedBy(row[22] != null ? row[22].toString() : null);
				shutdownNormsValueDTO.setNormParameterTypeId(row[23] != null ? row[23].toString() : null);
				shutdownNormsValueDTO.setNormParameterTypeName(row[24] != null ? row[24].toString() : null);
				shutdownNormsValueDTO.setNormParameterTypeDisplayName(row[25] != null ? row[25].toString() : null);
				shutdownNormsValueDTO.setUOM(row[28] != null ? row[28].toString() : null);
				shutdownNormsValueDTO.setIsEditable(row[29] != null ? Boolean.valueOf(row[29].toString()) : null);
				shutdownNormsValueDTO.setProductName(row[30] != null ? row[30].toString() : null);
				shutdownNormsValueDTOList.add(shutdownNormsValueDTO);
			}
			Map<String, Object> map = new HashMap<>();

			List<AopCalculation> aopCalculation = aopCalculationRepository
					.findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year, "shutdown-norms");
			map.put("mcuNormsValueDTOList", shutdownNormsValueDTOList);
			map.put("aopCalculation", aopCalculation);
			
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(map);
			aopMessageVM.setMessage("Data fetched successfully");

			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	public byte[] exportShutdownNorms(String year, UUID plantFKId, boolean isAfterSave, List<ShutdownNormsValueDTO> dtoList) {
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
				List<ShutdownNormsValueDTO> currentDtoList = new ArrayList<>();
				List<Boolean> isEditable = new ArrayList<>();
				if(!isAfterSave){
					 aopMessageVM = getShutdownNormsData(year, plantFKId.toString(), currentGradeId);
				}
				if (aopMessageVM!=null && aopMessageVM.getData() != null) {
					
					Map<String, Object> responseMap = (Map<String, Object>) aopMessageVM.getData();
					currentDtoList = (List<ShutdownNormsValueDTO>) responseMap.get("mcuNormsValueDTOList");
				} else if (isAfterSave) {
					currentDtoList = dtoList.stream()
				            .filter(dto -> currentGradeId.equals(dto.getGradeFkId()))
				            .collect(Collectors.toList()); 
				} else {
                    continue; 
                }
                
				Sheet sheet = workbook.createSheet(sheetName);
				int currentRow = 0;

				List<List<Object>> rows = new ArrayList<>();
				for (ShutdownNormsValueDTO dto : currentDtoList) {
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
	
	@Override
	public AOPMessageVM importExcel(String year, UUID plantFKId, String gradeId, MultipartFile file) {
		// TODO Auto-generated method stub
		try {
			Plants plant = plantsRepository.findById(plantFKId).get();
			List<ShutdownNormsValueDTO> data=null;
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			if(vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP") || vertical.getName().equalsIgnoreCase("PET")) {
				 data= readShutdownConsumption(file.getInputStream(), plantFKId, year);
			}
			else {
				data = readShutdownConsumption(file.getInputStream(), plantFKId, year);
			}
			
			Map<String,Object> records = savePPShutdownNormsData(data);
			@SuppressWarnings("unchecked")
			List<ShutdownNormsValueDTO> failedRecords = (List<ShutdownNormsValueDTO>) records.get("data");
			

			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (failedRecords != null && failedRecords.size() > 0) {
				byte[] fileByteArray =null;
				if(vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP") || vertical.getName().equalsIgnoreCase("PET")) {
					 fileByteArray = exportShutdownNorms(year, plantFKId, true, failedRecords);
				}
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
	
	public List<ShutdownNormsValueDTO> readShutdownConsumption(InputStream inputStream, UUID plantFKId, String year) {
	    List<ShutdownNormsValueDTO> configList = new ArrayList<>();
	    Map<String, String> gradeMap = getGradeNameIdMap(year, plantFKId);
	    try (Workbook workbook = new XSSFWorkbook(inputStream)) {
	        
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
	                
	                ShutdownNormsValueDTO dto = new ShutdownNormsValueDTO();
	                try {
	                    dto.setNormParameterTypeDisplayName(getStringCellValue(row.getCell(0), dto));
	                    dto.setProductName(getStringCellValue(row.getCell(1), dto));
	                    dto.setUOM(getStringCellValue(row.getCell(2), dto));

	                    dto.setFinancialYear(year);
	                    dto.setPlantFkId(plantFKId.toString());
	                    dto.setApril(getNumericCellValue(row.getCell(3), dto));
	                    dto.setMay(getNumericCellValue(row.getCell(4), dto));
	                    dto.setJune(getNumericCellValue(row.getCell(5), dto));
	                    dto.setJuly(getNumericCellValue(row.getCell(6), dto));
	                    dto.setAugust(getNumericCellValue(row.getCell(7), dto));
	                    dto.setSeptember(getNumericCellValue(row.getCell(8), dto));
	                    dto.setOctober(getNumericCellValue(row.getCell(9), dto));
	                    dto.setNovember(getNumericCellValue(row.getCell(10), dto));
	                    dto.setDecember(getNumericCellValue(row.getCell(11), dto));
	                    dto.setJanuary(getNumericCellValue(row.getCell(12), dto));
	                    dto.setFebruary(getNumericCellValue(row.getCell(13), dto));
	                    dto.setMarch(getNumericCellValue(row.getCell(14), dto));
	                    dto.setRemarks(getStringCellValue(row.getCell(15), dto));
	                    dto.setId(getStringCellValue(row.getCell(16), dto)); 
	                    dto.setGradeFkId(gradeId);

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
	    List<Map<String, String>> gradeInfoList = extractGradeInfo(gradesVM); // The method you modified earlier

	    Map<String, String> nameIdMap = new HashMap<>();
	    for (Map<String, String> info : gradeInfoList) {
	        String sanitizedName = Utility.sanitizeSheetName(info.get("displayName"));
	        nameIdMap.put(sanitizedName, info.get("gradeId"));
	    }
	    return nameIdMap;
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
	
	private static String getStringCellValue(Cell cell, ShutdownNormsValueDTO dto) {
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
	private static Double getNumericCellValue(Cell cell, ShutdownNormsValueDTO dto) {
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
	public AOPMessageVM saveShutDownNorms(String plantId,List<ShutdownNormsValueDTO> shutdownNormsValueDTOList) {
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		
		Map<String,Object> map=null;
		// Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		if(vertical.getName().equalsIgnoreCase("PP") || vertical.getName().equalsIgnoreCase("PE")) {
			 map=	savePPShutdownNormsData(shutdownNormsValueDTOList);
		}else {
			 map= saveShutdownNormsData(shutdownNormsValueDTOList);
		}
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		aopMessageVM.setCode(200);
		aopMessageVM.setData(map);
		aopMessageVM.setMessage("data updated successfully");
		return aopMessageVM;
	}

	
	public Map<String,Object> saveShutdownNormsData(List<ShutdownNormsValueDTO> shutdownNormsValueDTOList) {
		String year=null;
		UUID plantId=null;
		List<ShutdownNormsValue> shutdownNormsValueList = new ArrayList<>();
		try {
			for (ShutdownNormsValueDTO shutdownNormsValueDTO : shutdownNormsValueDTOList) {
				year=shutdownNormsValueDTO.getFinancialYear();
				plantId=UUID.fromString(shutdownNormsValueDTO.getPlantFkId());
				ShutdownNormsValue shutdownNormsValue = null;
				if (shutdownNormsValueDTO.getId() != null && !shutdownNormsValueDTO.getId().isEmpty()) {
					Optional<ShutdownNormsValue> shutdownNormsValueOpt =shutdownNormsRepository.findById(UUID.fromString(shutdownNormsValueDTO.getId()));
					if(shutdownNormsValueOpt.isPresent()) {
						shutdownNormsValue=shutdownNormsValueOpt.get();
						shutdownNormsValue.setModifiedOn(new Date());
					}
					
				} else {
					shutdownNormsValue = new ShutdownNormsValue();
					UUID siteId = null;
					UUID verticalId = null;
					UUID materialId = null;
					if (shutdownNormsValueDTO.getSiteFkId() != null) {
						siteId = UUID.fromString(shutdownNormsValueDTO.getSiteFkId());
					}
					if (shutdownNormsValueDTO.getPlantFkId() != null) {
						plantId = UUID.fromString(shutdownNormsValueDTO.getPlantFkId());
					}
					if (shutdownNormsValueDTO.getVerticalFkId() != null) {
						verticalId = UUID.fromString(shutdownNormsValueDTO.getVerticalFkId());
					}
					if (shutdownNormsValueDTO.getMaterialFkId() != null) {
						materialId = UUID.fromString(shutdownNormsValueDTO.getMaterialFkId());
					}
					UUID Id = shutdownNormsRepository.findIdByFilters(plantId, siteId, verticalId, materialId,
							shutdownNormsValueDTO.getFinancialYear());
					if (Id != null) {
						shutdownNormsValue.setId(Id);
					}

					shutdownNormsValue.setCreatedOn(new Date());
				}
				shutdownNormsValue.setApril(Optional.ofNullable(shutdownNormsValueDTO.getApril()).orElse(0.0));
				shutdownNormsValue.setMay(Optional.ofNullable(shutdownNormsValueDTO.getMay()).orElse(0.0));
				shutdownNormsValue.setJune(Optional.ofNullable(shutdownNormsValueDTO.getJune()).orElse(0.0));
				shutdownNormsValue.setJuly(Optional.ofNullable(shutdownNormsValueDTO.getJuly()).orElse(0.0));
				shutdownNormsValue.setAugust(Optional.ofNullable(shutdownNormsValueDTO.getAugust()).orElse(0.0));
				shutdownNormsValue.setSeptember(Optional.ofNullable(shutdownNormsValueDTO.getSeptember()).orElse(0.0));
				shutdownNormsValue.setOctober(Optional.ofNullable(shutdownNormsValueDTO.getOctober()).orElse(0.0));
				shutdownNormsValue.setNovember(Optional.ofNullable(shutdownNormsValueDTO.getNovember()).orElse(0.0));
				shutdownNormsValue.setDecember(Optional.ofNullable(shutdownNormsValueDTO.getDecember()).orElse(0.0));
				shutdownNormsValue.setJanuary(Optional.ofNullable(shutdownNormsValueDTO.getJanuary()).orElse(0.0));
				shutdownNormsValue.setFebruary(Optional.ofNullable(shutdownNormsValueDTO.getFebruary()).orElse(0.0));
				shutdownNormsValue.setMarch(Optional.ofNullable(shutdownNormsValueDTO.getMarch()).orElse(0.0));
				if (shutdownNormsValueDTO.getSiteFkId() != null) {
					shutdownNormsValue.setSiteFkId(UUID.fromString(shutdownNormsValueDTO.getSiteFkId()));
				}
				if (shutdownNormsValueDTO.getPlantFkId() != null) {
					shutdownNormsValue.setPlantFkId(UUID.fromString(shutdownNormsValueDTO.getPlantFkId()));
				}
				if (shutdownNormsValueDTO.getVerticalFkId() != null) {
					shutdownNormsValue.setVerticalFkId(UUID.fromString(shutdownNormsValueDTO.getVerticalFkId()));
				}
				if (shutdownNormsValueDTO.getMaterialFkId() != null) {
					shutdownNormsValue.setMaterialFkId(UUID.fromString(shutdownNormsValueDTO.getMaterialFkId()));
				}
				if (shutdownNormsValueDTO.getNormParameterTypeId() != null) {
					shutdownNormsValue
							.setNormParameterTypeFkId(UUID.fromString(shutdownNormsValueDTO.getNormParameterTypeId()));
				}

				shutdownNormsValue.setFinancialYear(shutdownNormsValueDTO.getFinancialYear());
				shutdownNormsValue.setRemarks(shutdownNormsValueDTO.getRemarks());
				shutdownNormsValue.setMcuVersion("V1");
				shutdownNormsValue.setUpdatedBy(Utility.getUserName());

				System.out.println("Data Saved Succussfully");
				shutdownNormsValueList.add(shutdownNormsRepository.save(shutdownNormsValue));
			}
			
			List<ScreenMapping> screenMappingList= screenMappingRepository.findByDependentScreen("shutdown-norms");
			for(ScreenMapping screenMapping:screenMappingList) {
				AopCalculation aopCalculation=new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(plantId);
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			Map<String,Object> map=new HashMap<>();
			map.put("data", shutdownNormsValueList);
			// TODO Auto-generated method stub
			return map;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to save data", ex);
		}
	}
	
	
	public Map<String,Object> savePPShutdownNormsData(List<ShutdownNormsValueDTO> shutdownNormsValueDTOList) {
		String year=null;
		UUID plantId=null;
		List<GradeShutdownNormsValue> gradeShutdownNormsValueList=new ArrayList<>();
		List<ShutdownNormsValueDTO> failedList = new ArrayList<>();
		UUID gradeId=null;
		UUID siteId = null;
		UUID verticalId = null;
		try {
			for (ShutdownNormsValueDTO shutdownNormsValueDTO : shutdownNormsValueDTOList) {
				if (shutdownNormsValueDTO.getSaveStatus() != null
						&& shutdownNormsValueDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(shutdownNormsValueDTO);
					continue;
				}
				year=shutdownNormsValueDTO.getFinancialYear();
				 gradeId=UUID.fromString(shutdownNormsValueDTO.getGradeFkId());
				
				plantId=UUID.fromString(shutdownNormsValueDTO.getPlantFkId());
				GradeShutdownNormsValue gradeShutdownNormsValue = new GradeShutdownNormsValue();
				if (shutdownNormsValueDTO.getId() != null && !shutdownNormsValueDTO.getId().isEmpty()) {
					gradeShutdownNormsValue.setId(UUID.fromString(shutdownNormsValueDTO.getId()));
					gradeShutdownNormsValue.setModifiedOn(new Date());
				} else {
					
					UUID materialId = null;
					if (shutdownNormsValueDTO.getSiteFkId() != null) {
						siteId = UUID.fromString(shutdownNormsValueDTO.getSiteFkId());
					}
					if (shutdownNormsValueDTO.getPlantFkId() != null) {
						plantId = UUID.fromString(shutdownNormsValueDTO.getPlantFkId());
					}
					if (shutdownNormsValueDTO.getVerticalFkId() != null) {
						verticalId = UUID.fromString(shutdownNormsValueDTO.getVerticalFkId());
					}
					if (shutdownNormsValueDTO.getMaterialFkId() != null) {
						materialId = UUID.fromString(shutdownNormsValueDTO.getMaterialFkId());
					}
					UUID Id = gradeShutdownNormsValueRepository.findIdByFilters(plantId, siteId, verticalId, materialId,
							shutdownNormsValueDTO.getFinancialYear(),gradeId);
					if (Id != null) {
						gradeShutdownNormsValue.setId(Id);
					}

					gradeShutdownNormsValue.setCreatedOn(new Date());
				}
				gradeShutdownNormsValue.setApril(Optional.ofNullable(shutdownNormsValueDTO.getApril()).orElse(0.0));
				gradeShutdownNormsValue.setMay(Optional.ofNullable(shutdownNormsValueDTO.getMay()).orElse(0.0));
				gradeShutdownNormsValue.setJune(Optional.ofNullable(shutdownNormsValueDTO.getJune()).orElse(0.0));
				gradeShutdownNormsValue.setJuly(Optional.ofNullable(shutdownNormsValueDTO.getJuly()).orElse(0.0));
				gradeShutdownNormsValue.setAugust(Optional.ofNullable(shutdownNormsValueDTO.getAugust()).orElse(0.0));
				gradeShutdownNormsValue.setSeptember(Optional.ofNullable(shutdownNormsValueDTO.getSeptember()).orElse(0.0));
				gradeShutdownNormsValue.setOctober(Optional.ofNullable(shutdownNormsValueDTO.getOctober()).orElse(0.0));
				gradeShutdownNormsValue.setNovember(Optional.ofNullable(shutdownNormsValueDTO.getNovember()).orElse(0.0));
				gradeShutdownNormsValue.setDecember(Optional.ofNullable(shutdownNormsValueDTO.getDecember()).orElse(0.0));
				gradeShutdownNormsValue.setJanuary(Optional.ofNullable(shutdownNormsValueDTO.getJanuary()).orElse(0.0));
				gradeShutdownNormsValue.setFebruary(Optional.ofNullable(shutdownNormsValueDTO.getFebruary()).orElse(0.0));
				gradeShutdownNormsValue.setMarch(Optional.ofNullable(shutdownNormsValueDTO.getMarch()).orElse(0.0));
				if (shutdownNormsValueDTO.getSiteFkId() != null) {
					gradeShutdownNormsValue.setSiteFkId(UUID.fromString(shutdownNormsValueDTO.getSiteFkId()));
				}
				if (shutdownNormsValueDTO.getPlantFkId() != null) {
					gradeShutdownNormsValue.setPlantFkId(UUID.fromString(shutdownNormsValueDTO.getPlantFkId()));
				}
				if (shutdownNormsValueDTO.getVerticalFkId() != null) {
					gradeShutdownNormsValue.setVerticalFkId(UUID.fromString(shutdownNormsValueDTO.getVerticalFkId()));
				}
				if (shutdownNormsValueDTO.getMaterialFkId() != null) {
					gradeShutdownNormsValue.setMaterialFkId(UUID.fromString(shutdownNormsValueDTO.getMaterialFkId()));
				}
				if (shutdownNormsValueDTO.getNormParameterTypeId() != null) {
					gradeShutdownNormsValue
							.setNormParameterTypeFkId(UUID.fromString(shutdownNormsValueDTO.getNormParameterTypeId()));
				}

				gradeShutdownNormsValue.setFinancialYear(shutdownNormsValueDTO.getFinancialYear());
				gradeShutdownNormsValue.setRemarks(shutdownNormsValueDTO.getRemarks());
				gradeShutdownNormsValue.setMcuVersion("V1");
				gradeShutdownNormsValue.setUpdatedBy(Utility.getUserName());
				if(shutdownNormsValueDTO.getGradeFkId()!=null) {
					gradeShutdownNormsValue.setGradeFkId(UUID.fromString(shutdownNormsValueDTO.getGradeFkId()));
				}
				gradeShutdownNormsValueList.add(gradeShutdownNormsValueRepository.save(gradeShutdownNormsValue));
			}
			String name=normParametersRepository.findNormParameterName(gradeId);
			if(name.equalsIgnoreCase("All Grade")) {
				Plants plant = plantsRepository.findById(plantId).get();
				// Sites site = siteRepository.findById(plant.getSiteFkId()).get();
				Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
				String verticalName = plantsRepository.findVerticalNameByPlantId(plantId);
				Sites site = siteRepository.findById(plant.getSiteFkId()).get();
				String procedureName=verticalName+"_"+site.getName()+"_DistributeShutdownGrades";
				distributeShutdownGrades( procedureName,  year,  plantId.toString());
			}
			List<ScreenMapping> screenMappingList= screenMappingRepository.findByDependentScreen("shutdown-norms");
			for(ScreenMapping screenMapping:screenMappingList) {
				AopCalculation aopCalculation=new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(plantId);
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			Map<String,Object> map=new HashMap<>();
			map.put("data", failedList);
			// TODO Auto-generated method stub
			return map;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save data", ex);
		}
	}


	@Override
	@Transactional
	public AOPMessageVM getShutdownNormsSPData(String year, String plantId) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String storedProcedure = vertical.getName() + "_"+site.getName()+"_CalculateShutdownNorms";
			Integer rowsAffected = getCalculatedShutdownNormsSP(storedProcedure, year, plant.getId().toString(),
					site.getId().toString(), vertical.getId().toString());
			
			
			aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year,
					"shutdown-norms");
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(rowsAffected);
			aopMessageVM.setMessage("SP executed successfully");

			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Transactional
	public Integer getCalculatedShutdownNormsSP(String procedureName, String finYear, String plantId,
			String siteId, String verticalId) {
		try {
						

			String callSql = "{call " + procedureName + "(?, ?, ?, ?)}";

			try (Connection connection = dataSource.getConnection();
					CallableStatement stmt = connection.prepareCall(callSql)) {

				// Set parameters in the correct order
				stmt.setString(1, plantId); // @finYear
				stmt.setString(2, siteId.toString()); // @plantId
				stmt.setString(3, verticalId.toString()); // @verticalId
				stmt.setString(4, finYear); // @siteId

				// Execute the stored procedure
				int rowsAffected = stmt.executeUpdate();

				// Optional: commit if auto-commit is off
				if (!connection.getAutoCommit()) {
					connection.commit();
				}

				return rowsAffected;

			} catch (SQLException e) {
				e.printStackTrace();
				return 0;
			}


		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	@Transactional
	public Integer distributeShutdownGrades(String procedureName, String aopYear, String plantId
			) {
		try {
						

			String callSql = "{call " + procedureName + "(?, ?)}";

			try (Connection connection = dataSource.getConnection();
					CallableStatement stmt = connection.prepareCall(callSql)) {

				// Set parameters in the correct order
				stmt.setString(1, plantId); // @finYear
				
				stmt.setString(2, aopYear); // @siteId

				// Execute the stored procedure
				int rowsAffected = stmt.executeUpdate();

				// Optional: commit if auto-commit is off
				if (!connection.getAutoCommit()) {
					connection.commit();
				}

				return rowsAffected;

			} catch (SQLException e) {
				e.printStackTrace();
				return 0;
			}


		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}


	public List<Object[]> getShutdownNorms(String year, UUID plantId, String viewName,UUID gradeId) {
		try {
			String sql = "SELECT TOP (1000) [Id], [Site_FK_Id], [Plant_FK_Id], [Vertical_FK_Id], "
					+ "[Material_FK_Id], [April], [May], [June], [July], [August], [September], "
					+ "[October], [November], [December], [January], [February], [March], "
					+ "[FinancialYear], [Remarks], [CreatedOn], [ModifiedOn], [MCUVersion], "
					+ "[UpdatedBy], [NormParameterTypeId], [NormParameterTypeName], "
					+ "[NormParameterTypeDisplayName], [NormTypeDisplayOrder], [MaterialDisplayOrder], [UOM],[isEditable],[DisplayName] "
					+ "FROM " + viewName + " "
					+ "WHERE Plant_FK_Id = :plantId AND Grade_FK_Id = :gradeId AND (FinancialYear = :year OR FinancialYear IS NULL) "
					+ "ORDER BY NormTypeDisplayOrder";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("year", year);
			query.setParameter("gradeId", gradeId);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<Object[]> getShutdownNormsMEG(String year, UUID plantId, String viewName) {
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

	@Override
	public AOPMessageVM getUniqueGrades(String year, String plantId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			// Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String viewName="vwScrn"+vertical.getName()+"ShutdownGrade";
			List<Object[]> grades=fetchUniqueGradeFkIds(viewName,UUID.fromString(plantId),year);
			List<Map<String, Object>> listOfMaps = new ArrayList<>();
			
			for (Object[] grade : grades) {
			    Map<String, Object> singleEntryMap = new HashMap<>();
			    singleEntryMap.put("gradeId", grade[0] != null ? grade[0].toString() : null);
			    singleEntryMap.put("displayName", grade[2] != null ? grade[2].toString() : null);
				singleEntryMap.put("name", grade[1] != null ? grade[1].toString() : null);
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
	
	 public List<Object[]> fetchUniqueGradeFkIds(String viewName, UUID plantId, String year) {
	        // Build SQL with safe view injection (ensure viewName is validated)
	        String sql = "SELECT * FROM " + viewName +
	                     " WHERE Plant_FK_Id = :plantId AND aopYear = :year order by DisplayOrder";

	        Query query = entityManager.createNativeQuery(sql);
	        query.setParameter("plantId", plantId);
	        query.setParameter("year", year);

	        @SuppressWarnings("unchecked")
	        List<Object[]> results = query.getResultList();
	        return results;
	    }




	@Override
	public AOPMessageVM getShutConsumptionData(String year, String plantId, String gradeId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<ShutdownConsumptionDTO> shutdownConsumptionDTOs=new ArrayList<ShutdownConsumptionDTO>();
		try {
			List<Object[]> obj=getShutdownHistoryData(plantId,year);
			for(Object[] row:obj) {
				ShutdownConsumptionDTO shutdownConsumptionDTO= new ShutdownConsumptionDTO();
				shutdownConsumptionDTO.setMaterial(row[0] != null ? row[0].toString() : null);
				shutdownConsumptionDTO.setUom(row[1] != null ? row[1].toString() : null);
				shutdownConsumptionDTO.setApril(row[2] != null ? Double.parseDouble(row[2].toString()) : 0.0);
				shutdownConsumptionDTO.setMay(row[3] != null ? Double.parseDouble(row[3].toString()) : 0.0);
				shutdownConsumptionDTO.setJune(row[4] != null ? Double.parseDouble(row[4].toString()) : 0.0);
				shutdownConsumptionDTO.setJuly(row[5] != null ? Double.parseDouble(row[5].toString()) : 0.0);
				shutdownConsumptionDTO.setAugust(row[6] != null ? Double.parseDouble(row[6].toString()) : 0.0);
				shutdownConsumptionDTO.setSeptember(row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0);
				shutdownConsumptionDTO.setOctober(row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0);
				shutdownConsumptionDTO.setNovember(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0);
				shutdownConsumptionDTO.setDecember(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0);
				shutdownConsumptionDTO.setJanuary(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0);
				shutdownConsumptionDTO.setFebruary(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0);
				shutdownConsumptionDTO.setMarch(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0);
				shutdownConsumptionDTOs.add(shutdownConsumptionDTO);
			}
		}catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setData(shutdownConsumptionDTOs);
		aopMessageVM.setMessage("Data fetched successfully");
		// TODO Auto-generated method stub
		return aopMessageVM;
	}
	
	public List<Object[]> getShutdownHistoryData(String plantId, String aopYear) {
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			 String storedProcedure = verticalName + "_" + site.getName() + "_ShutdownConsumtion";
			
			String sql = "EXEC " + storedProcedure
					+ " @plantId = :plantId, @aopYear = :aopYear";

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
	
	public List<Object[]> getShutdownConsumptionData(String plantId, String aopYear,String storedProcedure) {
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
	
	public List<Object[]> getShutdownConsumptionData(String plantId, String aopYear) {
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			
			 String storedProcedure = verticalName + "_" + site.getName() + "_ShutdownConsumtion";
			
			String sql = "EXEC " + storedProcedure
					+ " @plantId = :plantId, @aopYear = :aopYear, @siteId = :siteId, @verticalId = :verticalId";

			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);
			query.setParameter("siteId", site.getId());
			query.setParameter("verticalId", vertical.getId());

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	public AOPMessageVM getData(List<Object[]> obj,String plantId,String year){
		List<ShutdownNormsValueDTO> shutdownNormsValueDTOs = new ArrayList<ShutdownNormsValueDTO>();
		for(Object[] row:obj) {
			ShutdownNormsValueDTO shutdownNormsValueDTO = new ShutdownNormsValueDTO();
			
			shutdownNormsValueDTO.setId(row[0] != null ? row[0].toString() : null);
			shutdownNormsValueDTO.setProductName(row[1] != null ? row[1].toString() : null);
			shutdownNormsValueDTO.setUOM(row[2] != null ? row[2].toString() : null);
			shutdownNormsValueDTO.setMaterialFkId(row[6] != null ? row[6].toString() : null);
			shutdownNormsValueDTO.setApril(row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0);
			shutdownNormsValueDTO.setMay(row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0);
			shutdownNormsValueDTO.setJune(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0);
			shutdownNormsValueDTO.setJuly(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0);
			shutdownNormsValueDTO.setAugust(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0);
			shutdownNormsValueDTO.setSeptember(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0);
			shutdownNormsValueDTO.setOctober(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0);
			shutdownNormsValueDTO.setNovember(row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0);
			shutdownNormsValueDTO.setDecember(row[15] != null ? Double.parseDouble(row[15].toString()) : 0.0);
			shutdownNormsValueDTO.setJanuary(row[16] != null ? Double.parseDouble(row[16].toString()) : 0.0);
			shutdownNormsValueDTO.setFebruary(row[17] != null ? Double.parseDouble(row[17].toString()) : 0.0);
			shutdownNormsValueDTO.setMarch(row[18] != null ? Double.parseDouble(row[18].toString()) : 0.0);
			shutdownNormsValueDTO.setRemarks(row[20] != null ? row[20].toString() : null);
			shutdownNormsValueDTO.setNormParameterTypeDisplayName(row[21] != null ? row[21].toString() : null);
			shutdownNormsValueDTO.setSapCode(row[22] != null ? row[22].toString() : null);
			shutdownNormsValueDTOs.add(shutdownNormsValueDTO);
		}
		Map<String, Object> map = new HashMap<>();

		List<AopCalculation> aopCalculation = aopCalculationRepository
				.findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year, "shutdown-norms");
		map.put("mcuNormsValueDTOList", shutdownNormsValueDTOs);
		map.put("aopCalculation", aopCalculation);
		
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		aopMessageVM.setCode(200);
		aopMessageVM.setData(map);
		aopMessageVM.setMessage("Data fetched successfully");

		return aopMessageVM;
	}

	public AOPMessageVM getShutdownGradeData(List<Object[]> obj,String plantId,String year,String gradeId){
		List<ShutdownNormsValueDTO> shutdownNormsValueDTOList = new ArrayList<>();
		for (Object[] row : obj) {
			ShutdownNormsValueDTO shutdownNormsValueDTO = new ShutdownNormsValueDTO();
			if(row[5].toString()!=null && gradeId.equalsIgnoreCase(row[5].toString())) {
				shutdownNormsValueDTO.setId(row[0] != null ? row[0].toString() : null);
				shutdownNormsValueDTO.setSiteFkId(row[1] != null ? row[1].toString() : null);
				shutdownNormsValueDTO.setPlantFkId(row[2] != null ? row[2].toString() : null);
				shutdownNormsValueDTO.setVerticalFkId(row[3] != null ? row[3].toString() : null);
				shutdownNormsValueDTO.setMaterialFkId(row[4] != null ? row[4].toString() : null);
				shutdownNormsValueDTO.setGradeFkId(row[5] != null ? row[5].toString() : null);
				shutdownNormsValueDTO.setApril(row[6] != null ? Double.parseDouble(row[6].toString()) : 0.0);
				shutdownNormsValueDTO.setMay(row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0);
				shutdownNormsValueDTO.setJune(row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0);
				shutdownNormsValueDTO.setJuly(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0);
				shutdownNormsValueDTO.setAugust(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0);
				shutdownNormsValueDTO.setSeptember(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0);
				shutdownNormsValueDTO.setOctober(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0);
				shutdownNormsValueDTO.setNovember(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0);
				shutdownNormsValueDTO.setDecember(row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0);
				shutdownNormsValueDTO.setJanuary(row[15] != null ? Double.parseDouble(row[15].toString()) : 0.0);
				shutdownNormsValueDTO.setFebruary(row[16] != null ? Double.parseDouble(row[16].toString()) : 0.0);
				shutdownNormsValueDTO.setMarch(row[17] != null ? Double.parseDouble(row[17].toString()) : 0.0);

				shutdownNormsValueDTO.setFinancialYear(row[18] != null ? row[18].toString() : null);
				shutdownNormsValueDTO.setRemarks(row[19] != null ? row[19].toString() : " ");
				shutdownNormsValueDTO.setCreatedOn(row[20] != null ? (Date) row[20] : null);
				shutdownNormsValueDTO.setModifiedOn(row[21] != null ? (Date) row[21] : null);
				shutdownNormsValueDTO.setMcuVersion(row[22] != null ? row[22].toString() : null);
				shutdownNormsValueDTO.setUpdatedBy(row[23] != null ? row[23].toString() : null);
				shutdownNormsValueDTO.setNormParameterTypeId(row[24] != null ? row[24].toString() : null);
				shutdownNormsValueDTO.setNormParameterTypeName(row[25] != null ? row[25].toString() : null);
				shutdownNormsValueDTO.setNormParameterTypeDisplayName(row[26] != null ? row[26].toString() : null);
				shutdownNormsValueDTO.setMaterialDisplayOrder(row[28] != null ? Integer.parseInt(row[28].toString()) : null);
				shutdownNormsValueDTO.setUOM(row[29] != null ? row[29].toString() : null);
				shutdownNormsValueDTO.setIsEditable(row[30] != null ? Boolean.valueOf(row[30].toString()) : null);
				shutdownNormsValueDTO.setProductName(row[31] != null ? row[31].toString() : null);
				shutdownNormsValueDTOList.add(shutdownNormsValueDTO);

			}
		}
		Map<String, Object> map = new HashMap<>();

		List<AopCalculation> aopCalculation = aopCalculationRepository
				.findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year, "shutdown-norms");
		map.put("mcuNormsValueDTOList", shutdownNormsValueDTOList);
		map.put("aopCalculation", aopCalculation);
		
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		aopMessageVM.setCode(200);
		aopMessageVM.setData(map);
		aopMessageVM.setMessage("Data fetched successfully");

		return aopMessageVM;
	}




}
