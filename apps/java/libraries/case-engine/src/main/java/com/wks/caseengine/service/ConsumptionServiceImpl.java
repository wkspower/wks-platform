package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.CalculatedProcessDemandDTO;
import com.wks.caseengine.dto.PlantConsumpProjection;
import com.wks.caseengine.dto.PlantRequirementDTO;
import com.wks.caseengine.entity.CalculatedProcessDemand;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.repository.CalculatedProcessDemandRepository;
import com.wks.caseengine.repository.PlantsRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class ConsumptionServiceImpl implements ConsumptionService {

	private static final Logger logger = LoggerFactory.getLogger(ConsumptionServiceImpl.class);

	@PersistenceContext(unitName = "db1")
	private EntityManager entityManager;

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private CalculatedProcessDemandRepository calculatedProcessDemandRepository;

	@Override
	public List<PlantRequirementDTO> getCppConsumptions(UUID plantId, String year) {
		
		List<PlantRequirementDTO> cppConsumptionData = new ArrayList<>();
		Plants plant = plantsRepository.findById(plantId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
		String procedureName = "GetPlantConsumptionByMaterial";

		List<PlantConsumpProjection> results = plantsRepository.findPlantConsumptionByMaterial(plantId, year);

		

		for (PlantConsumpProjection result : results) {
			PlantRequirementDTO materialYearlyConsumptionData = new PlantRequirementDTO();
			materialYearlyConsumptionData.setPlantName(result.getPlantName());
			materialYearlyConsumptionData.setPlantCode(result.getPlantCode());
			materialYearlyConsumptionData.setCppUtilities(result.getCppUtilities());
			materialYearlyConsumptionData.setCppUtiltiyIds(result.getCppUtiltiyIds());
			materialYearlyConsumptionData.setUom(result.getUom());
			materialYearlyConsumptionData.setApril(result.getApril());
			materialYearlyConsumptionData.setMay(result.getMay());
			materialYearlyConsumptionData.setJune(result.getJune());
			materialYearlyConsumptionData.setJuly(result.getJuly());
			materialYearlyConsumptionData.setAug(result.getAug());
			materialYearlyConsumptionData.setSep(result.getSep());
			materialYearlyConsumptionData.setOct(result.getOct());
			materialYearlyConsumptionData.setNov(result.getNov());
			materialYearlyConsumptionData.setDec(result.getDec());
			materialYearlyConsumptionData.setJan(result.getJan());
			materialYearlyConsumptionData.setFeb(result.getFeb());
			materialYearlyConsumptionData.setMarch(result.getMarch());
			materialYearlyConsumptionData.setGrandTotal(result.getGrandTotal());
			materialYearlyConsumptionData.setRemarks(result.getRemarks());
			cppConsumptionData.add(materialYearlyConsumptionData);
		}
		return cppConsumptionData;
		// try {
		// 	List<Object[]> results = getAllCPPPlantConsumptionData(procedureName, plantId,year);

		// 	for (Object[] row : results) {
		// 		PlantRequirementDTO materialYearlyConsumptionData = new PlantRequirementDTO();
		// 		materialYearlyConsumptionData.setPlantName(row[0] != null ? row[0].toString() : null);
		// 		materialYearlyConsumptionData.setPlantCode(row[1] != null ? row[1].toString() : null);
		// 		materialYearlyConsumptionData.setCppUtilities(row[2] != null ? row[2].toString() : null);
		// 		materialYearlyConsumptionData.setCppUtiltiyIds(row[3] != null ? row[3].toString() : null);
		// 		// materialYearlyConsumptionData.setCppPlant(row[4] != null ? row[4].toString() : null);
		// 		// materialYearlyConsumptionData.setCppPlantId(row[5] != null ? row[5].toString() : null);
		// 		materialYearlyConsumptionData.setUom(row[4] != null ? row[4].toString() : null);
		// 		materialYearlyConsumptionData.setApril(row[5] != null ? Double.parseDouble(row[5].toString()) : 0.0);
		// 		materialYearlyConsumptionData.setMay(row[6] != null ? Double.parseDouble(row[6].toString()) : 0.0);
		// 		materialYearlyConsumptionData.setJune(row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0);
		// 		materialYearlyConsumptionData.setJuly(row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0);
		// 		materialYearlyConsumptionData.setAug(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0);
		// 		materialYearlyConsumptionData.setSep(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0);
		// 		materialYearlyConsumptionData.setOct(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0);
		// 		materialYearlyConsumptionData.setNov(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0);
		// 		materialYearlyConsumptionData.setDec(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0);
		// 		materialYearlyConsumptionData.setJan(row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0);
		// 		materialYearlyConsumptionData.setFeb(row[15] != null ? Double.parseDouble(row[15].toString()) : 0.0);
		// 		materialYearlyConsumptionData.setMarch(row[16] != null ? Double.parseDouble(row[16].toString()) : 0.0);
		// 		materialYearlyConsumptionData.setGrandTotal(row[17] != null ? Double.parseDouble(row[17].toString()) : 0.0);
		// 		cppConsumptionData.add(materialYearlyConsumptionData);

		// 	}
		// 	return cppConsumptionData;

		// } catch (IllegalArgumentException e) {
		// 	throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		// } catch (Exception ex) {
		// 	throw new RuntimeException("Failed to fetch data", ex);
		// }

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

	@Override
	public List<CalculatedProcessDemandDTO> getProcessDemand(String financialYear) {
		logger.info("Fetching process demand for financial year: {}", financialYear);
		
		List<Object[]> results = calculatedProcessDemandRepository.getProcessDemandByYear(financialYear);
		logger.info("Found {} records for financial year: {}", results.size(), financialYear);
		
		List<CalculatedProcessDemandDTO> dtoList = new ArrayList<>();
		for (Object[] row : results) {
			CalculatedProcessDemandDTO dto = mapRowToDTO(row);
			dtoList.add(dto);
		}
		
		return dtoList;
	}

	/**
	 * Maps SP result row to DTO.
	 * SP columns: id, financial_year, process_plant, process_plant_id, cpp_utility, cpp_utility_id,
	 *             cpp_plant, cpp_plant_id, uom, apr, may, jun, jul, aug, sep, oct, nov, dec, jan, feb, mar, is_calculated
	 */
	private CalculatedProcessDemandDTO mapRowToDTO(Object[] row) {
		return CalculatedProcessDemandDTO.builder()
				.id(row[0] != null ? UUID.fromString(row[0].toString()) : null)
				.financialYear(row[1] != null ? row[1].toString() : null)
				.processPlant(row[2] != null ? row[2].toString() : null)
				.processPlantId(row[3] != null ? row[3].toString() : null)
				.cppUtility(row[4] != null ? row[4].toString() : null)
				.cppUtilityId(row[5] != null ? row[5].toString() : null)
				.cppPlant(row[6] != null ? row[6].toString() : null)
				.cppPlantId(row[7] != null ? row[7].toString() : null)
				.uom(row[8] != null ? row[8].toString() : null)
				.apr(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0)
				.may(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0)
				.jun(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0)
				.jul(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0)
				.aug(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0)
				.sep(row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0)
				.oct(row[15] != null ? Double.parseDouble(row[15].toString()) : 0.0)
				.nov(row[16] != null ? Double.parseDouble(row[16].toString()) : 0.0)
				.dec(row[17] != null ? Double.parseDouble(row[17].toString()) : 0.0)
				.jan(row[18] != null ? Double.parseDouble(row[18].toString()) : 0.0)
				.feb(row[19] != null ? Double.parseDouble(row[19].toString()) : 0.0)
				.mar(row[20] != null ? Double.parseDouble(row[20].toString()) : 0.0)
				.isCalculated(row[21] != null && Integer.parseInt(row[21].toString()) == 1)
				.build();
	}
}
