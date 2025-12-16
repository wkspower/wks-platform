package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.PlantRequirementDTO;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.repository.PlantsRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class ConsumptionServiceImpl implements ConsumptionService {

	@PersistenceContext(unitName = "db1")
	private EntityManager entityManager;

	@Autowired
	private PlantsRepository plantsRepository;

	@Override
	public List<PlantRequirementDTO> getCppConsumptions(UUID plantId, String year) {
		
		List<PlantRequirementDTO> cppConsumptionData = new ArrayList<>();
		Plants plant = plantsRepository.findById(plantId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
		String procedureName = "GetPlantConsumptionByMaterial";
		try {
			List<Object[]> results = getAllCPPPlantConsumptionData(procedureName, plantId,year);

			for (Object[] row : results) {
				PlantRequirementDTO materialYearlyConsumptionData = new PlantRequirementDTO();
				materialYearlyConsumptionData.setPlantName(row[0] != null ? row[0].toString() : null);
				materialYearlyConsumptionData.setPlantCode(row[1] != null ? row[1].toString() : null);
				materialYearlyConsumptionData.setCppUtilities(row[2] != null ? row[2].toString() : null);
				materialYearlyConsumptionData.setCppUtiltiyIds(row[3] != null ? row[3].toString() : null);
				// materialYearlyConsumptionData.setCppPlant(row[4] != null ? row[4].toString() : null);
				// materialYearlyConsumptionData.setCppPlantId(row[5] != null ? row[5].toString() : null);
				materialYearlyConsumptionData.setUom(row[4] != null ? row[4].toString() : null);
				materialYearlyConsumptionData.setApril(row[5] != null ? Double.parseDouble(row[5].toString()) : 0.0);
				materialYearlyConsumptionData.setMay(row[6] != null ? Double.parseDouble(row[6].toString()) : 0.0);
				materialYearlyConsumptionData.setJune(row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0);
				materialYearlyConsumptionData.setJuly(row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0);
				materialYearlyConsumptionData.setAug(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0);
				materialYearlyConsumptionData.setSep(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0);
				materialYearlyConsumptionData.setOct(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0);
				materialYearlyConsumptionData.setNov(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0);
				materialYearlyConsumptionData.setDec(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0);
				materialYearlyConsumptionData.setJan(row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0);
				materialYearlyConsumptionData.setFeb(row[15] != null ? Double.parseDouble(row[15].toString()) : 0.0);
				materialYearlyConsumptionData.setMarch(row[16] != null ? Double.parseDouble(row[16].toString()) : 0.0);
				materialYearlyConsumptionData.setGrandTotal(row[17] != null ? Double.parseDouble(row[17].toString()) : 0.0);
				cppConsumptionData.add(materialYearlyConsumptionData);

			}
			return cppConsumptionData;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}

	public List<Object[]> getAllCPPPlantConsumptionData(String procedureName, UUID plantId,String financialYear) {
		try {

			String sql = "EXEC " + procedureName +
					" @CPPPlantId = :CPPPlantId, @AOPYear = :AOPYear";
			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("CPPPlantId", plantId);
			query.setParameter("AOPYear", financialYear);
			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
}
