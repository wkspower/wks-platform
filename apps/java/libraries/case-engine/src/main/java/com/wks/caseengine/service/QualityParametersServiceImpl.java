package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class QualityParametersServiceImpl implements QualityParametersService{
	
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

	@Override
	public AOPMessageVM getQualityParameters(String plantId, String year) {
		
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
				configurationDTO.setMay(
						(row[2] != null && !row[2].toString().trim().isEmpty()) ? Double.parseDouble(row[2].toString())
								: 0.0);
				
				configurationDTO.setRemarks((row[3] != null ? row[3].toString() : ""));
					configurationDTO.setAuditYear(row[4] != null ? row[4].toString() : "");
					configurationDTO.setUOM(row[5] != null ? row[5].toString() : "");
					configurationDTO.setNormType(row[6] != null ? row[6].toString() : "");
					configurationDTO.setIsEditable(row[7] != null ? ((Boolean) row[7]).booleanValue() : null);
					configurationDTO.setProductName(row[8] != null ? row[8].toString() : "");
				
				configurationDTOList.add(configurationDTO);
				
			}
			Map<String, Object> map = new HashMap<>(); 
			
			List<AopCalculation> aopCalculation=aopCalculationRepository.findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId),year,"quality-parameters");
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
	public AOPMessageVM saveQualityParameters(String year, String plantFKId,
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
			List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("quality-parameters");
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

	

}
