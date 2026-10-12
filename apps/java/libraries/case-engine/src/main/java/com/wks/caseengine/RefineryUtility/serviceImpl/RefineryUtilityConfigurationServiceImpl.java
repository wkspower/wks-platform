package com.wks.caseengine.RefineryUtility.serviceImpl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.RefineryUtility.dto.CommoditySelectionDTO;
import com.wks.caseengine.RefineryUtility.dto.ConsumerDemandDTO;
import com.wks.caseengine.RefineryUtility.dto.MonthWiseConstantsDTO;
import com.wks.caseengine.RefineryUtility.dto.PlantOwnerDTO;
import com.wks.caseengine.RefineryUtility.dto.SelectedPlantOwnerDTO;
import com.wks.caseengine.RefineryUtility.dto.TreatmentVendorDTO;
import com.wks.caseengine.RefineryUtility.service.RefineryUtilityConfigurationService;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.NormParametersRepository;

@Service
public class RefineryUtilityConfigurationServiceImpl implements RefineryUtilityConfigurationService {

    @Autowired
	private PlantsRepository plantsRepository;

    @Autowired 
	private SiteRepository siteRepository;

    @Autowired
	private VerticalsRepository verticalRepository;

    @Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;

    @PersistenceContext 
	private EntityManager entityManager;

	@Autowired
	private NormParametersRepository normParametersRepository;
    
