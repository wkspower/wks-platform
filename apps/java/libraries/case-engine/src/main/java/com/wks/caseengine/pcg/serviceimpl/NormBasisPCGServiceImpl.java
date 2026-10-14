package com.wks.caseengine.pcg.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.wks.caseengine.pcg.service.NormBasisPCGService;
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
import com.wks.caseengine.utility.Utility;

import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import com.wks.caseengine.dto.NormBasisPCGDTO;
import com.wks.caseengine.pcg.dto.GasifierDropdownAopBasisDTO;
import com.wks.caseengine.pcg.dto.TargetActualDafThroughtFilterDTO;
import com.wks.caseengine.pcg.dto.TargetGasifierFilterDTO;

@Service
public class NormBasisPCGServiceImpl implements NormBasisPCGService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@Autowired
	private SiteRepository siteRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private NormAttributeTransactionsRepository transactionsRepository;

	@Override
	public List<NormBasisPCGDTO> getAllNormBasis(UUID plantId, String aopYear) {

		Plants plant = plantsRepository.findById(plantId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		String procedureName = vertical.getName() + "_"+site.getName()+"_"+plant.getName() + "_GetConfiguration";
		List<NormBasisPCGDTO> normBasisDTOs = fetchNormBasisFromProcedure(plantId, aopYear, procedureName);
		return normBasisDTOs;
	}

	private List<NormBasisPCGDTO> fetchNormBasisFromProcedure(UUID plantId, String aopYear, String procedureName) {

		String sql = "EXEC " + procedureName + " @plantId = ?, @aopYear = ?";

		List<Object[]> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
			int columnCount = rs.getMetaData().getColumnCount();
			Object[] row = new Object[columnCount];
			for (int i = 0; i < columnCount; i++) {
				row[i] = rs.getObject(i + 1);
			}
			return row;
		}, plantId.toString(), aopYear);

		List<NormBasisPCGDTO> resultList = new ArrayList<>();

		for (Object[] row : rows) {
			NormBasisPCGDTO dto = new NormBasisPCGDTO();

			String idStr = row[0] != null ? row[0].toString() : null;
			UUID normParameterFkId = (idStr != null && !idStr.isBlank()) ? UUID.fromString(idStr) : null;

			dto.setNormParameterFkId(normParameterFkId);
			dto.setDisplayName(row[1] != null ? row[1].toString() : "");
			dto.setDependantAttributeId(row[2] != null ? row[2].toString() : "");
			dto.setTargetValue(row[3] != null ? row[3].toString() : "");
			dto.setRange(row[4] != null ? row[4].toString() : "");
			dto.setSelection(row[5] != null ? row[5].toString() : "");
			dto.setRemarks(row[6] != null ? row[6].toString() : "");
			dto.setUom(row[7] != null ? row[7].toString() : "");
			dto.setNormParameterTypeDisplayName(row[8] != null ? row[8].toString() : "");
			dto.setDataType(row[9] != null ? row[9].toString() : "");
			dto.setDependentAttributeConfig(row[10] != null ? row[10].toString() : "");

			resultList.add(dto);
		}

		return resultList;
	}

	@Transactional
	public AOPMessageVM saveNormBasis(List<NormBasisPCGDTO> normBasisDTOList, String aopYear, String plantId) {
		if (normBasisDTOList == null || normBasisDTOList.isEmpty()) {
			return null;
		}

		for (NormBasisPCGDTO dto : normBasisDTOList) {
			UUID paramId = dto.getNormParameterFkId();
			if (paramId == null) {
				continue;
			}

			String remarks = dto.getRemarks() != null ? dto.getRemarks() : "";
			saveOrUpdateTransaction(paramId, 4, aopYear, dto.getTargetValue(), remarks);
			saveOrUpdateTransaction(paramId, 5, aopYear, dto.getRange(), remarks);
			saveOrUpdateTransaction(paramId, 6, aopYear, dto.getSelection(), remarks);
		}
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		aopMessageVM.setCode(200);
		aopMessageVM.setData(aopMessageVM);
		aopMessageVM.setMessage("Data Update successfully");
		return aopMessageVM;
	}

	private void saveOrUpdateTransaction(UUID normParameterFKId, Integer aopMonth, String auditYear,
	        String attributeValue, String remarks) {

	    try {
	        Optional<NormAttributeTransactions> existingOpt = transactionsRepository
	                .findByNormParameterFKIdAndAopMonthAndAuditYear(normParameterFKId, aopMonth, auditYear);

	        NormAttributeTransactions transaction;
	        Date currentDate = new Date();

	        if (existingOpt.isPresent()) {
	            transaction = existingOpt.get();
	            transaction.setAttributeValue(attributeValue != null ? attributeValue : "");
	            transaction.setRemarks(remarks != null ? remarks : "");
	            transaction.setModifiedOn(currentDate);
	            transaction.setUserName(Utility.getUserName());
	        } else {
	            transaction = new NormAttributeTransactions();
	            transaction.setNormParameterFKId(normParameterFKId);
	            transaction.setAopMonth(aopMonth);
	            transaction.setAuditYear(auditYear);
	            transaction.setAttributeValue(attributeValue != null ? attributeValue : "");
	            transaction.setRemarks(remarks != null ? remarks : "");
	            transaction.setCreatedOn(currentDate);
	            transaction.setModifiedOn(currentDate);
	            transaction.setUserName(Utility.getUserName());
	        }

	        transactionsRepository.save(transaction);

	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid argument provided for transaction saving", e);
	    } catch (DataAccessException dae) {
	        throw new RuntimeException("Database error occurred while saving transaction record", dae);
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to save or update norm attribute transaction", ex);
	    }
	}
	
	@Override
	public AOPMessageVM LoadButtonNormCalculation(UUID plantId, String aopYear, UUID siteId, String periodFrom,
			String periodTo) {
		Plants plant = plantsRepository.findById(plantId).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();

		boolean crude = vertical.getName().equalsIgnoreCase("CRUDE");

		String procedureName = null;

		if (crude) {
			procedureName = vertical.getName() + "_" + site.getName() + "_LoadConfiguration";
		} else {
			procedureName = vertical.getName() + "_" + site.getName() + "_" + plant.getName() + "_" + "NormCalculation";
		}

		String errorMessage = executeNormCalculationProcedure(plantId, aopYear, siteId, periodFrom, periodTo,
				procedureName);

		AOPMessageVM aopMessageVM = new AOPMessageVM();

		if (errorMessage != null) {

			aopMessageVM.setCode(422);
			aopMessageVM.setMessage(errorMessage);
			return aopMessageVM;

		}

		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Norm Calculations Executed Successfully");
		return aopMessageVM;

	}

	@Override
	public List<GasifierDropdownAopBasisDTO> getGasifierDropdownAopBasis(UUID plantId, String aopYear) {

		Plants plant = plantsRepository.findById(plantId).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();

		String procedureName = vertical.getName() + "_" + site.getName()  + "_GetGasifierDropdownAopBasisFilter";

		String sql = "EXEC " + "[" + procedureName + "]" + " @plantId = ?, @aopYear = ?";

		return jdbcTemplate.query(sql, (rs, rowNum) -> {
			String id = null;
			try {
				id = rs.getString("id");
			} catch (Exception e1) {
				try {
					id = rs.getString("Id");
				} catch (Exception ignored) {}
			}
			return GasifierDropdownAopBasisDTO.builder()
					.id(id)
					.name(rs.getString("name"))
					.displayName(rs.getString("displayName"))
					.configuration(rs.getString("configuration"))
					.build();
		}, plantId.toString(), aopYear);
	}

	@Override
	public List<TargetGasifierFilterDTO> getTargetGasifierFilters(UUID plantId, String aopYear) {

		Plants plant = plantsRepository.findById(plantId).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();

		String procedureName = vertical.getName() + "_" + site.getName()  + "_GetTargetGasifierFilter";
		String sql = "EXEC " + "[" + procedureName + "]" + " @plantId = ?, @aopYear = ?";

		return jdbcTemplate.query(sql, (rs, rowNum) -> {
			String idStr = rs.getString("Id");
			String plantIdStr = rs.getString("PlantId");
			String normParamIdStr = rs.getString("NormParameterId");

			return TargetGasifierFilterDTO.builder()
					.id(idStr != null && !idStr.isBlank() ? UUID.fromString(idStr) : null)
					.gOperation(rs.getString("GOperation"))
					.plantId(plantIdStr != null && !plantIdStr.isBlank() ? UUID.fromString(plantIdStr) : null)
					.aopYear(rs.getString("AopYear"))
					.normParameterId(normParamIdStr != null && !normParamIdStr.isBlank() ? UUID.fromString(normParamIdStr) : null)
					.build();
		}, plantId.toString(), aopYear);
	}

	private String executeNormCalculationProcedure(UUID plantId, String aopYear, UUID siteId, String periodFrom,
			String periodTo, String procedureName) {

		try {
			String sanitizedProcedureName = procedureName;
			if (!sanitizedProcedureName.startsWith("[") && !sanitizedProcedureName.endsWith("]")) {
				sanitizedProcedureName = "[" + sanitizedProcedureName + "]";
			}

			StoredProcedureQuery query = entityManager.createStoredProcedureQuery(sanitizedProcedureName);

			// Input parameters
			query.registerStoredProcedureParameter("plantId", String.class, ParameterMode.IN);
			query.registerStoredProcedureParameter("AOPYear", String.class, ParameterMode.IN);
			query.registerStoredProcedureParameter("siteid", String.class, ParameterMode.IN);
			query.registerStoredProcedureParameter("PeriodFrom", String.class, ParameterMode.IN);
			query.registerStoredProcedureParameter("PeriodTo", String.class, ParameterMode.IN);

			// OUTPUT parameter
			query.registerStoredProcedureParameter("ErrorMessage", String.class, ParameterMode.OUT);

			query.setParameter("plantId", plantId.toString());
			query.setParameter("AOPYear", aopYear);
			query.setParameter("siteid", siteId.toString());
			query.setParameter("PeriodFrom", periodFrom);
			query.setParameter("PeriodTo", periodTo);

			query.execute();

			try {
				query.getResultList(); // flush any pending result sets
			} catch (Exception ignored) {
			}

			String errorMessage = (String) query.getOutputParameterValue("ErrorMessage");

			System.out.println("errorMessage string: " + errorMessage);

			return errorMessage;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to execute procedure", ex);
		}
	}

	@Transactional
	@Override
	public AOPMessageVM saveTargetGasifierFilters(TargetGasifierFilterDTO targetGasifierFilterDTO, String year) {

		UUID normParameterFKId = targetGasifierFilterDTO.getNormParameterId();
		String attributeValue = targetGasifierFilterDTO.getGOperation();

	Optional<NormAttributeTransactions> existingRecord = transactionsRepository
			.findByNormParameterFKIdAndAOPMonthAndAuditYear(normParameterFKId, 4, year);

	NormAttributeTransactions normAttributeTransactions;

	if (existingRecord.isPresent()) {
		normAttributeTransactions = existingRecord.get();
		normAttributeTransactions.setModifiedOn(new Date());
		
	} else {

		normAttributeTransactions = new NormAttributeTransactions();
		normAttributeTransactions.setCreatedOn(new Date());
		normAttributeTransactions.setNormParameterFKId(normParameterFKId);
		normAttributeTransactions.setAopMonth(4);
		normAttributeTransactions.setAuditYear(year);
	}

	normAttributeTransactions
			.setAttributeValue(attributeValue != null ? attributeValue.toString() : "");

	normAttributeTransactions.setUserName(Utility.getUserName());
	transactionsRepository.save(normAttributeTransactions);

	AOPMessageVM aopMessageVM = new AOPMessageVM();
	aopMessageVM.setCode(200);
	aopMessageVM.setMessage("Target Gasifier Filters Saved Successfully");
	return aopMessageVM;
}

	@Override
	public List<TargetActualDafThroughtFilterDTO> getTargetActualDafThroughtFilter(UUID plantId, String aopYear) {

		Plants plants = plantsRepository.findById(plantId).orElseThrow(() -> new RuntimeException("Plant not found"));
        String verticalName = verticalRepository.findById(plants.getVerticalFKId()).orElseThrow(() -> new RuntimeException("Vertical not found")).getName();
        String siteName = siteRepository.findById(plants.getSiteFkId()).orElseThrow(() -> new RuntimeException("Site not found")).getName();

        String procedureName = verticalName + "_" + siteName + "_GetTargetActualDafThroughtFilter";

		String sql = "EXEC " + "[" + procedureName + "]" + " @plantId = ?, @aopYear = ?";

		return jdbcTemplate.query(sql, (rs, rowNum) -> {
			String normParamIdStr = rs.getString("NormParameterId");
			String plantIdStr = rs.getString("PlantId");

			return TargetActualDafThroughtFilterDTO.builder()
					.normParameterId(normParamIdStr != null && !normParamIdStr.isBlank()
							? UUID.fromString(normParamIdStr) : null)
					.displayName(rs.getString("DisplayName"))
					.targetValue(rs.getObject("TargetValue") != null
							? rs.getDouble("TargetValue") : null)
					.range(rs.getObject("Range") != null
							? rs.getDouble("Range") : null)
					.remarks(rs.getString("Remarks"))
					.aopYear(rs.getString("aopYear"))
					.plantId(plantIdStr != null && !plantIdStr.isBlank()
							? UUID.fromString(plantIdStr) : null)
					.build();
		}, plantId.toString(), aopYear);
	}

	@Transactional
	@Override
	public AOPMessageVM saveTargetActualDafThroughtFilter(List<TargetActualDafThroughtFilterDTO> targetActualDafThroughtFilterDTOList, String year) {

      for (TargetActualDafThroughtFilterDTO targetActualDafThroughtFilterDTO : targetActualDafThroughtFilterDTOList) {
        Double targetValue = targetActualDafThroughtFilterDTO.getTargetValue();
        Double range = targetActualDafThroughtFilterDTO.getRange();
        String remarks = targetActualDafThroughtFilterDTO.getRemarks();

        saveTargetActualDafThroughtFilterData(targetValue, 4, year, targetActualDafThroughtFilterDTO.getNormParameterId(), remarks);
        saveTargetActualDafThroughtFilterData(range, 5, year, targetActualDafThroughtFilterDTO.getNormParameterId(), remarks);
      }

	AOPMessageVM aopMessageVM = new AOPMessageVM();
	aopMessageVM.setCode(200);
	aopMessageVM.setMessage("Target Actual DAF Throughput Saved Successfully");
	return aopMessageVM;
}

