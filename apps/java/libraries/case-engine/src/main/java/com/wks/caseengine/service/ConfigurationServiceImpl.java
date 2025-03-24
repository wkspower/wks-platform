package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Matcher;


import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.dto.ConfigurationDataDTO;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
@Service
public class ConfigurationServiceImpl implements ConfigurationService{
	
	@PersistenceContext
    private EntityManager entityManager;
	
	

	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;
	
	
	public List<ConfigurationDTO> getConfigurationData(String year, UUID plantFKId) {
        System.out.println("GET CofigurationDataService==============================>");
		 List<Object[]> obj= normAttributeTransactionsRepository.findByYearAndPlantFkId(year,plantFKId);
	     List<ConfigurationDTO> configurationDTOList = new ArrayList<>();
      int i=0;
		 for (Object[] row : obj) {
			ConfigurationDTO configurationDTO = new ConfigurationDTO();
		
			
			configurationDTO.setNormParameterFKId(row[0] != null ? row[0].toString() : "");
			configurationDTO.setJan((row[1] != null && !row[1].toString().trim().isEmpty()) ? Float.parseFloat(row[1].toString()) : null);
			configurationDTO.setFeb((row[2] != null && !row[2].toString().trim().isEmpty()) ? Float.parseFloat(row[2].toString()) : null);
			configurationDTO.setMar((row[3] != null && !row[3].toString().trim().isEmpty()) ? Float.parseFloat(row[3].toString()) : null);
			configurationDTO.setApr((row[4] != null && !row[4].toString().trim().isEmpty()) ? Float.parseFloat(row[4].toString()) : null);
			configurationDTO.setMay((row[5] != null && !row[5].toString().trim().isEmpty()) ? Float.parseFloat(row[5].toString()) : null);
			configurationDTO.setJun((row[6] != null && !row[6].toString().trim().isEmpty()) ? Float.parseFloat(row[6].toString()) : null);
			configurationDTO.setJul((row[7] != null && !row[7].toString().trim().isEmpty()) ? Float.parseFloat(row[7].toString()) : null);
			configurationDTO.setAug((row[8] != null && !row[8].toString().trim().isEmpty()) ? Float.parseFloat(row[8].toString()) : null);
			configurationDTO.setSep((row[9] != null && !row[9].toString().trim().isEmpty()) ? Float.parseFloat(row[9].toString()) : null);
			configurationDTO.setOct((row[10] != null && !row[10].toString().trim().isEmpty()) ? Float.parseFloat(row[10].toString()) : null);
			configurationDTO.setNov((row[11] != null && !row[11].toString().trim().isEmpty()) ? Float.parseFloat(row[11].toString()) : null);
			configurationDTO.setDec((row[12] != null && !row[12].toString().trim().isEmpty()) ? Float.parseFloat(row[12].toString()) : null);
			configurationDTO.setRemark((row[13] != null ? row[13].toString() : "" ));
			configurationDTO.setId(row[14] != null ? row[14].toString() : i+"#");
			configurationDTO.setAuditYear(row[15] != null ? row[15].toString() : "" );
			
			
			configurationDTOList.add(configurationDTO);
			if(row[14] == null){
				i++;
			}

		}
	   
		return configurationDTOList;
	}
 	/**
 	 * Extracts column names from the pivot SQL string.
 	 */
 	private List<String> getColumnNames(String pivotColumns) {
 	    List<String> columnNames = new ArrayList<>();
 	    if (pivotColumns != null) {
 	        String regex = "MAX\\(CASE WHEN MonthYear = '([^']+)' THEN AttributeValue END\\) AS \\[([^\\]]+)\\]";
 	        Pattern pattern = Pattern.compile(regex);
 	        Matcher matcher = pattern.matcher(pivotColumns);
 	        while (matcher.find()) {
 	            columnNames.add(matcher.group(2));  // Extract the alias inside []
 	        }
 	    }
 	    return columnNames;
 	}

	 @Transactional(propagation = Propagation.REQUIRES_NEW)
	 @Override
	 public String saveConfigurationData(String year, List<ConfigurationDTO> configurationDTOList) {
		 for (ConfigurationDTO configurationDTO : configurationDTOList) {
			 UUID normParameterFKId = UUID.fromString(configurationDTO.getNormParameterFKId());
 
			
			 for (int i = 1; i <= 12; i++) {
				 Float attributeValue = getAttributeValue(configurationDTO, i);
 
				
				 Optional<NormAttributeTransactions> existingRecord = 
					 normAttributeTransactionsRepository.findByNormParameterFKIdAndAOPMonthAndAuditYear(
						 normParameterFKId, i, year
					 );
 
				 NormAttributeTransactions normAttributeTransactions;
				 
				 if (existingRecord.isPresent()) {
				   
					 normAttributeTransactions = existingRecord.get();
					 normAttributeTransactions.setModifiedOn(new Date()); 
				 } else {
				  
					 normAttributeTransactions = new NormAttributeTransactions();
 //	                normAttributeTransactions.setId(UUID.randomUUID());
					 normAttributeTransactions.setCreatedOn(new Date());
					 normAttributeTransactions.setAttributeValueVersion("V1");
					 normAttributeTransactions.setUserName("System");
					 normAttributeTransactions.setNormParameterFKId(normParameterFKId);
					 normAttributeTransactions.setAopMonth(i);
					 normAttributeTransactions.setAuditYear(configurationDTO.getAuditYear());
					 normAttributeTransactions.setAuditYear(year);
					 
				 }
 
			 
				 normAttributeTransactions.setAttributeValue(attributeValue != null ? attributeValue.toString() : "0.0");
				 normAttributeTransactions.setRemarks(configurationDTO.getRemark());
 
				
				 normAttributeTransactionsRepository.save(normAttributeTransactions);
			 }
		 }
		 return "Configuration Data Saved/Updated Successfully";
	 }
 
	 
	 public Float getAttributeValue(ConfigurationDTO configurationDTO, Integer i) {
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
