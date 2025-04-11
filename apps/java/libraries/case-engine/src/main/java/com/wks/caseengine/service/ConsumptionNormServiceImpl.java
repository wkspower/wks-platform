package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.CalculatedConsumptionNormsDTO;
import com.wks.caseengine.dto.ConsumptionNormDTO;
import com.wks.caseengine.entity.AOPConsumptionNorm;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.ConsumptionNormRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

@Service
public class ConsumptionNormServiceImpl implements ConsumptionNormService {

	@Autowired
	private ConsumptionNormRepository aOPConsumptionNormRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@Override
	public AOPMessageVM getAOPConsumptionNorm(String plantId, String year) {

		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {

			List<Object[]> resultList = getConsumptionNorm(year, UUID.fromString(plantId));
			List<ConsumptionNormDTO> aOPConsumptionNormDTOList = new ArrayList<>();

			for (Object[] row : resultList) {
				ConsumptionNormDTO dto = new ConsumptionNormDTO();

				dto.setId(row[0] != null ? row[0].toString() : null);
				dto.setSiteFkId(row[1] != null ? row[1].toString() : null);
				dto.setVerticalFkId(row[2] != null ? row[2].toString() : null);
				dto.setAopCaseId(row[3] != null ? row[3].toString() : null);
				dto.setAopStatus(row[4] != null ? row[4].toString() : null);
				dto.setAopRemarks(row[5] != null ? row[5].toString() : null);
				dto.setMaterialFkId(row[6] != null ? row[6].toString() : null);
				dto.setJan(row[7] != null ? Float.valueOf(row[7].toString()) : null);
				dto.setFeb(row[8] != null ? Float.valueOf(row[8].toString()) : null);
				dto.setMarch(row[9] != null ? Float.valueOf(row[9].toString()) : null);
				dto.setApril(row[10] != null ? Float.valueOf(row[10].toString()) : null);
				dto.setMay(row[11] != null ? Float.valueOf(row[11].toString()) : null);
				dto.setJune(row[12] != null ? Float.valueOf(row[12].toString()) : null);
				dto.setJuly(row[13] != null ? Float.valueOf(row[13].toString()) : null);
				dto.setAug(row[14] != null ? Float.valueOf(row[14].toString()) : null);
				dto.setSep(row[15] != null ? Float.valueOf(row[15].toString()) : null);
				dto.setOct(row[16] != null ? Float.valueOf(row[16].toString()) : null);
				dto.setNov(row[17] != null ? Float.valueOf(row[17].toString()) : null);
				dto.setDec(row[18] != null ? Float.valueOf(row[18].toString()) : null);
				dto.setAopYear(row[19] != null ? row[19].toString() : null);
				dto.setPlantFkId(row[20] != null ? row[20].toString() : null);
				dto.setNormParameterTypeDisplayName(row[21] != null ? row[21].toString() : null);
				dto.setUOM(row[22] != null ? row[22].toString() : null);

				aOPConsumptionNormDTOList.add(dto);
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(aOPConsumptionNormDTOList);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch AOP Consumption Norms Data", ex);
		}
	}

	@Override
	public AOPMessageVM saveAOPConsumptionNorm(List<ConsumptionNormDTO> aOPConsumptionNormDTOList) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<AOPConsumptionNorm> aopConsumptionNormList = new ArrayList<>();
		try {
			for (ConsumptionNormDTO aOPConsumptionNormDTO : aOPConsumptionNormDTOList) {
				AOPConsumptionNorm aOPConsumptionNorm = new AOPConsumptionNorm();
				if (aOPConsumptionNormDTO.getId() != null && !aOPConsumptionNormDTO.getId().isEmpty()) {
					aOPConsumptionNorm.setId(UUID.fromString(aOPConsumptionNormDTO.getId()));
					// aOPConsumptionNorm.setModifiedOn(new Date());
				} else {
					UUID plantId = null;
					UUID siteId = null;
					UUID verticalId = null;
					UUID materialId = null;

					if (aOPConsumptionNormDTO.getSiteFkId() != null) {
						siteId = UUID.fromString(aOPConsumptionNormDTO.getSiteFkId());
					}
					if (aOPConsumptionNormDTO.getPlantFkId() != null) {
						plantId = UUID.fromString(aOPConsumptionNormDTO.getPlantFkId());
					}
					if (aOPConsumptionNormDTO.getVerticalFkId() != null) {
						verticalId = UUID.fromString(aOPConsumptionNormDTO.getVerticalFkId());
					}
					if (aOPConsumptionNormDTO.getMaterialFkId() != null) {
						materialId = UUID.fromString(aOPConsumptionNormDTO.getMaterialFkId());
					}

					UUID Id = aOPConsumptionNormRepository.findIdByFilters(plantId, siteId, verticalId, materialId,
							aOPConsumptionNormDTO.getAopYear());
					if (Id != null) {
						aOPConsumptionNorm.setId(Id);
					} else {
						throw new RuntimeException(
								"ID not found with given Plant Id, Site Id, Vertical Id, Material Id");
					}

					// aOPConsumptionNorm.setCreatedOn(new Date());
				}
				aOPConsumptionNorm.setAopCaseId(aOPConsumptionNormDTO.getAopCaseId());
				aOPConsumptionNorm.setAopCaseId(aOPConsumptionNormDTO.getAopCaseId());
				aOPConsumptionNorm.setAopRemarks(aOPConsumptionNormDTO.getAopRemarks());
				aOPConsumptionNorm.setAopStatus(aOPConsumptionNormDTO.getAopStatus());
				aOPConsumptionNorm.setAopYear(aOPConsumptionNormDTO.getAopYear());
				aOPConsumptionNorm.setJan(aOPConsumptionNormDTO.getJan());
				aOPConsumptionNorm.setFeb(aOPConsumptionNormDTO.getFeb());
				aOPConsumptionNorm.setMarch(aOPConsumptionNormDTO.getMarch());
				aOPConsumptionNorm.setApril(aOPConsumptionNormDTO.getApril());
				aOPConsumptionNorm.setMay(aOPConsumptionNormDTO.getMay());
				aOPConsumptionNorm.setJune(aOPConsumptionNormDTO.getJune());
				aOPConsumptionNorm.setJuly(aOPConsumptionNormDTO.getJuly());
				aOPConsumptionNorm.setAug(aOPConsumptionNormDTO.getAug());
				aOPConsumptionNorm.setSep(aOPConsumptionNormDTO.getSep());
				aOPConsumptionNorm.setOct(aOPConsumptionNormDTO.getOct());
				aOPConsumptionNorm.setNov(aOPConsumptionNormDTO.getNov());
				aOPConsumptionNorm.setDec(aOPConsumptionNormDTO.getDec());
				if (aOPConsumptionNormDTO.getId() != null) {
					aOPConsumptionNorm.setId(UUID.fromString(aOPConsumptionNormDTO.getId()));
				}
				if (aOPConsumptionNormDTO.getSiteFkId() != null) {
					aOPConsumptionNorm.setSiteFkId(UUID.fromString(aOPConsumptionNormDTO.getSiteFkId()));
				}
				if (aOPConsumptionNormDTO.getVerticalFkId() != null) {
					aOPConsumptionNorm.setVerticalFkId(UUID.fromString(aOPConsumptionNormDTO.getVerticalFkId()));
				}
				if (aOPConsumptionNormDTO.getMaterialFkId() != null) {
					aOPConsumptionNorm.setMaterialFkId(UUID.fromString(aOPConsumptionNormDTO.getMaterialFkId()));
				}
				if (aOPConsumptionNormDTO.getPlantFkId() != null) {
					aOPConsumptionNorm.setPlantFkId(UUID.fromString(aOPConsumptionNormDTO.getPlantFkId()));
				}

				aOPConsumptionNormRepository.save(aOPConsumptionNorm);
				aopConsumptionNormList.add(aOPConsumptionNorm);
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setData(aopConsumptionNormList);
			aopMessageVM.setMessage("Data saved successfully");
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for IDs", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save AOP Consumption Norms Data", ex);
		}
	}

