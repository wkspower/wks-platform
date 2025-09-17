package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wks.caseengine.dto.ModeWiseNormsDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.MCUNormsValue;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.MCUNormsValueRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class ModeWiseNormsServiceImpl implements ModeWiseNormsService {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@Autowired
	private MCUNormsValueRepository mcuNormsValueRepository;

	@Autowired
	private AopCalculationRepository aopCalculationRepository;

	@Autowired
	private ScreenMappingRepository screenMappingRepository;

	@Override
	public AOPMessageVM getModeWiseNormsData(String year, String plantId, String mode, String method) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<ModeWiseNormsDTO> modeWiseNormsDTOList = new ArrayList<>();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId))
				.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
		Sites site = siteRepository.findById(plant.getSiteFkId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
		String procedureName = vertical.getName() + "_" + site.getName() + "_GetModeWiseNorms";
		try {
			List<Object[]> results = getModeWiseNormsData(plantId, year, mode, method, procedureName);

			for (Object[] row : results) {
				ModeWiseNormsDTO modeWiseNormsDTO = new ModeWiseNormsDTO();
				modeWiseNormsDTO.setId(row[0] != null ? row[0].toString() : null);
				modeWiseNormsDTO.setNormParameterTypeId(row[2] != null ? row[2].toString() : null);
				modeWiseNormsDTO.setMaterialFKId(row[3] != null ? row[3].toString() : null);
				modeWiseNormsDTO.setNormType(row[4] != null ? row[4].toString() : null);
				modeWiseNormsDTO.setSapMaterialCode(row[5] != null ? row[5].toString() : null);
				modeWiseNormsDTO.setMaterialDisplayName(row[6] != null ? row[6].toString() : null);
				modeWiseNormsDTO.setUom(row[7] != null ? row[7].toString() : null);
				modeWiseNormsDTO.setMaterialName(row[8] != null ? row[8].toString() : null);
				modeWiseNormsDTO.setApril(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0);
				modeWiseNormsDTO.setMay(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0);
				modeWiseNormsDTO.setJune(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0);
				modeWiseNormsDTO.setJuly(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0);
				modeWiseNormsDTO.setAugust(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0);
				modeWiseNormsDTO.setSeptember(row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0);
				modeWiseNormsDTO.setOctober(row[15] != null ? Double.parseDouble(row[15].toString()) : 0.0);
				modeWiseNormsDTO.setNovember(row[16] != null ? Double.parseDouble(row[16].toString()) : 0.0);
				modeWiseNormsDTO.setDecember(row[17] != null ? Double.parseDouble(row[17].toString()) : 0.0);
				modeWiseNormsDTO.setJanuary(row[18] != null ? Double.parseDouble(row[18].toString()) : 0.0);
				modeWiseNormsDTO.setFebruary(row[19] != null ? Double.parseDouble(row[19].toString()) : 0.0);
				modeWiseNormsDTO.setMarch(row[20] != null ? Double.parseDouble(row[20].toString()) : 0.0);
				modeWiseNormsDTO.setRemark(row[22] != null ? row[22].toString() : null); // Was row[21]
				modeWiseNormsDTO.setIsEditable(row[24] != null ? Boolean.valueOf(row[24].toString()) : null); // Was
																												// row[23]
				modeWiseNormsDTO.setIsChecked(row[25] != null ? Boolean.valueOf(row[25].toString()) : null); // Was
																												// row[24]

				modeWiseNormsDTOList.add(modeWiseNormsDTO);

			}
			Map<String, Object> map = new HashMap<>();

			List<AopCalculation> aopCalculation = aopCalculationRepository
					.findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year, "normal-op-norms");
			map.put("mcuNormsValueDTOList", modeWiseNormsDTOList);
			map.put("aopCalculation", aopCalculation);
			aopMessageVM.setCode(200);
			aopMessageVM.setData(map);
			aopMessageVM.setMessage("Data fetched successfully");
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}

	public List<Object[]> getModeWiseNormsData(String plantId, String aopYear, String Mode,
			String Method, String procedureName) {
		try {

			String sql = "EXEC " + procedureName +
					" @plantId = :plantId, @aopYear = :aopYear, @Mode = :Mode, @Method = :Method";

			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);
			query.setParameter("Mode", Mode);
			query.setParameter("Method", Method);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM updateModeWiseNormsData(String year, String plantId, String Mode, String Method,
			List<ModeWiseNormsDTO> modeWiseNormsDTOList) {
		List<MCUNormsValue> mcuNormsValueList = new ArrayList<>();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId))
				.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
		Sites site = siteRepository.findById(plant.getSiteFkId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			for (ModeWiseNormsDTO modeWiseNormsDTO : modeWiseNormsDTOList) {
				MCUNormsValue mcuNormsValue = null;
				if (modeWiseNormsDTO.getId() == null) {
					mcuNormsValue = new MCUNormsValue();
					mcuNormsValue.setCreatedOn(new Date());
				} else {
					Optional<MCUNormsValue> mcuNormsValueOpt = mcuNormsValueRepository
							.findById(UUID.fromString(modeWiseNormsDTO.getId()));
					if (mcuNormsValueOpt.isPresent()) {
						mcuNormsValue = mcuNormsValueOpt.get();
						mcuNormsValue.setModifiedOn(new Date());
					}

				}
				mcuNormsValue.setApril(modeWiseNormsDTO.getApril());
				mcuNormsValue.setMay(modeWiseNormsDTO.getMay());
				mcuNormsValue.setJune(modeWiseNormsDTO.getJune());
				mcuNormsValue.setJuly(modeWiseNormsDTO.getJuly());
				mcuNormsValue.setAugust(modeWiseNormsDTO.getAugust());
				mcuNormsValue.setSeptember(modeWiseNormsDTO.getSeptember());
				mcuNormsValue.setOctober(modeWiseNormsDTO.getOctober());
				mcuNormsValue.setNovember(modeWiseNormsDTO.getNovember());
				mcuNormsValue.setDecember(modeWiseNormsDTO.getDecember());
				mcuNormsValue.setJanuary(modeWiseNormsDTO.getJanuary());
				mcuNormsValue.setFebruary(modeWiseNormsDTO.getFebruary());
				mcuNormsValue.setMarch(modeWiseNormsDTO.getMarch());
				mcuNormsValue.setUpdatedBy(Utility.getUserName());
				mcuNormsValue.setIsChecked(modeWiseNormsDTO.getIsChecked());
				mcuNormsValue.setFinancialYear(year);
				mcuNormsValue.setMaterialFkId(UUID.fromString(modeWiseNormsDTO.getMaterialFKId()));
				mcuNormsValue.setPlantFkId(UUID.fromString(plantId));
				mcuNormsValue.setSiteFkId(plant.getSiteFkId());
				mcuNormsValue.setVerticalFkId(plant.getVerticalFKId());
				mcuNormsValueList.add(mcuNormsValueRepository.save(mcuNormsValue));
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to update data", e);
		}
		List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("normal-op-norms");
		for (ScreenMapping screenMapping : screenMappingList) {
			AopCalculation aopCalculation = new AopCalculation();
			aopCalculation.setAopYear(year);
			aopCalculation.setIsChanged(true);
			aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
			aopCalculation.setPlantId(UUID.fromString(plantId));
			aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
			aopCalculationRepository.save(aopCalculation);
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Data updated successfully");
		aopMessageVM.setData(mcuNormsValueList);

		// TODO Auto-generated method stub
		return aopMessageVM;
	}

	@Override
	public AOPMessageVM getNormsMonthWiseModeTypeData(String plantId, String year, String modeVal) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		 String[] MONTHS = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    	};
		
		try {
			// Get the data
			List<Object[]> rows = mcuNormsValueRepository.getNormsMonthWiseModeTypeData(year, plantId, modeVal);

			// Get column names

			// Prepare the list of maps
			List<Map<String, Object>> resultList = new ArrayList<>();

			for (Object[] row : rows) {
				String materialId = row[0] != null ? row[0].toString() : null;

				for (int i = 1; i <= 12; i++) {
					String value = row[i] != null ? row[i].toString() : null;

					String mode = null;
					if ("Propane(2Z)".equalsIgnoreCase(value) || "Propane(1Z)".equalsIgnoreCase(value)) {
						mode = value;
					}

					Map<String, Object> map = new LinkedHashMap<>();
					map.put("NormParameter_FK_Id", materialId);
					map.put("month", MONTHS[i - 1]);
					map.put("mode", mode);

					resultList.add(map);
				}
			}

			Map<String, Object> data = new HashMap<>();
			data.put("data", resultList);
			// data.put("changedData", monthIdList);
			aopMessageVM.setCode(200);
			aopMessageVM.setData(data);
			aopMessageVM.setMessage("Data fetched successfully");
			return aopMessageVM;

		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

}
