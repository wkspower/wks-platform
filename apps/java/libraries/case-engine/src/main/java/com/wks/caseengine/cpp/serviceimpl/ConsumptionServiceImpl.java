package com.wks.caseengine.cpp.serviceimpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.cpp.service.ConsumptionService;
import com.wks.caseengine.dto.CalculatedProcessDemandDTO;
import com.wks.caseengine.dto.PlantConsumpProjection;
import com.wks.caseengine.dto.PlantRequirementDTO;
import com.wks.caseengine.dto.ProcessDemandUpdateRequest;
import com.wks.caseengine.dto.ProcessDemandUpdateResponse;
import com.wks.caseengine.entity.CalculatedProcessDemand;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ProcessDemandMaster;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.repository.CalculatedProcessDemandRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ProcessDemandMasterRepository;

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

	@Autowired
	private ProcessDemandMasterRepository processDemandMasterRepository;

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
				.remarks(row[22] != null ? row[22].toString() : null)
				.build();
	}

	@Override
	@Transactional
	public ProcessDemandUpdateResponse updateProcessDemand(String financialYear, List<ProcessDemandUpdateRequest> requests) {
		logger.info("Updating process demand for financial year: {}, items: {}", financialYear, requests.size());
		
		int successCount = 0;
		List<String> errors = new ArrayList<>();
		
		for (ProcessDemandUpdateRequest request : requests) {
			try {
				// Validate required fields
				if (request.getProcessPlantId() == null || request.getProcessPlantId().isBlank()) {
					errors.add("Missing processPlantId for request");
					continue;
				}
				if (request.getCppUtilityId() == null || request.getCppUtilityId().isBlank()) {
					errors.add("Missing cppUtilityId for processPlantId: " + request.getProcessPlantId());
					continue;
				}
				
				// Validate against master table
				Optional<ProcessDemandMaster> masterOpt = processDemandMasterRepository
						.findByProcessPlantIdAndCppUtilityIdAndIsActiveTrue(
								request.getProcessPlantId(), request.getCppUtilityId());
				
				if (masterOpt.isEmpty()) {
					errors.add("Invalid plant-utility combination: processPlantId=" + request.getProcessPlantId() 
							+ ", cppUtilityId=" + request.getCppUtilityId());
					continue;
				}
				
				ProcessDemandMaster master = masterOpt.get();
				
				// Find existing record or create new
				Optional<CalculatedProcessDemand> existingOpt = calculatedProcessDemandRepository
						.findByFinancialYearAndProcessPlantIdAndCppUtilityId(
								financialYear, request.getProcessPlantId(), request.getCppUtilityId());
				
				CalculatedProcessDemand entity;
				if (existingOpt.isPresent()) {
					// Update existing record
					entity = existingOpt.get();
					logger.debug("Updating existing record: {}", entity.getId());
				} else {
					// Create new record from master data
					entity = new CalculatedProcessDemand();
					entity.setFinancialYear(financialYear);
					entity.setProcessPlant(master.getProcessPlant());
					entity.setProcessPlantId(master.getProcessPlantId());
					entity.setCppUtility(master.getCppUtility());
					entity.setCppUtilityId(master.getCppUtilityId());
					entity.setCppPlant(master.getCppPlant());
					entity.setCppPlantId(master.getCppPlantId());
					entity.setUom(master.getUom());
					entity.setCreatedAt(LocalDateTime.now());
					// Initialize all months to 0
					entity.setApr(0.0);
					entity.setMay(0.0);
					entity.setJun(0.0);
					entity.setJul(0.0);
					entity.setAug(0.0);
					entity.setSep(0.0);
					entity.setOct(0.0);
					entity.setNov(0.0);
					entity.setDec(0.0);
					entity.setJan(0.0);
					entity.setFeb(0.0);
					entity.setMar(0.0);
					logger.debug("Creating new record for: {} - {}", request.getProcessPlantId(), request.getCppUtilityId());
				}
				
				// Update only non-null month values (partial update)
				if (request.getApr() != null) entity.setApr(request.getApr());
				if (request.getMay() != null) entity.setMay(request.getMay());
				if (request.getJun() != null) entity.setJun(request.getJun());
				if (request.getJul() != null) entity.setJul(request.getJul());
				if (request.getAug() != null) entity.setAug(request.getAug());
				if (request.getSep() != null) entity.setSep(request.getSep());
				if (request.getOct() != null) entity.setOct(request.getOct());
				if (request.getNov() != null) entity.setNov(request.getNov());
				if (request.getDec() != null) entity.setDec(request.getDec());
				if (request.getJan() != null) entity.setJan(request.getJan());
				if (request.getFeb() != null) entity.setFeb(request.getFeb());
				if (request.getMar() != null) entity.setMar(request.getMar());
				
				// Update audit fields
				entity.setRemarks(request.getRemarks());
				entity.setUpdatedAt(LocalDateTime.now());
				
				calculatedProcessDemandRepository.save(entity);
				successCount++;
				
			} catch (Exception e) {
				logger.error("Error processing request for processPlantId={}, cppUtilityId={}: {}", 
						request.getProcessPlantId(), request.getCppUtilityId(), e.getMessage());
				errors.add("Error processing processPlantId=" + request.getProcessPlantId() 
						+ ", cppUtilityId=" + request.getCppUtilityId() + ": " + e.getMessage());
			}
		}
		
		logger.info("Update complete. Success: {}, Failures: {}", successCount, errors.size());
		
		return ProcessDemandUpdateResponse.builder()
				.totalReceived(requests.size())
				.successCount(successCount)
				.failureCount(errors.size())
				.errors(errors)
				.build();
	}
}