    @Override
    public AOPMessageVM getMonthWiseConstants(String year, String plantFKId) {
		try {
			AOPMessageVM aopMessageVM = new AOPMessageVM();
		    Plants plant = plantsRepository.findById(UUID.fromString(plantFKId)).get();
		    Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		    Sites site = siteRepository.findById(plant.getSiteFkId()).get();

			String procedureName = vertical.getName()+"_"+site.getName() +"_"+"GetConstant";
		
			List<Object[]> resultList = new ArrayList<>();
		
			resultList = getMonthWiseConstantsFromSP(year, plantFKId, procedureName);
			List<MonthWiseConstantsDTO> dtoList = new ArrayList<>();

			for (Object[] row : resultList) {

				MonthWiseConstantsDTO dto = new MonthWiseConstantsDTO();

				dto.setNormParameterFKId(row[0] != null ? row[0].toString() : null);
				dto.setName(row[1] != null ? row[1].toString() : null);
				dto.setDisplayName(row[2] != null ? row[2].toString() : null);
				dto.setUOM(row[3] != null ? row[3].toString() : null);
				dto.setNormTypeName(row[4] != null ? row[4].toString() : null);
				dto.setApr(parseDouble(row[5]));
				dto.setMay(parseDouble(row[6]));
				dto.setJun(parseDouble(row[7]));
				dto.setJul(parseDouble(row[8]));
				dto.setAug(parseDouble(row[9]));
				dto.setSep(parseDouble(row[10]));
				dto.setOct(parseDouble(row[11]));
				dto.setNov(parseDouble(row[12]));
				dto.setDec(parseDouble(row[13]));
				dto.setJan(parseDouble(row[14]));
				dto.setFeb(parseDouble(row[15]));
				dto.setMar(parseDouble(row[16]));
				dto.setAuditYear(row[17] != null ? row[17].toString() : null);
				dto.setRemarks(row[18] != null ? row[18].toString() : null);
				dto.setDisplayOrder(row[19] != null ? row[19].toString() : null);
				Boolean isEditable = null;
				if (row[20] != null) {
					if (row[20] instanceof Boolean) {
						isEditable = (Boolean) row[20];
					} else if (row[20] instanceof Number) {
						isEditable = ((Number) row[20]).intValue() == 1;
					}
				}
				dto.setIsEditable(isEditable);
				
				dtoList.add(dto);
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(dtoList);
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
    public byte[] exportMonthWiseConstants(String year, String plantId, boolean isAfterSave, List<MonthWiseConstantsDTO> dtoList) {
        try {   
        
        	AOPMessageVM response = checkIsSummerWinterPlant(plantId);

            boolean isSummerWinter = false;
            if (response != null && response.getData() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.getData();
                
                if (data.containsKey("isSummerWinter") && data.get("isSummerWinter") != null) {
                    isSummerWinter = (Boolean) data.get("isSummerWinter");
                }
            }
            boolean summerWinterFlag = isSummerWinter;

            if (!isAfterSave) {
                AOPMessageVM aopMessageVM = getMonthWiseConstants(year, plantId);

                if (aopMessageVM != null && aopMessageVM.getData() != null) {
                    @SuppressWarnings("unchecked")
                    List<MonthWiseConstantsDTO> fetchedData = (List<MonthWiseConstantsDTO>) aopMessageVM.getData();
                    dtoList = fetchedData;
                }
            }

            if (dtoList == null) {
                dtoList = new ArrayList<>();
            }

            try (Workbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                Sheet sheet = workbook.createSheet("Sheet1");
                int currentRow = 0;

                List<String> innerHeaders = new ArrayList<>();
                innerHeaders.add("Type");         // Col 0
                innerHeaders.add("Particulars");  // Col 1
                innerHeaders.add("UOM");          // Col 2

                if (summerWinterFlag) {
                    innerHeaders.add("Summer");   // Col 3
                    innerHeaders.add("Winter");   // Col 4
                } else {
                    innerHeaders.add("Value");    // Col 3
                }

                innerHeaders.add("Remark");           // Col 4 (if false) or Col 5 (if true)
                innerHeaders.add("NormParameterId");  // Col 5 (if false) or Col 6 (if true)

                if (isAfterSave) {
                    innerHeaders.add("Status");
                    innerHeaders.add("Error Description");
                }

                // Create Header Row with Bold Bordered Style
                Row headerRow = sheet.createRow(currentRow++);
                for (int col = 0; col < innerHeaders.size(); col++) {
                    Cell cell = headerRow.createCell(col);
                    cell.setCellValue(innerHeaders.get(col));
                    cell.setCellStyle(Utility.createBoldBorderedStyle(workbook));
                }

                // Cell Styles matched from exportBusinessDemand
                CellStyle unlockedBorderedStyle = workbook.createCellStyle();
                unlockedBorderedStyle.setLocked(false);
                unlockedBorderedStyle.setBorderBottom(BorderStyle.THIN);
                unlockedBorderedStyle.setBorderTop(BorderStyle.THIN);
                unlockedBorderedStyle.setBorderLeft(BorderStyle.THIN);
                unlockedBorderedStyle.setBorderRight(BorderStyle.THIN);

                CellStyle lockedBorderedStyle = workbook.createCellStyle();
                lockedBorderedStyle.setLocked(true);
                lockedBorderedStyle.setBorderBottom(BorderStyle.THIN);
                lockedBorderedStyle.setBorderTop(BorderStyle.THIN);
                lockedBorderedStyle.setBorderLeft(BorderStyle.THIN);
                lockedBorderedStyle.setBorderRight(BorderStyle.THIN);

                // Determine dynamic column index thresholds
                int normParamIdIndex = summerWinterFlag ? 6 : 5;
                int lastVisibleColIndex = summerWinterFlag ? 5 : 4;

                for (MonthWiseConstantsDTO dto : dtoList) {
                    Row row = sheet.createRow(currentRow++);
                    List<Object> rowData = new ArrayList<>();
                    rowData.add(dto.getNormTypeName());
                    rowData.add(dto.getDisplayName());
                    rowData.add(dto.getUOM());

                    if (summerWinterFlag) {
                        rowData.add(dto.getApr()); // Summer
                        rowData.add(dto.getOct()); // Winter
                    } else {
                        rowData.add(dto.getApr()); // Value
                    }

                    rowData.add(dto.getRemarks());
                    rowData.add(dto.getNormParameterFKId());

                    if (isAfterSave) {
                        rowData.add(dto.getSaveStatus());
                        rowData.add(dto.getErrDescription());
                    }

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

                        boolean isEditableCol = summerWinterFlag ? (col >= 3 && col <= 5) : (col == 3 || col == 4);

                        if (isEditableCol) {
                            cell.setCellStyle(unlockedBorderedStyle);
                        } else {
                            cell.setCellStyle(lockedBorderedStyle);
                        }
                    }
                }

                // Protect the sheet so locked cells cannot be modified
                sheet.protectSheet("");

                // Auto-size all visible data columns dynamically
                for (int col = 0; col <= lastVisibleColIndex; col++) {
                    sheet.autoSizeColumn(col);
                }

                // Hide NormParameterId column dynamically
                sheet.setColumnHidden(normParamIdIndex, true);

                workbook.write(outputStream);
                return outputStream.toByteArray();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
    @Transactional
    public AOPMessageVM importMonthWiseConstants(String year, UUID plantId, MultipartFile file) {
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    try {
	    	AOPMessageVM response = checkIsSummerWinterPlant(plantId.toString());

	        boolean isSummerWinter = false;
	        if (response != null && response.getData() instanceof Map) {
	            @SuppressWarnings("unchecked")
	            Map<String, Object> data = (Map<String, Object>) response.getData();
	            
	            if (data.containsKey("isSummerWinter") && data.get("isSummerWinter") != null) {
	                isSummerWinter = (Boolean) data.get("isSummerWinter");
	            }
	        }
	        List<MonthWiseConstantsDTO> data = readMonthWiseConstants(file.getInputStream(), plantId, year,isSummerWinter);
	        List<MonthWiseConstantsDTO> failedList = saveMonthWiseConstants(year, plantId.toString(), data);

	        if (failedList != null && !failedList.isEmpty()) {
	            byte[] fileByteArray = exportMonthWiseConstants(year, plantId.toString(), true, failedList);
	            String base64File = Base64.getEncoder().encodeToString(fileByteArray);
	            
	            aopMessageVM.setData(base64File);
	            aopMessageVM.setCode(400);
	            aopMessageVM.setMessage("Partial data has been saved");
	        } else {
	            aopMessageVM.setCode(200);
	            aopMessageVM.setMessage("All data has been saved successfully");
	        }

	        return aopMessageVM;

	    } catch (Exception e) {
	        e.printStackTrace();
	        aopMessageVM.setCode(500);
	        aopMessageVM.setMessage("Error importing data: " + e.getMessage());
	        return aopMessageVM;
	    }
	}

    public List<MonthWiseConstantsDTO> readMonthWiseConstants(InputStream inputStream, UUID plantFKId, String year, Boolean isSummerWinter) {
        List<MonthWiseConstantsDTO> monthWiseConstantsDTOs = new ArrayList<>();

        // Safely handle null boolean flag
        boolean summerWinterFlag = Boolean.TRUE.equals(isSummerWinter);

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) {
                rowIterator.next(); // Skip header row
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                MonthWiseConstantsDTO dto = new MonthWiseConstantsDTO();
                try {
                    dto.setNormTypeName(getStringCellValue(row.getCell(0), dto));
                    dto.setDisplayName(getStringCellValue(row.getCell(1), dto));
                    dto.setUOM(getStringCellValue(row.getCell(2), dto));

                    if (summerWinterFlag) {
                        
                        dto.setApr(getNumericCellValue(row.getCell(3), dto));
                        dto.setOct(getNumericCellValue(row.getCell(4), dto));
                        dto.setRemarks(getStringCellValue(row.getCell(5), dto));
                        dto.setNormParameterFKId(getStringCellValue(row.getCell(6), dto));
                    } else {
                       
                        dto.setApr(getNumericCellValue(row.getCell(3), dto));
                        dto.setRemarks(getStringCellValue(row.getCell(4), dto));
                        dto.setNormParameterFKId(getStringCellValue(row.getCell(5), dto));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    dto.setErrDescription(e.getMessage());
                    dto.setSaveStatus("Failed");
                }

                validateIntegerUOMConstraint(dto, summerWinterFlag);

                monthWiseConstantsDTOs.add(dto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return monthWiseConstantsDTOs;
    } 
    
	// Helper method to validate integer-only constraint for UOM "%" or "M3/hr"
	private void validateIntegerUOMConstraint(MonthWiseConstantsDTO dto, boolean summerWinterFlag) {
	    if (dto.getSaveStatus() != null && dto.getSaveStatus().equalsIgnoreCase("Failed")) {
	        return;
	    }
	    String uom = dto.getUOM();
	    if (uom == null || (!uom.trim().equalsIgnoreCase("%") && !uom.trim().equalsIgnoreCase("M3/hr"))) {
	        return;
	    }
	    List<String> nonIntegerColumns = new ArrayList<>();
	    if (dto.getApr() != null && dto.getApr() % 1 != 0) {
	        nonIntegerColumns.add("Apr");
	    }
	    if (summerWinterFlag && dto.getOct() != null && dto.getOct() % 1 != 0) {
	        nonIntegerColumns.add("Oct");
	    }
	    if (!nonIntegerColumns.isEmpty()) {
	        dto.setSaveStatus("Failed");
	        dto.setErrDescription("Only integer values are allowed for UOM " + uom.trim());
	    }
	}

	// Helper method to skip empty rows
	private boolean isRowEmpty(Row row) {
	    for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
	        Cell cell = row.getCell(c);
	        if (cell != null && cell.getCellType() != CellType.BLANK) {
	            return false;
	        }
	    }
	    return true;
	}
	private static String getStringCellValue(Cell cell, MonthWiseConstantsDTO dto) {
	    try {
	        if (cell == null || cell.getCellType() == CellType.BLANK) {
	            return null;
	        }
	        
	        cell.setCellType(CellType.STRING);
	        String val = cell.getStringCellValue().trim();
	        
	        // Return null if the string is empty after trimming
	        return val.isEmpty() ? null : val;
	        
	    } catch (Exception e) {
	        dto.setSaveStatus("Failed");
	        dto.setErrDescription("Please enter correct values");
	        e.printStackTrace();
	    }
	    return null;
	}
	private static Double getNumericCellValue(Cell cell, MonthWiseConstantsDTO dto) {
	    if (cell == null || cell.getCellType() == CellType.BLANK) {
	        return null;
	    }

	    if (cell.getCellType() == CellType.NUMERIC) {
	        return cell.getNumericCellValue();
	    } 
	    
	    if (cell.getCellType() == CellType.STRING) {
	        String val = cell.getStringCellValue().trim();
	        if (val.isEmpty()) {
	            return null; // Return null for blank strings
	        }
	        try {
	            return Double.parseDouble(val);
	        } catch (NumberFormatException e) {
	            dto.setSaveStatus("Failed");
	            dto.setErrDescription("Please enter numeric values");
	        }
	    }
	    return null;
	}

	public List<Object[]> getMonthWiseConstantsFromSP(String aopYear, String plantId, String procedureName) {
		try {
			String sql = "EXEC " + "[" + procedureName + "]" + " @plantId = :plantId, @aopYear = :aopYear";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

    public Double parseDouble(Object value) {

		if (value == null) {
			return 0.0;
		}

		try {
			return Double.parseDouble(value.toString().trim());
		} catch (Exception e) {
			return 0.0;
		}
	}

	@Transactional 
	@Override
	public List<MonthWiseConstantsDTO> saveMonthWiseConstants(String year, String plantFKId,
			List<MonthWiseConstantsDTO> monthWiseConstantsDTOList) {
		try {
			List<MonthWiseConstantsDTO> failedList = new ArrayList<>();
	
			for (MonthWiseConstantsDTO monthWiseConstantsDTO : monthWiseConstantsDTOList) {
				
				if (monthWiseConstantsDTO.getSaveStatus() != null
						&& monthWiseConstantsDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(monthWiseConstantsDTO);
					continue;
				}

			for (int i = 1; i <= 12; i++) {
				
				saveMonthWiseConstants(monthWiseConstantsDTO, i, year);
				
			}
			
			if("Failed".equalsIgnoreCase(monthWiseConstantsDTO.getSaveStatus())) {
				failedList.add(monthWiseConstantsDTO);
			}
		}


		return failedList;
			
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save month wise constants", ex);
		}
	}

	
	public Double getAttributeValue(MonthWiseConstantsDTO monthWiseConstantsDTO, Integer i) {
		switch (i) {
			case 1:
				return monthWiseConstantsDTO.getJan();
			case 2:
				return monthWiseConstantsDTO.getFeb();
			case 3:
				return monthWiseConstantsDTO.getMar();
			case 4:
				return monthWiseConstantsDTO.getApr();
			case 5:
				return monthWiseConstantsDTO.getMay();
			case 6:
				return monthWiseConstantsDTO.getJun();
			case 7:
				return monthWiseConstantsDTO.getJul();
			case 8:
				return monthWiseConstantsDTO.getAug();
			case 9:
				return monthWiseConstantsDTO.getSep();
			case 10:
				return monthWiseConstantsDTO.getOct();
			case 11:
				return monthWiseConstantsDTO.getNov();
			case 12:
				return monthWiseConstantsDTO.getDec();

		}
		return monthWiseConstantsDTO.getJan();
	}

	public void saveMonthWiseConstants(MonthWiseConstantsDTO monthWiseConstantsDTO, Integer i, String year) {

		UUID normParameterFKId = UUID.fromString(monthWiseConstantsDTO.getNormParameterFKId());
		Double attributeValue = getAttributeValue(monthWiseConstantsDTO, i);
		String remark = monthWiseConstantsDTO.getRemarks();

	Optional<NormAttributeTransactions> existingRecord = normAttributeTransactionsRepository
			.findByNormParameterFKIdAndAOPMonthAndAuditYear(normParameterFKId, i, year);

	NormAttributeTransactions normAttributeTransactions;

	if (existingRecord.isPresent()) {
		normAttributeTransactions = existingRecord.get();
		normAttributeTransactions.setModifiedOn(new Date());
		Double existingValue = normAttributeTransactions.getAttributeValue() != null ? Double.parseDouble(normAttributeTransactions.getAttributeValue()) : null;
		boolean isRemarkValidationPassed = isRemarkValidationPassed(attributeValue, existingValue, remark, normAttributeTransactions.getRemarks());
		if(!isRemarkValidationPassed) { 
			monthWiseConstantsDTO.setSaveStatus("Failed");
			monthWiseConstantsDTO.setErrDescription("Please update remark");
			return;
		}
	} else {

		normAttributeTransactions = new NormAttributeTransactions();
		normAttributeTransactions.setCreatedOn(new Date());
		normAttributeTransactions.setUserName(Utility.getUserName());
		normAttributeTransactions.setNormParameterFKId(normParameterFKId);
		normAttributeTransactions.setAopMonth(i);
		normAttributeTransactions.setAuditYear(year);
	}

	normAttributeTransactions
			.setAttributeValue(attributeValue != null ? attributeValue.toString() : "0.0");
	normAttributeTransactions.setRemarks(remark);
	normAttributeTransactions.setUserName(Utility.getUserName());
	normAttributeTransactionsRepository.save(normAttributeTransactions);
}

private boolean isRemarkValidationPassed(Double newValue, Double existingValue, String newRemark, String existingRemark) {
  
	if(existingValue == null || newValue == null) {
		return true;
	}
	// Check if the value has changed (null-safe)
    boolean valueChanged = !Objects.equals(newValue, existingValue);
    
    if (valueChanged) {
        // If the value changed, the remark must be updated (must not be null/empty and must differ from the existing remark)
        boolean isRemarkUpdated = newRemark != null 
                && !newRemark.trim().isEmpty() 
                && !Objects.equals(newRemark, existingRemark);
        return isRemarkUpdated;
    }
    
   // return true if values not changed
    return true;
}

private boolean isRemarkValidationPassedForConsumerDemand(Double newValue, Double existingValue, String newRemark, String existingRemark) {
  
	if(existingValue == null || newValue == null) {
		return true;
	}
	// Check if the value has changed (null-safe)
    boolean valueChanged = !Objects.equals(newValue, existingValue);
    
    if (valueChanged) {
        // If the value changed, the remark must be updated (must not be null/empty and must differ from the existing remark)
        boolean isRemarkUpdated = newRemark != null 
                && !newRemark.trim().isEmpty() 
                && !Objects.equals(newRemark, existingRemark);
        return isRemarkUpdated;
    }
    
   // return true if values not changed
    return true;
}

@Override
public AOPMessageVM checkIsSummerWinterPlant(String plantId) {
	AOPMessageVM aopMessageVM = new AOPMessageVM();
	try {
		boolean isSummerWinter = false;
		try {
			String sql = "SELECT IsSummerWinter FROM vwScrnRefineryUtilityIsSummerWinter WHERE PlantId = :plantId";
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			List<?> resultList = query.getResultList();

			if (resultList != null && !resultList.isEmpty()) {
				Object val = resultList.get(0);
				if (val instanceof Boolean) {
					isSummerWinter = (Boolean) val;
				} else if (val instanceof Number) {
					isSummerWinter = ((Number) val).intValue() == 1;
				} else if (val != null) {
					isSummerWinter = Boolean.parseBoolean(val.toString()) || "1".equals(val.toString().trim()) || "true".equalsIgnoreCase(val.toString().trim());
				}
			} else {
				String countSql = "SELECT COUNT(*) FROM vwScrnRefineryUtilityIsSummerWinter WHERE PlantId = :plantId";
				Query countQuery = entityManager.createNativeQuery(countSql);
				countQuery.setParameter("plantId", plantId);
				Number count = (Number) countQuery.getSingleResult();
				isSummerWinter = (count != null && count.intValue() > 0);
			}
		} catch (Exception ex) {
			// Try fallback view name or plant name
			try {
				String sqlAlt = "SELECT IsSummerWinter FROM vwScrnRefineryUtilityCheckIsSummerWinterPlant WHERE PlantId = :plantId";
				Query altQuery = entityManager.createNativeQuery(sqlAlt);
				altQuery.setParameter("plantId", plantId);
				List<?> altList = altQuery.getResultList();
				if (altList != null && !altList.isEmpty()) {
					Object val = altList.get(0);
					isSummerWinter = Boolean.parseBoolean(val.toString()) || "1".equals(val.toString().trim()) || "true".equalsIgnoreCase(val.toString().trim());
				}
			} catch (Exception e1) {
				try {
					Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElse(null);
					if (plant != null && "PCG ASU".equalsIgnoreCase(plant.getName())) {
						isSummerWinter = true;
					}
				} catch (Exception e2) {
					// ignore
				}
			}
		}

		Map<String, Object> responseData = new HashMap<>();
		responseData.put("isSummerWinter", isSummerWinter);
		responseData.put("isTwoColumn", isSummerWinter);
		responseData.put("plantId", plantId);

		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Fetched plant summer/winter configuration successfully");
		aopMessageVM.setData(responseData);
		return aopMessageVM;
	} catch (Exception e) {
		throw new RuntimeException("Failed to check summer/winter plant configuration", e);
	}
}

@Override
public AOPMessageVM getTreatmentVendorData(String year, String plantFKId) {
	try {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		Plants plant = plantsRepository.findById(UUID.fromString(plantFKId)).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();

		String procedureName = vertical.getName()+"_"+site.getName() +"_"+"GetTreatmentVendor";
	
		List<Object[]> resultList = new ArrayList<>();
	
		resultList = getTreatmentVendorsFromSP(year, plantFKId, procedureName);
		List<TreatmentVendorDTO> dtoList = new ArrayList<>();

		for (Object[] row : resultList) {

			TreatmentVendorDTO dto = new TreatmentVendorDTO();

			dto.setNormParameterFKId(row[0] != null ? row[0].toString() : null);
			dto.setName(row[1] != null ? row[1].toString() : null);
			dto.setDisplayName(row[2] != null ? row[2].toString() : null);
			dto.setUom(row[3] != null ? row[3].toString() : null);
			dto.setNormTypeName(row[4] != null ? row[4].toString() : null);
			dto.setIsChecked(row[5] != null ? row[5].toString() : null);
			dto.setAuditYear(row[6] != null ? row[6].toString() : null);
			dto.setRemarks(row[7] != null ? row[7].toString() : null);
			dto.setDisplayOrder(row[8] != null ? Integer.parseInt(row[8].toString()) : null);
		
		Boolean isEditable = null;
		if (row[9] != null) {
			if (row[9] instanceof Boolean) {
				isEditable = (Boolean) row[9];
			} else if (row[9] instanceof Number) {
				isEditable = ((Number) row[9]).intValue() == 1;
			}
		}
		dto.setIsEditable(isEditable);
			dtoList.add(dto);
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Data fetched successfully");
		aopMessageVM.setData(dtoList);
		return aopMessageVM;
	} catch (IllegalArgumentException e) {
		throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
	} catch (Exception ex) {
		throw new RuntimeException("Failed to fetch data", ex);
	}
}

public List<Object[]> getTreatmentVendorsFromSP(String aopYear, String plantId, String procedureName) {
	try {
		String sql = "EXEC " + "[" + procedureName + "]" + " @plantId = :plantId, @aopYear = :aopYear";

		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("plantId", plantId);
		query.setParameter("aopYear", aopYear);

		return query.getResultList();
	} catch (IllegalArgumentException e) {
		throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
	} catch (Exception ex) {
		throw new RuntimeException("Failed to fetch data", ex);
	}
}

@Transactional 
@Override
	public List<TreatmentVendorDTO> saveTreatmentVendorData(String year, String plantFKId,
			List<TreatmentVendorDTO> treatmentVendorDTOList) {
		try {
			List<TreatmentVendorDTO> failedList = new ArrayList<>();
	
			for (TreatmentVendorDTO treatmentVendorDTO : treatmentVendorDTOList) {
				
				if (treatmentVendorDTO.getSaveStatus() != null
						&& treatmentVendorDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(treatmentVendorDTO);
					continue;
				}

			
				
				saveTreatmentVendorInDB(treatmentVendorDTO, 4, year, treatmentVendorDTO.getIsChecked());
				
			
			
			if("Failed".equalsIgnoreCase(treatmentVendorDTO.getSaveStatus())) {
				failedList.add(treatmentVendorDTO);
			}
		}


		return failedList;
			
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save month wise constants", ex);
		}
	}

	public void saveTreatmentVendorInDB(TreatmentVendorDTO treatmentVendorDTO, Integer i, String year, String attributeValue) {

		UUID normParameterFKId = UUID.fromString(treatmentVendorDTO.getNormParameterFKId());
		String remark = treatmentVendorDTO.getRemarks();

	Optional<NormAttributeTransactions> existingRecord = normAttributeTransactionsRepository
			.findByNormParameterFKIdAndAOPMonthAndAuditYear(normParameterFKId, i, year);

	NormAttributeTransactions normAttributeTransactions;

	if (existingRecord.isPresent()) {
		normAttributeTransactions = existingRecord.get();
		normAttributeTransactions.setModifiedOn(new Date());

	} else {

		normAttributeTransactions = new NormAttributeTransactions();
		normAttributeTransactions.setCreatedOn(new Date());
		normAttributeTransactions.setUserName(Utility.getUserName());
		normAttributeTransactions.setNormParameterFKId(normParameterFKId);
		normAttributeTransactions.setAopMonth(i);
		normAttributeTransactions.setAuditYear(year);
	}

	normAttributeTransactions
			.setAttributeValue(attributeValue);
	normAttributeTransactions.setRemarks(remark);
	normAttributeTransactions.setUserName(Utility.getUserName());
	normAttributeTransactionsRepository.save(normAttributeTransactions);
}

@Transactional
@Override
public AOPMessageVM deleteTreatmentVendorData(String normParameterFKId, String year) {

		UUID normParamUUID = UUID.fromString(normParameterFKId);

	int count = normParametersRepository.updateIsActiveToFalse(normParamUUID);

	if(count == 1){
			return new AOPMessageVM(200, "Treatment vendor parameter deleted successfully", null);
		} else {
			return new AOPMessageVM(404, "No treatment vendor parameter found for the given parameters", null);
		}

}

@Override
public AOPMessageVM getCommodityChemicalsData(String year, String plantFKId) {
	try {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		Plants plant = plantsRepository.findById(UUID.fromString(plantFKId)).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();

		String procedureName = vertical.getName()+"_"+site.getName() +"_"+"GetCommodityChemicals";
	
		List<Object[]> resultList = new ArrayList<>();
	
		resultList = getCommodityChemicalsDataFromSP(year, plantFKId, procedureName);
		List<CommoditySelectionDTO> dtoList = new ArrayList<>();

		for (Object[] row : resultList) {

			CommoditySelectionDTO dto = new CommoditySelectionDTO();

			dto.setNormParameterFKId(row[0] != null ? row[0].toString() : null);
			dto.setName(row[1] != null ? row[1].toString() : null);
			dto.setDisplayName(row[2] != null ? row[2].toString() : null);
			dto.setUom(row[3] != null ? row[3].toString() : null);
			dto.setNormTypeName(row[4] != null ? row[4].toString() : null);
			dto.setIsChecked(row[5] != null ? row[5].toString() : null);
			dto.setAuditYear(row[6] != null ? row[6].toString() : null);
			dto.setRemarks(row[7] != null ? row[7].toString() : null);
			dto.setDisplayOrder(row[8] != null ? Integer.parseInt(row[8].toString()) : null);
		
		Boolean isEditable = null;
		if (row[9] != null) {
			if (row[9] instanceof Boolean) {
				isEditable = (Boolean) row[9];
			} else if (row[9] instanceof Number) {
				isEditable = ((Number) row[9]).intValue() == 1;
			}
		}
		dto.setIsEditable(isEditable);
			dtoList.add(dto);
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Data fetched successfully");
		aopMessageVM.setData(dtoList);
		return aopMessageVM;
	} catch (IllegalArgumentException e) {
		throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
	} catch (Exception ex) {
		throw new RuntimeException("Failed to fetch data", ex);
	}
}

public List<Object[]> getCommodityChemicalsDataFromSP(String aopYear, String plantId, String procedureName) {
	try {
		String sql = "EXEC " + "[" + procedureName + "]" + " @plantId = :plantId, @aopYear = :aopYear";

		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("plantId", plantId);
		query.setParameter("aopYear", aopYear);

		return query.getResultList();
	} catch (IllegalArgumentException e) {
		throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
	} catch (Exception ex) {
		throw new RuntimeException("Failed to fetch data", ex);
	}
}

@Transactional 
@Override
	public List<CommoditySelectionDTO> saveCommodityChemicalsData(String year, String plantFKId,
			List<CommoditySelectionDTO> commoditySelectionDTOList) {
		try {
			List<CommoditySelectionDTO> failedList = new ArrayList<>();
	
			for (CommoditySelectionDTO commoditySelectionDTO : commoditySelectionDTOList) {
				
				if (commoditySelectionDTO.getSaveStatus() != null
						&& commoditySelectionDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(commoditySelectionDTO);
					continue;
				}

			
				
				saveCommodityChemicalsInDB(commoditySelectionDTO, 4, year, commoditySelectionDTO.getIsChecked());
				
			
			
			if("Failed".equalsIgnoreCase(commoditySelectionDTO.getSaveStatus())) {
				failedList.add(commoditySelectionDTO);
			}
		}


		return failedList;
			
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save commodity selection data", ex);
		}
	}

	public void saveCommodityChemicalsInDB(CommoditySelectionDTO commoditySelectionDTO, Integer i, String year, String attributeValue) {

		UUID normParameterFKId = UUID.fromString(commoditySelectionDTO.getNormParameterFKId());
		String remark = commoditySelectionDTO.getRemarks();

	Optional<NormAttributeTransactions> existingRecord = normAttributeTransactionsRepository
			.findByNormParameterFKIdAndAOPMonthAndAuditYear(normParameterFKId, i, year);

	NormAttributeTransactions normAttributeTransactions;

	if (existingRecord.isPresent()) {
		normAttributeTransactions = existingRecord.get();
		normAttributeTransactions.setModifiedOn(new Date());

	} else {

		normAttributeTransactions = new NormAttributeTransactions();
		normAttributeTransactions.setCreatedOn(new Date());
		normAttributeTransactions.setUserName(Utility.getUserName());
		normAttributeTransactions.setNormParameterFKId(normParameterFKId);
		normAttributeTransactions.setAopMonth(i);
		normAttributeTransactions.setAuditYear(year);
	}

	normAttributeTransactions
			.setAttributeValue(attributeValue);
	normAttributeTransactions.setRemarks(remark);
	normAttributeTransactions.setUserName(Utility.getUserName());
	normAttributeTransactionsRepository.save(normAttributeTransactions);
}

	@Override
	public AOPMessageVM getPlantOwnerDropdown(String plantFKId) {
		try {
			String sql = "SELECT CAST(Id AS VARCHAR(36)), PlantOwnerSelection, CAST(PlantId AS VARCHAR(36)) " +
					"FROM PlantOwner WHERE PlantId = :plantFKId";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantFKId", plantFKId);

			List<Object[]> resultList = query.getResultList();
			List<PlantOwnerDTO> dtoList = new ArrayList<>();

			for (Object[] row : resultList) {
				PlantOwnerDTO dto = new PlantOwnerDTO();
				dto.setId(row[0] != null ? row[0].toString() : null);
				dto.setPlantOwner(row[1] != null ? row[1].toString() : null);
				dto.setPlantId(row[2] != null ? row[2].toString() : null);
				dtoList.add(dto);
			}

			return new AOPMessageVM(200, "Plant owner dropdown fetched successfully", dtoList);
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch plant owner dropdown data", ex);
		}
	}

	@Override
	public AOPMessageVM getSelectedPlantOwner(String plantFKId) {
		try {
			String sql = "EXEC [dbo].[sp_GetSelectedOwner] @plantId = :plantId";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantFKId);

			List<Object[]> resultList = query.getResultList();
			List<SelectedPlantOwnerDTO> dtoList = new ArrayList<>();

			for (Object[] row : resultList) {
				SelectedPlantOwnerDTO dto = new SelectedPlantOwnerDTO();
				dto.setId(row[0] != null ? row[0].toString() : null);
				dto.setPlantOwnerSelection(row[1] != null ? row[1].toString() : null);
				dto.setPlantId(row[2] != null ? row[2].toString() : null);
				dto.setRemarks(row[3] != null ? row[3].toString() : null);
				dto.setNormParameterId(row[4] != null ? row[4].toString() : null);
				dtoList.add(dto);
			}

			return new AOPMessageVM(200, "Selected plant owner fetched successfully", dtoList);
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch selected plant owner data", ex);
		}
	}

	@Override
	@Transactional
	public List<SelectedPlantOwnerDTO> saveSelectedPlantOwner(String plantFKId, List<SelectedPlantOwnerDTO> dtoList) {
		List<SelectedPlantOwnerDTO> failedList = new ArrayList<>();

		for (SelectedPlantOwnerDTO dto : dtoList) {
			try {
				UUID normParameterFKId = UUID.fromString(dto.getNormParameterId());

				if(normParameterFKId == null) { 
					throw new RestInvalidArgumentException("Invalid UUID format for Norm Parameter ID", null);
				}

				Optional<NormAttributeTransactions> existingRecord =
						normAttributeTransactionsRepository.findByNormParameterFKIdAndAOPMonthOnly(normParameterFKId, 4);

				NormAttributeTransactions record;

				if (existingRecord.isPresent()) {
					record = existingRecord.get();
					record.setAttributeValue(dto.getId());
					record.setRemarks(dto.getRemarks());
					record.setModifiedOn(new Date());
					record.setUserName(Utility.getUserName());
				} else {
					record = new NormAttributeTransactions();
					record.setNormParameterFKId(normParameterFKId);
					record.setAttributeValue(dto.getId());
					record.setRemarks(dto.getRemarks());
					record.setAopMonth(4);
					record.setCreatedOn(new Date());
					record.setModifiedOn(new Date());
					record.setUserName(Utility.getUserName());
				}

				normAttributeTransactionsRepository.save(record);
				dto.setSaveStatus("Success");

			} catch (IllegalArgumentException e) {
				dto.setSaveStatus("Failed");
				dto.setErrDescription("Invalid UUID format: " + dto.getId());
				failedList.add(dto);
			} catch (Exception ex) {
				dto.setSaveStatus("Failed");
				dto.setErrDescription("Error saving record: " + ex.getMessage());
				failedList.add(dto);
			}
		}

		return failedList;
	}

	@Override
    public AOPMessageVM getConsumerDemandData(String year, String plantFKId) {
		try {
			AOPMessageVM aopMessageVM = new AOPMessageVM();
		    Plants plant = plantsRepository.findById(UUID.fromString(plantFKId)).get();
		    Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		    Sites site = siteRepository.findById(plant.getSiteFkId()).get();

			String procedureName = vertical.getName()+"_"+site.getName() +"_"+"GetConsumerDemand";
		
			List<Object[]> resultList = new ArrayList<>();
		
			resultList = getConsumerDemandDataFromSP(year, plantFKId, procedureName);
			List<ConsumerDemandDTO> dtoList = new ArrayList<>();

			for (Object[] row : resultList) {

				ConsumerDemandDTO dto = new ConsumerDemandDTO();

				dto.setNormParameterFKId(row[0] != null ? row[0].toString() : null);
				dto.setName(row[1] != null ? row[1].toString() : null);
				dto.setDisplayName(row[2] != null ? row[2].toString() : null);
				dto.setUom(row[3] != null ? row[3].toString() : null);
				dto.setNormTypeName(row[4] != null ? row[4].toString() : null);
				dto.setPreviousFYAvg(parseDouble(row[5]));
				dto.setApr(parseDouble(row[6]));
				dto.setMay(parseDouble(row[7]));
				dto.setJun(parseDouble(row[8]));
				dto.setJul(parseDouble(row[9]));
				dto.setAug(parseDouble(row[10]));
				dto.setSep(parseDouble(row[11]));
				dto.setOct(parseDouble(row[12]));
				dto.setNov(parseDouble(row[13]));
				dto.setDec(parseDouble(row[14]));
				dto.setJan(parseDouble(row[15]));
				dto.setFeb(parseDouble(row[16]));
				dto.setMar(parseDouble(row[17]));
				dto.setAuditYear(row[18] != null ? row[18].toString() : null);
				dto.setRemarks(row[19] != null ? row[19].toString() : null);
				dto.setDisplayOrder(row[20] != null ? Integer.parseInt(row[20].toString()) : null);
				Boolean isEditable = null;
				if (row[21] != null) {
					if (row[21] instanceof Boolean) {
						isEditable = (Boolean) row[21];
					} else if (row[21] instanceof Number) {
						isEditable = ((Number) row[21]).intValue() == 1;
					}
				}
				dto.setIsEditable(isEditable);
				
				dtoList.add(dto);
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(dtoList);
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<Object[]> getConsumerDemandDataFromSP(String aopYear, String plantId, String procedureName) {
		try {
			String sql = "EXEC " + "[" + procedureName + "]" + " @plantId = :plantId, @aopYear = :aopYear";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Transactional 
	@Override
	public List<ConsumerDemandDTO> saveConsumerDemandData(String year, String plantFKId,
			List<ConsumerDemandDTO> consumerDemandDTOList) {
		try {
			List<ConsumerDemandDTO> failedList = new ArrayList<>();
	
			for (ConsumerDemandDTO consumerDemandDTO : consumerDemandDTOList) {
				
				if (consumerDemandDTO.getSaveStatus() != null
						&& consumerDemandDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(consumerDemandDTO);
					continue;
				}

			for (int i = 1; i <= 12; i++) {
				
				saveConsumerDemandData(consumerDemandDTO, i, year);
				
			}
			
			if("Failed".equalsIgnoreCase(consumerDemandDTO.getSaveStatus())) {
				failedList.add(consumerDemandDTO);
			}
		}


		return failedList;
			
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save consumer demand data", ex);
		}
	}

	public void saveConsumerDemandData(ConsumerDemandDTO consumerDemandDTO, Integer i, String year) {

		UUID normParameterFKId = UUID.fromString(consumerDemandDTO.getNormParameterFKId());
		Double attributeValue = getAttributeValueForConsumerDemand(consumerDemandDTO, i);
		String remark = consumerDemandDTO.getRemarks();

	Optional<NormAttributeTransactions> existingRecord = normAttributeTransactionsRepository
			.findByNormParameterFKIdAndAOPMonthAndAuditYear(normParameterFKId, i, year);

	NormAttributeTransactions normAttributeTransactions;

	if (existingRecord.isPresent()) {
		normAttributeTransactions = existingRecord.get();
		normAttributeTransactions.setModifiedOn(new Date());
		Double existingValue = normAttributeTransactions.getAttributeValue() != null ? Double.parseDouble(normAttributeTransactions.getAttributeValue()) : null;
		boolean isRemarkValidationPassed = isRemarkValidationPassedForConsumerDemand(attributeValue, existingValue, remark, normAttributeTransactions.getRemarks());
		if(!isRemarkValidationPassed) { 
			consumerDemandDTO.setSaveStatus("Failed");
			consumerDemandDTO.setErrDescription("Please update remark");
			return;
		}
	} else {

		normAttributeTransactions = new NormAttributeTransactions();
		normAttributeTransactions.setCreatedOn(new Date());
		normAttributeTransactions.setUserName(Utility.getUserName());
		normAttributeTransactions.setNormParameterFKId(normParameterFKId);
		normAttributeTransactions.setAopMonth(i);
		normAttributeTransactions.setAuditYear(year);
	}

	normAttributeTransactions
			.setAttributeValue(attributeValue != null ? attributeValue.toString() : "0.0");
	normAttributeTransactions.setRemarks(remark);
	normAttributeTransactions.setUserName(Utility.getUserName());
	normAttributeTransactionsRepository.save(normAttributeTransactions);
}

public Double getAttributeValueForConsumerDemand(ConsumerDemandDTO consumerDemandDTO, Integer i) {
	switch (i) {
		case 1:
			return consumerDemandDTO.getJan();
		case 2:
			return consumerDemandDTO.getFeb();
		case 3:
			return consumerDemandDTO.getMar();
		case 4:
			return consumerDemandDTO.getApr();
		case 5:
			return consumerDemandDTO.getMay();
		case 6:
			return consumerDemandDTO.getJun();
		case 7:
			return consumerDemandDTO.getJul();
		case 8:
			return consumerDemandDTO.getAug();
		case 9:
			return consumerDemandDTO.getSep();
		case 10:
			return consumerDemandDTO.getOct();
		case 11:
			return consumerDemandDTO.getNov();
		case 12:
			return consumerDemandDTO.getDec();

	}
	return consumerDemandDTO.getJan();
}

}
