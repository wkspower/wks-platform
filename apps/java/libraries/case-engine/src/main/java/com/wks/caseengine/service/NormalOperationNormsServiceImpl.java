package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Collections;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wks.caseengine.dto.MCUNormsValueDTO;
import com.wks.caseengine.entity.MCUNormsValue;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.NormalOperationNormsRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

@Service
public class NormalOperationNormsServiceImpl implements NormalOperationNormsService {

	@Autowired
	private NormalOperationNormsRepository normalOperationNormsRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@Override
	public AOPMessageVM getNormalOperationNormsData(String year, String plantId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			List<Object[]> obj = getNormalOperationNorms(year, UUID.fromString(plantId));
			List<MCUNormsValueDTO> mCUNormsValueDTOList = new ArrayList<>();

			for (Object[] row : obj) {
				MCUNormsValueDTO dto = new MCUNormsValueDTO();
				dto.setId(row[0].toString());
				dto.setSiteFkId(row[1].toString());
				dto.setPlantFkId(row[2].toString());
				dto.setVerticalFkId(row[3].toString());
				dto.setMaterialFkId(row[4].toString());

				dto.setApril(row[5] != null ? Float.parseFloat(row[5].toString()) : null);
				dto.setMay(row[6] != null ? Float.parseFloat(row[6].toString()) : null);
				dto.setJune(row[7] != null ? Float.parseFloat(row[7].toString()) : null);
				dto.setJuly(row[8] != null ? Float.parseFloat(row[8].toString()) : null);
				dto.setAugust(row[9] != null ? Float.parseFloat(row[9].toString()) : null);
				dto.setSeptember(row[10] != null ? Float.parseFloat(row[10].toString()) : null);
				dto.setOctober(row[11] != null ? Float.parseFloat(row[11].toString()) : null);
				dto.setNovember(row[12] != null ? Float.parseFloat(row[12].toString()) : null);
				dto.setDecember(row[13] != null ? Float.parseFloat(row[13].toString()) : null);
				dto.setJanuary(row[14] != null ? Float.parseFloat(row[14].toString()) : null);
				dto.setFebruary(row[15] != null ? Float.parseFloat(row[15].toString()) : null);
				dto.setMarch(row[16] != null ? Float.parseFloat(row[16].toString()) : null);

				dto.setFinancialYear(row[17].toString());
				dto.setRemarks(row[18] != null ? row[18].toString() : " ");
				dto.setCreatedOn(row[19] != null ? (Date) row[19] : null);
				dto.setModifiedOn(row[20] != null ? (Date) row[20] : null);
				dto.setMcuVersion(row[21] != null ? row[21].toString() : null);
				dto.setUpdatedBy(row[22] != null ? row[22].toString() : null);
				dto.setNormParameterTypeId(row[23] != null ? row[23].toString() : null);
				dto.setNormParameterTypeName(row[24] != null ? row[24].toString() : null);
				dto.setNormParameterTypeDisplayName(row[25] != null ? row[25].toString() : null);
				dto.setUOM(row[26] != null ? row[26].toString() : null);

				mCUNormsValueDTOList.add(dto);
			}

			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(mCUNormsValueDTOList);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM saveNormalOperationNormsData(List<MCUNormsValueDTO> mCUNormsValueDTOList) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<MCUNormsValue> mcuNormsValueList = new ArrayList();
		try {
			for (MCUNormsValueDTO mCUNormsValueDTO : mCUNormsValueDTOList) {

				MCUNormsValue mCUNormsValue = new MCUNormsValue();
				if (mCUNormsValueDTO.getId() != null || !mCUNormsValueDTO.getId().isEmpty()) {
					mCUNormsValue.setId(UUID.fromString(mCUNormsValueDTO.getId()));
					mCUNormsValue.setModifiedOn(new Date());
				} else {
					mCUNormsValue.setCreatedOn(new Date());
				}
				mCUNormsValue.setApril(Optional.ofNullable(mCUNormsValueDTO.getApril()).orElse(0.0f));
				mCUNormsValue.setMay(Optional.ofNullable(mCUNormsValueDTO.getMay()).orElse(0.0f));
				mCUNormsValue.setJune(Optional.ofNullable(mCUNormsValueDTO.getJune()).orElse(0.0f));
				mCUNormsValue.setJuly(Optional.ofNullable(mCUNormsValueDTO.getJuly()).orElse(0.0f));
				mCUNormsValue.setAugust(Optional.ofNullable(mCUNormsValueDTO.getAugust()).orElse(0.0f));
				mCUNormsValue.setSeptember(Optional.ofNullable(mCUNormsValueDTO.getSeptember()).orElse(0.0f));
				mCUNormsValue.setOctober(Optional.ofNullable(mCUNormsValueDTO.getOctober()).orElse(0.0f));
				mCUNormsValue.setNovember(Optional.ofNullable(mCUNormsValueDTO.getNovember()).orElse(0.0f));
				mCUNormsValue.setDecember(Optional.ofNullable(mCUNormsValueDTO.getDecember()).orElse(0.0f));
				mCUNormsValue.setJanuary(Optional.ofNullable(mCUNormsValueDTO.getJanuary()).orElse(0.0f));
				mCUNormsValue.setFebruary(Optional.ofNullable(mCUNormsValueDTO.getFebruary()).orElse(0.0f));
				mCUNormsValue.setMarch(Optional.ofNullable(mCUNormsValueDTO.getMarch()).orElse(0.0f));
				if (mCUNormsValueDTO.getSiteFkId() != null) {
					mCUNormsValue.setSiteFkId(UUID.fromString(mCUNormsValueDTO.getSiteFkId()));
				}
				if (mCUNormsValueDTO.getPlantFkId() != null) {
					mCUNormsValue.setPlantFkId(UUID.fromString(mCUNormsValueDTO.getPlantFkId()));
				}
				if (mCUNormsValueDTO.getVerticalFkId() != null) {
					mCUNormsValue.setVerticalFkId(UUID.fromString(mCUNormsValueDTO.getVerticalFkId()));
				}
				if (mCUNormsValueDTO.getMaterialFkId() != null) {
					mCUNormsValue.setMaterialFkId(UUID.fromString(mCUNormsValueDTO.getMaterialFkId()));
				}
				if (mCUNormsValueDTO.getNormParameterTypeId() != null) {
					mCUNormsValue.setNormParameterTypeFkId(UUID.fromString(mCUNormsValueDTO.getNormParameterTypeId()));
				}

				mCUNormsValue.setFinancialYear(mCUNormsValueDTO.getFinancialYear());
				mCUNormsValue.setRemarks(mCUNormsValueDTO.getRemarks());
				mCUNormsValue.setMcuVersion("V1");
				mCUNormsValue.setUpdatedBy("System");

				System.out.println("Data Saved Succussfully");
				normalOperationNormsRepository.save(mCUNormsValue);
				mcuNormsValueList.add(mCUNormsValue);
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data saved successfully");
			aopMessageVM.setData(mcuNormsValueList);
			return aopMessageVM;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}

	@Override
	@Transactional
	public int calculateExpressionConsumptionNorms(String year, String plantId) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String storedProcedure = vertical.getName() + "_HMD_NormsCalculation";
			return executeStoredProcedure(storedProcedure, plantId, site.getId().toString(),
					vertical.getId().toString(), year);
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Transactional
	public int executeStoredProcedure(String procedureName, String plantId, String siteId, String verticalId,
			String finYear) {
		try {
			String sql = "EXEC " + procedureName
					+ " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @finYear = :finYear"; 
			Query query = entityManager.createNativeQuery(sql);

			// Setting all parameters
			query.setParameter("plantId", plantId);
			query.setParameter("siteId", siteId);
			query.setParameter("verticalId", verticalId);
			query.setParameter("finYear", finYear);
			return query.executeUpdate();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Transactional
	public List<Object[]> getNormalOperationNorms(String financialYear, UUID plantId) {
		try {
			Plants plant = plantsRepository.findById(plantId).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

			String viewName = "vwScrn" + vertical.getName() + "NormalOperationNorms";
			String sql = "SELECT * FROM " + viewName
					+ " WHERE FinancialYear = :financialYear AND Plant_FK_Id = :plantId"
					+ " ORDER BY NormParameterTypeDisplayName";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("financialYear", financialYear);
			query.setParameter("plantId", plantId);

			return query.getResultList(); // You can cast this to a DTO later
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

}