public AOPMessageVM saveTargetActualDafThroughtFilterData(Double attributeValue, Integer month, String year, UUID normParameterFKId, String remarks) {

	Optional<NormAttributeTransactions> existingRecord = transactionsRepository
			.findByNormParameterFKIdAndAOPMonthAndAuditYear(normParameterFKId, month, year);

	NormAttributeTransactions normAttributeTransactions;

	if (existingRecord.isPresent()) {
		normAttributeTransactions = existingRecord.get();
		normAttributeTransactions.setModifiedOn(new Date());
		
	} else {

		normAttributeTransactions = new NormAttributeTransactions();
		normAttributeTransactions.setCreatedOn(new Date());
		normAttributeTransactions.setNormParameterFKId(normParameterFKId);
		normAttributeTransactions.setAopMonth(month);
		normAttributeTransactions.setAuditYear(year);
	}

	normAttributeTransactions
			.setAttributeValue(attributeValue != null ? attributeValue.toString() : "0.0");
	normAttributeTransactions.setRemarks(remarks != null ? remarks : "");
	normAttributeTransactions.setUserName(Utility.getUserName());
	transactionsRepository.save(normAttributeTransactions);

	AOPMessageVM aopMessageVM = new AOPMessageVM();
	aopMessageVM.setCode(200);
	aopMessageVM.setMessage("Target Actual DAF Throught Filter Data Saved Successfully");
	return aopMessageVM;
	}

}
