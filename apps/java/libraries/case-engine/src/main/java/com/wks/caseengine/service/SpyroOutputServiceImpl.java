package com.wks.caseengine.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wks.caseengine.dto.SpyroOutputDTO;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class SpyroOutputServiceImpl implements SpyroOutputService{
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;
	
	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;

	@Override
	public AOPMessageVM getSpyroOutputData(String year, String plantId,String Mode,String type) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<Map<String, Object>> spyroOutputDataList = new ArrayList<>();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
        Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

        String siteId = site.getId().toString();
        String verticalId = vertical.getId().toString();
        String procedureName=vertical.getName()+"_"+site.getName()+"_GetSpyroOutput";
		try {
			List<Object[]> results = getData(plantId, year,siteId,verticalId,Mode,procedureName);

			for (Object[] row : results) {
				Map<String, Object> map = new HashMap<>(); // Create a new map for each row
				if(row[6].toString().contains(type)) {	
					map.put("VerticalFKId", row[0]);
					map.put("PlantFKId", row[1]);
					map.put("NormParameterFKID", row[2]);
					map.put("Particulars", row[3]);
					map.put("NormParameterDisplayName", row[4]);
					map.put("NormParameterTypeFKID", row[5]);
					map.put("Type", row[6]);
					map.put("UOM", row[7]);
					map.put("AuditYear", row[8]);
					map.put("Remarks", row[9]);
					map.put("Jan", row[10]);
					map.put("Feb", row[11]);
					map.put("Mar", row[12]);
					map.put("Apr", row[13]);
					map.put("May", row[14]);
					map.put("Jun", row[15]);
					map.put("Jul", row[16]);
					map.put("Aug", row[17]);
					map.put("Sep", row[18]);
					map.put("Oct", row[19]);
					map.put("Nov", row[20]);
					map.put("Dec", row[21]);
					spyroOutputDataList.add(map); // Add the map to the list here
				}
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(spyroOutputDataList);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}
	
	public List<Object[]> getData(String plantId, String AopYear, String siteId,
			String verticalId,String Mode,String procedureName) {
		try {
			
			String sql = "EXEC " + procedureName +
					" @plantId = :plantId,@siteId = :siteId,@verticalId = :verticalId, @AopYear = :AopYear, @Mode = :Mode";

			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("AopYear", AopYear);
			query.setParameter("siteId", siteId);
			query.setParameter("verticalId", verticalId);
			query.setParameter("Mode", Mode);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM updateSpyroOutputData(List<SpyroOutputDTO> spyroOutputDTOList) {
		AOPMessageVM aopMessageVM=new AOPMessageVM();
		try {
			for (SpyroOutputDTO spyroOutputDTO : spyroOutputDTOList) {
				UUID normParameterFKId = UUID.fromString(spyroOutputDTO.getNormParameterFKID());

				for (int i = 1; i <= 12; i++) {
					Double attributeValue = getAttributeValue(spyroOutputDTO, i);
		
					saveData(normParameterFKId, i, attributeValue, spyroOutputDTO);
				}
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data updated successfully");
			aopMessageVM.setData(spyroOutputDTOList);
			return aopMessageVM;
		
		

	} catch (IllegalArgumentException e) {
		throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
	} catch (Exception ex) {
		throw new RuntimeException("Failed to fetch data", ex);
	}
		
	}
	
	public Double getAttributeValue(SpyroOutputDTO spyroOutputDTO, Integer i) {
		switch (i) {
			case 1:
				return spyroOutputDTO.getJan();
			case 2:
				return spyroOutputDTO.getFeb();
			case 3:
				return spyroOutputDTO.getMar();
			case 4:
				return spyroOutputDTO.getApr();
			case 5:
				return spyroOutputDTO.getMay();
			case 6:
				return spyroOutputDTO.getJun();
			case 7:
				return spyroOutputDTO.getJul();
			case 8:
				return spyroOutputDTO.getAug();
			case 9:
				return spyroOutputDTO.getSep();
			case 10:
				return spyroOutputDTO.getOct();
			case 11:
				return spyroOutputDTO.getNov();
			case 12:
				return spyroOutputDTO.getDec();

		}
		return spyroOutputDTO.getJan();
	}
	
	void saveData(UUID normParameterFKId, Integer i, Double attributeValue,SpyroOutputDTO spyroOutputDTO) {
			

		Optional<NormAttributeTransactions> existingRecord = normAttributeTransactionsRepository
				.findByNormParameterFKIdAndAOPMonthAndAuditYear(normParameterFKId, i, spyroOutputDTO.getAuditYear());

		NormAttributeTransactions normAttributeTransactions;

		if (existingRecord.isPresent()) {

			normAttributeTransactions = existingRecord.get();
			normAttributeTransactions.setModifiedOn(new Date());
		} else {

			normAttributeTransactions = new NormAttributeTransactions();
			normAttributeTransactions.setCreatedOn(new Date());
			normAttributeTransactions.setAttributeValueVersion("V1");
			normAttributeTransactions.setUserName("System");
			normAttributeTransactions.setNormParameterFKId(normParameterFKId);
			normAttributeTransactions.setAopMonth(i);
			normAttributeTransactions.setAuditYear(spyroOutputDTO.getAuditYear());
		}

		normAttributeTransactions
				.setAttributeValue(attributeValue != null ? attributeValue.toString() : "0.0");
		normAttributeTransactions.setRemarks(spyroOutputDTO.getRemarks());

		normAttributeTransactionsRepository.save(normAttributeTransactions);
	}




}
