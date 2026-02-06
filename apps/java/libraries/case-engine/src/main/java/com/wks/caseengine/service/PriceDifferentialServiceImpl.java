package com.wks.caseengine.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.dto.PriceDifferentialTransactionDTO;
import com.wks.caseengine.dto.QualityTransactionDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.PriceDifferentialTransaction;
import com.wks.caseengine.entity.QualityTransaction;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.PriceDifferentialTransactionRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class PriceDifferentialServiceImpl implements PriceDifferentialService{
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private PlantsRepository plantsRepository;
	
	@Autowired
	private SiteRepository siteRepository;
	
	@Autowired
	private NormParametersRepository normParametersRepository;
	
	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;
	
	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;
	
	@Autowired
	private PriceDifferentialTransactionRepository priceDifferentialTransactionRepository;

	@Override
	public AOPMessageVM getPriceDifferential(String plantId, String year) {
		
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			List<Object[]> obj = new ArrayList<>();
			
				String procedureName = verticalName + "_" + site.getName() + "_GetPriceDifferential";
				obj = findByYearAndPlantId(year, UUID.fromString(plantId), procedureName);
			
			List<ConfigurationDTO> configurationDTOList = new ArrayList<>();
			int i = 0;
			for (Object[] row : obj) {
				ConfigurationDTO configurationDTO = new ConfigurationDTO();
				configurationDTO.setNormParameterFKId(row[0] != null ? row[0].toString() : "");

				configurationDTO.setApr(
						(row[1] != null && !row[1].toString().trim().isEmpty()) ? Double.parseDouble(row[1].toString())
								: 0.0);
				configurationDTO.setRemarks((row[2] != null ? row[2].toString() : ""));
					configurationDTO.setAuditYear(row[3] != null ? row[3].toString() : "");
					configurationDTO.setUOM(row[4] != null ? row[4].toString() : "");
					configurationDTO.setNormType(row[5] != null ? row[5].toString() : "");
					configurationDTO.setIsEditable(row[6] != null ? ((Boolean) row[6]).booleanValue() : null);
					configurationDTO.setProductName(row[7] != null ? row[7].toString() : "");
				
				configurationDTOList.add(configurationDTO);
			}
			Map<String, Object> map = new HashMap<>(); 
			
			List<AopCalculation> aopCalculation=aopCalculationRepository.findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId),year,"price-differential");
			map.put("configurationDTOList", configurationDTOList);
			map.put("aopCalculation", aopCalculation);
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(map);
			aopMessageVM.setMessage("Data fetched successfully");

			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM getPriceDifferentialTransaction(String plantId, String year) {
		
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			List<Object[]> obj = new ArrayList<>();
			
				String procedureName = verticalName + "_" + site.getName() + "_PriceDifferentialTransaction";
				obj = findByYearAndPlantId(year, UUID.fromString(plantId), procedureName);
			
			List<PriceDifferentialTransactionDTO> priceDifferentialTransactionDTOs = new ArrayList<>();
			for (Object[] row : obj) {
				PriceDifferentialTransactionDTO priceDifferentialTransactionDTO = new PriceDifferentialTransactionDTO();
				priceDifferentialTransactionDTO.setId(row[0] != null ? row[0].toString() : "");
				priceDifferentialTransactionDTO.setMaterialId(row[1] != null ? row[1].toString() : "");
				priceDifferentialTransactionDTO.setNormParameterTypeName(row[2] != null ? row[2].toString() : "");
				priceDifferentialTransactionDTO.setDisplayName(row[3] != null ? row[3].toString() : "");
				priceDifferentialTransactionDTO.setPercentage(
						(row[4] != null && !row[4].toString().trim().isEmpty()) ? Double.parseDouble(row[4].toString())
								: 0.0);
				priceDifferentialTransactionDTO.setPlantId(row[5] != null ? row[5].toString() : "");
				priceDifferentialTransactionDTO.setAopYear(row[6] != null ? row[6].toString() : "");
				priceDifferentialTransactionDTO.setRemark(row[10] != null ? row[10].toString() : "");
				
				
				priceDifferentialTransactionDTOs.add(priceDifferentialTransactionDTO);
			}
			Map<String, Object> map = new HashMap<>(); 
			
			map.put("data", priceDifferentialTransactionDTOs);
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(map);
			aopMessageVM.setMessage("Data fetched successfully");

			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<Object[]> findByYearAndPlantId(String aopYear, UUID plantId, String procedureName) {
		try {

			String sql = "EXEC " + procedureName
					+ " @plantId = :plantId, @aopYear = :aopYear";

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

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public AOPMessageVM savePriceDifferential(String year, String plantFKId,
			List<ConfigurationDTO> configurationDTOList) {
		try {
			List<ConfigurationDTO> failedList = new ArrayList<>();
			UUID plantId = UUID.fromString(plantFKId);
			String verticalName = plantsRepository.findVerticalNameByPlantId(plantId);
			Plants plant = plantsRepository.findById(plantId).orElseThrow();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();

			

			for (ConfigurationDTO configurationDTO : configurationDTOList) {
				if (configurationDTO.getSaveStatus() != null
						&& configurationDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(configurationDTO);
					continue;
				}

				UUID normParameterFKId = UUID.fromString(configurationDTO.getNormParameterFKId());

				Optional<NormParameters> optionNormParameters = normParametersRepository.findById(normParameterFKId);
				if (!optionNormParameters.isPresent()) {
					configurationDTO.setSaveStatus("Failed");
					configurationDTO.setErrDescription("Norm Paramter not found");
					failedList.add(configurationDTO);
					continue;
				}
				if (optionNormParameters.isPresent() && (!optionNormParameters.get().getIsEditable())) {
					continue;
				}

				for (int i = 1; i <= 12; i++) {
					Double attributeValue = getAttributeValue(configurationDTO, i);
					configurationDTO.setVertical(verticalName);
					saveData(optionNormParameters.get(), i, year, attributeValue, configurationDTO,plantFKId);
					if(configurationDTO.getSaveStatus()!=null && configurationDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
						failedList.add(configurationDTO);
						break;
					}
				}
			}
			List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("price-differential");
			for (ScreenMapping screenMapping : screenMappingList) {
				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantFKId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(failedList);
			aopMessageVM.setMessage("Data updated successfully");
			return aopMessageVM;
		} catch (Exception ex) {
			ex.printStackTrace();
			
			throw new RuntimeException("Failed to save data", ex);
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public AOPMessageVM savePriceDifferentialTransaction(String year, String plantFKId,
			List<PriceDifferentialTransactionDTO> priceDifferentialTransactionDTOs) {
		try {
			List<PriceDifferentialTransactionDTO> failedList = new ArrayList<>();
			UUID plantId = UUID.fromString(plantFKId);

			for (PriceDifferentialTransactionDTO priceDifferentialTransactionDTO : priceDifferentialTransactionDTOs) {
				if (priceDifferentialTransactionDTO.getSaveStatus() != null
						&& priceDifferentialTransactionDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(priceDifferentialTransactionDTO);
					continue;
				}
				PriceDifferentialTransaction priceDifferentialTransaction =null;
				UUID material=UUID.fromString(priceDifferentialTransactionDTO.getMaterialId());
				Optional<PriceDifferentialTransaction> priceDifferentialTransactionOpt =priceDifferentialTransactionRepository.findByMaterialPlantAndYear(material,plantId,year);
				if(priceDifferentialTransactionOpt.isPresent()) {
					priceDifferentialTransaction=priceDifferentialTransactionOpt.get();
				}else {
					priceDifferentialTransaction = new PriceDifferentialTransaction();
					priceDifferentialTransaction.setMaterialId(UUID.fromString(priceDifferentialTransactionDTO.getMaterialId()));
					priceDifferentialTransaction.setAopYear(year);
					priceDifferentialTransaction.setPlantId(plantId);
				}
				priceDifferentialTransaction.setPercentage(priceDifferentialTransactionDTO.getPercentage());
				priceDifferentialTransaction.setRemark(priceDifferentialTransactionDTO.getRemark());
				priceDifferentialTransaction.setUpdatedBy(Utility.getUserName());
				priceDifferentialTransaction.setModifiedOn(new Date());
				
				priceDifferentialTransactionRepository.save(priceDifferentialTransaction);
			}
			
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(failedList);
			aopMessageVM.setMessage("Data updated successfully");
			return aopMessageVM;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to save data", ex);
		}
	}

	
	void saveData(NormParameters normParameter, Integer i, String year, Double attributeValue,
			ConfigurationDTO configurationDTO,String plantFKId) {
		
		Optional<NormAttributeTransactions> existingRecord=null;
		
			 existingRecord = normAttributeTransactionsRepository
					.findByNormParameterFKIdAndAOPMonthAndAuditYear(normParameter.getId(), i, year);
		

		NormAttributeTransactions normAttributeTransactions;

		if (existingRecord.isPresent()) {

			normAttributeTransactions = existingRecord.get();
			normAttributeTransactions.setModifiedOn(new Date());
		} else {

			normAttributeTransactions = new NormAttributeTransactions();
			
			normAttributeTransactions.setCreatedOn(new Date());
			 
				normAttributeTransactions.setAttributeValueVersion("V1");
			
			
			normAttributeTransactions.setUserName(Utility.getUserName());
			normAttributeTransactions.setNormParameterFKId(normParameter.getId());
			normAttributeTransactions.setAopMonth(i);
			
			normAttributeTransactions.setAuditYear(year);
		}
		

		// Initial values
		String entityRemarks = normAttributeTransactions.getRemarks();
		String dtoRemarks = configurationDTO.getRemarks();

		String existingValue = normAttributeTransactions.getAttributeValue();
		String newValue = (attributeValue != null) ? attributeValue.toString() : null;

		// Determine if either field changed meaningfully
		boolean remarksChanged = !isBlank(dtoRemarks);
		  

		boolean attributeChanged = newValue != null 
		    && !newValue.equalsIgnoreCase(existingValue);

		if(newValue!=null && newValue.equalsIgnoreCase("0.0") && !existingRecord.isPresent()) {
			return;
		}
		if (remarksChanged) {
			// Update entity
			normAttributeTransactions.setAttributeValue(newValue != null ? newValue : "0.0");
			normAttributeTransactions.setRemarks(dtoRemarks);
		    normAttributeTransactionsRepository.save(normAttributeTransactions);
		} else if (!remarksChanged && attributeChanged) {
		    configurationDTO.setSaveStatus("Failed");
		    configurationDTO.setErrDescription("Please add/update remark or attribute value");
		}
		
	}
	
	boolean isBlank(String s) {
	    return s == null || s.isBlank(); // Java 11+; else use trim().isEmpty()
	}

	public Double getAttributeValue(ConfigurationDTO configurationDTO, Integer i) {
		switch (i) {
			case 1:
				return configurationDTO.getJan();
			case 2:
				return configurationDTO.getFeb();
			case 3:
				return configurationDTO.getMar();
			case 4:
				return configurationDTO.getApr();
			case 5:
				return configurationDTO.getMay();
			case 6:
				return configurationDTO.getJun();
			case 7:
				return configurationDTO.getJul();
			case 8:
				return configurationDTO.getAug();
			case 9:
				return configurationDTO.getSep();
			case 10:
				return configurationDTO.getOct();
			case 11:
				return configurationDTO.getNov();
			case 12:
				return configurationDTO.getDec();

		}
		return configurationDTO.getJan();
	}

	public byte[] exportPriceDifferentialTransaction(String year, String plantId, boolean isAfterSave, List<PriceDifferentialTransactionDTO> dtoList) {
	    try {   
	        if (!isAfterSave) {
	        	AOPMessageVM aopMessageVM = getPriceDifferentialTransaction(plantId,year);
	        	Map<String, Object> innerMap = (Map<String, Object>) aopMessageVM.getData();

		        if (innerMap != null) {
		             dtoList = (List<PriceDifferentialTransactionDTO>) innerMap.get("data");
		        }
	        }

	        Workbook workbook = new XSSFWorkbook();
	        Sheet sheet = workbook.createSheet("Sheet1");
	        int currentRow = 0;

	        List<String> innerHeaders = new ArrayList<>();
	        innerHeaders.add("Quality Type");
	        innerHeaders.add("Value %");
	        innerHeaders.add("Material Id");
	       
	        if (isAfterSave) {
	            innerHeaders.add("Status");
	            innerHeaders.add("Error Description");
	        }
	        Row headerRow = sheet.createRow(currentRow++);
	        for (int col = 0; col < innerHeaders.size(); col++) {
	            Cell cell = headerRow.createCell(col);
	            cell.setCellValue(innerHeaders.get(col));
	            cell.setCellStyle(Utility.createBoldBorderedStyle(workbook));
	        }

	        int dataRowCount = dtoList.size();
	        for (int i = 0; i < dataRowCount; i++) {
	        	PriceDifferentialTransactionDTO dto = dtoList.get(i);
	            Row row = sheet.createRow(currentRow++);
	            List<Object> rowData = new ArrayList<>();
	            
	            rowData.add(dto.getDisplayName());
	            rowData.add(dto.getPercentage());
	            rowData.add(dto.getMaterialId());
	            
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
	            }
	        }
	        sheet.setColumnHidden(2, true);
	        
	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        workbook.write(outputStream);
	        workbook.close();
	        return outputStream.toByteArray();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	@Override
	public AOPMessageVM importPriceDifferentialTransaction(String year,UUID plantId,MultipartFile file) {
		try {
			List<PriceDifferentialTransactionDTO> data = readPriceDifferentialTransaction(file.getInputStream(), plantId, year);
			 AOPMessageVM aopMessageVM = savePriceDifferentialTransaction(year, plantId.toString(),data);
			 List<PriceDifferentialTransactionDTO> failedList = (List<PriceDifferentialTransactionDTO>) aopMessageVM.getData();
			
			if (failedList != null && failedList.size() > 0) {
				byte[] fileByteArray = exportPriceDifferentialTransaction(year, plantId.toString(), true, failedList);
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
	
	public List<PriceDifferentialTransactionDTO> readPriceDifferentialTransaction(InputStream inputStream, UUID plantFKId, String year) {
	    List<PriceDifferentialTransactionDTO> priceDifferentialTransactionDTOs = new ArrayList<>();

	    try (Workbook workbook = new XSSFWorkbook(inputStream)) {
	        Sheet sheet = workbook.getSheetAt(0);
	        Iterator<Row> rowIterator = sheet.iterator();

	        if (rowIterator.hasNext())
	            rowIterator.next();  

	        while (rowIterator.hasNext()) {
	            Row row = rowIterator.next();
	            
	            PriceDifferentialTransactionDTO dto = new PriceDifferentialTransactionDTO();
	            try {
	            	dto.setDisplayName(getStringCellValue(row.getCell(0), dto));
	            	Double cellValue = getNumericCellValue(row.getCell(1), dto);
	                if (cellValue != null) {
	                    if (cellValue >= 0 && cellValue <= 100) {
	                        dto.setPercentage(cellValue);
	                    } else {
	                    	dto.setErrDescription("Percentage value should be between 0 to 100");
	    	                dto.setSaveStatus("Failed");
	                    }
	                }
	                dto.setPercentage(getNumericCellValue(row.getCell(1), dto));
	                dto.setMaterialId(getStringCellValue(row.getCell(2), dto));
	                dto.setId(getStringCellValue(row.getCell(3), dto));
	                dto.setAopYear(year);
	                dto.setPlantId(plantFKId.toString());
	              } 
	              catch (Exception e) {
	                e.printStackTrace();
	                dto.setErrDescription(e.getMessage());
	                dto.setSaveStatus("Failed");
	            }
	            priceDifferentialTransactionDTOs.add(dto);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return priceDifferentialTransactionDTOs;
	}
	private static String getStringCellValue(Cell cell, PriceDifferentialTransactionDTO dto) {
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
	private static Double getNumericCellValue(Cell cell, PriceDifferentialTransactionDTO dto) {
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


}
