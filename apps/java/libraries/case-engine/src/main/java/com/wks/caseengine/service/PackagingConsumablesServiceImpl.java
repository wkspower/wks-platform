package com.wks.caseengine.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.dto.OtherCostsTransactionDto;
import com.wks.caseengine.dto.PackagingAndConsumableTransactionDTO;
import com.wks.caseengine.dto.PriceDifferentialTransactionDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.PackagingAndConsumableTransaction;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.PriceDifferentialTransaction;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.PackagingAndConsumableTransactionRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class PackagingConsumablesServiceImpl implements PackagingConsumablesService{
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private PlantsRepository plantsRepository;
	
	@Autowired
	private SiteRepository siteRepository;
	
	@Autowired
	private VerticalsRepository verticalRepository;
	
	@Autowired
	private NormParametersRepository normParametersRepository;
	
	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;
	
	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;
	
	@Autowired
	private PackagingAndConsumableTransactionRepository packagingAndConsumableTransactionRepository;
	
	private DataSource dataSource;
	
	public PackagingConsumablesServiceImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@Override
	public AOPMessageVM getPackagingConsumables(String plantId, String year) {
		
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			List<Object[]> obj = new ArrayList<>();
			
				String procedureName = verticalName + "_" + site.getName() + "_GetPackagingConsumables";
				obj = findByYearAndPlantId(year, UUID.fromString(plantId), procedureName);
			
			List<ConfigurationDTO> configurationDTOList = new ArrayList<>();
			int i = 0;
			for (Object[] row : obj) {
				ConfigurationDTO configurationDTO = new ConfigurationDTO();
				configurationDTO.setNormParameterFKId(row[0] != null ? row[0].toString() : "");

				configurationDTO.setJan(
						(row[1] != null && !row[1].toString().trim().isEmpty())
								? Double.parseDouble(row[1].toString().trim())
								: 0.0);
				configurationDTO.setFeb(
						(row[2] != null && !row[2].toString().trim().isEmpty()) ? Double.parseDouble(row[2].toString())
								: 0.0);
				configurationDTO.setMar(
						(row[3] != null && !row[3].toString().trim().isEmpty()) ? Double.parseDouble(row[3].toString())
								: 0.0);
				configurationDTO.setApr(
						(row[4] != null && !row[4].toString().trim().isEmpty()) ? Double.parseDouble(row[4].toString())
								: 0.0);
				configurationDTO.setMay(
						(row[5] != null && !row[5].toString().trim().isEmpty()) ? Double.parseDouble(row[5].toString())
								: 0.0);
				configurationDTO.setJun(
						(row[6] != null && !row[6].toString().trim().isEmpty()) ? Double.parseDouble(row[6].toString())
								: 0.0);
				configurationDTO.setJul(
						(row[7] != null && !row[7].toString().trim().isEmpty()) ? Double.parseDouble(row[7].toString())
								: 0.0);
				configurationDTO.setAug(
						(row[8] != null && !row[8].toString().trim().isEmpty()) ? Double.parseDouble(row[8].toString())
								: 0.0);
				configurationDTO.setSep(
						(row[9] != null && !row[9].toString().trim().isEmpty()) ? Double.parseDouble(row[9].toString())
								: 0.0);
				configurationDTO.setOct((row[10] != null && !row[10].toString().trim().isEmpty())
						? Double.parseDouble(row[10].toString())
						: 0.0);
				configurationDTO.setNov((row[11] != null && !row[11].toString().trim().isEmpty())
						? Double.parseDouble(row[11].toString())
						: 0.0);
				configurationDTO.setDec((row[12] != null && !row[12].toString().trim().isEmpty())
						? Double.parseDouble(row[12].toString())
						: 0.0);
				configurationDTO.setRemarks((row[13] != null ? row[13].toString() : ""));
					configurationDTO.setAuditYear(row[14] != null ? row[14].toString() : "");
					configurationDTO.setUOM(row[15] != null ? row[15].toString() : "");
					configurationDTO.setNormType(row[16] != null ? row[16].toString() : "");
					configurationDTO.setIsEditable(row[17] != null ? ((Boolean) row[17]).booleanValue() : null);
					configurationDTO.setProductName(row[18] != null ? row[18].toString() : "");
				
				configurationDTOList.add(configurationDTO);
				if (row[14] == null) {
					i++;
				}
			}
			Map<String, Object> map = new HashMap<>(); 
			
			List<AopCalculation> aopCalculation=aopCalculationRepository.findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId),year,"packaging-consumables");
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
	public AOPMessageVM getPackagingConsumablesTransaction(String plantId, String year) {
		
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			List<Object[]> obj = new ArrayList<>();
			
				String procedureName = verticalName + "_" + site.getName() + "_PackagingAndConsumableTransaction";
				obj = findByYearAndPlantId(year, UUID.fromString(plantId), procedureName);
			
			List<PackagingAndConsumableTransactionDTO> packagingAndConsumableTransactionDTOs = new ArrayList<>();
			int i = 0;
			for (Object[] row : obj) {
				PackagingAndConsumableTransactionDTO packagingAndConsumableTransactionDTO = new PackagingAndConsumableTransactionDTO();
				packagingAndConsumableTransactionDTO.setId(row[0] != null ? row[0].toString() : "");
				packagingAndConsumableTransactionDTO.setMaterialId(row[1] != null ? row[1].toString() : "");
				packagingAndConsumableTransactionDTO.setSapMaterialCode(row[2] != null ? row[2].toString() : "");
				packagingAndConsumableTransactionDTO.setNormParameterTypeName(row[3] != null ? row[3].toString() : "");
				packagingAndConsumableTransactionDTO.setDisplayName(row[4] != null ? row[4].toString() : "");
				packagingAndConsumableTransactionDTO.setUom(row[5] != null ? row[5].toString() : "");
				packagingAndConsumableTransactionDTO.setPackagingPrice(
						(row[6] != null && !row[6].toString().trim().isEmpty())
								? Double.parseDouble(row[6].toString().trim())
								: 0.0);
				packagingAndConsumableTransactionDTO.setPrevBudget(
						(row[7] != null && !row[7].toString().trim().isEmpty()) ? Double.parseDouble(row[7].toString())
								: 0.0);
				packagingAndConsumableTransactionDTO.setPrevActual(
						(row[8] != null && !row[8].toString().trim().isEmpty()) ? Double.parseDouble(row[8].toString())
								: 0.0);
				packagingAndConsumableTransactionDTO.setProposedNorm(
						(row[9] != null && !row[9].toString().trim().isEmpty()) ? Double.parseDouble(row[9].toString())
								: 0.0);
				packagingAndConsumableTransactionDTO.setPlantId(row[10] != null ? row[10].toString() : "");
				packagingAndConsumableTransactionDTO.setAopYear(row[11] != null ? row[11].toString() : "");
				packagingAndConsumableTransactionDTO.setRemark((row[14] != null ? row[14].toString() : ""));
				packagingAndConsumableTransactionDTOs.add(packagingAndConsumableTransactionDTO);
				
			}
			Map<String, Object> map = new HashMap<>();

			List<AopCalculation> aopCalculation = aopCalculationRepository
					.findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year, "packaging-norms");
			
			map.put("aopCalculation", aopCalculation);
			map.put("data", packagingAndConsumableTransactionDTOs);
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
	public AOPMessageVM getCalculatePackagingNorms(String year, String plantId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_CalculatePackagingNorms";
			
			Integer result=  executeDynamicUpdateProcedure(storedProcedure, plantId, year);
			aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId),year,"packaging-norms");
			List<ScreenMapping> screenMappingList= screenMappingRepository.findByDependentScreen("packaging-norms");
			for(ScreenMapping screenMapping:screenMappingList) {
				AopCalculation aopCalculation=new AopCalculation();
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return aopMessageVM;
	}

	public int executeDynamicUpdateProcedure(String procedureName, String plantId,
			String aopYear) {
		try {
			
			String callSql = "{call " + procedureName + "(?, ?)}";

	        try (Connection connection = dataSource.getConnection();
	             CallableStatement stmt = connection.prepareCall(callSql)) {

	            stmt.setString(1, plantId); 
	            stmt.setString(2, aopYear); 
	            int rowsAffected = stmt.executeUpdate();

	            if (!connection.getAutoCommit()) {
	                connection.commit();
	            }

	            return rowsAffected;

	        } catch (SQLException e) {
	            e.printStackTrace();
	            return 0;
	        }

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public AOPMessageVM savePackagingConsumablesTransaction(String year, String plantFKId,
			List<PackagingAndConsumableTransactionDTO> packagingAndConsumableTransactionDTOs) {
		try {
			List<PackagingAndConsumableTransactionDTO> failedList = new ArrayList<>();
			UUID plantId = UUID.fromString(plantFKId);

			for (PackagingAndConsumableTransactionDTO packagingAndConsumableTransactionDTO : packagingAndConsumableTransactionDTOs) {
				if (packagingAndConsumableTransactionDTO.getSaveStatus() != null
						&& packagingAndConsumableTransactionDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(packagingAndConsumableTransactionDTO);
					continue;
				}
				Boolean update=false;
				Boolean changed=false;
				PackagingAndConsumableTransaction packagingAndConsumableTransaction =null;
				UUID material=UUID.fromString(packagingAndConsumableTransactionDTO.getMaterialId());
				Optional<PackagingAndConsumableTransaction> packagingAndConsumableTransactionOpt =packagingAndConsumableTransactionRepository.findByMaterialPlantAndYear(material,plantId,year);
				if(packagingAndConsumableTransactionOpt.isPresent()) {
					packagingAndConsumableTransaction=packagingAndConsumableTransactionOpt.get();
					update=true;
				}else {
					update=false;
					packagingAndConsumableTransaction = new PackagingAndConsumableTransaction();
					packagingAndConsumableTransaction.setMaterialId(UUID.fromString(packagingAndConsumableTransactionDTO.getMaterialId()));
					packagingAndConsumableTransaction.setAopYear(year);
					packagingAndConsumableTransaction.setPlantId(plantId);
					packagingAndConsumableTransaction.setPrevBudget(packagingAndConsumableTransactionDTO.getPrevBudget());
					packagingAndConsumableTransaction.setPrevActual(packagingAndConsumableTransactionDTO.getPrevActual());
				}
				if (update) {
				    if (!Objects.equals(packagingAndConsumableTransaction.getPackagingPrice(), packagingAndConsumableTransactionDTO.getPackagingPrice())) {
				        changed = true;
				    }
				    if (!Objects.equals(packagingAndConsumableTransaction.getProposedNorm(), packagingAndConsumableTransactionDTO.getProposedNorm())) {
				        changed = true;
				    }
				    
				    if (!Objects.equals(packagingAndConsumableTransaction.getPrevActual(), packagingAndConsumableTransactionDTO.getPrevActual())) {
				        changed = true;
				    }
				    
				    if (changed) {
				        String existingRemark = packagingAndConsumableTransaction.getRemark();
				        String newRemark = packagingAndConsumableTransactionDTO.getRemark();

				        if (Objects.equals(existingRemark, newRemark) || 
				           (existingRemark != null && existingRemark.equalsIgnoreCase(newRemark))) {
				            
				        	packagingAndConsumableTransactionDTO.setErrDescription("Please update remark");
				        	packagingAndConsumableTransactionDTO.setSaveStatus("Failed");
				            failedList.add(packagingAndConsumableTransactionDTO);
				            continue;
				        }
				    }
				}else {
					if(packagingAndConsumableTransactionDTO.getRemark()==null) {
						packagingAndConsumableTransactionDTO.setErrDescription("Please add remark");
						packagingAndConsumableTransactionDTO.setSaveStatus("Failed");
				            failedList.add(packagingAndConsumableTransactionDTO);
				            continue;
					}
				}

				packagingAndConsumableTransaction.setPackagingPrice(packagingAndConsumableTransactionDTO.getPackagingPrice());
				packagingAndConsumableTransaction.setRemark(packagingAndConsumableTransactionDTO.getRemark());
				packagingAndConsumableTransaction.setUpdatedBy(Utility.getUserName());
				packagingAndConsumableTransaction.setModifiedOn(new Date());
				packagingAndConsumableTransaction.setProposedNorm(packagingAndConsumableTransactionDTO.getProposedNorm());
				packagingAndConsumableTransaction.setPrevActual(packagingAndConsumableTransactionDTO.getPrevActual());
				packagingAndConsumableTransactionRepository.save(packagingAndConsumableTransaction);
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
	public AOPMessageVM savePackagingConsumables(String year, String plantFKId,
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
			List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("packaging-consumables");
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
	public byte[] exportPackagingConsumablesTransaction(String year, String plantId, boolean isAfterSave, List<PackagingAndConsumableTransactionDTO> dtoList) {
	    try {   
	        if (!isAfterSave) {
	        	AOPMessageVM aopMessageVM = getPackagingConsumablesTransaction(plantId,year);
	        	Map<String, Object> innerMap = (Map<String, Object>) aopMessageVM.getData();

		        if (innerMap != null) {
		             dtoList = (List<PackagingAndConsumableTransactionDTO>) innerMap.get("data");
		        }
	        }

	        Workbook workbook = new XSSFWorkbook();
	        Sheet sheet = workbook.createSheet("Sheet1");
	        int currentRow = 0;

	        List<String> innerHeaders = new ArrayList<>();
	        innerHeaders.add("SAP Material Code");
	        innerHeaders.add("Name of Item");
	        innerHeaders.add("Unit");
	        innerHeaders.add("Packaging Price (Rs)");
	        innerHeaders.add("Budget "+getNextFiscalYear(year));
	        innerHeaders.add("Actual "+getNextFiscalYear(year));
	        innerHeaders.add("Proposed Cost "+year);
	        innerHeaders.add("Remarks");
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
	        	PackagingAndConsumableTransactionDTO dto = dtoList.get(i);
	            Row row = sheet.createRow(currentRow++);
	            List<Object> rowData = new ArrayList<>();
	            rowData.add(dto.getSapMaterialCode());
	            rowData.add(dto.getDisplayName());
	            rowData.add(dto.getUom());
	            rowData.add(dto.getPackagingPrice());
	            rowData.add(dto.getPrevBudget());
	            rowData.add(dto.getPrevActual());
	            rowData.add(dto.getProposedNorm());
	            rowData.add(dto.getRemark());
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
	        sheet.setColumnHidden(8, true);
	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        workbook.write(outputStream);
	        workbook.close();
	        return outputStream.toByteArray();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	public String getNextFiscalYear(String currentYear) {
	    String[] parts = currentYear.split("-");
	    
	    int startYear = Integer.parseInt(parts[0]);
	    int endYearSuffix = Integer.parseInt(parts[1]);
	    int nextStartYear = startYear - 1;
	    int nextEndYearSuffix = endYearSuffix - 1;
	    return nextStartYear + "-" + String.format("%02d", nextEndYearSuffix % 100);
	}

	@Override
	public AOPMessageVM importPackagingConsumablesTransaction(String year,UUID plantId,MultipartFile file) {
		try {
			List<PackagingAndConsumableTransactionDTO> data = readPackagingConsumablesTransaction(file.getInputStream(), plantId, year);
			 AOPMessageVM aopMessageVM = savePackagingConsumablesTransaction(year, plantId.toString(),data);
			 List<PackagingAndConsumableTransactionDTO> failedList = (List<PackagingAndConsumableTransactionDTO>) aopMessageVM.getData();
			
			if (failedList != null && failedList.size() > 0) {
				byte[] fileByteArray = exportPackagingConsumablesTransaction(year, plantId.toString(), true, failedList);
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
	
	public List<PackagingAndConsumableTransactionDTO> readPackagingConsumablesTransaction(InputStream inputStream, UUID plantFKId, String year) {
	    List<PackagingAndConsumableTransactionDTO> packagingAndConsumableTransactionDTOs = new ArrayList<>();

	    try (Workbook workbook = new XSSFWorkbook(inputStream)) {
	        Sheet sheet = workbook.getSheetAt(0);
	        Iterator<Row> rowIterator = sheet.iterator();

	        if (rowIterator.hasNext())
	            rowIterator.next();  

	        while (rowIterator.hasNext()) {
	            Row row = rowIterator.next();
	            
	            PackagingAndConsumableTransactionDTO dto = new PackagingAndConsumableTransactionDTO();
	            try {
	            	dto.setSapMaterialCode(getStringCellValue(row.getCell(0), dto));
	                dto.setDisplayName(getStringCellValue(row.getCell(1), dto));
	                dto.setUom(getStringCellValue(row.getCell(2), dto));
	                dto.setPackagingPrice(getNumericCellValue(row.getCell(3), dto));
	                dto.setPrevBudget(getNumericCellValue(row.getCell(4), dto));
	                dto.setPrevActual(getNumericCellValue(row.getCell(5), dto));
	                dto.setProposedNorm(getNumericCellValue(row.getCell(6), dto));
	                dto.setRemark(getStringCellValue(row.getCell(7), dto));
	                dto.setMaterialId(getStringCellValue(row.getCell(8), dto));
	                dto.setPlantId(plantFKId.toString());
	                dto.setAopYear(year);
	              } 
	              catch (Exception e) {
	                e.printStackTrace();
	                dto.setErrDescription(e.getMessage());
	                dto.setSaveStatus("Failed");
	            }
	            packagingAndConsumableTransactionDTOs.add(dto);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return packagingAndConsumableTransactionDTOs;
	}

	private static java.util.Date getDateCellValue(Cell cell, PackagingAndConsumableTransactionDTO dto) {
	    if (cell == null || cell.getCellType() == CellType.BLANK) {
	        return null;
	    }

	    if (cell.getCellType() == CellType.NUMERIC) {
	        if (DateUtil.isCellDateFormatted(cell)) {
	            return cell.getDateCellValue();
	        } else {
	            dto.setSaveStatus("Failed");
	            dto.setErrDescription("Invalid date format in cell");
	        }
	    } else if (cell.getCellType() == CellType.STRING) {
	        String val = cell.getStringCellValue().trim();
	        if (val.isEmpty()) {
	            return null; 
	        }
	        try {
	            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
	            return sdf.parse(val);
	        } catch (java.text.ParseException e) {
	            dto.setSaveStatus("Failed");
	            dto.setErrDescription("Please enter date in correct format (yyyy-MM-dd)");
	        }
	    }
	    return null;
	}
	private static Integer getIntegerCellValue(Cell cell, PackagingAndConsumableTransactionDTO dto) {
	    if (cell == null || cell.getCellType() == CellType.BLANK) {
	        return null;
	    }

	    if (cell.getCellType() == CellType.NUMERIC) {
	        
	        return (int) cell.getNumericCellValue();
	    } 
	    
	    if (cell.getCellType() == CellType.STRING) {
	        String val = cell.getStringCellValue().trim();
	        if (val.isEmpty()) {
	            return null; 
	        }
	        try {
	            
	            return Integer.parseInt(val);
	        } catch (NumberFormatException e) {
	            dto.setSaveStatus("Failed");
	            dto.setErrDescription("Please enter valid integer values");
	        }
	    }
	    return null;
	}
	private static String getStringCellValue(Cell cell, PackagingAndConsumableTransactionDTO dto) {
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
	private static Double getNumericCellValue(Cell cell, PackagingAndConsumableTransactionDTO dto) {
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

	@Override
	public AOPMessageVM getQualityPackaging(
	    String plantId, String aopYear, String periodFrom, String periodTo,String type) {

	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    
	    Plants plant = plantsRepository.findById(UUID.fromString(plantId))
	                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
	    Sites site = siteRepository.findById(plant.getSiteFkId())
	                .orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
	    Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
	                .orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
	    String storedProcedure = vertical.getName() + "_" + site.getName() + "_QualityPackagingBasisReport";
	    
	    try {
	        List<List<Map<String, Object>>> allColMetadata = getAllColumnMetadataForPEE(
	                plantId, aopYear, periodFrom, periodTo, type, storedProcedure);

	        List<List<Object[]>> allGridData = getReportDataForPEE(
	                plantId, aopYear, periodFrom, periodTo, type, storedProcedure);

	       
	        if (allColMetadata.size() != allGridData.size()) {
	             throw new RuntimeException("Mismatch: Stored procedure returned " + allColMetadata.size()
	                        + " metadata lists but " + allGridData.size() + " data grids.");
	        }
	        List<Map<String, Object>> combined = new ArrayList<>();
	        
	        for (int i = 0; i < allGridData.size(); i++) {
	            List<Map<String, Object>> colMetadata = allColMetadata.get(i);
	            List<Object[]> rawRows = allGridData.get(i);
	            
	            List<String> colNames = colMetadata.stream()
	                                              .map(m -> (String)m.get("field"))
	                                              .collect(Collectors.toList());

	            String gridName = "UNKNOWN_GRID_" + (i + 1); // Default name
	            if (!colNames.isEmpty()) {
	                int lastColIdx = colNames.size() - 1;
	                if (colNames.get(lastColIdx).equalsIgnoreCase("GRID_TYPE") && !rawRows.isEmpty()) {
	                    Object gridTypeVal = rawRows.get(0)[lastColIdx];
	                    if (gridTypeVal != null) {
	                        gridName = gridTypeVal.toString();
	                    }
	                } else {
	                    gridName = colNames.get(0); 
	                }
	            }
	         	List<Map<String, Object>> gridDataMap = new ArrayList<>();
	            for (Object[] row : rawRows) {
	                Map<String, Object> rowMap = new LinkedHashMap<>();
	                for (int j = 0; j < colNames.size(); j++) {
	                    rowMap.put(colNames.get(j), row[j]);
	                }
	                gridDataMap.add(rowMap);
	            }

	            Map<String, Object> part = new LinkedHashMap<>();
	            part.put("gridName", gridName);
	            part.put("data", gridDataMap);
	            part.put("columns", colMetadata); 
	            combined.add(part);
	        }

	        aopMessageVM.setCode(200);
	        aopMessageVM.setMessage("SP Executed successfully");
	        aopMessageVM.setData(combined); 
	        return aopMessageVM;

	    } catch (Exception e) {
	        e.printStackTrace();
	        aopMessageVM.setCode(500); 
	        aopMessageVM.setMessage("Error processing report data: " + e.getMessage());
	        aopMessageVM.setData(null);
	        return aopMessageVM;
	    }
	}
	@Transactional(readOnly = true)
	public List<List<Map<String, Object>>> getAllColumnMetadataForPEE(
	    String plantId, String aopYear, String periodFrom, String periodTo, String type, String storedProcedure) {

	    String storedProcedureCall = "{ call " + storedProcedure + "(?, ?, ?, ?) }";

	    Session session = entityManager.unwrap(Session.class);

	    return session.doReturningWork(connection -> {
	        List<List<Map<String, Object>>> allMetadataGrids = new ArrayList<>();

	        try (java.sql.CallableStatement callableStatement = connection.prepareCall(storedProcedureCall)) {

	            callableStatement.setString(1, plantId);
	            callableStatement.setString(2, aopYear);
	            callableStatement.setString(3, periodFrom);
	            callableStatement.setString(4, periodTo);

	            boolean results = callableStatement.execute();

	            while (results || callableStatement.getUpdateCount() != -1) {
	                if (results) {
	                    try (java.sql.ResultSet rs = callableStatement.getResultSet()) {
	                        java.sql.ResultSetMetaData rsMetaData = rs.getMetaData();
	                        List<Map<String, Object>> currentMetadata = new ArrayList<>();

	                        for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
	                            Map<String, Object> columnInfo = new HashMap<>();
	                            String columnName = rsMetaData.getColumnLabel(i);
	                            String columnType = rsMetaData.getColumnTypeName(i);

	                            columnInfo.put("field", columnName);
	                            columnInfo.put("title", formatTitle(columnName)); // Use your formatting method
	                            columnInfo.put("editable", false); // Example property
	                            columnInfo.put("type", getFrontendType(columnType)); // Use your type mapping method
	                            currentMetadata.add(columnInfo);
	                        }
	                        allMetadataGrids.add(currentMetadata);
	                    }
	                }
	                // Move to the next result set or update count
	                results = callableStatement.getMoreResults();
	            }

	            return allMetadataGrids;

	        } catch (java.sql.SQLException e) {
	            throw new RuntimeException("Error executing stored procedure for metadata: " + storedProcedure + ". SQL Error: " + e.getMessage(), e);
	        }
	    });
	}
	
	// Helper method to format column titles
		private String formatTitle(String columnName) {
			return columnName.replace("_", " ");
		}

		// Helper method to map SQL data types to frontend types
		private String getFrontendType(String sqlTypeName) {
			switch (sqlTypeName.toUpperCase()) {
				case "VARCHAR":
				case "NVARCHAR":
				case "CHAR":
					return "string";
				case "INT":
				case "TINYINT":
				case "BIGINT":
				case "SMALLINT":
				case "DECIMAL":
				case "FLOAT":
				case "DOUBLE":
				case "NUMERIC":
					return "number";
				case "DATE":
				case "DATETIME":
				case "DATETIME2":
					return "date";
				default:
					return "string";
			}
		}

	
	@Transactional(readOnly = true) 
	public List<List<Object[]>> getReportDataForPEE(String plantId, String aopYear, String periodFrom, String periodTo,String type,String storedProcedure) {
   
	    String storedProcedureCall = "{ call " + storedProcedure + "(?, ?, ?, ?) }";
	   
	    Session session = entityManager.unwrap(Session.class);
  
	    return session.doReturningWork(connection -> {
	        
	        List<List<Object[]>> allGrids = new ArrayList<>();

	        try (java.sql.CallableStatement callableStatement = connection.prepareCall(storedProcedureCall)) {
	            
	            callableStatement.setString(1, plantId);
	            callableStatement.setString(2, aopYear);
	            callableStatement.setString(3, periodFrom);
	            callableStatement.setString(4, periodTo);

	            boolean results = callableStatement.execute();

	            while (results || callableStatement.getUpdateCount() != -1) {
	                if (results) {
	                    try (java.sql.ResultSet rs = callableStatement.getResultSet()) {
	                        java.sql.ResultSetMetaData metaData = rs.getMetaData();
	                        int columnCount = metaData.getColumnCount();

	                        List<Object[]> currentGrid = new ArrayList<>();
	                        while (rs.next()) {
	                            Object[] row = new Object[columnCount];
	                            for (int i = 1; i <= columnCount; i++) {
	                                
	                                row[i - 1] = rs.getObject(i);
	                            }
	                            currentGrid.add(row);
	                        }
	                        allGrids.add(currentGrid);
	                    }
	                }

	                
	                results = callableStatement.getMoreResults();
	            }

	            return allGrids;

	        } catch (java.sql.SQLException e) {
	            // Include the dynamic SP name in the error message for better debugging
	            throw new RuntimeException("Error executing stored procedure: " + storedProcedure + ". SQL Error: " + e.getMessage(), e);
	        }
	    });
	}



	@Override
	@Transactional
	public AOPMessageVM getCalculateOtherProductionNorms(String year, String plantId) {

		AOPMessageVM aopMessageVM = new AOPMessageVM();

		try {
			UUID plantUUID = UUID.fromString(plantId);

			Plants plant = plantsRepository.findById(plantUUID).orElseThrow();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).orElseThrow();

			String storedProcedure = vertical.getName() + "_" + site.getName() + "_CalculateOtherProduction";

			Integer result = executeDynamicUpdateProcedure(storedProcedure, plantId, year);

			aopCalculationRepository
					.deleteByPlantIdAndAopYearAndCalculationScreen(
							plantUUID,
							year,
							"other-production");

			List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("other-production");

			for (ScreenMapping screenMapping : screenMappingList) {

				if (screenMapping.getCalculationScreen()
						.equalsIgnoreCase(screenMapping.getDependentScreen())) {
					continue;
				}

				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculation.setPlantId(plantUUID);

				aopCalculationRepository.save(aopCalculation);
			}

			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("SP Executed successfully");
			aopMessageVM.setData(result);

			return aopMessageVM;

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to calculate Other Production", e);
		}
	}

}