	@Override
	@Transactional
	public int calculateExpressionConsumptionNorms(String year, String plantId) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String storedProcedure = vertical.getName() + "_HMD_CalculateConsumptionAOPValues";
			System.out.println(storedProcedure);
			return executeDynamicUpdateProcedure(storedProcedure, plantId, site.getId().toString(),
					vertical.getId().toString(), year);
		} catch (Exception e) {
			throw new RuntimeException("Failed to get data", e);
		}
	}

	@Transactional
	public int executeDynamicUpdateProcedure(String procedureName, String plantId, String siteId, String verticalId,
			String finYear) {
		try {
			String sql = "EXEC " + procedureName
					+ " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @finYear = :finYear";
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("siteId", siteId);
			query.setParameter("verticalId", verticalId);
			query.setParameter("finYear", finYear);
			return query.executeUpdate();
		} catch (Exception e) {
			throw new RuntimeException("Failed to get data", e);
		}

	}

	@Override
	@Transactional
	public AOPMessageVM getCalculatedConsumptionNorms(String year, String plantId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).orElseThrow();

			List<CalculatedConsumptionNormsDTO> listDTO = new ArrayList<>();
			String storedProcedure = vertical.getName() + "_HMD_CalculateConsumptionAOPValues";
			System.out.println("Executing SP: " + storedProcedure);

		List<Object[]> results = getCalculatedConsumptionNorms(storedProcedure, year, plant.getId().toString(),
				site.getId().toString(), vertical.getId().toString());

			for (Object[] row : results) {
				CalculatedConsumptionNormsDTO dto = new CalculatedConsumptionNormsDTO();
//	        	dto.setId(row[0] != null ? UUID.fromString(row[0].toString()) : null);
				dto.setSiteFkId(row[0] != null ? UUID.fromString(row[0].toString()) : null);
				dto.setVerticalFkId(row[1] != null ? UUID.fromString(row[1].toString()) : null);
				dto.setAopCaseId(row[2] != null ? row[2].toString() : "N/A");
				dto.setAopStatus(row[3] != null ? row[3].toString() : "N/A");
				dto.setAopRemarks(row[4] != null ? row[4].toString() : "");
				dto.setMaterialFkId(row[5] != null ? UUID.fromString(row[5].toString()) : null);

				dto.setJan(row[8] != null ? Float.parseFloat(row[8].toString()) : 0.0f);
				dto.setFeb(row[9] != null ? Float.parseFloat(row[9].toString()) : 0.0f);
				dto.setMarch(row[10] != null ? Float.parseFloat(row[10].toString()) : 0.0f);
				dto.setApril(row[11] != null ? Float.parseFloat(row[11].toString()) : 0.0f);
				dto.setMay(row[12] != null ? Float.parseFloat(row[12].toString()) : 0.0f);
				dto.setJune(row[13] != null ? Float.parseFloat(row[13].toString()) : 0.0f);
				dto.setJuly(row[14] != null ? Float.parseFloat(row[14].toString()) : 0.0f);
				dto.setAug(row[15] != null ? Float.parseFloat(row[15].toString()) : 0.0f);
				dto.setSep(row[16] != null ? Float.parseFloat(row[16].toString()) : 0.0f);
				dto.setOct(row[17] != null ? Float.parseFloat(row[17].toString()) : 0.0f);
				dto.setNov(row[18] != null ? Float.parseFloat(row[18].toString()) : 0.0f);
				dto.setDec(row[19] != null ? Float.parseFloat(row[19].toString()) : 0.0f);

				dto.setAopYear(row[20] != null ? row[20].toString() : "N/A");
				dto.setPlantFkId(row[21] != null ? UUID.fromString(row[21].toString()) : null);
				dto.setNormParameterTypeDisplayName(row[7] != null ? row[7].toString() : "N/A");
				dto.setUOM(row[6] != null ? row[6].toString() : "");
				listDTO.add(dto);
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(listDTO);
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch AOP Consumption Norms", ex);
		}
	}

	@Transactional
	public List<Object[]> getCalculatedConsumptionNorms(String procedureName, String finYear, String plantId,
			String siteId, String verticalId) {
		try {
			String sql = "EXEC " + procedureName
					+ " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @finYear = :finYear";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("siteId", siteId);
			query.setParameter("verticalId", verticalId);
			query.setParameter("finYear", finYear);

			return query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to fetch AOP Consumption Norms", e);
		}
	}

	@Transactional
	public List<Object[]> getConsumptionNorm(String aopYear, UUID plantFkId) {
		try {
			Plants plant = plantsRepository.findById(plantFkId).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

			String viewName = "vwScrn" + vertical.getName() + "AOPConsumptionNorms";
			String sql = "SELECT * FROM " + viewName + " WHERE Plant_FK_Id = :plantFkId AND AOPYear = :aopYear"
					+ " ORDER BY NormParameterType_DisplayName";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantFkId", plantFkId);
			query.setParameter("aopYear", aopYear);

			return query.getResultList(); // Later you can map this to a

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to fetch AOP Consumption Norms", e);
		}
	}

}
